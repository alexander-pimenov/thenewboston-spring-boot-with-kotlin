package tv.codealong.tutorials.springboot.thenewboston.utils

import java.util.*

fun isEven(number: Int): Boolean = number % 2 == 0
fun isOdd(number: Int): Boolean = number % 2 != 0
fun isPalindrome(word: String): Boolean =
    word.lowercase(Locale.getDefault()) == word.lowercase(Locale.getDefault()).reversed()
fun add(a: Int, b: Int) = a + b
