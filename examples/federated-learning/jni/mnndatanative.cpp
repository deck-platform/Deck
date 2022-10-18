//  Created by Jimmy Yuan on 2021/05/19.
//  @Copyright Copyright © 2021, BUPT Holding Limited
//  @Content CNN model training

#include <android/bitmap.h>
#include<android/log.h>
#include <jni.h>
#include <string.h>
#include <iostream>
#include <MNNTrain/DataLoader.hpp>
#include <MNNTrain/MnistDataset.hpp>
#include <MNNTrain/Dataset.hpp>
#include <MNN/expr/Executor.hpp>
#include <MNN/MNNForwardType.h>
#include <MNNTrain/SGD.hpp>
#include <MNNTrain/Lenet.hpp>
#include <MNNTrain/Module.hpp>
#include <MNN/AutoTime.hpp>
#include <MNNTrain/Loss.hpp>
#include <MNNTrain/LearningRateScheduler.hpp>
#include <MNNTrain/PipelineModule.hpp>
#include <MNNTrain/ImageDataset.hpp>
#include <MNN/expr/Expr.hpp>
#include <MNNTrain/Transformer.hpp>
#include <memory>
#include <time.h>
#define  LOG_TAG    "test===="
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// Global variable to get the current epoch value and current loss in this epoch
static jint curEpoch = 0;
static jfloat curLoss = 0.0;


extern "C"
JNIEXPORT jstring JNICALL
Java_com_osfans_trime_fl_MNNDataNative_getEpochAndLoss(JNIEnv *env, jclass clazz) {
    std::string result = std::to_string(curEpoch) + "," + std::to_string(curLoss);
    return env->NewStringUTF(result.data());
}


