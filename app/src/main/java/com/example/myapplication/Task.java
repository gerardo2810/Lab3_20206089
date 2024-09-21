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
    private ArrayList<TodoResponse> tasks;
    private ArrayAdapter<String> spinnerAdapter;
    private ImageView btnLogout;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com/") // Base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        taskSpinner = findViewById(R.id.taskSpinner);
        changeStatusButton = findViewById(R.id.btnChangeStatus);
        btnLogout = findViewById(R.id.btnLogout);

        tasks = (ArrayList<TodoResponse>) getIntent().getSerializableExtra("tasks");

        ArrayList<String> taskNames = new ArrayList<>();
        for (TodoResponse task : tasks) {
            String status = task.isCompleted() ? "Completado" : "No completado";
            taskNames.add(task.getTodo() + " - " + status);
        }

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, taskNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskSpinner.setAdapter(spinnerAdapter);

        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedTaskIndex = taskSpinner.getSelectedItemPosition();
                TodoResponse selectedTask = tasks.get(selectedTaskIndex);

                boolean newStatus = !selectedTask.isCompleted();
                selectedTask.setCompleted(newStatus);

                apiService.updateTodo(selectedTask.getId(), selectedTask).enqueue(new Callback<TodoResponse>() {
                    @Override
                    public void onResponse(Call<TodoResponse> call, Response<TodoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
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

        btnLogout.setOnClickListener(v -> {

            Intent logoutIntent = new Intent(Task.this, MainActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);

            finish();
        });
    }

    private void clearUserSession() {
        SharedPreferences preferences = getSharedPreferences("userSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Limpiar todos los datos guardados
        editor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(Task.this, PlayGame.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
