package org.komamitsu.tinylisp

import org.junit.Assert.*
import org.junit.Test

class CompilerTest {
    @Test
    fun simple() {
        val compiler = Compiler()
        compiler.process("(print (+ 35 7))".byteInputStream())
    }

    @Test
    fun car() {
        val compiler = Compiler()
        compiler.process("(print (car '(42 0)))".byteInputStream())
    }

    @Test
    fun cdr() {
        val compiler = Compiler()
        compiler.process("(print (cdr '(42 0)))".byteInputStream())
    }

    @Test
    fun cons() {
        run {
            val compiler = Compiler()
            compiler.process("(print (cons 1 2))".byteInputStream())
        }

        run {
            val compiler = Compiler()
            compiler.process("(print (cons '(1 2) '(3 4)))".byteInputStream())
        }
    }

    @Test
    fun equals() {
        run {
            val compiler = Compiler()
            compiler.process("(print (= 1 1))".byteInputStream())
        }

        run {
            val compiler = Compiler()
            compiler.process("(print (= 1 2))".byteInputStream())
        }

        run {
            val compiler = Compiler()
            compiler.process("(= (+ 35 7) (* 6 7))".byteInputStream())
        }
    }

    @Test
    fun simpleIf() {
        run {
            val compiler = Compiler()
            compiler.process("(print (if (= 1 1) 42 0))".byteInputStream())
        }

        run {
            val compiler = Compiler()
            compiler.process("(print (if (= 1 2) 42 0))".byteInputStream())
        }

        run {
            val compiler = Compiler()
            compiler.process("(print (if (= 1 2) 42))".byteInputStream())
        }
    }

    @Test
    fun normalIf() {
        run {
            val compiler = Compiler()
            compiler.process("(print (if (= (+ 35 7) (* 6 7)) (- 50 8) 0))".byteInputStream())
        }
    }

   @Test
    fun simpleDefun() {
       val compiler = Compiler()
        compiler.process((
                "(defun x (a b) (* a b))" +
                "(print (x 6 7))").byteInputStream())
    }

    @Test
    fun fib() {
        val compiler = Compiler()
        compiler.process(("(defun fib (n) (if (= n 0) 1 (" +
                "  if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))" +
                "(print (fib 10))").byteInputStream())
    }
}

