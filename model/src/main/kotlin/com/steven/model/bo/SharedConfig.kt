package com.steven.model.bo

data class SharedConfig(
    val imageSize: Int,
    val channels: Int,
    val batchSize: Int,
    val featureLayerIndex: Int = 3
)