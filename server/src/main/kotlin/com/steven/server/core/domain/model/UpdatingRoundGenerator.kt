package com.steven.server.core.domain.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * for test only
 */
class UpdatingRoundGenerator(private val currentRoundJson: UpdatingRound?,
                             private val timeWindow: Long,
                             private val minUpdates: Int) : UpdatingRoundStrategy {

    override fun createUpdatingRound(): UpdatingRound =
            if (checkCurrentUpdatingRound(currentRoundJson)) {
                currentRoundJson!!
            } else {
                createNewUpdatingRound()
            }


    private fun createNewUpdatingRound(): UpdatingRound {
        val currentDate = Date()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(currentDate)
        val roundId = "round_$timeStamp"
        return UpdatingRound(roundId,
                currentDate.time,
                currentDate.time + timeWindow,
                minUpdates)
    }

    private fun checkCurrentUpdatingRound(updatingRound: UpdatingRound?): Boolean {
        return updatingRound != null && updatingRound.endDate >= Date().time
    }
}