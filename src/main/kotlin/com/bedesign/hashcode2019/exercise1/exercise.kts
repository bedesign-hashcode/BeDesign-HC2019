import com.bedesign.hashcode2019.framework.ExerciseScript
import java.io.File
import kotlin.math.min

val run = true
data class Photo(val id: Int, val type: String, val numTags: Int, val tags: List<String>)
data class Slide(val photo1: Photo, val photo2: Photo? = null) {
    val id = listOf(photo1.id, photo2?.id ?: -1).filter { it >= 0 }.joinToString(" ") { "$it" }
    val type = if (photo1.type == "H") "H" else "V"
    val tags: List<String> = (photo1.tags + (photo2?.tags ?: listOf())).toSet().toList()
}

fun scoreSimilar(p1: Slide, p2: Slide): Int {
    val uncommonP1 = p1.tags.filter { !p2.tags.contains(it) }.size
    val uncommonP2 = p2.tags.filter { !p1.tags.contains(it) }.size
    val common = p1.tags.size - uncommonP1
    return min(common, min(uncommonP1, uncommonP2))
}

fun fillInverseTagToPhotoMap(s: String, index: Int, photos: MutableList<Photo>, inverseMatrix: MutableMap<String, MutableList<Photo>>) {
    val values = s.split(" ")
    val photo = Photo(index, values[0], values[1].toInt(), values.subList(2, s.split(" ").size))
    photos.add(photo)
    for (tag in photo.tags) {
        if (inverseMatrix.containsKey(tag)) {
            inverseMatrix[tag]!!.add(photo)
        } else {
            inverseMatrix[tag] = mutableListOf(photo)
        }
    }
}

fun removePhotoFromInverseMatrix(slide: Slide, inverseMatrix: MutableMap<String, MutableList<Photo>>) {
    slide.tags.forEach { tag -> inverseMatrix[tag]?.remove(slide.photo1); slide.photo2?.let { inverseMatrix[tag]?.remove(it) } }
}


fun notInProcessedSlide(processedSlides: MutableList<Int>, it: Slide) = !processedSlides.contains(it.photo1.id) && if (it.photo2 != null ) !processedSlides.contains(it.photo2.id) else true

fun getNextNotProcessedPhoto(photos: MutableList<Photo>, processedSlides: List<Int>, slideForVertical: Map<Int, Slide>): Slide? {
    val nextPhoto = photos.first { notInProcessedSlide(processedSlides.toMutableList(), Slide(it)) }
    return if (nextPhoto.type == "V" ) {
        if (slideForVertical[nextPhoto.id] != null) slideForVertical[nextPhoto.id]
        else getNextNotProcessedPhoto(photos, processedSlides.toList() + nextPhoto.id, slideForVertical)
    } else {
        Slide(nextPhoto)
    }
}

fun evaluateVerticalSlides(photos: List<Photo>): Map<Int, Slide> {
    val slides = mutableMapOf<Int, Slide>()
    val verticalPhotos = photos.filter { it.type == "V" }.toMutableList()
    var evaluatedPhotos = 0
    var nextVertical: Photo
    val startSize = verticalPhotos.size
    while (evaluatedPhotos < startSize) {
        nextVertical = verticalPhotos.iterator().next()
        verticalPhotos.remove(nextVertical)
        var maxTags: Int = -1
        var withMaxTags: Photo? = null
        var stop = false
        verticalPhotos.forEach {
            if (!stop) {
                val slide = Slide(nextVertical, it)
                if (slide.tags.size > maxTags) {
                    maxTags = slide.tags.size
                    withMaxTags = it
                }
            }
        }
        verticalPhotos.remove(withMaxTags!!)
        slides[nextVertical.id] = Slide(nextVertical, withMaxTags)
        slides[withMaxTags!!.id] = Slide(nextVertical, withMaxTags)
        evaluatedPhotos += 2
        if (evaluatedPhotos % 1000 == 0) {
            println("Evaluated $evaluatedPhotos verticals on $startSize")
        }
    }
    return slides
}

ExerciseScript(Runnable {
    val lines = File("/Users/mmanzi/workspace/octopus/BeDesign-HC2019/src/main/resources/a_example.txt").readLines()

    val inverseMatrix = mutableMapOf<String, MutableList<Photo>>()
    val photos = mutableListOf<Photo>()
    lines.subList(1, lines.size).forEachIndexed { index, s ->
        fillInverseTagToPhotoMap(s, index, photos, inverseMatrix)
    }

    val slideForVertical = evaluateVerticalSlides(photos)
    val processedSlidesIds = mutableListOf<Int>()
    val processedSlides = mutableListOf<Slide>()
    var processedPhotos = 0
    var slide = getNextNotProcessedPhoto(photos, processedSlidesIds, slideForVertical)!!
    removePhotoFromInverseMatrix(slide, inverseMatrix)
    val startSize = photos.size - slideForVertical.size / 2
    while (processedPhotos < startSize) {
        if (notInProcessedSlide(processedSlidesIds, slide)) {
            var perfectSlideScore = -1
            var perfectSlide: Slide? = null
            slide.tags.forEach { tag ->
                if (inverseMatrix.containsKey(tag)) {
                    inverseMatrix[tag]?.forEach { photoToCheck ->
                        if (photoToCheck.type == "H") {
                            val scoreForPhoto = scoreSimilar(slide, Slide(photoToCheck))
                            if (scoreForPhoto > perfectSlideScore) {
                                perfectSlide = Slide(photoToCheck)
                                perfectSlideScore = scoreForPhoto
                            }
                        } else {
                            val longestSlideForPhotoToCheck = slideForVertical[photoToCheck.id]
                            if (longestSlideForPhotoToCheck != null) {
                                val scoreForPhoto = scoreSimilar(slide, longestSlideForPhotoToCheck)
                                if (scoreForPhoto > perfectSlideScore) {
                                    perfectSlide = longestSlideForPhotoToCheck
                                    perfectSlideScore = scoreForPhoto
                                }
                            }
                        }
                    }
                }
            }
            processedSlides.add(slide)
            processedSlidesIds.add(slide.photo1.id)
            slide.photo2?.let { processedSlidesIds.add(it.id) }
            photos.remove(slide.photo1)
            slide.photo2?.let { photos.remove(it) }
            processedPhotos += 1
            slide.photo2?.let { processedPhotos += 1 }

            if (processedPhotos < startSize) {
                if (perfectSlide == null) {
                    try {
                        perfectSlide = getNextNotProcessedPhoto(photos, processedSlidesIds, slideForVertical)
                    } catch (e: Exception) {
                        println("Ao")
                    }
                }

                removePhotoFromInverseMatrix(perfectSlide!!, inverseMatrix)
                slide = perfectSlide!!

            }
            if (processedPhotos % 1000 == 0) {
                println("Processed $processedPhotos")
            }
        }
    }

    val result = (listOf("" + (processedSlides.size)) + processedSlides.map { it.id }).filter { it.isNotEmpty() }
    println(result)
//    writeToFile(result, "/Users/mmanzi/workspace/octopus/BeDesign-HC2019/src/test/kotlin/com/bedesign/hashcode2019/a_example.txt.result")
})