package org.komamitsu.tinylisp

class Eval {
    private fun getCell(node: Node): Cell {
        return when (node) {
            is Cell -> node
            else -> throw IllegalStateException("This Node isn't a Cell: $node")
        }
    }

    private fun getNilTerminatedCell(node: Node): Cell {
        val cell = getCell(node)
        if (cell.cdr !is Nil) {
            throw IllegalStateException("This Node isn't a nil terminated Cell: $node")
        }
        return cell
    }

    private fun getInteger(node: Node): Integer {
        if (node !is Integer) {
            throw IllegalStateException("This Node isn't an Integer: $node")
        }
        return node
    }

    private fun getSymbol(node: Node): Symbol {
        if (node !is Symbol) {
            throw IllegalStateException("This Node isn't a Symbol: $node")
        }
        return node
    }

    private fun getFunc(node: Node): EmbeddedFunc {
        if (node !is EmbeddedFunc) {
            throw IllegalStateException("This Node isn't a Func: $node")
        }
        return node
    }

    fun eval(env: Env, node: Node): Node {
        if (node.quoted)
            return node

        return when (node) {
            is Cell -> evalCell(env, node)
            is Integer -> node
            is True -> node
            is Nil -> node
            is Symbol -> eval(env, evalSymbol(env, node))
            else -> throw IllegalStateException("This Node can't be evaluated: $node")
        }
    }

    fun evalSymbol(env: Env, symbol: Symbol): Node {
        return env.search(symbol) ?:
                throw IllegalStateException("Unknown Symbol: $symbol")
    }

    fun evalCell(env: Env, cell: Cell): Node {
        return when (cell.car) {
            is Symbol -> {
                val key = cell.car
                val value = cell.cdr
                val bounded = evalSymbol(env, key)
                when (bounded) {
                    is EmbeddedFunc -> applyToEmbeddedFunc(env, bounded, value)
                    is Func -> applyToFunc(env, bounded, value)
                    is Integer -> bounded
                    is Cell -> evalCell(env, bounded)
                    else -> throw IllegalStateException("This Node in a Cell can't be evaluated: $bounded")
                }
            }
            else -> throw IllegalStateException(cell.toString())
        }
    }

    fun applyToEmbeddedFunc(env: Env, embeddedFunc: EmbeddedFunc, params: Node): Node {
        return embeddedFunc.process(env, getCell(params))
    }

    fun applyToFunc(env: Env, func: Func, params: Node): Node {
        val currentEnv = Env(env).mergeAsParent(func.capturedEnv)
        var argsCell = func.params
        var paramsCell = getCell(params)

        while (true) {
            val argName = argsCell.car
            val paramValue = paramsCell.car
            currentEnv.addEntry(getSymbol(argName).key, eval(env, paramValue))

            val nextArgName = argsCell.cdr
            val nextParamValue = paramsCell.cdr
            if (nextArgName is Nil) {
                if (nextParamValue is Nil)
                    break

                throw IllegalStateException(
                        "The numbers of Func's arguments and parameters don't match: " +
                                "args=[$argsCell], params=[$paramsCell]")
            }
            else {
                if (nextParamValue is Nil)
                    throw IllegalStateException(
                            "The numbers of Func's arguments and parameters don't match: " +
                                    "args=[$argsCell], params=[$paramsCell]")
            }
            argsCell = getCell(nextArgName)
            paramsCell = getCell(nextParamValue)
        }

        var body = func.body
        while (true) {
            val result = eval(currentEnv, body.car)
            if (body.cdr is Nil) {
                return result
            }
            body = getCell(body.cdr)
        }
    }

    fun <A, T> foldLeft(env: Env, params: Cell, f: (A?, T) -> A?): A
        where T: Node
    {
        var result: A? = null
        var first = true
        var car = params.car
        var cdr = params.cdr
        while (true) {
            val evaluated = eval(env, car)
            result = f(result, evaluated as T)
            if (first) {
                first = false
            }
            if (cdr is Nil) {
                if (result == null) {
                    throw IllegalStateException(params.toString())
                }
                return result
            }
            val evaluatedNext = getCell(cdr)
            car = evaluatedNext.car
            cdr = evaluatedNext.cdr
        }
    }

    private fun compare(env: Env, params: Cell, init: () -> Boolean,
                        f: (Pair<Boolean, Integer>, Integer) -> Boolean): Bool
    {
        val result = foldLeft<Pair<Boolean, Integer>, Integer>(env, params,
                { acc, x ->
                    if (acc == null) {
                        Pair(init(), x)
                    } else {
                        Pair(f(acc, x), x)
                    }
                }
        )
        return if (result.first) True else Nil
    }

