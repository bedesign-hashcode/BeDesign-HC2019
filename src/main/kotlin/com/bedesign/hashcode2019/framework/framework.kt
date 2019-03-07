package com.bedesign.hashcode2019.framework

import com.bedesign.hashcode2019.writeToFile
import de.swirtz.ktsobjectloader.KtsObjectLoader
import java.io.File
import java.io.FileNotFoundException

val loader = KtsObjectLoader()

data class ExerciseScript(val runnable: Runnable) {
    fun run() {
        runnable.run()
    }
}

class Exercise {
    fun startProgramming(scriptPath: String) {
        val srcPath = System.getProperty("user.dir")
        val scriptFullPath = "$srcPath/src/main/kotlin/${javaClass.name.replace(".", "/").replace("framework/${javaClass.simpleName}", "")}$scriptPath"
        readScriptWithSolution(scriptFullPath)
    }

    companion object {
        private fun readScriptWithSolution(scriptPath: String) {
            var previouslyReadedLines = listOf<String>()
            while (true) {
                try {
                    val scriptFile = File(scriptPath)
                    if (scriptFile.exists()) {
                        val lastReadedLines = scriptFile.readLines()
                        if (fileChanged(lastReadedLines, previouslyReadedLines)) {
                            previouslyReadedLines = lastReadedLines
                            writeToFile(exerciseSolution(lastReadedLines), scriptPath.replace(".kts", "Impl.kt"))
                            if (lastReadedLines.any { it == "val run = true" }) runScript(scriptFile)
                        }
                    }
                } catch (e: Exception) {
                    if (e !is FileNotFoundException) e.printStackTrace()
                } finally {
                    Thread.sleep(1000)
                }
            }
        }

        private fun exerciseSolution(lastReadedLines: List<String>) = lastReadedLines.map { if (it.startsWith("ExerciseScript(Runnable")) "val solution = $it" else it } + listOf(
                """fun main() { solution.run() }"""
        )

        private fun runScript(scriptFile: File) {
            val reader = scriptFile.bufferedReader()
            val loadedObj: ExerciseScript = loader.load(reader)
            loadedObj.run()
            reader.close()
        }

        private fun fileChanged(lastReadedLines: List<String>, previouslyReadedLines: List<String>) = lastReadedLines.any { !previouslyReadedLines.contains(it) }
    }
}