package org.komamitsu.tinylisp

import org.junit.Assert.*
import org.junit.Test

class ParserTest {
    @Test
    fun parse() {
        assertEquals(
                Cell(Symbol("-"), Cell(Integer(50), Cell(
                        Cell(Symbol("+"), Cell(Integer(5), Cell(Integer(3), Nil))), Nil))),
                Parser("(- 50 (+ 5 3))".byteInputStream()).parse())
    }
}