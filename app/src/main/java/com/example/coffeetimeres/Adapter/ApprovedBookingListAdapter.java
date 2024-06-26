package com.example.coffeetimeres.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
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
import com.example.coffeetimeres.Domain.ApiKeyLoader;
import com.example.coffeetimeres.Domain.BookingItem;
import com.example.coffeetimeres.Interface.BookingItemClickListener;
import com.example.coffeetimeres.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApprovedBookingListAdapter extends RecyclerView.Adapter<ApprovedBookingListAdapter.ViewHolder> implements BookingItemClickListener {
    private  String token;
    private Context mContext;
    private static Context context;
    private List<BookingItem> bookingList;

    //    public ApprovedBookingListAdapter(Context context, List<BookingItem> bookingList) {
//        this.context = context;
//        this.bookingList = bookingList;
//    }
    public ApprovedBookingListAdapter(Context context, List<BookingItem> bookingList, String token) {
        mContext = context;
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

                if ("approved".equals(status)) {
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

                    String pickUpTime = jsonObject.getString("pick_up_time").substring(0, 16).replace('T', ' ');
                    String smartphoneKey = jsonObject.getJSONObject("user").getString("smartphone_key");
                    Log.e("TAG token", smartphoneKey);
                    bookingList.add(new BookingItem(status, name, cafeName, pickUpTime, coffeeName, coffeeDescription, coffeeImage, id, cafeId, coffeeId,smartphoneKey));
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

    private void sendNotification(String to, String title, String body, String action, String message) {
        try {
            JSONObject notificationBody = new JSONObject();
            notificationBody.put("title", title);
            notificationBody.put("body", body);
            JSONObject data = new JSONObject();
            data.put("action", action);
            data.put("message", message);
            JSONObject requestBody = new JSONObject();
            requestBody.put("to", to);
            requestBody.put("notification", notificationBody);
            requestBody.put("data", data);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", requestBody,
                    new Response.Listener
                            <JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("TAG", "Уведомление успешно отправлено");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("TAG", "Ошибка при отправке уведомления: " + error.getMessage());
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String apiKeyLoader = null;
                    try {
                        apiKeyLoader = ApiKeyLoader.getApiKey();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    headers.put("Authorization", apiKeyLoader.toString());
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        BookingItem bookingItem = bookingList.get(position);

        OrderActivity orderActivity;
        // Создаем AlertDialog.Builder для настройки AlertDialog
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

        // Устанавливаем заголовок и сообщение
        alert.setTitle("Причина отклонения заказа");
        alert.setMessage("Введите причину:");

        // Создаем EditText для ввода текста
        final EditText input = new EditText(mContext);

        // Устанавливаем EditText в AlertDialog
        alert.setView(input);

        // Добавляем кнопки и обрабатываем нажатия
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Получаем введенный текст
                String reason = input.getText().toString();

                String newStatus = "reject";
                System.out.println(newStatus + " Reason: " + reason);

                notifyDataSetChanged();
                if (!reason.isEmpty()) { // Проверяем, что строка reason не пустая
                    sendNotification(bookingItem.getYour_smartphone_key_here(), "Ваш заказ отклонен", "Причина: " + reason, "show_message", "Заказ отклонен");
                    newStatus = "delete";
                }
                bookingList.remove(position);
                updateBookingStatus(bookingItem.getBookingId(), newStatus, reason);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Отмена.
            }
        });

        // Отображаем AlertDialog
        alert.show();
    }


    @Override
    public void onApproveClick(int position) {
        BookingItem bookingItem = bookingList.get(position);
        String newStatus = "delete";
        System.out.println(newStatus);

        updateBookingStatus(bookingItem.getBookingId(), newStatus,null);
        // Удаление элемента из списка и обновление отображения RecyclerView
        bookingList.remove(position);
        notifyDataSetChanged();
    }
    private void updateBookingStatus(int bookingId, String newStatus, String reason) {
        Log.e("TAG_bookingId_", String.valueOf(bookingId));
        String url = "https://losermaru.pythonanywhere.com/orders/" + bookingId;
        System.out.println("наш айди" + bookingId);
        System.out.println("ADAPTER TOKEN"  + token);
        JSONObject requestBody = new JSONObject();
        try {
            Log.e("STATUS",newStatus);
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