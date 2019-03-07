package com.bedesign.hashcode2019

import java.io.File

fun readFromFile(p: String) = File(p).readLines()

fun writeToFile(s: String, p: String) = File(p).printWriter().use { out -> out.print(s) }

fun writeToFile(s: List<String>, p: String) = File(p).printWriter().use { out ->
    val size = s.size
    s.forEachIndexed { index, s -> if (index == size - 1) out.print(s) else out.println(s) }
}