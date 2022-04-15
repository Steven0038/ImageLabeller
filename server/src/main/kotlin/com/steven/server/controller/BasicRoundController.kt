package com.steven.server.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.steven.server.core.domain.model.RoundController
import com.steven.server.core.domain.model.UpdatingRound
import com.steven.server.core.domain.repository.ServerRepository
import java.text.SimpleDateFormat
import java.util.*


// TODO Refactor this class. It is doing too many things
class BasicRoundController(private val repository: ServerRepository,
                           initialCurrentRound: UpdatingRound?,
                           private val timeWindow: Long,
                           private val minUpdates: Int) : RoundController {

    private var currentRound = initialCurrentRound
    private var numberOfClientUpdates: Int = 0

    override fun startRound(): UpdatingRound {
        currentRound = if (checkCurrentUpdatingRound(currentRound)) {
            currentRound!!
        } else {
            createNewUpdatingRound()
        }
        repository.storeCurrentUpdatingRound(currentRound!!)
        return currentRound!!
    }

    override fun freezeRound() {

    }

    override fun endRound(): Boolean {
        numberOfClientUpdates = 0
        currentRound = null
        repository.clearClientUpdates()
        return true
    }

    override fun checkCurrentRoundAndIsMinUpdatesLargerThanClientUpdates() = currentRound?.let { it.minUpdates > numberOfClientUpdates } ?: false

    override fun onNewClientUpdate() {
        numberOfClientUpdates++
    }

    override fun getCurrentRound(): UpdatingRound = currentRound!!

    override fun currentRoundToJson(): String = jacksonObjectMapper().writeValueAsString(currentRound!!)

    private fun createNewUpdatingRound(): UpdatingRound {
        val currentDate = Date()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(currentDate)
        val roundId = "round_$timeStamp"
        return UpdatingRound(roundId,
                currentDate.time,
                currentDate.time + timeWindow,
                minUpdates)
    }

    private fun checkCurrentUpdatingRound(updatingRound: UpdatingRound?) =
            updatingRound != null && updatingRound.endDate >= Date().time
}