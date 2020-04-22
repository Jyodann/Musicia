package com.kimiwakirei.recyclerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private EditText username, userPassword, userEmail;
    private Button registerButton;
    private TextView userLogin;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        firebaseAuth = FirebaseAuth.getInstance();

        UIViews();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerButton.setEnabled(false);
                registerButton.setText("Attempting to Register..");
            if (validate()){
                String user_email = userEmail.getText().toString().trim();
                String user_password = userPassword.getText().toString().trim();

                firebaseAuth.createUserWithEmailAndPassword(user_email,user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            registerButton.setEnabled(true);
                            registerButton.setText("Register");
                            sendEmailVeri();
                            finish();
                        }else {
                            Toast.makeText(RegistrationActivity.this,"Registration Failed. :( Please try again",Toast.LENGTH_LONG).show();
                            registerButton.setEnabled(true);
                            registerButton.setText("Register");
                        }
                    }
                });
            }
            }
        });

        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginPage.class);
                startActivity(intent);
            }
        });
    }

    private boolean validate() {
        String name = username.getText().toString();
        String password = userPassword.getText().toString();
        String email = userEmail.getText().toString();

        if (name.isEmpty() || password.isEmpty() || email.isEmpty()){
            Toast.makeText(this,"One or more of the fields are empty",Toast.LENGTH_LONG).show();
            registerButton.setEnabled(true);
            registerButton.setText("Register");
            return false;
        }
        else {
            return true;
        }
    }

    private void UIViews(){
        username = findViewById(R.id.usernameRegister);
        userPassword = findViewById(R.id.passwordRegister);
        userEmail = findViewById(R.id.emailRegister);

        registerButton = findViewById(R.id.registerButton);
        userLogin = findViewById(R.id.signedUpView);
    }

    private void sendEmailVeri(){
        final FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        registerButton.setEnabled(true);
                        registerButton.setText("Register");
                        sendUserData();
                        Toast.makeText(RegistrationActivity.this,"Registered Successfully. :) A verification email is on its way!",Toast.LENGTH_LONG).show();
                        firebaseAuth.signOut();
                        finish();

                    }else {
                        Toast.makeText(RegistrationActivity.this,"Registration failed. :( Please try again.",Toast.LENGTH_LONG).show();
                        registerButton.setEnabled(true);
                        registerButton.setText("Register");
                    }

                }
            });
        }
    }

    private void sendUserData(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        //gets user, and data:
        DatabaseReference myRef = firebaseDatabase.getReference(firebaseAuth.getUid());
        UserProfile userProfile = new UserProfile(username.getText().toString(),userEmail.getText().toString());
        myRef.setValue(userProfile);
    }
}
