package com.example.coffeetimeres.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.example.coffeetimeres.Adapter.BookingListAdapter;
import com.example.coffeetimeres.Adapter.BookingListApprovedAdapter;
import com.example.coffeetimeres.Domain.BookingItem;
import com.example.coffeetimeres.R;
import com.squareup.picasso.Picasso;

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
    private BookingListAdapter secondAdapter;
    private RecyclerView secondRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        String token = getIntent().getStringExtra("access_token");
        recyclerView = findViewById(R.id.firstRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        secondRecyclerView = findViewById(R.id.secondRecyclerView);
        secondAdapter = new BookingListAdapter(this, new ArrayList<>());
        secondRecyclerView.setAdapter(secondAdapter);
        bookingList = new ArrayList<>();
        adapter = new BookingListAdapter(OrderActivity.this, bookingList, token);
        recyclerView.setAdapter(adapter);
        executeGetRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        executeGetRequest();
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
                        parseResponseSecond(response);
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
            boolean hasNewAcceptedItems = false;

            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                int id = jsonObject.getInt("id");
                String status = jsonObject.getString("status");

                if ("waiting".equals(status)) {

                    String name = jsonObject.getString("name");

                    JSONObject cafeObject = jsonObject.getJSONObject("cafe");
                    int cafeId = cafeObject.getInt("id");
                    String cafeName = cafeObject.getString("name");

                    JSONObject coffeeObject = jsonObject.getJSONObject("coffee");
                    int coffeeId = coffeeObject.getInt("id");
                    String coffeeName = coffeeObject.getString("name");
                    String coffeeDescription = coffeeObject.getString("description");
                    String coffeeImage = coffeeObject.getString("image");

                    String pickUpTime = jsonObject.getString("pick_up_time").substring(0, 16);

                    BookingItem booking = new BookingItem(status, name, cafeName, pickUpTime, coffeeName, coffeeDescription, coffeeImage, id, cafeId, coffeeId);

                    if (!bookingList.contains(booking)) {
                        bookingList.add(booking);
                        hasNewAcceptedItems = true;
                    }
                }
            }

            adapter.notifyDataSetChanged();

            if (hasNewAcceptedItems) {
                // Если есть новые принятые элементы, выполните необходимые действия
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MyTag", "Ошибка при разборе ответа: " + e.getMessage());
            Toast.makeText(OrderActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
        }
    }
    private void parseResponseSecond(JSONArray response) {
        try {
            boolean hasNewAcceptedItems = false;

            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                int id = jsonObject.getInt("id");
                String status = jsonObject.getString("status");

                if ("approved".equals(status)) {

                    String name = jsonObject.getString("name");

                    JSONObject cafeObject = jsonObject.getJSONObject("cafe");
                    int cafeId = cafeObject.getInt("id");
                    String cafeName = cafeObject.getString("name");

                    JSONObject coffeeObject = jsonObject.getJSONObject("coffee");
                    int coffeeId = coffeeObject.getInt("id");
                    String coffeeName = coffeeObject.getString("name");
                    String coffeeDescription = coffeeObject.getString("description");
                    String coffeeImage = coffeeObject.getString("image");

                    String pickUpTime = jsonObject.getString("pick_up_time").substring(0, 16);

                    BookingItem booking = new BookingItem(status, name, cafeName, pickUpTime, coffeeName, coffeeDescription, coffeeImage, id, cafeId, coffeeId);

                    if (!bookingList.contains(booking)) {
                        bookingList.add(booking);
                        hasNewAcceptedItems = true;
                    }
                }
            }

            secondAdapter.notifyDataSetChanged();

            if (hasNewAcceptedItems) {
                // Если есть новые принятые элементы, выполните необходимые действия
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MyTag", "Ошибка при разборе ответа: " + e.getMessage());
            Toast.makeText(OrderActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
        }
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
