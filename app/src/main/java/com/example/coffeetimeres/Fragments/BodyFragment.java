package com.example.coffeetimeres.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;


import com.example.coffeetimeres.R;

import org.jetbrains.annotations.Nullable;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;

public class BodyFragment extends Fragment {

    private LinearLayout linearLayout;

    public BodyFragment() {
        // Обязательный пустой публичный конструктор
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Надуваем макет для этого фрагмента
        View view = inflater.inflate(R.layout.body_fragment, container, false);
        linearLayout = view.findViewById(R.id.bodyFrag); // Инициализация linearLayout
        return view;
    }

    // Метод для отображения LinearLayout
    public void showLinearLayout() {
        if (linearLayout != null) {
            linearLayout.setVisibility(View.VISIBLE);
        }
    }
}
