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
        val buf = StringBuilder()
        buf.append("(")
        var cell = this
        while (true) {
            buf.append(cell.car)
            val cdr = cell.cdr
            if (cdr is NilNode) {
                break
            }
            else if (cdr is CellNode) {
                buf.append(" ")
                cell = cdr
            }
            else {
                buf.append(" . ${cell.cdr}")
                break
            }
        }
        buf.append(")")

        return buf.toString()

        if (cdr is CellNode) {
            return "($car $cdr)"
        }
        else {
            return "($car, $cdr)"
        }
    }
}

