package com.ethan.smarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import adil.dev.lib.materialnumberpicker.dialog.GenderPickerDialog;

public class Profile extends AppCompatActivity
        implements View.OnClickListener{
    private TextView name, gender, location;
    private CardView submit;
    private ImageView avatar;
    private static final String filename = "data.json";


    database db = new database();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        name = findViewById(R.id.name);
        gender = findViewById(R.id.gender);
        location = findViewById(R.id.location);
        avatar = findViewById(R.id.avatar);
        gender.setOnClickListener(this);

        db.init(this, "data");
        String ans = db.readFile();
        if (!ans.equals("{}")){
            try {
                JSONObject jb = new JSONObject(ans);
                name.setText(jb.get("Name").toString());
                gender.setText(jb.get("Gender").toString());
                location.setText(jb.get("Location").toString());
                if(gender.getText().equals("Male")){
                    avatar.setImageResource(R.drawable.man);
                }
                else{
                    avatar.setImageResource(R.drawable.woman);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.gender){
            GenderPickerDialog dialog=new GenderPickerDialog(this);
            dialog.setOnSelectingGender(new GenderPickerDialog.OnGenderSelectListener() {
                @Override
                public void onSelectingGender(String value) {
                    gender.setTextColor(Color.WHITE);
                    gender.setText(value);
                }
            });
            dialog.show();
        }
    }

    public void submit(View view){
        try {
            db.init(this, "data");
            JSONObject jb = new JSONObject();
            jb.put("Name", name.getText());
            jb.put("Gender", gender.getText());
            jb.put("Location", location.getText());
            if(gender.equals("Male")){
                avatar.setImageResource(R.drawable.man);
            }
            else if(gender.equals("Female")){
                avatar.setImageResource(R.drawable.woman);
            }
            else{
                avatar.setImageResource(R.drawable.profile);
            }
            db.writeFile(jb.toString());
        } catch (NullPointerException | JSONException ignored){}
        finish();
    }

}