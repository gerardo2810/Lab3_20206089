package com.example.myapplication.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);
    @GET("/todos/user/{userId}")
    Call<List<TodoResponse>> getUserTodos(@Path("userId") int userId);
    @PUT("todos/{id}")
    Call<TodoResponse> updateTodo(@Path("id") int todoId, @Body TodoResponse todoResponse);

}