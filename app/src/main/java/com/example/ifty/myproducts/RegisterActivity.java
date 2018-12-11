package com.example.ifty.myproducts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextView emailText, passwordText, confirmPassText;
    private Button regBtn, existingAccBtn;
    private ProgressBar regProgressBar;
    private FirebaseAuth mAuth;
    private String customerOrSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();

        customerOrSeller=getIntent().getStringExtra("customerOrSeller");

        emailText=findViewById(R.id.reg_email);
        passwordText=findViewById(R.id.reg_password);
        confirmPassText=findViewById(R.id.reg_confirm_pass);
        regBtn=findViewById(R.id.reg_button);
        existingAccBtn=findViewById(R.id.existing_account);
        regProgressBar=findViewById(R.id.reg_progressBar);

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

    public void createAccount(View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String confirmPass = confirmPassText.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPass)){
            if (password.equals(confirmPass)){
                regProgressBar.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            if (customerOrSeller.equals("seller")){
                                Intent intent = new Intent(RegisterActivity.this,SetupActivity.class);
                                intent.putExtra("intentChecker","first_time");
                                startActivity(intent);
                                finish();
                            }
                            else if (customerOrSeller.equals("customer")){
                                Intent intent = new Intent(RegisterActivity.this,CustomerSetupActivity.class);
                                intent.putExtra("intentChecker","first_time");
                                finish();
                                startActivity(intent);
                            }
                        }
                        else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "Error : "+errorMessage, Toast.LENGTH_SHORT).show();
                        }
                        regProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
            else {
                Toast.makeText(this, "Password did not match", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Please fill up all The field!", Toast.LENGTH_LONG).show();
        }
    }

    public void existingAccount(View view) {
        Intent intent = new Intent(this,LoginActivity.class);
        finish();
        startActivity(intent);
    }
}
