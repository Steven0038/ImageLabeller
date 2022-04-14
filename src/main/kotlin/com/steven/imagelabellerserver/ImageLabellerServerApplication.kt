package com.steven.imagelabellerserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ImageLabellerServerApplication

fun main(args: Array<String>) {
    runApplication<ImageLabellerServerApplication>(*args)
}
