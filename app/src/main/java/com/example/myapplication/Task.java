package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.api.TodoResponse;

import java.util.ArrayList;

public class Task extends AppCompatActivity {

    private Spinner taskSpinner;
    private Button changeStatusButton;
    private ArrayList<TodoResponse> tasks; // Lista de tareas del usuario
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        taskSpinner = findViewById(R.id.taskSpinner);
        changeStatusButton = findViewById(R.id.btnChangeStatus);

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

                // Actualizar el Spinner con el nuevo estado
                String status = newStatus ? "Completado" : "No completado";
                taskNames.set(selectedTaskIndex, selectedTask.getTodo() + " - " + status);
                spinnerAdapter.notifyDataSetChanged();

                // Aquí podrías hacer una llamada a la API para actualizar el estado de la tarea en el backend, si es necesario

                Toast.makeText(Task.this, "Estado de la tarea cambiado", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón de logout y regreso en el menú
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita el botón de regreso
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Al presionar el botón de regreso, volvemos a la PlayGameActivity
        Intent intent = new Intent(Task.this, PlayGame.class);
        startActivity(intent);
        finish();
        return true;
    }
}
