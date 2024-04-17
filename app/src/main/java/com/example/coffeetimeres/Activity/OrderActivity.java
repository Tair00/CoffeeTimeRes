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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        String token = getIntent().getStringExtra("access_token");
        recyclerView = findViewById(R.id.view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        adapter = new BookingListAdapter(OrderActivity.this, bookingList, token);

        recyclerView.setAdapter(adapter);

        // Выполните GET-запрос к серверу для получения данных
        executeGetRequest();
    }

    private void executeGetRequest() {
        String token = getIntent().getStringExtra("access_token"); // Получение значения токена

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://losermaru.pythonanywhere.com/reservation";

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
            boolean hasNewApprovedItems = false;

            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                Integer id = jsonObject.getInt("restaurant_id");
                String status = jsonObject.getString("status");

                if ("waiting".equals(status)) {
                    String time = jsonObject.getString("time");
                    String name = jsonObject.getString("name");

                    BookingItem booking = new BookingItem(status,  time, name, id);

                    if (!bookingList.contains(booking)) {
                        bookingList.add(booking);
                        hasNewApprovedItems = true;
                    }

                    fetchRestaurantName(id, booking);
                }
            }

            adapter.notifyDataSetChanged();

            if (hasNewApprovedItems) {

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MyTag", "Ошибка при разборе ответа: " + e.getMessage());
            Toast.makeText(OrderActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchRestaurantName(int id, BookingItem booking) {
        String url = "https://losermaru.pythonanywhere.com/restaurant/" + id;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String name = response.getString("name");
                            booking.setName(name);
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
