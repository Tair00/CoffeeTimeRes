package com.example.coffeetimeres.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.coffeetimeres.Activity.OrderActivity;
import com.example.coffeetimeres.Domain.BookingItem;
import com.example.coffeetimeres.Interface.BookingItemClickListener;
import com.example.coffeetimeres.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingListAdapter extends RecyclerView.Adapter<BookingListAdapter.ViewHolder> implements BookingItemClickListener {
    private  String token;
    private Context context;
    private List<BookingItem> bookingList;

    public BookingListAdapter(Context context, List<BookingItem> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }
    public BookingListAdapter(Context context, List<BookingItem> bookingList, String token) {
        this.context = context;
        this.bookingList = bookingList;
        this.token = token;
    }
    public void fetchData() {
        String serverUrl = "https://losermaru.pythonanywhere.com/reservation";
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, serverUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Ошибка при получении данных: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        System.out.println("Ошибка при получении данных: " + error.getMessage());
                    }
                });
        queue.add(request);
    }

    private void parseResponse(JSONObject response) {
        try {
            JSONArray jsonArray = response.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String status = jsonObject.getString("status");
                if (status == "waiting") {
                    String time = jsonObject.getString("time");
                    String name = jsonObject.getString("name");
                    bookingList.add(new BookingItem(status , time, name));
                }
            }
            notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка при разборе ответа", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.coffee_item, parent, false);
        return new ViewHolder(view, this); // Передача текущего адаптера в качестве слушателя
    }


    @Override
    public int getItemCount() {
        int count = 0;
        for (BookingItem item : bookingList) {
            String status = item.getStatus();
            if ("waiting".equals(status)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int count = 0;
        for (BookingItem item : bookingList) {
            String status = item.getStatus();
            if ("waiting".equals(status)) {
                if (count == position) {
                    holder.timeTextView.setText(item.getTime());
                    holder.nameTextView.setText(item.getName());

                    break;
                }
                count++;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView timeTextView;
        public TextView nameTextView;
        ConstraintLayout btnReject, btnApprove;
        BookingItemClickListener listener;

        public ViewHolder(View itemView, BookingItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            timeTextView = itemView.findViewById(R.id.time);
            nameTextView = itemView.findViewById(R.id.coffee_name);
            btnReject = itemView.findViewById(R.id.crossBtn);
            btnApprove = itemView.findViewById(R.id.check_markBtn);

            btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onRejectClick(position);
                    }
                }
            });

            btnApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onApproveClick(position);
                    }
                }
            });
        }
    }


    @Override
    public void onRejectClick(int position) {
        // Ваш код обработки нажатия на кнопку отклонения заказа
        BookingItem bookingItem = bookingList.get(position);
        String newStatus = "rejected";
        System.out.println(newStatus);
        updateBookingStatus(bookingItem.getId(), newStatus);
    }

    @Override
    public void onApproveClick(int position) {
        // Ваш код обработки нажатия на кнопку подтверждения заказа
        BookingItem bookingItem = bookingList.get(position);
        String newStatus = "approved";
        System.out.println(newStatus);
        updateBookingStatus(bookingItem.getId(), newStatus);
    }

    private void updateBookingStatus(int bookingId, String newStatus) {
        String url = "https://losermaru.pythonanywhere.com/orders/" + bookingId;
        System.out.println("наш айди" + bookingId);
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("status", newStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(" Goood");
                        // Обработка успешного ответа
                        // Например, обновление UI или другие действия
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Обработка ошибки запроса
                        Log.e("MyTag", "Error: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                System.out.println("ADAPTER TOKEN"  + token);
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }
}