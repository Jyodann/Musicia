package com.kimiwakirei.recyclerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordReset extends AppCompatActivity {

    private EditText passwordEmail;
    private Button resetPassword;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        passwordEmail = findViewById(R.id.emailReset);
        resetPassword = findViewById(R.id.btnPasswordReset);
        firebaseAuth = FirebaseAuth.getInstance();

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = passwordEmail.getText().toString().trim();

                resetPassword.setEnabled(false);
                resetPassword.setText("Attempting to reset...");

                if (userEmail.isEmpty()){
                    Toast.makeText(PasswordReset.this,"Enter an Email", Toast.LENGTH_LONG).show();
                    resetPassword.setText("Reset Password");
                    resetPassword.setEnabled(true);
                }else {
                    firebaseAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isComplete()){
                                Toast.makeText(PasswordReset.this,"An email has been sent! Follow the instructions to reset your password :)", Toast.LENGTH_LONG).show();
                                finish();
                                startActivity(new Intent(PasswordReset.this, LoginPage.class));
                                resetPassword.setText("Reset Password");
                                resetPassword.setEnabled(true);
                            }else {
                                Toast.makeText(PasswordReset.this,"Failed to reset, please try again", Toast.LENGTH_LONG).show();
                                resetPassword.setText("Reset Password");
                                resetPassword.setEnabled(true);
                            }
                        }
                    });
                }
            }
        });
    }
}
