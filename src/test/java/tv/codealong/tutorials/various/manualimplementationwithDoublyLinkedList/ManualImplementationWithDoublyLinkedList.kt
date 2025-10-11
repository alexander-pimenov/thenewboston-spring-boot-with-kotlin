package tv.codealong.tutorials.various.manualimplementationwithDoublyLinkedList

class ManualLRUCache<K, V>(private val capacity: Int) {
    private val nodeMap = HashMap<K, Node<K, V>>()
    private val head = Node<K, V>(null, null)
    private val tail = Node<K, V>(null, null)

    init {
        head.next = tail
        tail.prev = head
    }

    fun get(key: K): V? {
        val node = nodeMap[key] ?: return null
        moveToHead(node) // Вручную перемещаем в начало
        return node.value
    }

    // ... много ручной работы с указателями

    private fun moveToHead(node: Node<K, V>) {
        TODO("Not yet implemented")
    }

    class Node<K, V>(
        val head: K? = null,
        val tail: V? = null,
    ) {
        val value: V? = null
        var next: Node<K, V>? = null
        var prev: Node<K, V>? = null
    }
}

class SingleLinkList<T> : Iterable<T> {
    var head: ListItem<T>? = null
    var tail: ListItem<T>? = null

    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {
            var current: ListItem<T>? = head

            override fun hasNext(): Boolean {
                return current != null
            }

            override fun next(): T {
                val data: T? = current?.data
                current = current!!.next
                return data!!
            }
        }
    }

    class ListItem<T> {
        var data: T? = null
        var next: ListItem<T>? = null
    }

    val isEmpty: Boolean
        get() = head == null

    fun addToEnd(item: T) {
        val newItem = ListItem<T>()
        newItem.data = item
        if (isEmpty) {
            head = newItem
            tail = newItem
        } else {
            tail!!.next = newItem
            tail = newItem
        }
    }

    fun reverse() {
        if (!isEmpty && head!!.next != null) {
            tail = head
            var current = head!!.next
            head!!.next = null
            while (current != null) {
                val next = current.next
                current.next = head
                head = current
                current = next
            }
        }
    }
}