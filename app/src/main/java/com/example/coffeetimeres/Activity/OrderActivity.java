package com.example.coffeetimeres.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.coffeetimeres.Adapter.ApprovedBookingListAdapter;
import com.example.coffeetimeres.Adapter.BookingListAdapter;
import com.example.coffeetimeres.Domain.BookingItem;
import com.example.coffeetimeres.R;
import com.example.coffeetimeres.notificasion.PushService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderActivity extends Activity {
    private RecyclerView recyclerView;
    private BookingListAdapter adapter;

    private List<BookingItem> bookingList;
    FirebaseAnalytics mFirebaseAnalytics;
    private List<BookingItem> approvedBookingList;
    private ApprovedBookingListAdapter secondAdapter;
    private RecyclerView secondRecyclerView;
    private BroadcastReceiver pushBroadcastReceiver;
    private String token ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        String token = getIntent().getStringExtra("access_token");
        FirebaseApp.initializeApp(this);
        sendTokenToServer();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        recyclerView = findViewById(R.id.view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        adapter = new BookingListAdapter(OrderActivity.this, bookingList, token);
        recyclerView.setAdapter(adapter);

        secondRecyclerView = findViewById(R.id.secondRecyclerView);
        secondRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        approvedBookingList = new ArrayList<>();
        secondAdapter = new ApprovedBookingListAdapter(OrderActivity.this, approvedBookingList,token);
        secondRecyclerView.setAdapter(secondAdapter);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        String token = task.getResult();
                        Log.e("Token", "token -> " + token);
                    }
                });

        pushBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("TAG_NEW_TOKEn", token);
               executeGetRequest(token);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PushService.INTENT_FILTER);
        registerReceiver(pushBroadcastReceiver, intentFilter);

        executeGetRequest(token);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(pushBroadcastReceiver);
        super.onDestroy();
    }

    public void executeGetRequest(String token) {
        bookingList.clear();
        approvedBookingList.clear();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://losermaru.pythonanywhere.com/orders/";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Обработка успешного ответа от сервера
                        parseResponse(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Обработка ошибки запроса
                        Toast.makeText(OrderActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        System.out.println("-------------------" + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Добавление заголовка авторизации
                return headers;
            }
        };
        queue.add(request);
    }

    private void parseResponse(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                String status = jsonObject.getString("status");
                BookingItem booking = parseBookingItem(jsonObject);

                if ("waiting".equals(status)) {
                    bookingList.add( booking); // Добавление элемента в начало списка
                } else if ("approved".equals(status)) {
                    if (!approvedBookingList.contains(booking)) {
                        approvedBookingList.add( booking); // Добавление элемента в начало списка только если его нет в списке
                    }
                }
            }

            adapter.notifyDataSetChanged();
            secondAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MyTag", "Error parsing response: " + e.getMessage());
            Toast.makeText(OrderActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
        }
    }

    private BookingItem parseBookingItem(JSONObject jsonObject) throws JSONException {
        String status = jsonObject.getString("status");
        int id = jsonObject.getInt("id");
        String name = jsonObject.getString("name");
        String pickUpTime = jsonObject.getString("pick_up_time").substring(0, 16).replace('T', ' ');

        JSONObject cafeObject = jsonObject.getJSONObject("cafe");
        int cafeId = cafeObject.getInt("id");
        String cafeName = cafeObject.getString("name");

        JSONObject coffeeObject = jsonObject.getJSONObject("coffee");
        int coffeeId = coffeeObject.getInt("id");
        String coffeeName = coffeeObject.getString("name");
        String coffeeDescription = coffeeObject.getString("description");
        String coffeeImage = coffeeObject.getString("image");
        String smartphoneKey = jsonObject.getJSONObject("user").getString("smartphone_key");
        Log.e("TAG token", smartphoneKey);
        return new BookingItem(status, name, cafeName, pickUpTime, coffeeName, coffeeDescription, coffeeImage, id, cafeId, coffeeId, smartphoneKey);
    }

    private void fetchRestaurantName(int id, BookingItem booking) {
        String url = "https://losermaru.pythonanywhere.com/orders/" + id;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String name = response.getString("name");
                            booking.setCoffeeName(name);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(request);
    }

    private void sendTokenToServer() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String ratingUrl = "https://losermaru.pythonanywhere.com/cafe/cafe_key";
        String token = getIntent().getStringExtra("access_token");
        JSONObject jsonBody = new JSONObject();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            // Если не удалось получить токен, обработайте ошибку здесь
                            return;
                        }
                        // Токен успешно получен
                        String Mytoken = task.getResult();
                        // Создание JSON тела запроса
                        JSONObject jsonBody = new JSONObject();
                        try {
                            jsonBody.put("cafe_key",Mytoken );
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ratingUrl, jsonBody,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.e("TAG_TOKENLOL", Mytoken);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // Обработка ошибки
                                        String errorMessage = "Error sending rating: " + error.getMessage();
                                        Toast.makeText(OrderActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                        if (error.networkResponse != null) {
                                            int statusCode = error.networkResponse.statusCode;
                                            String responseData = new String(error.networkResponse.data);
                                            Log.e("ErrorResponse", "Status Code: " + statusCode);
                                            Log.e("ErrorResponse", "Response Data: " + responseData);
                                        }
                                    }
                                }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Authorization", "Bearer " + token);
                                return headers;
                            }
                        };

                        // Добавление запроса в очередь
                        queue.add(request);
                    }
                });
    }


}