# ImageLabeller
federated server of mobile image classification model improvement

## What is it?
This is a POC federated server project, could train an CNN image classification model and involved to 
federated learning cycle interacting with Android mobile clientside.

I reconstruct it to springboot maven Multi Module Project and adds some features to make it more like production usage.

Including: 
- an improved CNN model trainer construct with dl4j alongside with multiple test datasets with prebuild hyper-params, 
- flexible image data preprocessing module, allow you to train with your own dataset
- a high concurrency rest api build with rate limiter and cache mechanism

You could also check out my contribution of the workable PhotoLabeller project use dl4j official cifar10 dataset from the following branches.
- [serverside](https://github.com/Steven0038/PhotoLabeller)
- [clientside](https://github.com/Steven0038/PhotoLabellerServer)

## Tech stack
- spring boot with java and kotlin
- Deeplearning4j (model training)
- GeoLite (upload region restrict)
- Guava with rate limiter
- redis (model upload and download cache)
- mongo DB (TODO: record model param update records, and federated cycle control)

## Environment preparation
- JDK up to 8
- redis at port 6379
- mongo at port 27017
- IDEA or eclipse
- android studio with physical android phones
- server and phone devices should under the same WI-FI environment

## Work Flow
- train an initial model with image dataset
- deploy initial model to android platform client side
- setup and start federated sever
- do clientside image capture and label, and run client side on device training
- client side automatically upload trained model param to server
- while upload model params reach the default server setting, server will do federated average and get a new federated model
- manually or automatically(TODO) deploy the improved model to clientside

## Architecture
the classical   

## Installation
git clone this project and android client side from this [branch](https://github.com/Steven0038/PhotoLabeller)

## Train initial model
1. adjust the dataset directory path
`````
model/src/main/kotlin/com/steven/model/bo/TrainParams.kt
`````

2. use 
````
model/src/main/kotlin/com/steven/model/Main.kt
````
to train the initial model with IDE run param argument, arg[0] is train, arg[1] is the destination path dir to save model,

ex:
````
train E:/workspace/phModelNew
````

3. after get the trained model, rename the file to model.zip, and put to the initial model path define under
```server/src/main/resources/application.properties```
````
model_dir = E:/workspace/phModelNew
````

4. make sure the model_dir exist, and start the local server at
````
server/src/main/kotlin/com/steven/server/ServerApplication.kt
````

5. visits the following APIs to make sure all necessary documents initialized
```
http://localhost:8080/service/federatedservice/available 
```
```
http://localhost:8080/service/federatedservice/currentRound
```

## Connect with android client side to test

### set up connection properties
```
dev\PhotoLabeller\app\gradle.properties parameter_server_url
```
IPV6 to your own machine's

### set up the classification model and params

1. deploy the same initial model file to Photolabeller android project under
```
PhotoLabeller\app\src\main\assets
```

2. four different places should be modified to math model's name also it's labels 
```
photolabeller.di.MainAppModule private final val modelFileName
```
````
photolabeller.trainer.ClientImageLoader public final fun createDataSet() val label
````
````
photolabeller.labeller.MainFragment.Companion public final val label
````
```
photolabeller.config.SharedConfig public final val labels
```

### Android on device model raining and participate federated server model average
1. run app [branch](https://github.com/Steven0038/PhotoLabeller)
2. take some pictures and manually label the prediction to actual classification
3. run on device training, and will automatically update model params to serverside
4. if default min_updates threshold is reached, serverside will run federated average algo. and produce new federated model

### References
This project inspired from [PhotoLabellerServer](https://github.com/mccorby/PhotoLabellerServer),
the core package usage with MIT license.

You could also check out my contribution of the workable PhotoLabeller project use dl4j official cifar10 dataset from the following branches.
- [serverside](https://github.com/Steven0038/PhotoLabeller)
- [clientside](https://github.com/Steven0038/PhotoLabellerServer)