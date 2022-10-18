package com.osfans.trime.fl;

public class MNNDataNative {
    // load libraries
    static void loadGpuLibrary(String name) {
        try {
            System.loadLibrary(name);
        } catch (Throwable ce) {
            System.out.println("load MNNTrain " + name + " GPU so exception=%s");
        }
    }
    // load mnn library
    static {
        System.loadLibrary("MNNTrain");
        System.loadLibrary("MNN_Express");
        System.loadLibrary("MNN");
        System.loadLibrary("mnncore");
    }

    /**
     * We call the training module of C++ to complete training task, and obtain the relevant information required by Android UI
     * @param modelCachePath the storage path of model in Android
     * @param dataCachePath the storage path of training dataset in Android
     * @return result of client training with total local epochs (format: "loss,trainSamples,accuracy,testSamples")
     */
    public static native String nativeCreateDatasetFromFile(String modelCachePath, String dataCachePath);
    /*
    Return: current epoch and the loss value in this epoch (format: "epoch,loss")
     */

    /**
     *
     * @return the local epoch index in each global epoch training, and the training loss in this local epoch
     */
    public static native String getEpochAndLoss();

    public static native int nativeAggregateMnn(String loadDir, String[] mnnDir, String saveDir);

    public static native double getAggregateLoss(double[] loss, double[] train_weight);

    public static native double getAggregateAcc(double[] acc, double[] test_weight);

}
