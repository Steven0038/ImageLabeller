package com.steven.server.controller

import com.steven.server.core.domain.model.FederatedServer
import com.steven.server.core.domain.model.Logger
import com.steven.server.core.domain.model.RoundController
import com.steven.server.core.domain.model.UpdatesStrategy
import com.steven.server.core.domain.repository.ServerRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class FederatedServerImpl : FederatedServer {

    lateinit var repository: ServerRepository
    lateinit var updateStrategy: UpdatesStrategy
    lateinit var roundController: RoundController
    lateinit var properties: Properties
    lateinit var logger: Logger

    companion object {
        var instance = FederatedServerImpl()
    }

    override fun initialise(
        repository: ServerRepository,
        updatesStrategy: UpdatesStrategy,
        roundController: RoundController,
        logger: Logger,
        properties: Properties
    ) {
        instance.let {
            it.repository = repository
            it.updateStrategy = updatesStrategy
            it.roundController = roundController
            it.logger = logger
            it.properties = properties
        }
    }

    // TODO This logic to UseCase when created
    override fun pushUpdate(clientUpdate: ByteArray, samples: Int) {
        logger.log("[pushUpdate] Storing update in server: $samples")

        repository.storeClientUpdate(clientUpdate, samples)
        roundController.onNewClientUpdate()
        when (roundController.checkCurrentRoundAndIsMinUpdatesLargerThanClientUpdates()) {
            true -> Unit.also { logger.log("[pushUpdate] still not satisfied min update") }
            false -> processUpdates().also { logger.log("[pushUpdate] processUpdates done") }
        }
    }

    // TODO This logic to UseCase when created
    private fun processUpdates() {
        logger.log("[processUpdates]...")

        roundController.freezeRound()
        val newModel = updateStrategy.processUpdates()
        newModel.flush()
        repository.storeModel(newModel.toByteArray())
        newModel.close()
        roundController.endRound()
    }

    override fun getUpdatingRound() = roundController.getCurrentRound()

    override fun getModelFile() = repository.retrieveModel()

    override fun getUpdatingRoundAsJson() = roundController.currentRoundToJson()
}