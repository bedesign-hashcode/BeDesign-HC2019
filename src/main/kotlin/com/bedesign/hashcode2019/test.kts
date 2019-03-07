import com.bedesign.hashcode2019.FastScript
import java.io.File
import kotlin.math.min

data class Photo(val id: Int, val type: String, val numTags: Int, val tags: List<String>)
data class Slide(val photo1: Photo, val photo2: Photo? = null) {
    val id = listOf(photo1.id, photo2?.id ?: -1).filter { it >= 0 }.joinToString(" ") { "$it" }
    val type = if (photo1.type == "H") "H" else "V"
    val tags: List<String> = (photo1.tags + (photo2?.tags ?: listOf())).toSet().toList()
}
val run = true
val test = ""
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

fun notInProcessedSlidePhoto(processedSlides: MutableList<Slide>, it: Photo) = processedSlides.map { p -> p.id }.none { pId -> pId.contains(it.id.toString()) }

fun notInProcessedSlide(processedSlides: MutableList<Slide>, it: Slide) = processedSlides.map { p -> p.id }.none { pId -> pId.contains(it.id.toString()) }


fun slideOfTwoVerticalWithMaxNumberOfTags(vertical: Photo, photos: MutableList<Photo>, processedSlides: MutableList<Slide>): Slide {
    var maxTags: Int = -1
    var withMaxTags: Photo? = null
    photos.filter { it.type == "V" }.filter { notInProcessedSlidePhoto(processedSlides, it) }.forEach {
        val slide = Slide(vertical, it)
        if (slide.tags.size > maxTags) {
            maxTags = slide.tags.size
            withMaxTags = it
        }
    }
    return Slide(vertical, withMaxTags)
}

fun getNextNotProcessedPhoto(photos: MutableList<Photo>, processedSlides: MutableList<Slide>): Slide? {
    val nextPhoto = photos.first { notInProcessedSlidePhoto(processedSlides, it) }
    return if (nextPhoto.type == "V") {
        val nextTwoVerticalSlides = slideOfTwoVerticalWithMaxNumberOfTags(nextPhoto, photos, processedSlides)
        if (nextTwoVerticalSlides.photo2 == null) getNextNotProcessedPhoto(photos, processedSlides.apply { add(Slide(nextPhoto)) })
        else nextTwoVerticalSlides
    } else {
        Slide(nextPhoto)
    }
}

fun evaluatePerfectSlideScoreFor(slide: Slide, firstVertical: Photo, inverseMatrix: MutableMap<String, MutableList<Photo>>): Pair<Int, Slide> {
    var perfectSlideScore = -1
    var perfectSlide: Slide? = null
    slide.tags.forEach { tag ->
        if (inverseMatrix.containsKey(tag)) {
            inverseMatrix[tag]?.filter { it.type == "V" }?.forEach { photoToCheck ->
                val scoreForPhoto = scoreSimilar(slide, Slide(firstVertical, photoToCheck))
                if (scoreForPhoto > perfectSlideScore) {
                    perfectSlide = Slide(photoToCheck)
                    perfectSlideScore = scoreForPhoto
                }
            }
        }
    }
    return Pair(perfectSlideScore, perfectSlide!!)
}

FastScript(Runnable {
    val lines = File("/Users/mmanzi/workspace/octopus/BeDesign-HC2019/src/main/resources/a_example.txt").readLines()
    val size = lines[0]

    val inverseMatrix = mutableMapOf<String, MutableList<Photo>>()
    val photos = mutableListOf<Photo>()
    lines.subList(1, lines.size).forEachIndexed { index, s ->
        fillInverseTagToPhotoMap(s, index, photos, inverseMatrix)
    }

    val processedSlides = mutableListOf<Slide>()
    var slide = getNextNotProcessedPhoto(photos, processedSlides)!!
    removePhotoFromInverseMatrix(slide, inverseMatrix)
    while (processedSlides.size < photos.size) {
        if (notInProcessedSlide(processedSlides, slide)) {
            println("Processing photo ${slide.id} with tags ${slide.tags}")
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
                            val perfectSlideAndScore: Pair<Int, Slide> = evaluatePerfectSlideScoreFor(slide, photoToCheck, inverseMatrix)
                            perfectSlideScore = perfectSlideAndScore.first
                            perfectSlide = perfectSlideAndScore.second
                        }
                    }
                }
            }
            if (perfectSlide == null) {
                perfectSlide = getNextNotProcessedPhoto(photos, processedSlides)
            }

            if (perfectSlide != null) {
                removePhotoFromInverseMatrix(perfectSlide!!, inverseMatrix)
                processedSlides.add(slide)
                slide = perfectSlide!!
            }
        }
    }
    println(processedSlides.map { "${it.id} - ${it.type} - ${it.tags}" })
})
