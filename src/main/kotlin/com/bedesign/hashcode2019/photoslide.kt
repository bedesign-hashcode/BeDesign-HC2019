package com.bedesign.hashcode2019

import kotlin.math.min

data class Photo(val id: Int, val type: String, val nTags: Int, val tags: List<String>)

fun toPhoto(s: String, id: Int): Photo {
    val values = s.split(" ")
    return Photo(id, values[0], values[1].toInt(), (2 .. (values.size - 1)).map { values[it] })
}

data class Slide(val order: Int, val photos: List<Photo>) {
    fun tags(): Set<String> = photos.map { it.tags }.flatten().toSet()
    fun nTags() = tags().size
}

fun scorer(first: Photo, second: Photo, common: Int): Int {
    val uncommonF = first.tags.size - common
    val uncommonS = second.tags.size - common
    return min(uncommonS, min(uncommonF, common))
}

fun main() {
    val lines = readFromFile("/Users/mmanzi/Downloads/e_shiny_selfies.txt")
    var l: Int? = null
    var index = 0
    val inverse: MutableMap<String, MutableList<Photo>> = mutableMapOf()
    val photos: MutableList<Photo> = mutableListOf()
    lines.forEach {
        if (l == null) {
            l = it.toInt()
        } else {
            val photo = toPhoto(it, index)
            photos.add(photo)
            index += 1
            photo.tags.forEach { t ->
                if (inverse[t] != null) {
                    inverse[t]!!.add(photo)
                } else {
                    inverse[t] = mutableListOf()
                }
            }
        }
    }

    val processed = mutableSetOf<Int>()

    var element = photos[0]
    processed.add(element.id)
    val size = photos.size
    while (processed.size < photos.size) {
        var similar = processing(photos, inverse, element, size)
        element = similar ?: photos[photos.map { it.id }.first { !processed.contains(it) }]
        processed.add(element.id)
        element.tags.forEach { t -> inverse[t]!!.remove(element) }
        if (processed.size % 1000 == 0) {
            println(processed.size)
        }
    }
    println(processed)
}

fun processing(photos: MutableList<Photo>, inverse: MutableMap<String, MutableList<Photo>>, inProcess: Photo, size: Int): Photo? {
    val occurrencies = IntArray(size)
    occurrencies.forEach { occurrencies[it] = - 1 }
    for (tag in inProcess.tags) {
        if (inverse[tag] != null) {
            inverse[tag]!!.forEach { occurrencies[it.id] += 1 }
        }
    }
    var maxIndex = 0
    var max = - 1
    for (i in 0 .. (photos.size - 1)) {
        val score = scorer(inProcess, photos[i], occurrencies[i])
        if (score > max) {
            max = score
            maxIndex = i
        }
    }
    return photos[maxIndex]
}