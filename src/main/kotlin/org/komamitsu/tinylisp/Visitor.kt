package org.komamitsu.tinylisp

interface Visitor<T> {
    fun visitIntegerNode(integerNode: IntegerNode) : T
    fun visitBoolNode(boolNode: BoolNode) : T
    fun visitSymbolNode(symbolNode: SymbolNode) : T
    fun visitCellNode(cell: CellNode) : T
}