// @Return: trainSamples, testSamples, LOSS, ACC
extern "C"
JNIEXPORT jstring JNICALL
Java_com_osfans_trime_fl_MNNDataNative_nativeCreateDatasetFromFile(JNIEnv *env, jclass clazz,
        jstring modelCachePath, jstring dataCachePath) {
    LOGE("======TEST======");
    const char *modelPath = env->GetStringUTFChars(modelCachePath, 0);
    const char *dataPath = env->GetStringUTFChars(dataCachePath, 0);

    float accuracy = 0.0;
    float LOSS = 0.0;
    int trainSamples = 0;
    int testSamples = 0;

    auto exe = Executor::getGlobalExecutor();
    MNN::BackendConfig config;
    exe->setGlobalExecutorConfig(MNN_FORWARD_CPU, config, 4);
    std::shared_ptr<MNN::Train::SGD> sgd(new MNN::Train::SGD);

    // init lenet
    std::shared_ptr<MNN::Train::Module> model(new MNN::Train::Model::Lenet);

    // model->parameters() to get model parameter
    auto para_0 = model->parameters();
    {
        // Load model snapshot
        auto para = Variable::load(modelPath);
        int times = para.size()/8;
        for(int i = 0; i < para_0.size(); i++)
            para_0[i] = para[i * times];
        model->loadParameters(para_0);
    }

    sgd->append(model->parameters());
//    sgd->setMomentum(0.9f);
//    sgd->setMomentum2(0.99f);
//    sgd->setWeightDecay(0.0005f);

    // train data setting
    auto datasetPtr      = MNN::Train::MnistDataset::create(dataPath, MNN::Train::MnistDataset::Mode::TRAIN);
    const size_t batchSize = 64;
    bool shuffle = true;
    const size_t numWorkers = 0;
    auto dataLoader = std::shared_ptr<MNN::Train::DataLoader>(datasetPtr.createLoader(batchSize, true, shuffle, numWorkers));
    size_t total_size = dataLoader->size();
    size_t iterations = dataLoader->iterNumber();
    iterations = iterations / 150;
    trainSamples = iterations * batchSize;

    // test data setting
    auto testDataset            = MNN::Train::MnistDataset::create("/data/local/tmp/mnist_data", MNN::Train::MnistDataset::Mode::TEST);
    const size_t testBatchSize  = 20;
    const size_t testNumWorkers = 0;
    shuffle = false;
    auto testDataLoader = std::shared_ptr<MNN::Train::DataLoader>(testDataset.createLoader(testBatchSize, true, shuffle, testNumWorkers));
    size_t testIterations = testDataLoader->iterNumber();
    testIterations = testIterations / 100;
    testSamples = testIterations * testBatchSize;

    // start training
    for (int epoch = 0; epoch < 5; ++epoch){
        curEpoch = (jint)epoch;
        model->clearCache();
        exe->gc(Executor::FULL);
        exe->resetProfile();
        {
            dataLoader->reset();
            model->setIsTraining(true);
            int lastIndex = 0;
            int moveBatchSize = 0;
            for (int i = 0; i < iterations; i++) {
                auto trainData  = dataLoader->next();
                auto example    = trainData[0];
                auto cast       = _Cast<float>(example.first[0]);
                example.first[0] = cast * _Const(1.0f / 255.0f);
                moveBatchSize += example.first[0]->getInfo()->dim[0];
                auto newTarget = _OneHot(_Cast<int32_t>(example.second[0]), _Scalar<int>(10), _Scalar<float>(1.0f),
                                         _Scalar<float>(0.0f));
                auto predict = model->forward(example.first[0]);
                auto loss    = MNN::Train::_CrossEntropy(predict, newTarget);
                auto lossvalue = loss->readMap<float>();
                LOSS = *lossvalue;
                curLoss = LOSS;
                float rate   = MNN::Train::LrScheduler::inv(0.01, epoch * iterations + i, 0.0001, 0.75);
                sgd->setLearningRate(rate);
                sgd->step(loss);
            }
        }

        Variable::save(model->parameters(), modelPath);

        LOGE("Train finished, start testing");

        int correct = 0;
        testDataLoader->reset();
        model->setIsTraining(false);
        int moveBatchSize = 0;
        // start testing
        for (int i = 0; i < testIterations; i++) {
            auto data       = testDataLoader->next();
            auto example    = data[0];
            moveBatchSize += example.first[0]->getInfo()->dim[0];
            auto cast       = _Cast<float>(example.first[0]);
            example.first[0] = cast * _Const(1.0f / 255.0f);
            auto predict    = model->forward(example.first[0]);
            predict         = _ArgMax(predict, 1);
            auto accu       = _Cast<int32_t>(_Equal(predict, _Cast<int32_t>(example.second[0]))).sum({});
            correct += accu->readMap<int32_t>()[0];
        }
        auto accu = (float)correct / (float)testSamples;
        accuracy = accu;
        exe->dumpProfile();

    }

    std::string result = std::to_string(LOSS) + "," + std::to_string(trainSamples) +
                         "," + std::to_string(accuracy) + "," + std::to_string(testSamples);

    env->ReleaseStringUTFChars(modelCachePath, modelPath);
    env->ReleaseStringUTFChars(dataCachePath, dataPath);

    LOGE("Ready to return result");

    return env->NewStringUTF(result.data());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_osfans_trime_fl_MNNDataNative_nativeAggregateMnn(JNIEnv *env, jclass clazz,
                                                                jstring loadDir,
                                                                jobjectArray mnnDir,
                                                                jstring saveDir) {
    // TODO: implement nativeAggregateMnn()
    int agg = 1;
    std::string load_dir = std::string(env->GetStringUTFChars(loadDir, 0));
    std::string save_dir = std::string(env->GetStringUTFChars(saveDir, 0));
    jsize size = env->GetArrayLength(mnnDir);

    std::vector<MNN::Express::VARP> out;
    float total_weights = 0;
    for (int i = 0; i < size; i++) {
        jstring obj = (jstring)env->GetObjectArrayElement(mnnDir, i);
        std::string mnn = std::string(env->GetStringUTFChars(obj,NULL));
        if (mnn.length() < 4 || mnn.substr(mnn.length() - 4, 4) != ".mnn")
            continue;

        int pos = mnn.find('_');
        float weight = std::stof(mnn.substr(0, pos));

        std::string model = load_dir + mnn;

        MNN_PRINT("Appending model: %s\n", model.c_str());

        auto cur_model = MNN::Express::Variable::load(model.c_str());

        for (int j = 0; j < cur_model.size(); j++)
            auto map_ptr_1 = cur_model[j].get()->readMap<float>()[1];

        if (out.size() == 0) { // first model
            out = cur_model;
            for (int j = 0; j < out.size(); ++j)
                out[j]->input(out[j] * MNN::Express::_Const(weight)); // TODO: add weights here using MNN::Express::_Scalar<float>(2)
        }
        else {
            for (int j = 0; j < out.size(); ++j)
                out[j]->input(out[j] + cur_model[j] * MNN::Express::_Const(weight)); // TODO: add weights here using MNN::Express::_Scalar<float>(2)
        }

        total_weights += weight; // TODO: updated to weights
    }

    for (int i = 0; i < out.size(); ++i) {
        out[i]->input(out[i] / MNN::Express::_Const(total_weights));
    }

    MNN::Express::Variable::save(out, save_dir.c_str());

    auto agg_model = MNN::Express::Variable::load(save_dir.c_str());
    for (int i = 0; i < agg_model.size(); i++)
        auto map_ptr_1 = agg_model[i].get()->readMap<float>()[1];
    return (jint)agg;
}extern "C"
JNIEXPORT jdouble JNICALL
Java_com_osfans_trime_fl_MNNDataNative_getAggregateLoss(JNIEnv *env, jclass clazz,
                                                              jdoubleArray loss,
                                                              jdoubleArray train_weight) {
    // TODO: implement getAggregateLoss()
    jsize size = env->GetArrayLength(loss);
    jdouble* l = env->GetDoubleArrayElements(loss, 0);
    jdouble* tw = env->GetDoubleArrayElements(train_weight, 0);
    double sum_loss = 0;
    double sum_tw = 0;
    for (int i = 0; i < size; i++) {
        sum_loss = sum_loss + l[i] * tw[i];
        sum_tw += tw[i];
    }
    env->ReleaseDoubleArrayElements(loss, l, 0);
    env->ReleaseDoubleArrayElements(train_weight, tw, 0);
    return sum_loss / sum_tw;
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_com_osfans_trime_fl_MNNDataNative_getAggregateAcc(JNIEnv *env, jclass clazz,
                                                             jdoubleArray acc,
                                                             jdoubleArray test_weight) {
    // TODO: implement getAggregateAcc()
    jsize size = env->GetArrayLength(acc);
    jdouble* a = env->GetDoubleArrayElements(acc, 0);
    jdouble* tw = env->GetDoubleArrayElements(test_weight, 0);
    double sum_acc = 0;
    double sum_tw = 0;
    for (int i = 0; i < size; i++) {
        sum_acc = sum_acc + a[i] * tw[i];
        sum_tw += tw[i];
    }
    env->ReleaseDoubleArrayElements(acc, a, 0);
    env->ReleaseDoubleArrayElements(test_weight, tw, 0);
    return sum_acc / sum_tw;
}