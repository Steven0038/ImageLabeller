package com.steven.server.core.domain.model

import com.steven.server.core.domain.repository.ServerRepository
import java.io.File
import java.util.*

interface FederatedServer {
    fun initialise(repository: ServerRepository,
                   updatesStrategy: UpdatesStrategy,
                   roundController: RoundController,
                   logger: Logger,
                   properties: Properties
    )

    fun pushUpdate(clientUpdate: ByteArray, samples: Int)

    fun getUpdatingRound(): UpdatingRound

    fun getModelFile(): File

    fun getUpdatingRoundAsJson(): String
}