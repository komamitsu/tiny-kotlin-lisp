package org.komamitsu.tinylisp

import org.junit.Test

import org.junit.Assert.*

class EvalTest {
    private val eval = Eval()

    @Test
    fun evalAdd() {
        assertEquals(
                Integer(49),
                eval.eval(eval.getWorld(),
                        Cell(Symbol("+"), Cell(Integer(42), Cell(Integer(7), Nil)))))
    }

    @Test
    fun evalSubtract() {
        assertEquals(
                Integer(30),
                eval.eval(eval.getWorld(),
                        Cell(Symbol("-"), Cell(Integer(42), Cell(Integer(7), Cell(Integer(5), Nil))))))
    }

    @Test
    fun evalMultiply() {
        assertEquals(
                Integer(42),
                eval.eval(eval.getWorld(),
                        Cell(Symbol("*"), Cell(Integer(3), Cell(Integer(7), Cell(Integer(2), Nil))))))
    }

    @Test
    fun evalDivide() {
        assertEquals(
                Integer(6),
                eval.eval(eval.getWorld(),
                        Cell(Symbol("/"), Cell(Integer(42), Cell(Integer(7), Nil)))))
    }

    @Test
    fun evalCar() {
        assertEquals(
                Integer(42),
                eval.eval(eval.getWorld(),
                        Cell(Symbol("car"), Cell(Cell(Integer(42), Cell(Integer(7), Nil)), Nil))))
    }

    @Test
    fun evalCdr() {
        assertEquals(
                Cell(Integer(7), Nil),
                eval.eval(eval.getWorld(),
                        Cell(Symbol("cdr"), Cell(Cell(Integer(42), Cell(Integer(7), Nil)), Nil))))
    }

    @Test
    fun evalEqual() {
        assertEquals(
                True,
                eval.eval(eval.getWorld(),
                        Cell(Symbol("="), Cell(Integer(42), Cell(Integer(42), Cell(Integer(42), Nil))))))

        assertEquals(
                Nil,
                eval.eval(eval.getWorld(),
                        Cell(Symbol("="), Cell(Integer(42), Cell(Integer(42), Cell(Integer(7), Nil))))))
    }

    @Test
    fun evalNotEqual() {
        assertEquals(
                True,
                eval.eval(eval.getWorld(),
                        Cell(Symbol("/="), Cell(Integer(42), Cell(Integer(41), Cell(Integer(40), Nil))))))

        assertEquals(
                Nil,
                eval.eval(eval.getWorld(),
                        Cell(Symbol("/="), Cell(Integer(42), Cell(Integer(7), Cell(Integer(42), Nil))))))

        assertEquals(
                Nil,
                eval.eval(eval.getWorld(),
                        Cell(Symbol("/="), Cell(Integer(42), Cell(Integer(42), Cell(Integer(7), Nil))))))
    }

    @Test
    fun evalGraterThan() {
        assertEquals(
                True,
                eval.eval(eval.getWorld(),
                        Cell(Symbol(">"), Cell(Integer(42), Cell(Integer(41), Cell(Integer(40), Nil))))))

        assertEquals(
                Nil,
                eval.eval(eval.getWorld(),
                        Cell(Symbol(">"), Cell(Integer(42), Cell(Integer(41), Cell(Integer(41), Nil))))))
    }

    @Test
    fun evalLessThan() {
        assertEquals(
                True,
                eval.eval(eval.getWorld(),
                        Cell(Symbol("<"), Cell(Integer(42), Cell(Integer(43), Cell(Integer(44), Nil))))))

        assertEquals(
                Nil,
                eval.eval(eval.getWorld(),
                        Cell(Symbol("<"), Cell(Integer(42), Cell(Integer(43), Cell(Integer(42), Nil))))))
    }

