package com.steven.server.util

import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*


class Extensions {
    @Deprecated("not used anymore")
    fun hash(file: File): String {
        val bytes = file.toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun getTodayStr(): String {
        return SimpleDateFormat("dd-MM-yyyy").format(Date())
    }
}