package com.steven.model

import com.steven.model.bo.SharedConfig
import com.steven.model.trainer.ImageTrainer
import org.deeplearning4j.util.ModelSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


fun main(args: Array<String>) {
    val seed = 123
    val log: Logger = LoggerFactory.getLogger("main")
    // Harvard Weather Image Recognition
//        val numLabels = 11
//        val numEpochs = 20
//        val batchSize = 25
//        val saveFile = "hv_weather_federated_beta3-${Date().time}.zip"
//        val trainFileDir = "E:\\dataset\\WeatherImageRecognition" // https://www.kaggle.com/jehanbhathena/weather-dataset

    // multi weather
//    val numLabels = 4
//    val numEpochs = 5
//    val batchSize = 10
//    val saveFile = "weather_federated_beta3-${Date().time}.zip"
//    val trainFileDir = "E:\\dataset\\MultiClassWeatherDataset" // https://www.kaggle.com/pratik2901/multiclass-weather-dataset

    // sp-weather
//        val numLabels = 5
//        val numEpochs = 5
//        val batchSize = 20
//        val saveFile = "sp_weather_federated_beta3-${Date().time}.zip"
//        val trainFileDir = "E:\\dataset\\SP-Weather" // https://github.com/ZebaKhanam91/SP-Weather

    // customize cifar
//        val numLabels = 10
//        val numEpochs = 50
//        val batchSize = 100
//        val saveFile = "cifar10_federated_beta3-${Date().time}.zip"
//        val trainFileDir = "E:\\dataset\\cifar10_dl4j.v1\\train" // https://www.cs.toronto.edu/~kriz/cifar.html

    // customized car body type
//        val numLabels = 2 // largeCar: Wagon,SUV,Minivan,Cap,Van; smallCar: Coupe,Sedan,Hatchback
////        val numEpochs = 4 // 0.6322
//        val numEpochs = 3 // 0.6331
//        val batchSize = 30
//        val saveFile = "car_body_federated_beta3-${Date().time}.zip"
//        val trainFileDir = "E:\\dataset\\StanfordCarBodyTypeData\\stanford_cars_type" // https://www.kaggle.com/mayurmahurkar/stanford-car-body-type-data

    // Vehicle Detection Image Set
//        val numLabels = 2 // vehicle, non-vehicle
//        val numEpochs = 3
//        val batchSize = 30
//        val saveFile = "vehicle_detection_beta3-${Date().time}.zip"
//        val trainFileDir = "E:\\dataset\\VehicleDetectionImageSet" // https://www.kaggle.com/brsdincer/vehicle-detection-image-set

    // Garbage Classification
    val numLabels = 6
    val numEpochs = 15
    val batchSize = 20
    val saveFile = "garbage_beta3-${Date().time}.zip"
    val trainFileDir = "E:\\dataset\\GarbageClassification" // https://www.kaggle.com/brsdincer/vehicle-detection-image-set


    val config = SharedConfig(32, 3, 100)
    val trainer = ImageTrainer(config)

    if (args.isNotEmpty() && args[0] == "train") {
        var model = trainer.createModel(seed, numLabels)
        model = trainer.train(model, numEpochs, batchSize, trainFileDir)

        if (args[1].isNotEmpty()) {
            log.info("Saving model to ${args[1]}")
            trainer.saveModel(model, args[1] + "/$saveFile")
        }

    } else {
        // do trained model evaluation
        val evaluator = ImageTrainer(config)
//        val model = ModelSerializer.restoreMultiLayerNetwork(File("E:\\workspace\\phModelNew\\experiment_results\\weather_federated_beta3-1645674600781.zip"))
//        val model = ModelSerializer.restoreMultiLayerNetwork(File("E:\\workspace\\phModelNew\\experiment_results\\cifar10_federated_beta3-1645755772576-0.5110-DONE.zip"))
//        val model = ModelSerializer.restoreMultiLayerNetwork(File("E:\\workspace\\phModelNew\\experiment_results\\hv_weather_federated_beta3-1645769724702-0.6118-DONE.zip"))
//        val model = ModelSerializer.restoreMultiLayerNetwork(File("E:\\workspace\\phModelNew\\experiment_results\\sp_weather_federated_beta3-1645754818939-0.6664-DONE.zip"))
        val model = ModelSerializer.restoreMultiLayerNetwork(File("E:\\workspace\\phModelNew\\experiment_results\\garbage_beta3-1646711956078.zip")) // 0.5244
        evaluator.eval(model, batchSize, trainFileDir)
    }
}