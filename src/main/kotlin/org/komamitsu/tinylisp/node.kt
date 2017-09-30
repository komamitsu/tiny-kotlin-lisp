package org.komamitsu.tinylisp

sealed class Node {
    var quoted: Boolean = false

    abstract fun <T> accept(visitor: Visitor<T>) : T

    abstract fun shouldBeEvaluated() : Boolean

    fun asBoolNode() : BoolNode {
        if (this is BoolNode) {
            return this
        }
        else {
            throw IllegalStateException("This isn't a BoolNode: $this")
        }
    }

    fun asIntegerNode() : IntegerNode {
        if (this is IntegerNode) {
            return this
        }
        else {
            throw IllegalStateException("This isn't a IntegerNode: $this")
        }
    }

    fun asSymbolNode() : SymbolNode {
        if (this is SymbolNode) {
            return this
        }
        else {
            throw IllegalStateException("This isn't a SymbolNode: $this")
        }
    }

    fun asCellNode() : CellNode {
        if (this is CellNode) {
            return this
        }
        else {
            throw IllegalStateException("This isn't a CellNode: $this")
        }
    }

    fun asNilTerminatedCellNode() : CellNode {
        if (this is CellNode && this.cdr is NilNode) {
            return this
        }
        else {
            throw IllegalStateException("This isn't a Nil terminated CellNode: $this")
        }
    }

    fun getCarOfCellNode() : Node {
        if (this is NilNode) {
            return NilNode
        }
        else {
            return this.asCellNode().car
        }
    }

    fun getCdrOfCellNode() : Node {
        return this.asCellNode().cdr
    }

    fun getCarOfNilTerminatedCellNode() : Node {
        return this.asNilTerminatedCellNode().car
    }
}

data class IntegerNode(val value: Long) : Node() {
    override fun shouldBeEvaluated(): Boolean {
        return false
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIntegerNode(this)
    }

    override fun toString(): String {
        return value.toString()
    }
}

sealed class BoolNode(val bool: Boolean) : Node() {
    override fun shouldBeEvaluated(): Boolean {
        return false
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBoolNode(this)
    }

    override fun toString(): String {
        return bool.toString()
    }
}

object TrueNode : BoolNode(true) {
    override fun toString(): String {
        return "T"
    }
}

object NilNode : BoolNode(false) {
    override fun toString(): String {
        return "NIL"
    }
}

data class SymbolNode(val key: String) : Node() {
    override fun shouldBeEvaluated(): Boolean {
        return !quoted
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitSymbolNode(this)
    }

    override fun toString(): String {
        return key
    }
}

data class CellNode(val car: Node, var cdr: Node) : Node() {
    override fun shouldBeEvaluated(): Boolean {
        return !quoted
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitCellNode(this)
    }

    override fun toString(): String {
        return "($car, $cdr)"
    }
}

/*
data class CarNode(val cellNode: CellNode) : Node() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitCarNode(env, this)
    }
}

data class CdrOpNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        val node = cellNode.cdr
        return when (node) {
            is IntegerNode -> visitor.visitIntegerNode(env, node)
            is SymbolNode -> visitor.visitSymbolNode(env, node)
            is CellNode -> visitor.visitCellNode(env, node)
            is BoolNode -> visitor.visitBoolNode(env, node)
            else -> throw IllegalStateException("Unexpected node at CdrNode: $node")
        }
    }
}

data class ConsOpNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        val first = cellNode.car
        val second = cellNode.cdr.asNilTerminatedCellNode() ?: throw IllegalStateException("Unexpected node at CdrNode: ${cellNode.cdr}")
        return visitor.visitCellNode(env, CellNode(first, second))
    }
}

data class EqualNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitEqualNode(env, this)
    }
}

data class NotEqualNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitNotEqualNode(env, this)
    }
}

data class GreaterThanNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitGreaterThanNode(env, this)
    }
}

data class GreaterThanOrEqualNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitGreaterThanOrEqualNode(env, this)
    }
}

data class LessThanNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitLessThanNode(env, this)
    }
}

data class LessThanOrEqualNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitLessThanOrEqualNode(env, this)
    }
}

data class AddNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitAddNode(env, this)
    }
}

data class SubtractNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitSubtractNode(env, this)
    }
}

data class DivideNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitDivideNode(env, this)
    }
}

data class MultiplyNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitMultiplyNode(env, this)
    }
}

data class IfNode(val cellNode: CellNode) : OpNode() {
    override fun <T> accept(env: Env, visitor: Visitor<T>): T {
        return visitor.visitIfNode(env, this)
    }
}
*/
