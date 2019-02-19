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
    private String ifOrder=null;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        customerOrSeller=getIntent().getStringExtra("customerOrSeller");
        ifOrder=getIntent().getStringExtra("ifOrder");
        postId=getIntent().getStringExtra("postId");

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
                        if (ifOrder.equals("fromOrder")){
                            Intent intent = new Intent(LoginActivity.this,ProductOrderActivity.class);
                            intent.putExtra("postId",postId);
                            finish();
                            startActivity(intent);
                        }
                        else {
                            sendToMain();
                        }
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
        if (ifOrder.equals("fromOrder")){
            Intent intent = new Intent(this,RegisterActivity.class);
            intent.putExtra("ifOrder","fromOrder");
            intent.putExtra("postId",postId);
            finish();
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(this,RegisterActivity.class);
            intent.putExtra("customerOrSeller",customerOrSeller);
            intent.putExtra("ifOrder","notFromOrder");
            finish();
            startActivity(intent);}
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (ifOrder.equals("fromOrder")){
            Intent intent = new Intent(LoginActivity.this,ProductOrderActivity.class);
            intent.putExtra("postId",postId);
            finish();
            startActivity(intent);
        }
    }
}
