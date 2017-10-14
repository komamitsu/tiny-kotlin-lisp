package org.komamitsu.tinylisp

import org.codehaus.commons.compiler.CompilerFactoryFactory
import org.codehaus.janino.ByteArrayClassLoader
import java.io.File
import java.io.FileOutputStream
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicLong
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest


class CompileVisitor(val outputJarPath: Path) : Visitor<Unit> {
    val classDir = "org/komamitsu/tinylisp"
    val classDependencies = mapOf(
            Pair(classDir,
                    listOf( Node::class,
                            IntegerNode::class,
                            BoolNode::class,
                            TrueNode::class,
                            NilNode::class,
                            CellNode::class)
            ),
            Pair("kotlin",
                    listOf( kotlin.KotlinNullPointerException::class,
                            kotlin.UninitializedPropertyAccessException::class)
            ),
            Pair("kotlin/jvm/internal",
                    listOf(kotlin.jvm.internal.Intrinsics::class)
            )
    )

    val varSeq = AtomicLong()
    var rootVarName: String? = null
    var functions = mutableListOf<String>()
    var sourceCodes = mutableListOf<StringBuilder>()
    var returnVarNames = mutableListOf<String>()

    init {
        pushSourceCode()
        rootVarName = declareReturnVar("init")
    }

    private fun sourceCode() : StringBuilder {
        return sourceCodes.last()
    }

    private fun pushSourceCode() {
        sourceCodes.add(StringBuilder())
    }

    private fun popSourceCode() {
        if (sourceCodes.isEmpty())
            throw IllegalStateException("sourceCode is empty")

        functions.add(sourceCodes.removeAt(sourceCodes.size - 1).toString())
    }

    private fun pushReturnVarName(varName: String, label: String) {
        returnVarNames.add(varName)
        comment("Pushed a var name ($label): $varName")
    }

    private fun popReturnVarName(label: String): String {
        if (returnVarNames.isEmpty())
            throw IllegalStateException("returnVarNames is empty")

        val varName = returnVarNames.removeAt(returnVarNames.size - 1)
        comment("Popped a var name ($label): $varName")
        return varName
    }

    private fun declareReturnVar(label: String): String {
        val varName = createNewVarName()
        sourceCode().append("Node $varName = null;\n")
        pushReturnVarName(varName, label)
        return varName
    }

    fun <A, T> foldLeft(params: CellNode, f: (A?, T) -> A?): A
        where T: Node
    {
        var result: A? = null
        var first = true
        var car = params.car
        var cdr = params.cdr
        while (true) {
            result = f(result, car as T)
            if (first) {
                first = false
            }
            if (cdr is NilNode) {
                if (result == null) {
                    throw IllegalStateException(params.toString())
                }
                return result
            }

            val evaluatedNext = cdr as CellNode
            car = evaluatedNext.car
            cdr = evaluatedNext.cdr
        }
    }

    override fun visitIntegerNode(integerNode: IntegerNode): Unit {
        val returnVarName = popReturnVarName("visitIntegerNode")
        sourceCode().append("$returnVarName = new IntegerNode(${integerNode.value});\n")
    }

    override fun visitBoolNode(boolNode: BoolNode): Unit {
        val returnVarName = popReturnVarName("visitBoolNode")

        if (boolNode.bool) {
            sourceCode().append("$returnVarName = TrueNode.INSTANCE;\n")
        }
        else {
            sourceCode().append("$returnVarName = NilNode.INSTANCE;\n")
        }
    }

    override fun visitSymbolNode(symbolNode: SymbolNode): Unit {
        val returnVarName = popReturnVarName("visitSymbolNode")
        sourceCode().append("$returnVarName = ${symbolNode.key};\n")
    }

    private fun createNewVarName() : String {
        return "var" + varSeq.getAndIncrement()
    }

    private fun withBlock(f: () -> Unit) {
        sourceCode().append("{\n")
        f.invoke()
        sourceCode().append("}\n")
    }

    private fun declareVar(node: Node): String {
        val varName = declareReturnVar("declareVar")
        withBlock {
            node.accept(this)
        }
        return varName
    }

    private fun declareVars(cell: CellNode) : List<String> {
        val keys = mutableListOf<String>()
        foldLeft<Boolean, Node>(cell,
                { _, x ->
                    val varname = declareVar(x)
                    keys.add(varname)
                    true
                }
        )

        return keys
    }

    private fun representQuotedCellNode(cell: CellNode) {
        if (cell.quoted) {
            cell.car.quoted = true
            cell.cdr.quoted = true
        }

        val carVarName = declareReturnVar("representQuotedCellNode#0")
        withBlock {
            cell.car.accept(this)
        }

        val cdrVarName = declareReturnVar("representQuotedCellNode#1")
        withBlock {
            cell.cdr.accept(this)
        }

        val returnVarName = popReturnVarName("representQuotedCellNode")
        sourceCode().append("$returnVarName = new CellNode($carVarName, $cdrVarName);")
    }

