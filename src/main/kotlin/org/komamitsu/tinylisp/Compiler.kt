package org.komamitsu.tinylisp

import java.io.InputStream

class Compiler {
    fun process(input: InputStream) {
        val visitor = CompileVisitor()
        val parser = Parser(input)

        while (true) {
            val node = parser.parse() ?: break
            node.accept(visitor)
        }

        visitor.compile()
    }
}