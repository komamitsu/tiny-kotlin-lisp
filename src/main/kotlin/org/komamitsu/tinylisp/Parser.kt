package org.komamitsu.tinylisp

import java.io.EOFException
import java.io.InputStream

class Parser(val input: InputStream) {
    private val readAhead = mutableListOf<Char>()

    fun parse(): Node? {
        try {
            skipSpaces()
        }
        catch (e: EOFException) {
            return null
        }

        return parseWithoutToken()
    }

    private fun parseWithoutToken(): Node {
        return parseWithToken(nextToken())
    }

    private fun parseWithToken(token: String) : Node {
        return when (token) {
            "'" -> {
                val node = parseWithoutToken()
                node.quoted = true
                node
            }
            "(" -> parseCell()
            ")" -> throw IllegalStateException("Unexpected token: [$token]")
            "t" -> TrueNode
            else -> {
                val first = token[0]
                if (first.isDigit() || ((first == '+' || first == '-') && token.length > 1 && token[1].isDigit()))
                    IntegerNode(token.toLong())
                else
                    SymbolNode(token)
            }
        }
    }

    private fun parseCell() : Node {
        val token = nextToken()
        return when (token) {
            ")" -> NilNode
            "." -> {
                // The next should be a cdr
                val cdr = parseWithoutToken()
                val nextToken = nextToken()
                if (nextToken != ")")
                    throw IllegalStateException("Unexpected token: [$nextToken]")
                cdr
            }
        /*
        "=" -> EqualNode(asCell(parseCell()))
        "/=" -> NotEqualNode(asCell(parseCell()))
        "<" -> LessThanNode(asCell(parseCell()))
        "<=" -> LessThanOrEqualNode(asCell(parseCell()))
        ">" -> GreaterThanNode(asCell(parseCell()))
        ">=" -> GreaterThanOrEqualNode(asCell(parseCell()))
        "+" -> AddNode(asCell(parseCell()))
        "-" -> SubtractNode(asCell(parseCell()))
        "*" -> MultiplyNode(asCell(parseCell()))
        "/" -> DivideNode(asCell(parseCell()))
        "if" -> IfNode(asCell(parseCell()))
        "cdr" -> CarNode(asCell(parseCell()))
        "cdr" -> CdrOpNode(asCell(parseCell()))
        "cons" -> ConsOpNode(asCell(parseCell()))
        */
            else -> {
                val car = parseWithToken(token)
                val cdr = parseCell()
                CellNode(car, cdr)
            }
        }
    }

    private fun readFromStream(): Char {
        val b = input.read()
        if (b == -1)
            throw EOFException()
        return b.toChar()
    }

    private fun skipSpaces() {
        while (true) {
            val c = readFromStream()
            if (c.isWhitespace())
                continue

            readAhead.add(c)
            return
        }
    }

    private fun nextChar() : Char {
        if (readAhead.isEmpty()) {
            return readFromStream()
        }
        else {
            return readAhead.removeAt(0)
        }
    }

    private fun nextToken() : String {
        while (true) {
            val token = nextEmptiableToken()
            if (token.isNotBlank())
                return token
        }
    }

    private fun nextEmptiableToken() : String {
        val c = nextChar()
        if (c == '\'' || c == '(' || c == ')' || c == '.') {
            return c.toString()
        }

        val s = StringBuilder()
        if (!c.isWhitespace())
            s.append(c)

        while (true) {
            val c = nextChar()
            if (c == '\'' || c == '(' || c == ')' || c == '.') {
                readAhead.add(c)
                return s.toString().trim()
            }
            else if (c.isWhitespace()) {
                if (s.isNotEmpty())
                    return s.toString().trim()
            }
            else if (c.isLetterOrDigit() || c == '-' || c == '+' || c == '*' || c == '/'
                    || c == '=' || c == '>' || c == '<') {
                s.append(c)
            }
            else {
                throw IllegalStateException("Unexpected character: [$c]")
            }
        }
    }
}