    fun getWorld(): Env {
        val global = Env.Global

        global.addEntry("car", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                val cell = getNilTerminatedCell(params)
                return getCell(cell.car).car
            }
        })

        global.addEntry("cdr", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                val cell = getNilTerminatedCell(params)
                return getCell(cell.car).cdr
            }
        })

        global.addEntry("cons", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                val first = params.car
                val second = getNilTerminatedCell(params.cdr).car
                return Cell(first, second)
            }
        })

        global.addEntry("+", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                return foldLeft<Integer, Integer>(env, params,
                        { acc, x -> if (acc == null) x else Integer(acc.value + x.value) })
            }
        })


        global.addEntry("-", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                return foldLeft<Integer, Integer>(env, params,
                        { acc, x -> if (acc == null) x else Integer(acc.value - x.value) })
            }
        })

        global.addEntry("*", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                return foldLeft<Integer, Integer>(env, params,
                        { acc, x -> if (acc == null) x else Integer(acc.value * x.value) })
            }
        })

        global.addEntry("/", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                return foldLeft<Integer, Integer>(env, params,
                        { acc, x -> if (acc == null) x else Integer(acc.value / x.value) })
            }
        })

        global.addEntry("=", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                return compare(env, params, { true },
                        { acc, x -> acc.first && acc.second.value == x.value })
            }
        })

        global.addEntry("/=", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                val result = foldLeft<Pair<Boolean, MutableList<Integer>>, Integer>(env, params,
                        { acc, x ->
                            if (acc == null) {
                                Pair(true, mutableListOf(x))
                            }
                            else {
                                if (acc.first) {
                                    val state = !acc.second.contains(x)
                                    acc.second.add(x)
                                    Pair(state, acc.second)
                                } else {
                                    acc
                                }
                            }
                        })
                return if (result.first) True else Nil
            }
        })

        global.addEntry("<=", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                return compare(env, params, { true },
                        { acc, x -> acc.first && acc.second.value <= x.value })
            }
        })

        global.addEntry(">=", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                return compare(env, params, { true },
                        { acc, x -> acc.first && acc.second.value >= x.value })
            }
        })

        global.addEntry("<", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                return compare(env, params, { true },
                        { acc, x -> acc.first && acc.second.value < x.value })
            }
        })

        global.addEntry(">", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                return compare(env, params, { true },
                        { acc, x -> acc.first && acc.second.value > x.value })
            }
        })

        /*
        (defun . (test . ((a . (b)) . (+ . (a . (b))))
                   ^ funcName
                         ^ next
                          ^ args
                                      ^ body
         */
        global.addEntry("defun", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                val funcName = getSymbol(params.car)
                val next = getCell(params.cdr)
                // TODO: Check all arguments are Symbol
                val args = getCell(next.car)
                val body = getCell(next.cdr)

                Env.Global.addEntry(funcName.key, Func(env, args, body))

                return funcName
            }
        })

        global.addEntry("setq", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                val varName = getSymbol(params.car)
                val value = getNilTerminatedCell(params.cdr).car
                val evaluatedValue = eval(env, value)

                Env.Global.addEntry(varName.key, evaluatedValue)

                return varName
            }
        })

        global.addEntry("if", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                val cond = eval(env, params.car)
                val thenElse = getCell(params.cdr)
                return when (cond) {
                    is Nil -> {
                        if (thenElse.cdr !is Nil)
                            eval(env, getCell(thenElse.cdr).car)
                        else
                            Nil
                    }
                    else -> eval(env, thenElse.car)
                }
            }
        })

        global.addEntry("print", object : EmbeddedFunc() {
            override fun process(env: Env, params: Cell): Node {
                printNode(env, eval(env, getNilTerminatedCell(params).car))
                println()
                return Nil
            }
        })

        return Env(global)
    }

    private fun printNode(env:Env, node: Node) {
        when (node) {
            is Integer -> print(node.value)
            is Bool -> print(node.bool)
            is Cell -> {
                print("(")
                var isFirst = true
                foldLeft<Unit, Node>(env, node,
                        { acc, x ->
                            if (isFirst)
                                isFirst = false
                            else
                                print(" ")

                            printNode(env, x)
                        })
                print(")")
            }
            is Symbol -> print(node.key)
            is True -> print("T")
            is Nil -> print("NIL")
            else -> print(node)
        }
    }
}