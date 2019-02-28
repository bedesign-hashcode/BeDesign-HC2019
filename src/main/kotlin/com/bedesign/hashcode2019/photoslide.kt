package com.bedesign.hashcode2019

import kotlin.math.min

var negativeCounter = 0

data class Photo(val id: Int, val type: String, val nTags: Int, val tags: List<String>, val composedId: String)

fun toPhoto(s: String, id: Int): Photo {
    if (s.isEmpty()) {
        negativeCounter -= 1
        return Photo( negativeCounter, "", 0, listOf(), "" + negativeCounter)
    }

    val values = s.split(" ")
    return if (values[0] == "H") Photo(id, values[0], values[1].toInt(), (2 .. (values.size - 1)).map { values[it] }, "" + id)
    else Photo(id, values[0], values[1].toInt(), (2 .. (values.size - 2)).map { values[it] }, values[values.size - 1].replace("-", " "))
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
    val lines = readFromFile("/Users/mmanzi/Downloads/c_memorable_moments.txt")

    var allHorizzontal = mutableListOf<String>()
    allHorizzontal.add(lines[0])
    var firstVerticalIndex = -1
    var firstVertical = ""
    var indexLines = 0
    lines.subList(1, lines.size).forEach {
        if (it.split(" ")[0].equals("H")) {
            allHorizzontal.add(it)
        } else {
            if (firstVertical.isEmpty()) {
                firstVertical = it
                firstVerticalIndex = indexLines
                allHorizzontal.add("")
            } else {
                val valuesFirst = firstVertical.split(" ")
                val tagsFirst = (2 .. (valuesFirst.size - 1)).map { valuesFirst[it] }
                val valuesSecond = it.split(" ")
                val tagsSecond = (2 .. (valuesSecond.size - 1)).map { valuesSecond[it] }
                val sum = (tagsFirst + tagsSecond).toSet()
                allHorizzontal.add("V ${sum.size} ${sum.joinToString(" ")} $firstVerticalIndex-$indexLines")
                firstVertical = ""
                firstVerticalIndex = -1
            }
        }
        indexLines += 1
    }

    var l: Int? = null
    var index = 0
    val inverse: MutableMap<String, MutableList<Photo>> = mutableMapOf()
    val photos: MutableList<Photo> = mutableListOf()
    allHorizzontal.forEach {
        if (l == null) {
            l = it.toInt()
        } else {
            val photo = toPhoto(it, index)
            photos.add(photo)
            photo.tags.forEach { t ->
                if (inverse[t] != null) {
                    inverse[t]!!.add(photo)
                } else {
                    inverse[t] = mutableListOf(photo)
                }
            }
            index += 1
        }
    }

    val processed = mutableSetOf<String>()

    var element = photos[0]
    processed.add(element.composedId)
    element.tags.forEach { t -> inverse[t]!!.remove(element) }
    val size = photos.size
    while (processed.size < photos.size) {
        var similar = processing(photos, inverse, element, size)
        element = if(similar != null && similar.tags.size != 0) similar else photos.first { !processed.contains(it.composedId) }
        processed.add(element.composedId)
        element.tags.forEach { t -> inverse[t]!!.remove(element) }
        if (processed.size % 1000 == 0) {
            println(processed.size)
        }
    }
    val withoutEmpty = processed.filter { !it.contains("-") }
    val result = listOf("" + withoutEmpty.size) + withoutEmpty
    writeToFile(result , "/Users/mmanzi/Downloads/result.txt")

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
        if (score > max && score > 0) {
            max = score
            maxIndex = i
        }
    }
    return photos[maxIndex]
}