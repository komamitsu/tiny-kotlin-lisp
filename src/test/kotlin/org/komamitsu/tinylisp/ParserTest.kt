package org.komamitsu.tinylisp

import org.junit.Assert.*
import org.junit.Test

class ParserTest {
    @Test
    fun parse() {
        assertEquals(
                CellNode(SymbolNode("-"), CellNode(IntegerNode(50), CellNode(
                        CellNode(SymbolNode("+"), CellNode(IntegerNode(5), CellNode(IntegerNode(3), NilNode))), NilNode))),
                Parser("(- 50 (+ 5 3))".byteInputStream()).parse())
    }
}