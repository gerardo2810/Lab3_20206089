package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class PlayGame extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvTimer, tvBreakTime;
    private ImageView btnLogout, ivGenderIcon, btnStartRestart;
    private CountDownTimer timer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 1500000; // 25 minutos en milisegundos (25 * 60 * 1000)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        // Referencias de vistas
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvTimer = findViewById(R.id.tvTimer);
        tvBreakTime = findViewById(R.id.tvBreakTime);
        ivGenderIcon = findViewById(R.id.ivGenderIcon);
        btnLogout = findViewById(R.id.btnLogout);
        btnStartRestart = findViewById(R.id.btnStartRestart);

        // Configurar datos del usuario desde la sesión previa (debes pasar los datos desde MainActivity)
        String userName = getIntent().getStringExtra("userName");
        String userEmail = getIntent().getStringExtra("userEmail");
        String gender = getIntent().getStringExtra("gender");

        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);
        if (gender.equals("male")) {
            ivGenderIcon.setImageResource(R.drawable.baseline_male_24);
        } else {
            ivGenderIcon.setImageResource(R.drawable.baseline_female_24);
        }

        // Botón para cerrar sesión y regresar a MainActivity
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(PlayGame.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Botón para iniciar/reiniciar el temporizador
        btnStartRestart.setOnClickListener(v -> {
            if (isTimerRunning) {
                resetTimer();
            } else {
                startTimer();
            }
        });
    }

    // Iniciar el temporizador
    private void startTimer() {
        timer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnStartRestart.setImageResource(R.drawable.baseline_play_arrow_24); // Cambiar ícono a play
            }
        }.start();

        isTimerRunning = true;
        btnStartRestart.setImageResource(R.drawable.baseline_restore_24); // Cambiar ícono a restart
    }

    // Resetear el temporizador
    private void resetTimer() {
        timer.cancel();
        timeLeftInMillis = 1500000; // Reiniciar a 25 minutos
        updateTimer();
        isTimerRunning = false;
        btnStartRestart.setImageResource(R.drawable.baseline_play_arrow_24); // Cambiar ícono a play
    }

    // Actualizar la vista del temporizador
    private void updateTimer() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        tvTimer.setText(timeFormatted);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
