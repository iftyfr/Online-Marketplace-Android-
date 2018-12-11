package com.example.ifty.myproducts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmailText, loginPassText;
    Button loginBtn, loginRegBtn;
    ProgressBar loginProgressBar;
    FirebaseAuth mAuth;
    private String customerOrSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); mAuth=FirebaseAuth.getInstance();

        customerOrSeller=getIntent().getStringExtra("customerOrSeller");

        loginEmailText= findViewById(R.id.login_email);
        loginPassText= findViewById(R.id.login_password);
        loginBtn= findViewById(R.id.login_button);
        loginRegBtn= findViewById(R.id.create_account);
        loginProgressBar=findViewById(R.id.login_progressBar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent intent = new Intent(this,MainActivity.class);
        finish();
        startActivity(intent);

    }

    public void loginToAccount(View view) {
        String email=loginEmailText.getText().toString();
        String password=loginPassText.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            loginProgressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        sendToMain();
                    }
                    else {
                        String errorMessage=task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, "Error : "+errorMessage, Toast.LENGTH_LONG).show();
                    }
                    loginProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
        else {
            Toast.makeText(this, "Please fill up all The fields!", Toast.LENGTH_LONG).show();
        }
    }

    public void createNewAccount(View view) {
        Intent intent = new Intent(this,RegisterActivity.class);
        intent.putExtra("customerOrSeller",customerOrSeller);
        finish();
        startActivity(intent);
    }
}
