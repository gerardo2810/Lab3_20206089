package com.example.myapplication;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.LoginRequest;
import com.example.myapplication.api.LoginResponse;
import com.example.myapplication.databinding.ActivityMainBinding;

import com.example.myapplication.utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        binding.btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = binding.etUsername.getText().toString();
        String password = binding.etPassword.getText().toString();

        LoginRequest loginRequest = new LoginRequest(username, password);

        apiService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    Toast.makeText(MainActivity.this, "Bienvenido " + loginResponse.getFirstName(), Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(MainActivity.this, PlayGame.class);
                    intent.putExtra("userName", loginResponse.getFirstName() + " " + loginResponse.getLastName());
                    intent.putExtra("userEmail", loginResponse.getEmail());
                    intent.putExtra("gender", loginResponse.getGender());
                    startActivity(intent);

                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Login fallido", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
