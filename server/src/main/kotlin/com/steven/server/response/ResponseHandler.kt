package com.steven.server.response

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


class ResponseHandler {
    fun generateResponse(message: String, status: HttpStatus, responseObj: Any?): ResponseEntity<Any?> {
        val map: MutableMap<String, Any> = HashMap()
        map["message"] = message
        map["status"] = status.value()
        map["data"] = responseObj ?: ""
        return ResponseEntity(map, status)
    }
}