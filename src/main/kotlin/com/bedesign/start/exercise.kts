import com.bedesign.framework.ExerciseScript
import java.io.File

val run = true
ExerciseScript(Runnable {
    val lines = File("/Users/mmanzi/workspace/octopus/BeDesign-HC2019/src/main/resources/2020Trial/a_example.in").readLines()

    println(lines[1].split(" ").map { it.toInt() }.sum())
})
