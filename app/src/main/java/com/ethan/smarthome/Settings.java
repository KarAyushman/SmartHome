package com.ethan.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.util.ArrayList;
import java.util.List;

public class Settings extends AppCompatActivity implements View.OnClickListener, MyDialogFragment.NumberPick {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener((View.OnClickListener) this);
    }

    @Override
    public void onClick(View v) {
        MyDialogFragment myDialogFragment = new MyDialogFragment();
        myDialogFragment.show(getSupportFragmentManager(), "MyFragment");
    }

    @Override
    public void onFinishNumberPick(String inputText) {
        Button btn = findViewById(R.id.btn);
        btn.setText(inputText);
    }
}


