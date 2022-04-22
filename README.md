# ImageLabeller
federated server of mobile image classification model improvement

## What is it?
This is a POC federated server project inspired from [PhotoLabellerServer](https://github.com/mccorby/PhotoLabellerServer),
could train an CNN image classification model and involved to federated learning cycle interacting with clientside.

I reconstruct it to springboot maven Multi Module Project and adds some features to make it more like production usage.

Including: 
- an improved CNN model trainer construct with dl4j alongside with multiple test datasets with prebuild hyper-params, 
- flexible image data preprocessing module, allow you to train with your own dataset
- a high concurrency rest api build with rate limiter and cache mechanism

## Tech stack
- spring boot with java and kotlin
- Deeplearning4j
- GeoLite (upload region restrict)
- Guava with rate limiter
- redis (model upload and download cache)
- mongo DB (TODO: record model param update records, and federated cycle control)

## Work Flow
- train an initial model with image dataset
- deploy initial model to android client side or other platform
- setup and start federated sever 
- do clientside image capture and label, and run client side on device training
- client side upload trained model param to server
- while upload model params reach the default server setting, server will do federated average and get a new federated model
- manually or automatically(TODO) deploy the improved model to clientside

## Environment preparation

java up to 8

docker could be used to run quickly installation
#### redis
````
docker pull redis:latest

docker run -itd --name redis-test -p 6379:6379 redis
````
#### mongo (TODO)
````
docker pull mongo:latest

docker run -itd --name mongo -p 27017:27017 mongo --auth

docker exec -it mongo mongo admin

db.createUser({ user:'admin',pwd:'123456',roles:[ { role:'userAdminAnyDatabase', db: 'admin'},"readWriteAnyDatabase"]});

db.auth('admin', '123456')
````

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
to train the initial model with IDE run param argument, arg[0] is train, arg[1] is the model save destination path,

ex:
````
train E:/workspace/phModelNew web
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

5. visits the following APIs the make sure all necessary document initialized
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

### android on device model raining and participate federated server model average
1. run app
2. take some pictures and manually label the prediction to actual classification
3. run on device training, and will automatically update model params to serverside
4. if default min_updates threshold is reached, serverside will run federated average algo. and produce new federated model
