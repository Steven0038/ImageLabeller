package com.steven.server.core.domain.model

interface RoundController {
    fun startRound(): UpdatingRound
    fun freezeRound()
    fun endRound(): Boolean
    fun checkCurrentRoundAndIsMinUpdatesLargerThanClientUpdates(): Boolean
    fun onNewClientUpdate()
    fun getCurrentRound(): UpdatingRound
    fun currentRoundToJson(): String
}