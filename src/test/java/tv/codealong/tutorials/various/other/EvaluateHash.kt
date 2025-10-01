package tv.codealong.tutorials.various.other

import java.util.Objects.hash

class EvaluateHash {


}

/**
 * Хэш преобразуется в номер партиции по формуле:
 * partition = hash(key) % total_partitions
 */
fun main(args: Array<String>) {


    val totalPartitions = 10
    val partition1: Int = hash("some_key_for_test") % totalPartitions    // hash() = 2033546674 -> partition1 = 4
    println("partition1 => $partition1. hash: ${hash("some_key_for_test")}")
    val partition2: Int = hash("good weather") % totalPartitions         // hash() = -1018673232 -> partition2 = -2
    println("partition2 => $partition2. hash: ${hash("good weather")}")
    val partition3: Int = hash("have a nice day") % totalPartitions      // hash() = -933326065 -> partition3 = -5
    println("partition3 => $partition3. hash: ${hash("have a nice day")}")

    val partition4: Int = hash(123) % totalPartitions                    // 4
    println(partition4)
    val partition5: Int = hash(789) % totalPartitions                    //  0
    println(partition5)

}