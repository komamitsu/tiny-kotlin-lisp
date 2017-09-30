package org.komamitsu.tinylisp

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File

class Args(parser: ArgParser) {
    val inputFile by parser.storing(
            "-i", "--input-file",
            help = "input source code file").default(null)

    val verbose by parser.flagging(
            "-v", "--verbose",
            help = "verbose mode")
}

fun main(args : Array<String>) {
    val interpreter = Interpreter()
    val arguments = Args(ArgParser(args));
    if (arguments.inputFile != null) {
        val input = File(arguments.inputFile).inputStream()
        interpreter.process(input, { })
    }
    else {
        while (true) {
            print("> ")
            try {
                val input = readLine().orEmpty().byteInputStream()
                interpreter.process(input,
                        { node ->
                            if (arguments.verbose) {
                                println(node)
                            }
                        })
            }
            catch (e: IllegalStateException) {
                e.printStackTrace(System.out)
            }
            println()
        }
    }
}