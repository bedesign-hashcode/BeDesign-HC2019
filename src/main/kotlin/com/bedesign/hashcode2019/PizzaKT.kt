package com.bedesign.hashcode2019

import java.util.concurrent.Executors

fun onThread(run: Runnable) {

}

fun main() {
    val executor = Executors.newFixedThreadPool(8)
    executor.submit {
        println("Pippo")
    }
    println("Ole")
}