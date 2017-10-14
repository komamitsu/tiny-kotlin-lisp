package org.komamitsu.tinylisp

open class Env(var parent: Env?) {
    companion object Global: Env(null)

    val table: MutableMap<String, Node> = mutableMapOf()

    fun addEntry(key: String, value: Node) {
        table.put(key, value)
    }

    fun search(symbolNode: SymbolNode): Node? {
        var current: org.komamitsu.tinylisp.Env? = this
        while (true) {
            if (current == null) {
                return null
            }
            val value = current.table[symbolNode.key]
            if (value != null) {
                if (value is SymbolNode) {
                    return search(value)
                }
                return value
            }

            current = current.parent
        }
    }
}