package tv.codealong.tutorials.springboot.thenewboston.utils

fun String.maskVersion(maxBits: Int = 8): Int {
    this.trim().removePrefix("v")
        .split('.')
        .map { it.toInt() }.let {
            val major = it[0]
            val minor = it[1]
            val patch = it[2]
            return (major shl maxBits * 2) or (minor shl maxBits * 1) or (patch shl maxBits * 0)
        }
}

fun Int.unmaskVersion(maxBits: Int = 8): String {
    val major = (this shr maxBits * 2) and ((1 shl maxBits) - 1)
    val minor = (this shr maxBits * 1) and ((1 shl maxBits) - 1)
    val patch = (this shr maxBits * 0) and ((1 shl maxBits) - 1)
    return "v$major.$minor.$patch"
}