package com.steven.server.core.domain.repository

import com.steven.server.core.domain.model.ClientUpdate
import com.steven.server.core.domain.model.UpdatingRound
import java.io.File

interface ServerRepository {
    fun storeClientUpdate(updateByteArray: ByteArray, samples: Int)
    fun listClientUpdates(): List<ClientUpdate>
    fun getTotalSamples(): Int
    fun clearClientUpdates(): Boolean
    fun storeCurrentUpdatingRound(updatingRound: UpdatingRound)
    fun retrieveCurrentUpdatingRound(): UpdatingRound
    fun retrieveModel(): File
    fun restoreClientUpdates()
    fun storeModel(newModel: ByteArray): File
}