package com.example.coffeetimeres.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.example.coffeetimeres.Domain.BookingItem;
import com.example.coffeetimeres.Interface.BookingItemClickListener;
import com.example.coffeetimeres.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApprovedBookingListAdapter extends RecyclerView.Adapter<ApprovedBookingListAdapter.ViewHolder> implements BookingItemClickListener {
    private  String token;
    private static Context context;
    private List<BookingItem> bookingList;

    public ApprovedBookingListAdapter(Context context, List<BookingItem> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }
    public ApprovedBookingListAdapter(Context context, List<BookingItem> bookingList, String token) {
        this.context = context;
        this.bookingList = bookingList;
        this.token = token;
    }
    public void fetchData() {
        String serverUrl = "https://losermaru.pythonanywhere.com/orders/";
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
            boolean hasNewAcceptedItems = false;

            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(String.valueOf(i));
                int id = jsonObject.getInt("id");
                String status = jsonObject.getString("status");

                if ("waiting".equals(status)) {
                    JSONObject userObject = jsonObject.getJSONObject("user");
                    String name = userObject.getString("name");

                    JSONObject cafeObject = jsonObject.getJSONObject("cafe");
                    int cafeId = cafeObject.getInt("id");
                    String cafeName = cafeObject.getString("name");

                    JSONObject coffeeObject = jsonObject.getJSONObject("coffee");
                    int coffeeId = coffeeObject.getInt("id");
                    String coffeeName = coffeeObject.getString("name");
                    String coffeeDescription = coffeeObject.getString("description");
                    String coffeeImage = coffeeObject.getString("image");

                    String pickUpTime = jsonObject.getString("pick_up_time").substring(0, 16);
                    bookingList.add(new BookingItem(status, name, cafeName, pickUpTime, coffeeName, coffeeDescription, coffeeImage, id, cafeId, coffeeId));
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
        View view = LayoutInflater.from(context).inflate(R.layout.coffee_approved_item, parent, false);
        return new ViewHolder(view, this); // Передача текущего адаптера в качестве слушателя
    }


    @Override
    public int getItemCount() {
        int count = 0;
        for (BookingItem item : bookingList) {
            String status = item.getStatus();
            if ("approved".equals(status)) {
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
            if ("approved".equals(status)) {
                if (count == position) {
                    holder.timeTextView.setText(item.getPickUpTime());
                    holder.nameTextView.setText(item.getCoffeeName());
                    holder.name.setText(item.getUserName());
                    break;
                }
                count++;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView timeTextView;
        public TextView nameTextView ,name;
        ConstraintLayout btnReject, btnApprove;
        BookingItemClickListener listener;

        public ViewHolder(View itemView, BookingItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            timeTextView = itemView.findViewById(R.id.time);
            nameTextView = itemView.findViewById(R.id.coffee_name);
            name = itemView.findViewById(R.id.name);
            btnReject = itemView.findViewById(R.id.crossBtn);
            btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_up_down);
                        anim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                // После окончания анимации удаляем элемент из списка
                                listener.onRejectClick(position);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                        v.startAnimation(anim);
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

        updateBookingStatus(bookingItem.getBookingId(), newStatus);

        // Удаление элемента из списка и обновление отображения RecyclerView
        bookingList.remove(position);
        notifyDataSetChanged();
    }
    @Override
    public void onApproveClick(int position) {
        // Ваш код обработки нажатия на кнопку подтверждения заказа
        BookingItem bookingItem = bookingList.get(position);
        String newStatus = "approved";
        System.out.println(newStatus);

        updateBookingStatus(bookingItem.getBookingId(), newStatus);

        // Удаление элемента из списка и обновление отображения RecyclerView
        bookingList.remove(position);
        notifyDataSetChanged();
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
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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