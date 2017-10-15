package org.komamitsu.tinylisp

class InterpretVisitor(var env: Env) : Visitor<Node> {
    fun <A, T> foldLeft(params: CellNode, f: (A?, T) -> A?): A
        where T: Node
    {
        var result: A? = null
        var first = true
        var car = params.car
        var cdr = params.cdr
        while (true) {
            val evaluated = car.accept(this)

            result = f(result, evaluated as T)
            if (first) {
                first = false
            }
            if (cdr is NilNode) {
                if (result == null) {
                    throw IllegalStateException(params.toString())
                }
                return result
            }

            val evaluatedNext = cdr as CellNode
            car = evaluatedNext.car
            cdr = evaluatedNext.cdr
        }
    }

    private fun compare(params: CellNode, init: () -> Boolean,
                        f: (Pair<Boolean, IntegerNode>, IntegerNode) -> Boolean): BoolNode
    {
        val result = foldLeft<Pair<Boolean, IntegerNode>, IntegerNode>(params,
                { acc, x ->
                    val evaluated = x.accept(this).asIntegerNode()
                    if (acc == null) {
                        Pair(init(), evaluated)
                    } else {
                        Pair(f(acc, evaluated), evaluated)
                    }
                }
        )
        return if (result.first) TrueNode else NilNode
    }

    override fun visitIntegerNode(integerNode: IntegerNode): Node {
        return integerNode
    }

    override fun visitBoolNode(boolNode: BoolNode): Node {
        return boolNode
    }

    override fun visitSymbolNode(symbolNode: SymbolNode): Node {
        return env.search(symbolNode) ?: throw IllegalStateException("Unknown Symbol: $symbolNode")
    }

    override fun visitCellNode(cell: CellNode): Node {
        if (cell.quoted) {
            return cell
        }

        val name = cell.car.asSymbolNode()
        val params = cell.cdr.asCellNode()
        return when (name.key) {
            "car" -> {
                val consCell = params.getCarOfNilTerminatedCellNode().accept(this).asCellNode()
                consCell.car
            }

            "cdr" -> {
                val consCell = params.getCarOfNilTerminatedCellNode().accept(this).asCellNode()
                consCell.cdr
            }

            "cons" -> {
                val first = params.car.accept(this)
                val second = params.cdr.getCarOfNilTerminatedCellNode().accept(this)
                CellNode(first, second)
            }

            "=" -> {
                compare(params, { true },
                        { acc, x -> acc.first && acc.second.value == x.value })
            }

            "/=" -> {
                val result = foldLeft<Pair<Boolean, MutableList<IntegerNode>>, IntegerNode>(params,
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
                if (result.first) TrueNode else NilNode
            }

            "<=" -> {
                compare(params, { true },
                        { acc, x -> acc.first && acc.second.value <= x.value })
            }

            ">=" -> {
                compare(params, { true },
                        { acc, x -> acc.first && acc.second.value >= x.value })
            }

            "<" -> {
                compare(params, { true },
                        { acc, x -> acc.first && acc.second.value < x.value })
            }

            ">" -> {
                compare(params, { true },
                        { acc, x -> acc.first && acc.second.value > x.value })
            }

            "+" -> {
                foldLeft<IntegerNode, IntegerNode>(params,
                        { acc, x -> if (acc == null) x else IntegerNode(acc.value + x.value) })
            }

            "-" -> {
                foldLeft<IntegerNode, IntegerNode>(params,
                        { acc, x -> if (acc == null) x else IntegerNode(acc.value - x.value) })
            }

            "*" -> {
                foldLeft<IntegerNode, IntegerNode>(params,
                        { acc, x -> if (acc == null) x else IntegerNode(acc.value * x.value) })
            }

            "/" -> {
                foldLeft<IntegerNode, IntegerNode>(params,
                        { acc, x -> if (acc == null) x else IntegerNode(acc.value / x.value) })
            }

            "if" -> {
                val cond = params.getCarOfCellNode().accept(this).asBoolNode()
                val thenElse = params.getCdrOfCellNode().asCellNode()
                return when (cond) {
                    is NilNode -> {
                        thenElse.cdr.getCarOfCellNode().accept(this)
                    }
                    else -> thenElse.car.accept(this)
                }
            }

            "defun" -> {
                val funcName = params.car.asSymbolNode()
                val next = params.cdr.asCellNode()
                // TODO: Check all arguments are Symbol
                val args = next.car.asCellNode()
                val body = next.cdr.asCellNode()

                env.addEntry(funcName.key, CellNode(args, body))

                funcName
            }

            "print" -> {
                System.out.println(params.getCarOfNilTerminatedCellNode().accept(this))

                NilNode
            }

            else -> {
                val newEnv = Env(null)

                val funcName = name
                val func = env.search(funcName) ?: throw IllegalStateException("Unexpected Symbol: $funcName")
                var argsCell = func.getCarOfCellNode().asCellNode()

                var paramsCell = params.asCellNode()

                while (true) {
                    val argName = argsCell.car.asSymbolNode()
                    val paramValue = paramsCell.car
                    newEnv.addEntry(argName.key, paramValue.accept(this))

                    val nextArgName = argsCell.cdr
                    val nextParamValue = paramsCell.cdr
                    if (nextArgName is NilNode) {
                        if (nextParamValue is NilNode)
                            break

                        throw IllegalStateException(
                                "The numbers of Func's arguments and parameters don't match: " +
                                        "args=[$argsCell], params=[$paramsCell]")
                    }
                    else {
                        if (nextParamValue is NilNode)
                            throw IllegalStateException(
                                    "The numbers of Func's arguments and parameters don't match: " +
                                            "args=[$argsCell], params=[$paramsCell]")
                    }
                    argsCell = nextArgName.asCellNode()
                    paramsCell = nextParamValue.asCellNode()
                }
                newEnv.parent = env
                env = newEnv

                val body = func.getCdrOfCellNode()
                val result = body.getCarOfCellNode().asCellNode().accept(this)

                // Pop the current env
                if (env.parent != null)
                    env = env.parent!!

                result
            }
        }
    }
}