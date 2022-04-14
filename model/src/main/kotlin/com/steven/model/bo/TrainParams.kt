package com.steven.model.bo

import java.util.*

/**
 * dataset training params
 *
 * @param numLabels number of labels, also the number classes and folders
 * @param numEpochs
 * @param batchSize
 * @param saveFile the trained model file name
 * @param trainFileDir the location of folder of train/eval dataset, set to your own download dataset location
 */
sealed class TrainParams(
    var numLabels: Int,
    var numEpochs: Int,
    var batchSize: Int,
    var saveFile: String,
    var trainFileDir: String
) {
    // https://www.kaggle.com/jehanbhathena/weather-dataset
    class HARVARD_WEATHER :
        TrainParams(11, 20, 25, "hv_weather_federated_beta3-${Date().time}.zip", "E:\\dataset\\WeatherImageRecognition")

    // https://www.kaggle.com/pratik2901/multiclass-weather-dataset
    class MULTI_WEATHER :
        TrainParams(4, 5, 10, "weather_federated_beta3-${Date().time}.zip", "E:\\dataset\\MultiClassWeatherDataset")

    // https://github.com/ZebaKhanam91/SP-Weather
    class SP_WEATHER :
        TrainParams(4, 5, 10, "sp_weather_federated_beta3-${Date().time}.zip", "E:\\dataset\\SP-Weather")

    // https://www.cs.toronto.edu/~kriz/cifar.html
    class CUSTOMIZED_CIFAR :
        TrainParams(10, 50, 100, "cifar10_federated_beta3-${Date().time}.zip", "E:\\dataset\\cifar10_dl4j.v1\\train")

    // https://www.cs.toronto.edu/~kriz/cifar.html
    // largeCar: Wagon,SUV,Minivan,Cap,Van; smallCar: Coupe,Sedan,Hatchback
    class CUSTOMIZED_CAR_BODY_TYPE :
        TrainParams(2, 3, 30, "car_body_federated_beta3-${Date().time}.zip", "E:\\dataset\\StanfordCarBodyTypeData\\stanford_cars_type")

    // https://www.kaggle.com/brsdincer/vehicle-detection-image-set
    class VEHICLE_DETECTION :
        TrainParams(2, 3, 30, "vehicle_detection_beta3-${Date().time}.zip", "E:\\dataset\\VehicleDetectionImageSet")

    // https://www.kaggle.com/datasets/asdasdasasdas/garbage-classification
    class GARBAGE :
        TrainParams(6, 15, 20, "garbage_beta3-${Date().time}.zip", "E:\\dataset\\GarbageClassification")
}