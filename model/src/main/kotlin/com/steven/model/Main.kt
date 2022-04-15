package com.steven.model

import com.steven.model.bo.SharedConfig
import com.steven.model.bo.TrainParams
import com.steven.model.trainer.ImageTrainer
import org.deeplearning4j.util.ModelSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File


/**
 * train the initial federated model
 * @param args the user input run configuration, could set by IDE [0]: do train or evaluation, [1] trained model save destination folder
 * ex: train E:/workspace/phModelNew
 */
fun main(args: Array<String>) {
    val seed = 123
    val log: Logger = LoggerFactory.getLogger("main")

    // TODO set dataset params to your want to train or add your own dataset
//    val trainParams = TrainParams.HARVARD_WEATHER()
//    val trainParams = TrainParams.MULTI_WEATHER()
//    val trainParams = TrainParams.SP_WEATHER()
//    val trainParams = TrainParams.CUSTOMIZED_CIFAR()
//    val trainParams = TrainParams.CUSTOMIZED_CAR_BODY_TYPE()
//    val trainParams = TrainParams.VEHICLE_DETECTION()
    val trainParams = TrainParams.GARBAGE()

    val config = SharedConfig(32, 3, 100)
    val trainer = ImageTrainer(config)

    if (args.isNotEmpty() && args[0] == "train") {
        var model = trainer.createModel(seed, trainParams.numLabels)
        model = trainer.train(model, trainParams)

        if (args[1].isNotEmpty()) {
            log.info("Saving model to ${args[1]}")
            trainer.saveModel(model, args[1] + "/${trainParams.saveFile}")
        }

    } else {
        // do trained model evaluation
        val evaluator = ImageTrainer(config)
//        val evalModelFileLocation = "E:\\workspace\\phModelNew\\experiment_results\\weather_federated_beta3-1645674600781.zip""
//        val evalModelFileLocation = "E:\\workspace\\phModelNew\\experiment_results\\cifar10_federated_beta3-1645755772576-0.5110-DONE.zip"
//        val evalModelFileLocation = "E:\\workspace\\phModelNew\\experiment_results\\hv_weather_federated_beta3-1645769724702-0.6118-DONE.zip"
//        val evalModelFileLocation = "E:\\workspace\\phModelNew\\experiment_results\\sp_weather_federated_beta3-1645754818939-0.6664-DONE.zip"
        val evalModelFileLocation = "E:\\workspace\\phModelNew\\experiment_results\\garbage_beta3-1646711956078.zip"
        val model =
            ModelSerializer.restoreMultiLayerNetwork(File(evalModelFileLocation))
        evaluator.eval(model, trainParams.batchSize, trainParams.trainFileDir)
    }
}