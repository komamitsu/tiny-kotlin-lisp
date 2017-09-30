package org.komamitsu.tinylisp

import org.junit.Assert.*
import org.junit.Test

class InterpreterTest {
    @Test
    fun simple() {
        val interpreter = Interpreter()
        interpreter.process("(+ 35 7)".byteInputStream(),
                { node -> assertEquals(IntegerNode(42), node) })
    }

    @Test
    fun simpleDefun() {
        val interpreter = Interpreter()
        interpreter.process("(defun x (a b) (* a b))".byteInputStream(),
                { node -> assertEquals(SymbolNode("x"), node) })
        interpreter.process("(x 6 7)".byteInputStream(),
                { node -> assertEquals(IntegerNode(42), node) })
    }

    @Test
    fun simpleIf() {
        val interpreter = Interpreter()
        interpreter.process("(if (= 1 1) 42 0)".byteInputStream(),
                { node -> assertEquals(IntegerNode(42), node) })
        interpreter.process("(if (= 1 2) 0 42)".byteInputStream(),
                { node -> assertEquals(IntegerNode(42), node) })
        interpreter.process("(if (= 1 2) 42)".byteInputStream(),
                { node -> assertEquals(NilNode, node) })
    }

    @Test
    fun normalIf() {
        val interpreter = Interpreter()
        interpreter.process("(if (= (+ 35 7) (* 6 7)) (- 50 8) 0)".byteInputStream(),
                { node -> assertEquals(IntegerNode(42), node) })
    }

    @Test
    fun simpleRecursion() {
        val interpreter = Interpreter()
        interpreter.process("(defun f (n) (if (= n 0) 0 (f (- n 1))))".byteInputStream(),
                { node -> assertEquals(SymbolNode("f"), node) })
        interpreter.process("(f 1)".byteInputStream(),
                { node -> assertEquals(IntegerNode(0), node) })
    }

    @Test
    fun simpleFib() {
        val interpreter = Interpreter()
        interpreter.process("(defun fib (n x1 x2) (if (<= n 0) x1 (fib (- n 1) x2 (+ x1 x2))))".byteInputStream(),
                { node -> assertEquals(SymbolNode("fib"), node) })
        interpreter.process("(fib 10 1 1)".byteInputStream(),
                { node -> assertEquals(IntegerNode(89), node) })
    }

    @Test
    fun fib() {
        val interpreter = Interpreter()
        interpreter.process(("(defun fib (n) (if (= n 0) 1 (" +
                "if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))").byteInputStream(),
                { node -> assertEquals(SymbolNode("fib"), node) })
        interpreter.process("(fib 10)".byteInputStream(),
                { node -> assertEquals(IntegerNode(89), node) })
    }

    @Test
    fun car() {
        val interpreter = Interpreter()
        interpreter.process("(car '(42 0))".byteInputStream(),
                { node -> assertEquals(IntegerNode(42), node) })
    }

    @Test
    fun cdr() {
        val interpreter = Interpreter()
        interpreter.process("(cdr '(42 0))".byteInputStream(),
                { node -> assertEquals(CellNode(IntegerNode(0), NilNode), node) })
    }

    @Test
    fun cons() {
        val interpreter = Interpreter()
        interpreter.process("(cons 1 2)".byteInputStream(),
                { node -> assertEquals(CellNode(IntegerNode(1), IntegerNode(2)), node) })
    }

    @Test
    fun consCell() {
        val interpreter = Interpreter()
        interpreter.process("(cons '(1 2) '(3 4))".byteInputStream(),
                { node -> assertEquals(CellNode(CellNode(IntegerNode(1), CellNode(IntegerNode(2), NilNode)),
                        CellNode(IntegerNode(3), CellNode(IntegerNode(4), NilNode))), node) })
    }

    @Test
    fun dot() {
        val interpreter = Interpreter()
        interpreter.process("'(1 . 2)".byteInputStream(),
                { node -> assertEquals(CellNode(IntegerNode(1), IntegerNode(2)), node) })
    }

    @Test
    fun dotList() {
        val interpreter = Interpreter()
        interpreter.process("'(1 . (2))".byteInputStream(),
                { node -> assertEquals(CellNode(IntegerNode(1), CellNode(IntegerNode(2), NilNode)), node) })
    }
}