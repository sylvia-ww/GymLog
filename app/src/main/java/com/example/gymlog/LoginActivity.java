package com.example.gymlog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.gymlog.database.GymLogRepository;
import com.example.gymlog.database.entities.User;
import com.example.gymlog.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private GymLogRepository repository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = GymLogRepository.getRepository(getApplication());

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyUser();
            }
        });
    }

    private void verifyUser() {

        String username = binding.userNameLoginEditText.getText().toString();

        if (username.isEmpty()) {
            toastMaker("Username should not be blank");
            return;
        }
        LiveData<User> userObserver = repository.getUserByUserName(username);
        //observes for db to send back something
        userObserver.observe(this, user -> {
            if (user != null) {
                String password = binding.passwordLoginEditText.getText().toString();
                if (password.equals(user.getPassword())) {
                    //gets sharedpreferences
                    SharedPreferences sharedPreferences = getApplicationContext().
                            getSharedPreferences(MainActivity.SHARED_PREFERENCE_USERID_KEY,
                                    Context.MODE_PRIVATE);
                    SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
                    //stores an integer
                    sharedPrefEditor.putInt(MainActivity.SHARED_PREFERENCE_USERID_KEY, user.getId());
                    //saves changes
                    sharedPrefEditor.apply();
                    //starts main activity
                    startActivity(MainActivity.mainActivityIntentFactory(getApplicationContext(), user.getId()));
                } else {
                    toastMaker("Invalid password");
                    //if password is invalid, selects the entered password
                    binding.passwordLoginEditText.setSelection(0);
                }
            } else {
                toastMaker(String.format("%s is not a valid username.", username));
                //if username is invalid, selects the entered username
                binding.userNameLoginEditText.setSelection(0);
            }
        });
    }

    private void toastMaker(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    static Intent loginIntentFactory(Context context) {
        //no info is being sent to login screen, so just switch to it
        return new Intent(context, LoginActivity.class);
    }


}