package tv.codealong.tutorials.various.accessOrder

import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * Часть 1: LinkedHashMap с accessOrder = true
 *
 * LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true)
 * //                              ↑      ↑     ↑
 * //                          размер loadFactor accessOrder
 *
 * Что это значит:
 * - 16 - начальная вместимость (как ArrayList capacity)
 * - 0.75f - load factor (когда HashMap увеличивается в размере)
 * - true - ключевой параметр! Включает ordering по доступу.
 *
 * Часть 2: removeEldestEntry - "Волшебный метод"
 * kotlin
 * override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, CacheEntry<V>>?): Boolean {
 *     return size > config.maxSize
 * }
 * Как работает:
 * Вызывается автоматически при КАЖДОМ добавлении нового элемента
 * eldest - самый старый элемент (первый в LinkedHashMap)
 * Если возвращаем true - eldest удаляется автоматически
 *
 *
 */
fun main() {
    val map = LinkedHashMap<String, String>() // accessOrder = false
    map["A"] = "1"
    map["B"] = "2"
    map["C"] = "3"
    map["D"] = "4"
    map["E"] = "5"
    map.get("A") // Получаем "1", но порядок не меняется: [A, B, C, D, E]
    for ((key, value) in map) {
        println("$key: $value")
    }
    println("-----------")

    val map2 = LinkedHashMap<String, String>(16, 0.75f, true)
    map2["A"] = "1"
    map2["B"] = "2"
    map2["C"] = "3"
    map2["D"] = "4"
    map2["E"] = "5"
    map2.get("A") // Получаем "1" и A перемещается в конец: [B, C, D, E, A]
//                        ↑ Самый недавно использованный
    for ((key, value) in map2) {
        println("$key: $value")
    }
    println("-----------")

    //Если я хочу переопределить override fun removeEldestEntry, то нужно создать новый объект через object !!!
    val map3 = object : LinkedHashMap<String, String>( 3,  0.75f,  true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, String>): Boolean {
            return size > 3 // 3 - максимальное количество элементов
        }
    }
    map3["A"] = "1" // [A]
    map3["B"] = "2" // [A, B]
    map3["C"] = "3" // [A, B, C] - достигли максимума
    map3["D"] = "4" // [B, C, D] - A автоматически удалился!
    //             removeEldestEntry вернул true для A
    for ((key, value) in map3) {
        println("$key: $value")
    }
    println("-----------")
    demonstrateLRU()

}

/**
 * ЦИКЛ РАБОТЫ LRU
 * Цель: Отслеживать порядок доступа к элементам, чтобы знать какой элемент давно не использовался (Least Recently Used)
 */
fun demonstrateLRU() {
    val lruCache = Collections.synchronizedMap(
        object : LinkedHashMap<String, String>(3, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<String, String>): Boolean {
                val shouldRemove = size > 3
                if (shouldRemove) {
                    println("🗑️ Вытесняем: ${eldest.key}")
                }
                return shouldRemove
            }
        }
    )

    println("🔹 Добавляем A, B, C:")
    lruCache["A"] = "1" // [A]
    lruCache["B"] = "2" // [A, B]
    lruCache["C"] = "3" // [A, B, C]
    println("Состояние: ${lruCache.keys.toList()}")

    println("\n🔹 Обращаемся к A (должна переместиться в конец):")
    lruCache["A"]        // [B, C, A] - A стала самой "свежей"
    println("После доступа к A: ${lruCache.keys.toList()}")

    println("\n🔹 Добавляем D (должна вытеснить B):")
    lruCache["D"] = "4" // [C, A, D] - B удалилась как самая "старая"
    println("После добавления D: ${lruCache.keys.toList()}")
}