package com.steven.server.core.datasource

import com.steven.server.core.domain.model.ClientUpdate

interface MemoryDataSource {
    fun addUpdate(clientUpdate: ClientUpdate)
    fun getUpdates(): List<ClientUpdate>
    fun clear()
}