    private fun evalAndReturn(node: Node) {
        val returnVarName = declareReturnVar("evalAndReturn")
        withBlock {
            node.accept(this)
        }
        sourceCode().append("${popReturnVarName("evalAndReturn")} = $returnVarName;\n")
    }

    private fun evalCondAndReturn(condStr: String) {
        val returnVarName = popReturnVarName("evalCondAndReturn")
        withBlock {
            sourceCode().append("if ($condStr) {\n")
            sourceCode().append("  $returnVarName = TrueNode.INSTANCE;\n")
            sourceCode().append("}\n")
            sourceCode().append("else {\n")
            sourceCode().append("  $returnVarName = NilNode.INSTANCE;\n")
            sourceCode().append("}\n")
        }
    }

    private fun evalArithmeticAndReturn(params: CellNode, operator: String) {
        val vars = declareVars(params)
        val varsWithField = vars.map({ x -> "$x.asIntegerNode().getValue()"})
        val returnVarName = popReturnVarName("evalArithmeticAndReturn")
        sourceCode().append("$returnVarName = new IntegerNode(")
        sourceCode().append(varsWithField.joinToString(separator = " $operator "))
        sourceCode().append(");\n")
    }

    private fun comment(comment: String) {
        sourceCode().append("// $comment\n")
    }

    override fun visitCellNode(cell: CellNode): Unit {
        if (cell.quoted) {
            return representQuotedCellNode(cell)
        }

        val name = cell.car.asSymbolNode()
        val params = cell.cdr.asCellNode()

        comment("Entering '${name.key}'")

        when (name.key) {
            "car" -> {
                comment("Entering 'car'")
                val consCell = params.getCarOfNilTerminatedCellNode()
                val car = consCell.getCarOfCellNode()
                evalAndReturn(car)
                comment("Exiting 'car'")
            }

            "cdr" -> {
                val consCell = params.getCarOfNilTerminatedCellNode()
                val cdr = consCell.getCdrOfCellNode()
                cdr.quoted = consCell.quoted
                evalAndReturn(cdr)
            }

            "cons" -> {
                val first = params.car
                val second = params.cdr.getCarOfNilTerminatedCellNode()
                val consCell = CellNode(first, second)
                consCell.quoted = true
                evalAndReturn(consCell)
            }

            "=" -> {
                val vars = declareVars(params)
                val equals = vars.fold(
                        Pair<String?, List<Pair<String, String>>>(null, listOf()),
                        { acc, varname ->
                            val lastVarname = acc.first
                            val pairs = acc.second
                            if (lastVarname == null) {
                                Pair(varname, pairs)
                            }
                            else {
                                Pair(varname, pairs.plus(Pair(lastVarname, varname)))
                            }
                        }).second
                        .map({pair -> String.format("(%s.asIntegerNode().getValue() == %s.asIntegerNode().getValue())", pair.first, pair.second)})
                        .joinToString(separator = " && ")

                evalCondAndReturn(equals)
            }

            "/=" -> {
                TODO()
            }

            "<=" -> {
                TODO()
            }

            ">=" -> {
                TODO()
            }

            "<" -> {
                TODO()
            }

            ">" -> {
                TODO()
            }

            "+" -> {
                evalArithmeticAndReturn(params, "+")
            }

            "-" -> {
                evalArithmeticAndReturn(params, "-")
            }

            "*" -> {
                evalArithmeticAndReturn(params, "*")
            }

            "/" -> {
                evalArithmeticAndReturn(params, "/")
            }

            "if" -> {
                val cond = params.getCarOfCellNode()
                val thenElse = params.getCdrOfCellNode().asCellNode()

                val condVarName = declareReturnVar("if: cond")
                withBlock {
                    evalAndReturn(cond)
                }

                val returnVarName = popReturnVarName("if")

                val elseReturnVarName = declareReturnVar("if: else")

                val thenReturnVarName = declareReturnVar("if: then")
                sourceCode().append("if ($condVarName.asBoolNode().getBool()) ")
                withBlock {
                    evalAndReturn(thenElse.car)
                    sourceCode().append("$returnVarName = $thenReturnVarName;\n")
                }

                sourceCode().append("else ")
                withBlock {
                    if (thenElse.cdr is NilNode) {
                        NilNode.accept(this)
                    } else {
                        evalAndReturn(thenElse.cdr.getCarOfNilTerminatedCellNode())
                    }
                    sourceCode().append("$returnVarName = $elseReturnVarName;\n")
                }
            }

            "print" -> {
                val returnVarName = declareReturnVar("print")
                withBlock {
                    evalAndReturn(params.getCarOfNilTerminatedCellNode())
                    sourceCode().append("System.out.println($returnVarName);\n")
                }
            }

            "defun" -> {
                val funcName = params.car.asSymbolNode()

                pushSourceCode()

                val next = params.cdr.asCellNode()
                val args = next.car.asCellNode()
                sourceCode().append("public static Node ${funcName.key}(\n")
                foldLeft<NilNode, Node>(args,
                        { last, x ->
                            sourceCode().append("    ")
                            if (last != null) {
                                sourceCode().append(", ")
                            }
                            sourceCode().append("Node ${x.asSymbolNode().key}\n")
                            NilNode
                        })
                sourceCode().append(")\n")
                sourceCode().append("{\n")

                val bodyList = next.cdr.asCellNode()
                foldLeft<NilNode, Node>(bodyList,
                        { last, x ->
                            sourceCode().append("{\n")
                            val returnVarName = declareReturnVar("defun")
                            x.accept(this)
                            sourceCode().append("  return ($returnVarName);\n")
                            sourceCode().append("}\n")
                            NilNode
                        })

                sourceCode().append("}\n")
                sourceCode().append("\n")

                popSourceCode()
            }

            else -> {
                val returnVarName = popReturnVarName("apply")
                val varNames = declareVars(params)
                withBlock {
                    sourceCode().append("$returnVarName = ${name.key}(${varNames.joinToString(", ")});\n")
                }
            }
        }
        comment("Exiting '${name.key}'")
    }

