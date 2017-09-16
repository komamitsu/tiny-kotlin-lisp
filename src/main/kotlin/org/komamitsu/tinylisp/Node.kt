package org.komamitsu.tinylisp

sealed class Node {
    var quoted: Boolean = false
}

data class Integer(val value: Long) : Node()
data class Symbol(val key: String) : Node()
data class Cell(val car: Node, var cdr: Node) : Node()
abstract class EmbeddedFunc : Node() {
    abstract fun process(env: Env, params: Cell): Node
}
data class Func(val capturedEnv: Env, val params: Cell, val body: Cell) : Node()

sealed class Bool(val bool: Boolean) : Node()
object True : Bool(true)
object Nil : Bool(false)