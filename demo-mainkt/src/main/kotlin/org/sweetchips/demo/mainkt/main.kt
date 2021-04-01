package org.sweetchips.demo.mainkt

import org.sweetchips.annotations.Uncheckcast
import java.lang.ClassCastException

@Uncheckcast(String::class, Integer::class)
fun main(args: Array<String>) {
    val i = 0.0
    i as String
    i as Integer
    try {
        i as Long
    } catch (_: ClassCastException) {
    }
}