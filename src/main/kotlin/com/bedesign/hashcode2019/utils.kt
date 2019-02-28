package com.bedesign.hashcode2019

import java.io.File

fun readFromFile(p: String) = File(p).readLines()

fun writeToFile(s: String, p: String) {
    File(p).printWriter().use { out -> out.println(s) }
}