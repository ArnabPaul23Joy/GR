package com.example.gesturerecognition;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class IMUDataModel implements SensorEventListener {
    private static final String TAG = "IMUDataModelService";
    private ArrayList<Float> all_sensor_data = new ArrayList<Float>();
    private float last_acc[] = new float[3];
    private float last_gyro[] = new float[3];
    private float [][] imuInputData = new float[100][6];
    private OutputStreamWriter outputStreamWriter;

    private IMUCallback imuCallback;
    private Handler handler;
    private int freqCount = 0;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer, senGyroscope;
    private static IMUDataModel imuDataModel;
    private IMUDataModel(Context context, IMUCallback imuCallback) throws Exception {
        initialize(context, imuCallback);
    }
    public void readIMUInput(String label, boolean isCollect){
        if(!isCollect)
            clearData();
        imuCallback.updateViews(false);
        handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                getSamplevalue();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(all_sensor_data.size() == 600) {
                            Log.d(TAG, "freqCount:   " + freqCount);
                            freqCount = 0;
                            if(isCollect)
                                writeOutputStream(label);
                            else
                                initializeInput();
                            imuCallback.updateViews(true);
                            return;
                        }
                        getSamplevalue();
                        handler.postDelayed(this, 10);
                    }
                };
                handler.postDelayed(runnable, 10);
            }
        });
    }
    private void initializeInput(){
        int k = 0;
        for(int i = 0; i < all_sensor_data.size(); ){
            for(int j = 0; j < 6 ; j++)
                imuInputData[k][j] = all_sensor_data.get(i + j).floatValue();
            i = i + 6;
            k++;
        }
        clearData();
    }

    public float[][] getInput(){return imuInputData;}

    public float[][] scaleInput(float [][] imuData){
        for(int j = 0; j < 6; j++){
            float minVal = imuData[0][j];
            float maxVal = imuData[0][j];
            for(int i = 0; i < 100 ; i++){
                if(imuData[i][j] < minVal)
                    minVal = imuData[i][j];
                else if(imuData[i][j] > maxVal)
                    maxVal = imuData[i][j];
            }
            for(int i = 0; i < 100 ; i++){
                imuData[i][j] = (imuData[i][j] - minVal) / (maxVal - minVal);
            }
        }
        return imuData;
    }
    public static IMUDataModel getInstance(Context context, IMUCallback imuCallback) throws Exception {
        if(imuDataModel == null)
            imuDataModel = new IMUDataModel(context, imuCallback);
        return imuDataModel;
    }
    private void initialize(Context context, IMUCallback imuCallback)throws Exception{
        Log.d(TAG, "onStartCommand");
        senSensorManager = (SensorManager)  context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_GAME);
        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_GAME);
        createFile(context);
        this.imuCallback = imuCallback;
        for(int i=0;i<100;i++)
            for(int j=0;j<6;j++)
                imuInputData[i][j] = 0.0f;
    }
    private void createFile(Context context) throws FileNotFoundException {
        File dir = new File(context.getApplicationContext().getFilesDir(), "gesture_data");
        if(!dir.exists())
            dir.mkdir();
        File file = new File(dir, "imu_data.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(file, true);
        outputStreamWriter = new OutputStreamWriter(fileOutputStream);
    }
    private void updateSensorData(SensorEvent sensorEvent){
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            last_gyro[0] = sensorEvent.values[0];
            last_gyro[1] = sensorEvent.values[1];
            last_gyro[2] = sensorEvent.values[2];
        }
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            last_acc[0] = sensorEvent.values[0];
            last_acc[1] = sensorEvent.values[1];
            last_acc[2] = sensorEvent.values[2];
        }
    }
    public void getSamplevalue(){
        freqCount ++;
        all_sensor_data.add(last_gyro[0]);
        all_sensor_data.add(last_gyro[1]);
        all_sensor_data.add(last_gyro[2]);
        all_sensor_data.add(last_acc[0]);
        all_sensor_data.add(last_acc[1]);
        all_sensor_data.add(last_acc[2]);
    }
    public float[][][][] getstftData(float[][] imuData){
        float[][] tempimuData = new float[imuData.length][imuData[0].length];
        float[] column = new float[100];
        for(int i = 0; i < imuData.length; i++){
            column[i] = 0.0f;
            for(int j = 0; j < imuData[0].length; j++)
                tempimuData[i][j] = 0.0f;
        }
        float[][][][] stftInput = new float[33][5][2][6];
        for(int i=0;i<33;i++)
            for(int j=0;j<5;j++)
                for(int k = 0; k <2; k++)
                    for(int l = 0; l <6; l++)
                        stftInput[i][j][k][l] = 0.0f;
        for(int i = 0; i < imuData.length; i++)
            for(int j = 0;j < imuData[0].length; j++)
                tempimuData[i][j] = imuData[i][j];
        for(int j = 0; j < 6; j++){
            for(int i = 0; i < 100; i++){
                column[i] = tempimuData[i][j];
            }
            float[][][] stftResult = STFT.prepareData(column, 100);
            for(int k =0 ; k < 33 ; k++)
                for( int l = 0; l < 5; l++)
                    for( int i = 0; i < 2; i++)
                        stftInput[k][l][i][j] = stftResult[k][l][i];
        }
        Log.d(TAG, "stftInput.shape:  " + String.valueOf(stftInput.length ) + "  " + String.valueOf(stftInput[0].length) + "  " + String.valueOf(stftInput[0][0].length)+ "  " + String.valueOf(stftInput[0][0][0].length));
//        Log.d(TAG, "stftInput[32][5][0][3]:  " + String.valueOf(stftInput[32][4][0][3]) + "  " + String.valueOf(stftInput[0].length));
        return stftInput;
    }
    private void clearData(){
        if(all_sensor_data != null && !all_sensor_data.isEmpty())
            all_sensor_data.clear();
    }
    private void writeOutputStream(String sampleData) {
        try{
            for(float floatVal : all_sensor_data){
                sampleData += ','+Float.toString(floatVal);
            }
            outputStreamWriter.append(sampleData + "\n");
            all_sensor_data.clear();
        }catch (Exception e){
            imuCallback.showToast(e.getMessage());
        }
    }
    public void saveFile(){
        try{
            if(outputStreamWriter != null)
                outputStreamWriter.flush();
        }catch (Exception e){
            imuCallback.showToast(e.getMessage());
        }
    }
    public void closeOutputWriter(){
        try{
            imuDataModel = null;
            if(outputStreamWriter == null)
                return;
            outputStreamWriter.close();
        }catch (Exception e){
            imuCallback.showToast(e.getMessage());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        try {
            updateSensorData(sensorEvent);
        } catch (Exception e) {
            imuCallback.showToast(e.getMessage());
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}