package com.example.gymlog;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymlog.database.GymLogRepository;
import com.example.gymlog.database.entities.GymLog;
import com.example.gymlog.database.entities.User;
import com.example.gymlog.databinding.ActivityMainBinding;
import com.example.gymlog.viewHolders.GymLogAdapter;
import com.example.gymlog.viewHolders.GymLogViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //key for user id
    static final String MAIN_ACTIVITY_USER_ID = "com.example.gymlog.MAIN_ACTIVITY_USER_ID";
    static final String SHARED_PREFERENCE_USERID_KEY = "com.example.gymlog.SHARED_PREFERENCE_USERID_KEY";
    static final String SAVED_INSTANCE_STATE_USERID_KEY = "com.example.gymlog.SAVED_INSTANCE_STATE_USERID_KEY";
    private static final int LOGGED_OUT = -1;
    private ActivityMainBinding binding;
    private GymLogRepository repository;
    private GymLogViewModel gymLogViewModel;

    public static final String TAG = "DAC_GYMLOG";
    String exercise = "";
    double weight = 0.0;
    int reps = 0;

    private int loggedInUserId = LOGGED_OUT;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gymLogViewModel = new ViewModelProvider(this).get(GymLogViewModel.class);


        RecyclerView recyclerView = binding.logDisplayRecyclerView;
        final GymLogAdapter adapter = new GymLogAdapter(new GymLogAdapter.GymLogDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //make db
        repository = GymLogRepository.getRepository(getApplication());
        loginUser(savedInstanceState);

        gymLogViewModel.getAllLogsById(loggedInUserId).observe(this,gymLogs -> {
            adapter.submitList(gymLogs);
        });

        //user is not logged in at this point, go to login screen
        if(loggedInUserId == -1) {
            Intent intent = LoginActivity.loginIntentFactory(getApplicationContext());
            startActivity(intent);
        }

        updateSharedPreference();

        //TODO: remove two lines below
       // binding.logDisplayTextView.setMovementMethod(new ScrollingMovementMethod());

        //initial display
       // updateDisplay();

        binding.logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInformationFromDisplay();
                insertGymLogRecord();
                //TODO: remove line below
              //  updateDisplay();
            }
        });

        /* TODO: remove this block
        binding.exerciseInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDisplay();
            }
        });
*/
    }



    private void loginUser(Bundle savedInstanceState) {
        //checks sharedpreferences for logged in user
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);

        loggedInUserId = sharedPreferences.getInt(getString(R.string.preference_userId_key), LOGGED_OUT);

        if(loggedInUserId == LOGGED_OUT & savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_STATE_USERID_KEY)) {
            loggedInUserId = savedInstanceState.getInt(SAVED_INSTANCE_STATE_USERID_KEY, LOGGED_OUT);
        }
        //if still logged out, pull from intent
        if(loggedInUserId == LOGGED_OUT){
            loggedInUserId = getIntent().getIntExtra(MAIN_ACTIVITY_USER_ID, LOGGED_OUT);
        }
        //starts main activity
        if(loggedInUserId == LOGGED_OUT) {
            return;
        }
        LiveData<User> userObserver = repository.getUserByUserId(loggedInUserId);
        userObserver.observe(this, u -> {
            if (u != null) {
                this.user = u;
                invalidateOptionsMenu();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_INSTANCE_STATE_USERID_KEY, loggedInUserId);
        updateSharedPreference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.logoutMenuItem);
        //set menu to be always visible
        item.setVisible(true);
        if(user == null){
            return false;
        }
        item.setTitle(user.getUsername());
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                showLogoutDialog();
                return false;
            }
        });
        return true;
    }

    private void showLogoutDialog(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        //instantiates memory for alert dialog, rendered using a single thread to ensure only
        //1 alert dialog at a time, preventing app from locking up
        final AlertDialog alertDialog = alertBuilder.create();

        //building the alert
        alertBuilder.setMessage("Logout?");

        //adding "press yes to logout" button
        alertBuilder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });

        //"stop logout" button
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        //showing the freshly built alert
        alertBuilder.create().show();
    }

    private void logout() {

        loggedInUserId = LOGGED_OUT;
        updateSharedPreference();
        getIntent().putExtra(MAIN_ACTIVITY_USER_ID,loggedInUserId);

        startActivity(LoginActivity.loginIntentFactory(getApplicationContext()));
    }

    private void updateSharedPreference(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.putInt(getString(R.string.preference_userId_key),loggedInUserId);
        sharedPrefEditor.apply();
    }

    static Intent mainActivityIntentFactory(Context context, int userId){
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MAIN_ACTIVITY_USER_ID, userId);
        return intent;
    }

    private void insertGymLogRecord() {
        if (exercise.isEmpty()) {
            return;
        }
        GymLog log = new GymLog(exercise, weight, reps, loggedInUserId);
        repository.insertGymLog(log);
    }

    @Deprecated
    private void updateDisplay() {
        ArrayList<GymLog> allLogs = repository.getAllLogsByUserId(loggedInUserId);
        if (allLogs.isEmpty()) {
            //binding.logDisplayTextView.setText(R.string.nothing_to_show_time_to_hit_the_gym);
        }
        //building display
        StringBuilder sb = new StringBuilder();
        for (GymLog log : allLogs) {
            sb.append(log);
        }
       // binding.logDisplayTextView.setText(sb.toString());
    }


    private void getInformationFromDisplay() {
        //reading exercise name
        exercise = binding.exerciseInputEditText.getText().toString();

        //reading weight
        try {
            weight = Double.parseDouble(binding.weightInputEditText.getText().toString());
        } catch (NumberFormatException e) {
            Log.d(TAG, "Error reading value from Weight edit text.");
        }

        //reading # of reps
        try {
            reps = Integer.parseInt(binding.repInputEditText.getText().toString());
        } catch (NumberFormatException e) {
            Log.d(TAG, "Error reading value from reps edit text.");
        }
    }
}