package com.example.gesturerecognition;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DataPredictionActivity extends Activity implements IMUCallback {
    private static final String TAG = "DataPredictionActivity";
    private Button predictionButton;
    private TextView outcomeText;
    private IMUDataModel imuDataModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_prediction);
        predictionButton = findViewById(R.id.start_prediction);
        outcomeText = findViewById(R.id.prediction_outcome);
        try{
            imuDataModel = IMUDataModel.getInstance(DataPredictionActivity.this, DataPredictionActivity.this);
        }catch (Exception e){
            showToast(e.getMessage());
            finish();
        }
        predictionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imuDataModel.readIMUInput("prediction", false);
            }
        });
    }
    @Override
    protected void onDestroy() {
        imuDataModel.closeOutputWriter();
        super.onDestroy();
    }

    @Override
    public void updateViews(boolean val) {
        if(val)
            analyseData();
        predictionButton.setEnabled(val);
    }
    @Override
    public void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void analyseData(){
        float [][] imuInputData = imuDataModel.getInput();
        float[][][][] stftData = imuDataModel.getstftData(imuInputData);
        imuInputData = imuDataModel.scaleInput(imuInputData);
//        for (int i = 0; i < 100; i++)
//            for (int j = 0; j < 6; j++)
//                Log.d(TAG, "imuInput: " + String.valueOf(imuInputData[i][j]));

//        for(int i=0;i<33;i++)
//            for(int j=0;j<5;j++)
//                for(int k = 0; k <2; k++)
//                    for(int l = 0; l <6; l++)
//                        Log.d(TAG, "stftInput: " + String.valueOf(stftData[i][j][k][l]));
        IMUMLModel imumlModel = IMUMLModel.getInstance(DataPredictionActivity.this);
        int outputVal = imumlModel.doInference(imuInputData, stftData);
        if(outputVal == 1){
            showToast("Scrolled down");
            outcomeText.setText(R.string.outcome_one);
        }
        else if(outputVal == 2){
            showToast("Scrolled up");
            outcomeText.setText(R.string.outcome_two);
        }
        else if(outputVal == 3){
            showToast("thumb tap");
            outcomeText.setText(R.string.outcome_three);
        }
        else{
            showToast("wrong gesture");
            outcomeText.setText(R.string.outcome_zero);
        }
    }
}