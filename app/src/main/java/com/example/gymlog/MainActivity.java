package com.example.gymlog;


import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gymlog.database.GymLogRepository;
import com.example.gymlog.database.entities.GymLog;
import com.example.gymlog.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private GymLogRepository repository;

    public static final String TAG = "DAC_GYMLOG";
    String exercise = "";
    double weight = 0.0;
    int reps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //make db
        repository = GymLogRepository.getRepository(getApplication());

        //allows scrolling
        binding.logDisplayTextView.setMovementMethod(new ScrollingMovementMethod());

        binding.logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInformationFromDisplay();
                insertGymLogRecord();
                updateDisplay();
            }
        });

    }

    private void insertGymLogRecord(){
        GymLog log = new GymLog(exercise, weight, reps);
        repository.insertGymLog(log);
    }

    private void updateDisplay(){
        String currentInfo = binding.logDisplayTextView.getText().toString();
        Log.d(TAG, "current info: "+currentInfo);
        String newDisplay = String.format(Locale.US, "Exercise: %s%nWeight: %.2f%nReps: %d%n--------%n%s",
                exercise, weight, reps, currentInfo);
        binding.logDisplayTextView.setText(newDisplay);
        Log.i(TAG,repository.getAllLogs().toString());
    }


    private void getInformationFromDisplay(){
        //reading exercise name
        exercise = binding.exerciseInputEditText.getText().toString();

        //reading weight
        try {
            weight = Double.parseDouble(binding.weightInputEditText.getText().toString());
        }catch (NumberFormatException e){
            Log.d(TAG, "Error reading value from Weight edit text.");
        }

        //reading # of reps
        try {
            reps = Integer.parseInt(binding.repInputEditText.getText().toString());
        }catch (NumberFormatException e){
            Log.d(TAG, "Error reading value from reps edit text.");
        }
    }
}