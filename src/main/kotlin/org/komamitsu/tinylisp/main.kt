package org.komamitsu.tinylisp

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import java.nio.file.Path
import java.nio.file.Paths

sealed class Mode {
    abstract fun processInputFile(inputFilePath: Path, generatedSourceFilePath: Path?)

    abstract fun processInputAndOutputFiles(inputFilePath: Path, outputFilePath: Path, generatedSourceFilePath: Path?)

    abstract fun processREPL(verbose: Boolean)
}

class Interpret: Mode() {
    val interpreter = Interpreter()

    override fun processInputFile(inputFilePath: Path, generatedSourceFilePath: Path?) {
        val input = inputFilePath.toFile().inputStream()
        interpreter.process(input, { })
    }

    override fun processInputAndOutputFiles(inputFilePath: Path, outputFilePath: Path, generatedSourceFilePath: Path?) {
        throw IllegalArgumentException("This mode doesn't support to create a Jar file")
    }

    override fun processREPL(verbose: Boolean) {
        while (true) {
            print("> ")
            try {
                val input = readLine().orEmpty().byteInputStream()
                interpreter.process(input,
                        { node ->
                            if (verbose) {
                                println(node)
                            }
                        })
            }
            catch (e: IllegalStateException) {
                e.printStackTrace(System.out)
            }
            println()
        }
    }
}

class Compile: Mode() {
    override fun processInputFile(inputFilePath: Path, generatedSourceFilePath: Path?) {
        val outputJarFile = createTempFile(suffix = "CompiledLisp.jar")
        try {
            val compiler = Compiler(outputJarFile.toPath(), generatedSourceFilePath)
            val input = inputFilePath.toFile().inputStream()
            compiler.process(input)

            ProcessBuilder("java", "-jar", outputJarFile.absolutePath)
                    .inheritIO()
                    .start()
                    .waitFor()
        }
        finally {
            outputJarFile.delete()
        }
    }

    override fun processInputAndOutputFiles(inputFilePath: Path, outputFilePath: Path, generatedSourceFilePath: Path?) {
        if (!outputFilePath.toString().endsWith(".jar")) {
            throw IllegalArgumentException("Output file path should have extension name '.jar'")
        }
        val compiler = Compiler(outputFilePath, generatedSourceFilePath)
        val input = inputFilePath.toFile().inputStream()
        compiler.process(input)
    }

    override fun processREPL(verbose: Boolean) {
        throw IllegalArgumentException("This mode doesn't support REPL")
    }
}

class Args(parser: ArgParser) {
    val inputFile by parser.storing(
            "-i", "--input-file",
            help = "input source code file path. Without this option, it works as REPL").default(null)

    val outputFile by parser.storing(
            "-o", "--output-file",
            help = "output Jar file path. This option is available only with 'compile' mode").default(null)

    val generatedSourceFile by parser.storing(
            "-s", "--source-file-generated",
            help = "generated source file path. This option is for debugging and available only with 'compile' mode").default(null)

    val verbose by parser.flagging(
            "-v", "--verbose",
            help = "verbose mode. This option is available only in REPL")

    val mode by parser.positional("MODE",
            help = "'interpret' or 'compile'") {
        when (this) {
            "interpret" -> Interpret()
            "compile" -> Compile()
            else -> throw IllegalArgumentException("Unknown mode name: $this")
        }
    }
}

fun main(args : Array<String>) = mainBody {
    val arguments = Args(ArgParser(args))

    if (arguments.inputFile != null) {
        val generatedSourceFilePath = if (arguments.generatedSourceFile != null) {
            Paths.get(arguments.generatedSourceFile)
        }
        else {
            null
        }

        if (arguments.outputFile != null) {
            arguments.mode.processInputAndOutputFiles(
                    Paths.get(arguments.inputFile),
                    Paths.get(arguments.outputFile),
                    generatedSourceFilePath)
        }
        else {
            arguments.mode.processInputFile(
                    Paths.get(arguments.inputFile),
                    generatedSourceFilePath)
        }
    }
    else {
        arguments.mode.processREPL(arguments.verbose)
    }
}