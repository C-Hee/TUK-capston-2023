package com.example.gyroapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class JoinActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText idEditText, pwEditText;
    private Button joinButton;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    private static final Pattern IS_ONLY_NUMBER = Pattern.compile("^[0-9]*?");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        idEditText = findViewById(R.id.idEditText);
        pwEditText = findViewById(R.id.pwEditText);

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

        Log.e("Telechips","R.id.joinButton : " + R.id.joinButton);

        switch(b.getId()) {
            case R.id.joinButton:
                String id = idEditText.getText().toString();
                String pw = pwEditText.getText().toString();

                JoinService(id, pw);

//                if(IS_ONLY_NUMBER.matcher(id).matches())
//                    JoinService(id, pw);
//                else
//                    Toast.makeText(JoinActivity.this, "아이디에 숫자만 입력해 주세요", Toast.LENGTH_SHORT).show();

                break;

        }
    }

    public void JoinService(String id, String pw){

        mFirebaseAuth.createUserWithEmailAndPassword(id+"@naver.com", pw).addOnCompleteListener(JoinActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                    UserAccount account = new UserAccount();

                    account.setIdToken(firebaseUser.getUid());
                    account.setID(firebaseUser.getEmail());
                    account.setPW(pw);

                    mDatabaseReference.child("UserAccount").child(firebaseUser.getUid()).setValue(account);

                    Toast.makeText(JoinActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(JoinActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Log.e("Telechips","Error : " + task.getException().getMessage());
                    Toast.makeText(JoinActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}