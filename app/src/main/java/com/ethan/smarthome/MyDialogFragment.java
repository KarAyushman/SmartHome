package com.ethan.smarthome;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MyDialogFragment
        extends DialogFragment
        implements NumberPicker.OnValueChangeListener{

    public interface NumberPick {
        void onFinishNumberPick(String inputText);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =  inflater.inflate(R.layout.wheelpick, container, false);
        NumberPicker number = view.findViewById(R.id.number);
        Button btn = view.findViewById(R.id.btn);
        number.setMaxValue(60);
        number.setMinValue(0);
        number.setFormatter(new NumberPicker.Formatter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });
        number.setOnValueChangedListener(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPick activity = (NumberPick) getActivity();
                assert activity != null;
                activity.onFinishNumberPick(String.valueOf(number.getValue()));
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }
}


