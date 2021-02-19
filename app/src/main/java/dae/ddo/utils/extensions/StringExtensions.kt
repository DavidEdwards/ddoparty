package dae.ddo.utils.extensions

fun <T> T?.orElse(item: T): T {
    return this ?: item
}