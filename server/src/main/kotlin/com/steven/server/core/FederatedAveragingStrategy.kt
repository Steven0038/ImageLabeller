package com.steven.server.core

import com.steven.server.core.domain.model.ClientUpdate
import com.steven.server.core.domain.model.UpdatesStrategy
import com.steven.server.core.domain.repository.ServerRepository
import org.apache.commons.io.FileUtils
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.io.ByteArrayOutputStream


class FederatedAveragingStrategy(private val repository: ServerRepository, private val layerIndex: Int) :
    UpdatesStrategy {

    override fun processUpdates(): ByteArrayOutputStream {
        println("[processUpdates]...")

        val outputStream = ByteArrayOutputStream() //以下储存下轮模型, 并准备在之后推送给客户端
        try {
            val totalSamples: Int = repository.getTotalSamples() //取得所有客户端上传模型总数
            val model: MultiLayerNetwork =
                ModelSerializer.restoreMultiLayerNetwork(repository.retrieveModel()) //取得当前伺服器端模型
            val shape = model.getLayer(layerIndex).params().shape() //取得指定层数(当前为3)参数的 ndArray 形状

            val clientUpdates = repository.listClientUpdates()

            // 计算需要更新的 INDArray 参数
            val sumUpdates = clientUpdates.fold( //将当前轮数上传的客户端模型列表作为fold函数输入
                Nd4j.zeros(shape[0], shape[1]), //创建累加用 ndarray 初始值
                { sumUpdates, next -> processSingleUpdate(next, totalSamples, sumUpdates) } //单一客户模型经个别处理后将回传值加入上列初始值
            )

            model.getLayer(layerIndex).setParams(sumUpdates) // 更新模型指定层数之参数为联邦平均过后之INDArray参数

            ModelSerializer.writeModel(model, outputStream, true)
            repository.storeModel(outputStream.toByteArray())
        } catch (e: Exception) {
            println(e)
        }

        return outputStream
    }

    // 累加个别客户端上传模型之参数
    private fun processSingleUpdate(next: ClientUpdate, totalSamples: Int, sumUpdates: INDArray): INDArray {
        val update = Nd4j.fromByteArray(FileUtils.readFileToByteArray(next.file)) //读取单一客户端模型檔案后转换INDArray
        val normaliser = next.samples.toDouble().div(totalSamples.toDouble()) //计算单一客户数上传模型数占所有客户上传模型数之比例权重
        val normalisedUpdate = update.div(normaliser) //由比例权重计算单一客户本轮应该加入平均之模型参数
        println("Processing ${next.file}")
        return sumUpdates.addi(normalisedUpdate)
    }
}