package com.kimiwakirei.recyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {

    private EditText name,password;
    private Button login;
    private TextView signUp;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        //Auth user:
        if (user != null){
            finish();
            startActivity(new Intent(LoginPage.this, MainActivity.class));
        }

        name = findViewById(R.id.nameField);
        password = findViewById(R.id.passwordField);

        login = findViewById(R.id.loginButton);

    }

    private void validate(String username, String password){
        login.setEnabled(false);
        login.setText("Attempting to login...");

        firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    checkEmailVerification();
                }
                else {
                    Toast.makeText(LoginPage.this,"Wrong email or password!",Toast.LENGTH_SHORT).show();
                    login.setText("Login");
                    login.setEnabled(true);
                }
            }
        });
    }


    public void loginOnClick(View view) {
        if (name.getText().toString().isEmpty() || password.getText().toString().isEmpty()){
            Toast.makeText(LoginPage.this,"One of the fields are empty :(", Toast.LENGTH_SHORT).show();
        }else {
            validate(name.getText().toString().trim(),password.getText().toString().trim());
        }

    }

    public void registrationOnClick(View view) {
        startActivity(new Intent(LoginPage.this, RegistrationActivity.class));
    }

    private void checkEmailVerification(){
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        boolean emailFlag = firebaseUser.isEmailVerified();


        if (emailFlag){
            login.setText("Login");
            login.setEnabled(true);

            finish();
            startActivity(new Intent(LoginPage.this, MainActivity.class));
        }
        else {
            login.setText("Login");
            login.setEnabled(true);
            Toast.makeText(this,"Please Verify your email",Toast.LENGTH_LONG).show();
            firebaseUser.sendEmailVerification();
            firebaseAuth.signOut();
        }
    }

    public void forgotPassword(View view) {

        startActivity(new Intent(LoginPage.this, PasswordReset.class));
    }
}
