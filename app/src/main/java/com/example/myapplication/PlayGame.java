package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.TodoResponse;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

public class PlayGame extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvTimer;
    private ImageView ivGenderIcon, btnStartRestart, btnLogout;
    private CountDownTimer timer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 1500000;
    private long breakTimeInMillis = 300000;
    private ApiService apiService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        userId = getIntent().getIntExtra("userId", -1);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        ivGenderIcon = findViewById(R.id.ivGenderIcon);
        tvTimer = findViewById(R.id.tvTimer);
        btnStartRestart = findViewById(R.id.btnStartRestart);
        btnLogout = findViewById(R.id.btnLogout);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String userEmail = intent.getStringExtra("userEmail");
        String gender = intent.getStringExtra("gender");

        tvUserName.setText("Nombre: " + userName);
        tvUserEmail.setText("Correo: " + userEmail);

        if (gender.equals("male")) {
            ivGenderIcon.setImageResource(R.drawable.baseline_male_24);
        } else if (gender.equals("female")) {
            ivGenderIcon.setImageResource(R.drawable.baseline_female_24);
        }

        btnStartRestart.setOnClickListener(v -> {
            if (isTimerRunning) {
                resetTimer();
                btnStartRestart.setImageResource(R.drawable.baseline_play_arrow_24);
            } else {
                startWorkTimer();
                btnStartRestart.setImageResource(R.drawable.baseline_restore_24);
            }
        });

        btnLogout.setOnClickListener(v -> {
            clearUserSession();

            Intent logoutIntent = new Intent(PlayGame.this, MainActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);

            finish();
        });
    }

    private void clearUserSession() {
        SharedPreferences preferences = getSharedPreferences("userSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    private void startWorkTimer() {
        timer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                showBreakDialog();
            }
        }.start();

        isTimerRunning = true;
    }

    private void showBreakDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¡Felicidades!")
                .setMessage("Empezó el tiempo de descanso!")
                .setPositiveButton("Entendido", (dialogInterface, i) -> {
                    startBreakTimer();
                    checkUserTasks();
                })
                .setCancelable(false)
                .show();
    }

    private void startBreakTimer() {
        timer = new CountDownTimer(breakTimeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                breakTimeInMillis = millisUntilFinished;
                updateBreakTimer();
            }

            @Override
            public void onFinish() {
                Toast.makeText(PlayGame.this, "Tiempo de descanso finalizado", Toast.LENGTH_SHORT).show();
                isTimerRunning = false;
            }
        }.start();
        isTimerRunning = true;
    }

    private void updateTimer() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateBreakTimer() {
        int minutes = (int) (breakTimeInMillis / 1000) / 60;
        int seconds = (int) (breakTimeInMillis / 1000) % 60;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void checkUserTasks() {
        apiService.getUserTodos(userId).enqueue(new Callback<List<TodoResponse>>() {
            @Override
            public void onResponse(Call<List<TodoResponse>> call, Response<List<TodoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TodoResponse> tasks = response.body();
                    if (tasks.isEmpty()) {
                        showNoTasksDialog();
                    } else {
                        Intent intent = new Intent(PlayGame.this, Task.class);
                        intent.putExtra("tasks", (ArrayList<TodoResponse>) tasks);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TodoResponse>> call, Throwable t) {
                Toast.makeText(PlayGame.this, "Error al cargar las tareas", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showNoTasksDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¡Descanso!")
                .setMessage("Inició el tiempo de descanso. No tienes tareas.")
                .setPositiveButton("Entendido", (dialogInterface, i) -> dialogInterface.dismiss())
                .setCancelable(false)
                .show();
    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timeLeftInMillis = 1500000;
        updateTimer();
        isTimerRunning = false;
        btnStartRestart.setImageResource(R.drawable.baseline_play_arrow_24);
    }
}
