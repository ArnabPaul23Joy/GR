package com.example.gesturerecognition;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;


public class DataCollectionActivity extends Activity implements IMUCallback {
    private static final String TAG = "MainActivity";
    private EditText editText;
    private Button button, saveButton;
    private TextView textView;
    private int totalCount = 0;
    private IMUDataModel imuDataModel;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 1);
        textView = findViewById(R.id.total_count);
        editText = findViewById(R.id.gesture_name);
        button = findViewById(R.id.gesture_button);
        saveButton = findViewById(R.id.save_button);
        try{
            imuDataModel = IMUDataModel.getInstance(DataCollectionActivity.this, DataCollectionActivity.this);
        }catch (Exception e){
            showToast(e.getMessage());
            finish();
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalCount++;
                imuDataModel.readIMUInput(editText.getText().toString(), true);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalCount = 0;
                textView.setText(Integer.toString(totalCount));
                imuDataModel.saveFile();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public void updateViews(boolean enabled){
        textView.setText(Integer.toString(totalCount));
        editText.setEnabled(enabled);
        button.setEnabled(enabled);
        saveButton.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        imuDataModel.closeOutputWriter();
        super.onDestroy();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}