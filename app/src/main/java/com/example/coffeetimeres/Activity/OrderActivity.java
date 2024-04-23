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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        String token = getIntent().getStringExtra("access_token");
        FirebaseApp.initializeApp(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        recyclerView = findViewById(R.id.view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        adapter = new BookingListAdapter(OrderActivity.this, bookingList, token);
        recyclerView.setAdapter(adapter);

        secondRecyclerView = findViewById(R.id.secondRecyclerView);
        secondRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        approvedBookingList = new ArrayList<>();
        secondAdapter = new ApprovedBookingListAdapter(OrderActivity.this, approvedBookingList);
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
                Bundle extras = intent.getExtras();
                Log.e("TAG", "MESSAGE_RECEIVED ");
                if (extras != null) {
                    String key = extras.keySet().iterator().next();
                    if (key.equals(PushService.KEY_ACTION)) {
                        Log.e("TAG", "KEY  -> " + key);
                        String action = extras.getString(key);
                        if (action != null) {
                            if (action.equals(PushService.ACTIONS_SHOW_MESSAGE)) {
                                String message = extras.getString(PushService.KEY_MESSAGE);
                                if (message != null) {
                                    Log.e("TAG", "MESSAGE_KEY  -> " + message);
                                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("TAG", "NO_");
                            }
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PushService.INTENT_FILTER);
        registerReceiver(pushBroadcastReceiver, intentFilter);
        executeGetRequest();
    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(pushBroadcastReceiver);
        super.onDestroy();
    }
    private void executeGetRequest() {
        String token = getIntent().getStringExtra("access_token"); // Получение значения токена
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
                    bookingList.add(booking);
                } else if ("approved".equals(status)) {
                    approvedBookingList.add(booking);
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
        String pickUpTime = jsonObject.getString("pick_up_time").substring(0, 16);

        JSONObject cafeObject = jsonObject.getJSONObject("cafe");
        int cafeId = cafeObject.getInt("id");
        String cafeName = cafeObject.getString("name");

        JSONObject coffeeObject = jsonObject.getJSONObject("coffee");
        int coffeeId = coffeeObject.getInt("id");
        String coffeeName = coffeeObject.getString("name");
        String coffeeDescription = coffeeObject.getString("description");
        String coffeeImage = coffeeObject.getString("image");

        return new BookingItem(status, name, cafeName, pickUpTime, coffeeName, coffeeDescription, coffeeImage, id, cafeId, coffeeId);
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
}