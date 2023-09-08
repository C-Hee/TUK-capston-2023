package com.example.gyroapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText idEditText, pwEditText;
    private Button loginButton, joinButton;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    private static final Pattern IS_ONLY_NUMBER = Pattern.compile("^[0-9]*?");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idEditText = findViewById(R.id.idEditText);
        pwEditText = findViewById(R.id.pwEditText);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        joinButton = findViewById(R.id.joinButton);
        joinButton.setOnClickListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("gyroapp");

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Button b = (Button) view;
        Intent intent;

        switch(b.getId()) {
            case R.id.loginButton:
                String id = idEditText.getText().toString();
                String pw = pwEditText.getText().toString();
                if(IS_ONLY_NUMBER.matcher(id).matches())
                    LoginService(id, pw);
                else
                    Toast.makeText(MainActivity.this, "숫자만 입력해 주세요", Toast.LENGTH_SHORT).show();
                break;

            case R.id.joinButton:
                intent = new Intent(this, JoinActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void LoginService(String id, String pw){

        mFirebaseAuth.signInWithEmailAndPassword(id+"@naver.com", pw).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    Intent intent = new Intent(MainActivity.this, ReceiveActivity.class);
                    String input = idEditText.getText().toString();
                    intent.putExtra("ID", input);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}