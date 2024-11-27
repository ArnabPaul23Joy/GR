package com.example.gesturerecognition;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.example.gesturerecognition.R;

public class HomeActivity extends Activity {
    private Button dataCollection;
    private Button dataPrediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataCollection = findViewById(R.id.collect_data);
        dataPrediction = findViewById(R.id.predict_data);
        dataCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, DataCollectionActivity.class));
                finish();
            }
        });
        dataPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, DataPredictionActivity.class));
                finish();
            }
        });
    }
}