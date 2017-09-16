package org.komamitsu.tinylisp

import java.io.InputStream

class Interpreter() {
    private val eval = Eval()
    private val env = eval.getWorld()

    fun process(input: InputStream, callback: (Node) -> Unit) {
        val parser = Parser(input)
        while (true) {
            val node = parser.parse() ?: return
            callback(eval.eval(env, node))
        }
    }
}