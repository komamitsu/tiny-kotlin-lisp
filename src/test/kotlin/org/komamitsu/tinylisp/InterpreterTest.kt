package org.komamitsu.tinylisp

import org.junit.Assert.*
import org.junit.Test

class InterpreterTest {
    @Test
    fun simple() {
        val interpreter = Interpreter()
        interpreter.process("(+ 35 7)".byteInputStream(),
                { node -> assertEquals(Integer(42), node) })
    }
    @Test
    fun simpleDefun() {
        val interpreter = Interpreter()
        interpreter.process("(defun x (a b) (* a b))".byteInputStream(),
                { node -> assertEquals(Symbol("x"), node) })
        interpreter.process("(x 6 7)".byteInputStream(),
                { node -> assertEquals(Integer(42), node) })
    }

    @Test
    fun simpleIf() {
        val interpreter = Interpreter()
        interpreter.process("(if (= (+ 35 7) (* 6 7)) (- 50 8) 0)".byteInputStream(),
                { node -> assertEquals(Integer(42), node) })
    }

    @Test
    fun simpleRecursion() {
        val interpreter = Interpreter()
        interpreter.process("(defun f (n) (if (= n 0) 0 (f (- n 1))))".byteInputStream(),
                { node -> assertEquals(Symbol("f"), node) })
        interpreter.process("(f 1)".byteInputStream(),
                { node -> assertEquals(Integer(0), node) })
    }

    @Test
    fun simpleFib() {
        val interpreter = Interpreter()
        interpreter.process("(defun fib (n x1 x2) (if (<= n 0) x1 (fib (- n 1) x2 (+ x1 x2))))".byteInputStream(),
                { node -> assertEquals(Symbol("fib"), node) })
        interpreter.process("(fib 10 1 1)".byteInputStream(),
                { node -> assertEquals(Integer(89), node) })
    }

    @Test
    fun fib() {
        val interpreter = Interpreter()
        interpreter.process(("(defun fib (n) (if (= n 0) 1 (" +
                "if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))").byteInputStream(),
                { node -> assertEquals(Symbol("fib"), node) })
        interpreter.process("(fib 10)".byteInputStream(),
                { node -> assertEquals(Integer(89), node) })
    }

    @Test
    fun car() {
        val interpreter = Interpreter()
        interpreter.process("(car '(42 0))".byteInputStream(),
                { node -> assertEquals(Integer(42), node) })
    }

    @Test
    fun cdr() {
        val interpreter = Interpreter()
        interpreter.process("(cdr '(42 0))".byteInputStream(),
                { node -> assertEquals(Cell(Integer(0), Nil), node) })
    }

    @Test
    fun cons() {
        val interpreter = Interpreter()
        interpreter.process("(cons 1 2)".byteInputStream(),
                { node -> assertEquals(Cell(Integer(1), Integer(2)), node) })
    }

    @Test
    fun consCell() {
        val interpreter = Interpreter()
        interpreter.process("(cons '(1 2) '(3 4))".byteInputStream(),
                { node -> assertEquals(Cell(Cell(Integer(1), Cell(Integer(2), Nil)),
                        Cell(Integer(3), Cell(Integer(4), Nil))), node) })
    }

    @Test
    fun dot() {
        val interpreter = Interpreter()
        interpreter.process("'(1 . 2)".byteInputStream(),
                { node -> assertEquals(Cell(Integer(1), Integer(2)), node) })
    }

    @Test
    fun dotList() {
        val interpreter = Interpreter()
        interpreter.process("'(1 . (2))".byteInputStream(),
                { node -> assertEquals(Cell(Integer(1), Cell(Integer(2), Nil)), node) })
    }
}