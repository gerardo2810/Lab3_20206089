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
    private long timeLeftInMillis = 1500000; // 25 minutos en milisegundos
    private long breakTimeInMillis = 300000; // 5 minutos en milisegundos
    private ApiService apiService;
    private int userId; // El userId debe ser pasado desde el login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        // Configurar Retrofit para hacer la solicitud de tareas
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Obtener el userId desde el intent (pasado en el inicio de sesión)
        userId = getIntent().getIntExtra("userId", -1);

        // Referencias a las vistas
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        ivGenderIcon = findViewById(R.id.ivGenderIcon);
        tvTimer = findViewById(R.id.tvTimer);
        btnStartRestart = findViewById(R.id.btnStartRestart);
        btnLogout = findViewById(R.id.btnLogout); // Referencia al botón de logout

        // Obtener los datos del Intent
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String userEmail = intent.getStringExtra("userEmail");
        String gender = intent.getStringExtra("gender");

        // Mostrar los datos del usuario
        tvUserName.setText("Nombre: " + userName);
        tvUserEmail.setText("Correo: " + userEmail);

        // Mostrar el ícono correcto basado en el género
        if (gender.equals("male")) {
            ivGenderIcon.setImageResource(R.drawable.baseline_male_24); // Reemplazar con ícono masculino
        } else if (gender.equals("female")) {
            ivGenderIcon.setImageResource(R.drawable.baseline_female_24); // Reemplazar con ícono femenino
        }

        // Botón para iniciar/reiniciar el temporizador
        btnStartRestart.setOnClickListener(v -> {
            if (isTimerRunning) {
                // Si el temporizador está corriendo, reiniciamos el temporizador
                resetTimer();
                // Cambiamos el ícono del botón a "play" después de reiniciar
                btnStartRestart.setImageResource(R.drawable.baseline_play_arrow_24);
            } else {
                // Si el temporizador no está corriendo, lo iniciamos
                startWorkTimer();
                // Cambiamos el ícono del botón a "restart" después de iniciar el temporizador
                btnStartRestart.setImageResource(R.drawable.baseline_restore_24);
            }
        });

        // Botón de logout: Cerrar sesión y volver a MainActivity
        btnLogout.setOnClickListener(v -> {
            // Limpiar datos de sesión
            clearUserSession();

            // Volver a la pantalla de login (MainActivity)
            Intent logoutIntent = new Intent(PlayGame.this, MainActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Eliminar actividad actual del stack
            startActivity(logoutIntent);

            // Finalizar la actividad actual para que no se pueda regresar
            finish();
        });
    }

    // Método para limpiar la sesión del usuario usando SharedPreferences
    private void clearUserSession() {
        SharedPreferences preferences = getSharedPreferences("userSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Limpiar todos los datos guardados
        editor.apply();
    }

    // Iniciar el temporizador de trabajo
    private void startWorkTimer() {
        timer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                showBreakDialog();  // Mostrar el diálogo al finalizar el temporizador
            }
        }.start();

        isTimerRunning = true;
    }

    // Mostrar el dialogo al finalizar el tiempo de trabajo
    private void showBreakDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¡Felicidades!")
                .setMessage("Empezó el tiempo de descanso!")
                .setPositiveButton("Entendido", (dialogInterface, i) -> {
                    startBreakTimer();  // Iniciar el temporizador de descanso
                    checkUserTasks();   // Revisar si el usuario tiene tareas
                })
                .setCancelable(false)
                .show();
    }

    // Iniciar el temporizador de descanso
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

    // Actualizar el temporizador en pantalla
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

    // Verificar las tareas del usuario
    private void checkUserTasks() {
        apiService.getUserTodos(userId).enqueue(new Callback<List<TodoResponse>>() {
            @Override
            public void onResponse(Call<List<TodoResponse>> call, Response<List<TodoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TodoResponse> tasks = response.body();
                    if (tasks.isEmpty()) {
                        showNoTasksDialog(); // Mostrar un diálogo si no tiene tareas
                    } else {
                        // Abrir la actividad con las tareas si tiene al menos una tarea
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


    // Mostrar un dialog si no tiene tareas
    private void showNoTasksDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¡Descanso!")
                .setMessage("Inició el tiempo de descanso. No tienes tareas.")
                .setPositiveButton("Entendido", (dialogInterface, i) -> dialogInterface.dismiss())
                .setCancelable(false)
                .show();
    }

    // Resetear el temporizador de trabajo
    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timeLeftInMillis = 1500000; // Reiniciar a 25 minutos
        updateTimer();
        isTimerRunning = false;
        btnStartRestart.setImageResource(R.drawable.baseline_play_arrow_24);  // Cambiar ícono a "play"
    }
}
