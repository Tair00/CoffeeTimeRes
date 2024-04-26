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


public class BodyFragment extends DialogFragment {
    private String name;
    private OnBodySetListener onBodySetListener;

    public static BodyFragment newInstance() {
        return new BodyFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.body_fragment, null);
        final EditText etUserName = dialogView.findViewById(R.id.bodyText);
        ConstraintLayout btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> onSubmitClicked(etUserName));

        builder.setView(dialogView);
        setCancelable(false);

        return builder.create();
    }

    private void onSubmitClicked(EditText etUserName) {
        name = etUserName.getText().toString();
        if (name.isEmpty()) {
            etUserName.setError(getString(R.string.error_empty_name));
        } else {
            notifyUserNameSet();
            dismiss();
        }
    }

    private void notifyUserNameSet() {
        if (onBodySetListener != null) {
            onBodySetListener.onUserNameSet(name);
        }
    }

    public void setOnBodySetListener(OnBodySetListener listener) {
        this.onBodySetListener = listener;
    }

    public interface OnBodySetListener {
        void onUserNameSet(String userName
        );
    }
}
