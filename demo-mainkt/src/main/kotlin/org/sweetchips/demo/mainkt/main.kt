package org.sweetchips.demo.mainkt

import org.sweetchips.annotations.Hide
import org.sweetchips.annotations.Uncheckcast
import java.lang.ClassCastException

private const val CLASS_NAME = "org.sweetchips.demo.mainkt.MainKt"

fun main(args: Array<String>) {
    println("checkConst: ${checkConst()}")
    println("checkHide: ${checkHide()}")
    println("checkCast: ${checkCast()}")
    println("checkTail: ${checkTail()}")
    println("checkOver: ${checkOver()}")
}

@Hide
private const val SYNTHETIC = 0x00001000

@Hide
private fun checkHide(): Boolean {
    return Class.forName(CLASS_NAME).let {
        return it.getDeclaredMethod("checkHide").modifiers.and(SYNTHETIC) == SYNTHETIC
                && it.getDeclaredField("SYNTHETIC").modifiers.and(SYNTHETIC) == SYNTHETIC
    }
}

@Uncheckcast(String::class, Integer::class)
private fun checkCast(): Boolean {
    val i = 0.0
    i as String
    i as Integer
    try {
        i as Long
    } catch (_: ClassCastException) {
        return true
    }
    return false
}

private const val sMax = Short.MAX_VALUE

private fun checkTail(): Boolean {
    return try {
        recursive(0) > sMax
    } catch (_: StackOverflowError) {
        false
    }
}

private fun checkOver(): Boolean {
    return try {
        over(0) <= sMax
    } catch (_: StackOverflowError) {
        true
    }
}

private fun recursive(x: Long): Long {
    val y = x + 1
    if (y > sMax) {
        return y
    }
    return recursive(y);
}

private fun over(x: Long): Long {
    val y = x + 1
    if (y > sMax) {
        return y
    }
    return over(y);
}

private fun checkConst(): Boolean {
    Class.forName(CLASS_NAME).declaredFields.map { it.name }.let {
        return it.contains("SYNTHETIC")
                && !it.contains("CLASS_NAME")
                && !it.contains("sMax")
    }
}