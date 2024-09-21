package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.api.TodoResponse;
import java.util.ArrayList;

public class Task extends AppCompatActivity {

    private Spinner taskSpinner;
    private ArrayList<TodoResponse> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        taskSpinner = findViewById(R.id.taskSpinner);

        // Obtener las tareas pasadas por el intent
        tasks = (ArrayList<TodoResponse>) getIntent().getSerializableExtra("tasks");

        // Preparar el adaptador para mostrar las tareas en el Spinner
        ArrayList<TodoResponse> tasks = (ArrayList<TodoResponse>) getIntent().getSerializableExtra("tasks");

        ArrayList<String> taskNames = new ArrayList<>();
        for (TodoResponse task : tasks) {
            String taskStatus = task.isCompleted() ? "Completado" : "No completado";
            taskNames.add(task.getTodo() + " - " + taskStatus);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, taskNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskSpinner.setAdapter(adapter);

    }
}
