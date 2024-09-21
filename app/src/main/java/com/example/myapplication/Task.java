package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.TodoResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Task extends AppCompatActivity {

    private Spinner taskSpinner;
    private Button changeStatusButton;
    private ArrayList<TodoResponse> tasks; // Lista de tareas del usuario
    private ArrayAdapter<String> spinnerAdapter;
    private ImageView btnLogout; // Referencia al botón de logout
    private ApiService apiService; // Referencia al servicio API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Configurar Retrofit para hacer la solicitud PUT
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com/") // Base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Habilitar la flecha de regreso en la barra de acción
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        taskSpinner = findViewById(R.id.taskSpinner);
        changeStatusButton = findViewById(R.id.btnChangeStatus);
        btnLogout = findViewById(R.id.btnLogout); // Referencia al botón de logout

        // Obtener las tareas del Intent
        tasks = (ArrayList<TodoResponse>) getIntent().getSerializableExtra("tasks");

        // Crear un adaptador para el Spinner con el formato "<nombre> - <Completado/No completado>"
        ArrayList<String> taskNames = new ArrayList<>();
        for (TodoResponse task : tasks) {
            String status = task.isCompleted() ? "Completado" : "No completado";
            taskNames.add(task.getTodo() + " - " + status);
        }

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, taskNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskSpinner.setAdapter(spinnerAdapter);

        // Botón para cambiar el estado de la tarea seleccionada
        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedTaskIndex = taskSpinner.getSelectedItemPosition();
                TodoResponse selectedTask = tasks.get(selectedTaskIndex);

                // Cambiar el estado de completado
                boolean newStatus = !selectedTask.isCompleted();
                selectedTask.setCompleted(newStatus);

                // Llamar a la API para actualizar el estado de la tarea (PUT request)
                apiService.updateTodo(selectedTask.getId(), selectedTask).enqueue(new Callback<TodoResponse>() {
                    @Override
                    public void onResponse(Call<TodoResponse> call, Response<TodoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Actualizar el Spinner con el nuevo estado
                            String status = newStatus ? "Completado" : "No completado";
                            taskNames.set(selectedTaskIndex, selectedTask.getTodo() + " - " + status);
                            spinnerAdapter.notifyDataSetChanged();

                            Toast.makeText(Task.this, "Estado de la tarea actualizado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Task.this, "Error al actualizar la tarea", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<TodoResponse> call, Throwable t) {
                        Toast.makeText(Task.this, "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Botón de logout: Cerrar sesión y volver a MainActivity
        btnLogout.setOnClickListener(v -> {
            clearUserSession(); // Limpiar la sesión del usuario

            // Volver a la pantalla de login (MainActivity)
            Intent logoutIntent = new Intent(Task.this, MainActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);

            // Finalizar la actividad actual
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

    // Manejar el botón de regreso en la barra de acción
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Volver a PlayGame cuando se presiona la flecha de regreso
            Intent intent = new Intent(Task.this, PlayGame.class);
            startActivity(intent);
            finish(); // Finalizar la actividad actual
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
