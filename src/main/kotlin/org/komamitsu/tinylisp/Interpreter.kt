package org.komamitsu.tinylisp

import java.io.InputStream

class Interpreter() {
    val visitor = InterpretVisitor(Env(null))

    fun process(input: InputStream, callback: (Node) -> Unit) {
        val parser = Parser(input)
        while (true) {
            val node = parser.parse() ?: return
            callback(node.accept(visitor))
        }
    }
}
