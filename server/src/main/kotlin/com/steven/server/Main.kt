package com.steven.server

import com.steven.server.core.datasource.FileDataSourceImpl
import com.steven.server.core.datasource.MemoryDataSourceImpl
import com.steven.server.core.datasource.ServerRepositoryImpl
import com.steven.server.core.FederatedAveragingStrategy
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*

/**
 * force executing one round federated average to produce federated model, even when the min_updates not satisfied
 */
fun main(args: Array<String>) {
    val properties = Properties()
    properties.load(FileInputStream("./server/src/main/resources/application.properties"))

    val rootPath = Paths.get(properties.getProperty("model_dir"))
    val fileDataSource = FileDataSourceImpl(rootPath)
    val memoryDataSource = MemoryDataSourceImpl()
    val repository = ServerRepositoryImpl(fileDataSource, memoryDataSource)
    repository.restoreClientUpdates()
    val updatesStrategy = FederatedAveragingStrategy(repository, 3)

    updatesStrategy.processUpdates()
}