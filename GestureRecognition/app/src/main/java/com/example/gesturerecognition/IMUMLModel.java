package com.example.gesturerecognition;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.apache.commons.math3.analysis.function.Max;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class IMUMLModel {
    private static final String TAG = "IMUMLModel";
    private static IMUMLModel imuMLModel;
    private MappedByteBuffer mappedByteBuffer;
    private IMUMLModel(Context context)throws IOException {
        mappedByteBuffer = loadModelFile(context);
    }
    public static IMUMLModel getInstance(Context context){
        try{
            if(imuMLModel == null)
                imuMLModel = new IMUMLModel(context);
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        return imuMLModel;
    }
    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("imu_gesture_lite_model_2.0.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declareLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }
    public int doInference(float[][] imuInput, float[][][][] stftInput) {
        float[][] output=new float[1][8];
        Object[] inputs = {imuInput, stftInput};
        Map<Integer, Object> outputs = new HashMap<>();
//        for(int i=0;i<100;i++)
//            for(int j=0;j<6;j++)
//                Log.d(TAG, "imuInput: " + String.valueOf(imuInput[i][j]));
//
//        for(int i=0;i<33;i++)
//            for(int j=0;j<5;j++)
//                for(int k = 0; k <2; k++)
//                    for(int l = 0; l <6; l++)
//                        Log.d(TAG, "stftInput: " + String.valueOf(stftInput[i][j][k][l]));
        try {
            Interpreter tflite = new Interpreter(mappedByteBuffer);
//            tflite.getOutputTensor().shape();
            Log.d(TAG, "tflite created!!!");
            outputs.put(0, output);
            tflite.runForMultipleInputsOutputs(inputs, outputs);
            Log.d(TAG, "tflite.runForMultipleInputsOutputs!!!");
        }catch (Exception ex){
            Log.d(TAG, "ex.getMessage():  " + ex.getMessage());
            ex.printStackTrace();
        }
        Log.d(TAG, "output:  " + String.valueOf(output[0][0]) + "  " + String.valueOf(output[0][1]));
        int indx=0;
        float maxVal=-1.0f;
        for(int i=0;i<8;i++){
            if(maxVal<output[0][i]){
                indx = i;
                maxVal =  output[0][i];
            }
        }
        return indx;
    }
}