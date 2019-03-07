package com.bedesign.framework

import com.bedesign.writeToFile
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
    fun startProgramming(packageStr: String, scriptPath: String) {
        val srcPath = System.getProperty("user.dir")
        val scriptFullPath = "$srcPath/src/main/kotlin/${javaClass.name.replace(".", "/").replace("framework/${javaClass.simpleName}", "")}$scriptPath"
        readScriptWithSolution(packageStr, scriptFullPath)
    }

    companion object {
        private fun exerciseSolution(packageStr: String, lastReadedLines: List<String>) =
                listOf("package $packageStr") +
                lastReadedLines.map { if (it.startsWith("ExerciseScript(Runnable")) "val solution = $it" else it } + listOf(
                """fun main() { solution.run() }"""
        )

        private fun runScript(scriptFile: File) {
            val reader = scriptFile.bufferedReader()
            val loadedObj: ExerciseScript = loader.load(reader)
            loadedObj.run()
            reader.close()
        }

        private fun fileChanged(lastReadedLines: List<String>, previouslyReadedLines: List<String>) = lastReadedLines.size != previouslyReadedLines.size || lastReadedLines.any { !previouslyReadedLines.contains(it) }

        private fun readScriptWithSolution(packageStr: String, scriptPath: String) {
            var previouslyReadedLines = listOf<String>()
            while (true) {
                try {
                    val scriptFile = File(scriptPath)
                    if (scriptFile.exists()) {
                        val lastReadedLines = scriptFile.readLines()
                        if (fileChanged(lastReadedLines, previouslyReadedLines)) {
                            previouslyReadedLines = lastReadedLines
                            writeToFile(exerciseSolution(packageStr, lastReadedLines), scriptPath.replace(".kts", "Impl.kt"))
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
    }
}