    @Test
    fun evalGraterThanOrEquals() {
        assertEquals(
                True,
                eval.eval(eval.getWorld(),
                        Cell(Symbol(">="), Cell(Integer(42), Cell(Integer(41), Cell(Integer(41), Nil))))))

        assertEquals(
                Nil,
                eval.eval(eval.getWorld(),
                        Cell(Symbol(">="), Cell(Integer(42), Cell(Integer(41), Cell(Integer(42), Nil))))))
    }

    @Test
    fun evalLessThanOrEquals() {
        assertEquals(
                True,
                eval.eval(eval.getWorld(),
                        Cell(Symbol("<="), Cell(Integer(42), Cell(Integer(43), Cell(Integer(43), Nil))))))

        assertEquals(
                Nil,
                eval.eval(eval.getWorld(),
                        Cell(Symbol("<="), Cell(Integer(42), Cell(Integer(43), Cell(Integer(42), Nil))))))
    }

    @Test
    fun evalDefun() {
        val env = eval.getWorld()
        /*
            (defun . (test . ((a . (b)) . (* . (a . (b))))
                      ^ funcName
                             ^ next
                              ^ args
                                          ^ body
         */
        assertEquals(
                Symbol("test"),
                eval.eval(env,
                        Cell(Symbol("defun"), Cell(
                                Symbol("test"), Cell(
                                Cell(Symbol("a"), Cell(Symbol("b"), Nil)), Cell(
                                Cell(Symbol("*"), Cell(Symbol("a"), Cell(Symbol("b"), Nil))), Nil
                        ))))))

        assertEquals(
                Integer(42),
                eval.eval(env,
                        Cell(Symbol("test"), Cell(Integer(6), Cell(Integer(7), Nil)))))
    }

    @Test
    fun evalNestedDefun() {
        val env = eval.getWorld()
        /*
            (defun . (defun_x . ((a) . (defun . (x . ((b) . (* . (a . (b)))))))))
                      ^ 0#funcName
                                ^ 0#next
                                 ^ 0#args
                                       ^ 0#body
                                                 ^ 1#funcName
                                                     ^ 1#next
                                                      ^ 1#args
                                                            ^ 1#body
         */
        assertEquals(
                Symbol("defun_x"),
                eval.eval(env,
                        Cell(Symbol("defun"), Cell(
                                Symbol("defun_x"), Cell(
                                Cell(Symbol("a"), Nil), Cell(
                                Cell(Symbol("defun"), Cell(
                                    Symbol("x"), Cell(
                                    Cell(Symbol("b"), Nil), Cell(
                                    Cell(Symbol("*"), Cell(Symbol("a"), Cell(Symbol("b"), Nil))),
                                        Nil)))),
                                Nil))))))

        assertEquals(
                Symbol("x"),
                eval.eval(env,
                        Cell(Symbol("defun_x"), Cell(Integer(6), Nil))))

        assertEquals(
                Integer(42),
                eval.eval(env,
                        Cell(Symbol("x"), Cell(Integer(7), Nil))))

    }

    @Test
    fun evalSetq() {
        val env = eval.getWorld()

        assertEquals(
                Symbol("x"),
                eval.eval(env,
                        Cell(Symbol("setq"), Cell(
                                Symbol("x"), Cell(Integer(42), Nil)))))
        assertEquals(
                Integer(42),
                eval.eval(env, Symbol("x")))
    }

    @Test
    fun evalIf() {
        val env = eval.getWorld()

        assertEquals(
                Integer(42),
                eval.eval(env,
                        Cell(Symbol("if"), Cell(True, Cell(Integer(42), Cell(Integer(0), Nil))))))

        assertEquals(
                Integer(42),
                eval.eval(env,
                        Cell(Symbol("if"), Cell(Integer(0), Cell(Integer(42), Cell(Integer(0), Nil))))))

        assertEquals(
                Integer(42),
                eval.eval(env,
                        Cell(Symbol("if"), Cell(Nil, Cell(Integer(0), Cell(Integer(42), Nil))))))

        assertEquals(
                Nil,
                eval.eval(env,
                        Cell(Symbol("if"), Cell(Nil, Cell(Integer(0), Nil)))))
    }
}