    fun dumpCode() : String {
        val buf = StringBuilder()
        buf.append("package org.komamitsu.tinylisp;\n")
        buf.append("\n")
        buf.append("public class Runner {\n")

        functions.forEach({ s -> buf.append(s) })

        sourceCodes.reversed().forEachIndexed(
                { index, s ->
                    if (index == 0) {
                        buf.append("public static void main(String[] args) {\n")
                        buf.append(s)
                        buf.append("}\n")
                    }
                    else {
                        buf.append(s)
                    }
                    buf.append("\n")
                }
        )

        buf.append("}\n")

        return buf.toString()
    }

    private fun getByteCode(srcCode: String) : ByteArray {
        val compiler = CompilerFactoryFactory.getDefaultCompilerFactory().newSimpleCompiler()
        compiler.cook(srcCode)

        val resultField = compiler.javaClass.getDeclaredField("result")
        resultField.isAccessible = true
        val loader = resultField.get(compiler) as ByteArrayClassLoader
        val classesField = loader.javaClass.getDeclaredField("classes")
        classesField.isAccessible = true
        val classes = classesField.get(loader) as Map<String, ByteArray>
        val byteCode = classes.get("org.komamitsu.tinylisp.Runner") ?: throw RuntimeException("Failed to get bytecode")

        return byteCode
    }

    fun createClassFileFromByteCode(byteCode: ByteArray, workDir: Path) {
        for (dirAndClasses in classDependencies.entries) {
            val dir = workDir.resolve(dirAndClasses.key)
            Files.createDirectories(dir)

            dirAndClasses.value.stream()
                    .forEach { clazz ->
                        val simpleClassName = "${clazz.simpleName}.class"
                        val kotlinClassPath = dir.resolve(simpleClassName)
                        clazz.java.getResourceAsStream(simpleClassName).use {
                            Files.copy(it, kotlinClassPath)
                        }
                    }
        }


        val classFile = workDir.resolve(classDir).resolve("Runner.class").toFile()
        classFile.writeBytes(byteCode)
    }

    private fun createJar(workDir: Path) : File {
        val version = "1.0.0"
        val manifest = Manifest()
        val global = manifest.getMainAttributes()
        global.put(Attributes.Name.MANIFEST_VERSION, version)
        global.put(Attributes.Name.MAIN_CLASS, "org.komamitsu.tinylisp.Runner")

        val jarFile = outputJarPath.toFile()
        val os = FileOutputStream(jarFile)
        JarOutputStream(os, manifest).use {
            Files.walk(workDir, Int.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
                    .forEach({ path ->
                        if (path.toFile().isFile && path.toString().endsWith(".class")) {
                            val relativePath = JarEntry(workDir.relativize(path).toString())
                            it.putNextEntry(relativePath)
                            it.write(path.toFile().readBytes())
                        }
                    })
        }

        return jarFile
    }

    fun compile() {
        val byteCode = getByteCode(dumpCode())
        val tempDir = createTempDir(prefix = "tinyKotlinLisp")
        try {
            createClassFileFromByteCode(byteCode, tempDir.toPath())
            createJar(tempDir.toPath())
        }
        finally {
            tempDir.deleteRecursively()
        }
    }
}
