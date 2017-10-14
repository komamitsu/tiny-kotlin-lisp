package org.komamitsu.tinylisp

import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths

class Compiler(val outputJarPath: Path = Paths.get("CompiledLisp.jar"), val dumpSourceFile: Path? = null) {
    fun process(input: InputStream) {
        val visitor = CompileVisitor(outputJarPath)
        val parser = Parser(input)

        while (true) {
            val node = parser.parse() ?: break
            node.accept(visitor)
        }

        if (dumpSourceFile != null) {
            dumpSourceFile.toFile().writeText(visitor.dumpCode())
        }

        visitor.compile()
    }
}