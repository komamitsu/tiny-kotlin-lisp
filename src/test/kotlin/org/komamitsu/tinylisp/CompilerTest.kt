package org.komamitsu.tinylisp

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CompilerTest {
    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    private fun run(lispCode: String, assert: (String) -> Unit) {
        val outputJarFile = tempFolder.root.toPath().resolve("TestCompiledLisp.jar")
        val dumpSourceFile = tempFolder.newFile()
        val compiler = Compiler(outputJarFile, dumpSourceFile.toPath())
        compiler.process(lispCode.byteInputStream())

        val processBuilder = ProcessBuilder("java", "-jar", outputJarFile.toString())
        val redirectFile = tempFolder.newFile()
        assertEquals(0, processBuilder.redirectOutput(redirectFile).start().waitFor())

        val actual = redirectFile.readText().trim()
        assert(actual)
    }

    @Test
    fun simple() {
        run("(print (+ 35 7))", { actual ->
            assertEquals("42", actual)
        })
    }

    @Test
    fun car() {
        run("(print (car '(42 0)))", { actual ->
            assertEquals("42", actual)
        })
    }

    @Test
    fun cdr() {
        run("(print (cdr '(42 0)))", { actual ->
            assertEquals("(0, NIL)", actual)
        })
    }

    @Test
    fun cons() {
        run("(print (cons 1 2))", { actual ->
            assertEquals("(1, 2)", actual)
        })

        run("(print (cons '(1 2) '(3 4)))", { actual ->
            assertEquals("((1, (2, NIL)), (3, (4, NIL)))", actual)
        })
    }

    @Test
    fun equals() {
        run("(print (= 1 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (= 1 2))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (= 1 1 2))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (= 1 2 2))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (= 1 1 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (= (+ 35 7) (* 6 7)))", { actual ->
            assertEquals("T", actual)
        })
    }

    @Test
    fun notEquals() {
        run("(print (/= 1 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (/= 1 2))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (/= 1 2 3))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (/= 1 2 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (/= (+ 35 7) (* 6 7)))", { actual ->
            assertEquals("NIL", actual)
        })
    }

    @Test
    fun smallerThanOrEquals() {
        run("(print (<= 1 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (<= 1 2))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (<= 2 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (<= 1 1 2))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (<= 1 2 2))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (<= 2 2 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (<= 2 1 2))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (<= 1 1 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (<= (+ 35 6) (* 6 7)))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (<= (+ 35 7) (* 6 7)))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (<= (+ 35 8) (* 6 7)))", { actual ->
            assertEquals("NIL", actual)
        })
    }

    @Test
    fun smallerThan() {
        run("(print (< 1 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (< 1 2))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (< 1 1 2))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (< 1 2 2))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (< 1 1 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (< 1 2 3))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (< (+ 35 6) (* 6 7)))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (< (+ 35 7) (* 6 7)))", { actual ->
            assertEquals("NIL", actual)
        })
    }

    @Test
    fun greaterThanOrEquals() {
        run("(print (>= 1 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (>= 1 2))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (>= 2 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (>= 2 2 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (>= 2 1 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (>= 2 1 2))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (>= 1 2 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (>= 1 1 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (>= (+ 35 8) (* 6 7)))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (>= (+ 35 7) (* 6 7)))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (>= (+ 35 6) (* 6 7)))", { actual ->
            assertEquals("NIL", actual)
        })
    }

    @Test
    fun greaterThan() {
        run("(print (> 1 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (> 2 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (> 2 2 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (> 2 1 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (> 1 1 1))", { actual ->
            assertEquals("NIL", actual)
        })

        run("(print (> 3 2 1))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (> (+ 35 8) (* 6 7)))", { actual ->
            assertEquals("T", actual)
        })

        run("(print (> (+ 35 7) (* 6 7)))", { actual ->
            assertEquals("NIL", actual)
        })
    }

    @Test
    fun simpleIf() {
        run("(print (if (= 1 1) 42 0))", { actual ->
            assertEquals("42", actual)
        })

        run("(print (if (= 1 2) 42 0))", { actual ->
            assertEquals("0", actual)
        })

        run("(print (if (= 1 2) 42))", { actual ->
            assertEquals("NIL", actual)
        })
    }

    @Test
    fun normalIf() {
        run("(print (if (= (+ 35 7) (* 6 7)) (- 50 8) 0))", { actual ->
            assertEquals("42", actual)
        })
    }

   @Test
    fun simpleDefun() {
       run("(defun x (a b) (* a b)) (print (x 6 7))", { actual ->
           assertEquals("42", actual)
       })
    }

    @Test
    fun fib() {
        run("(defun fib (n) (if (= n 0) 1 (" +
                "  if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))" +
                "(print (fib 10))",
                { actual ->
                    assertEquals("89", actual)
                }
        )
    }

    @Test
    fun fib2() {
        run("(defun fib (n x1 x2) (if (<= n 0) x1 (fib (- n 1) x2 (+ x1 x2))))" +
                "(print (fib 10 1 1))", { actual ->
            assertEquals("89", actual)
        })
    }
}

