package com.example.ifty.myproducts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class NewAdPostActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ArrayList<Item> arrayList;
    private Toolbar newPostToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ad_post);

        newPostToolbar=findViewById(R.id.newPostToolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
    }


    public void electItem(View view) {
        Intent intent = new Intent(this, ItemsActivity.class);
        intent.putExtra("type", "Electronics");
        startActivity(intent);
    }

    public void carItem(View view) {
        Intent intent = new Intent(this, ItemsActivity.class);
        intent.putExtra("type", "Cars & Vehicles");
        startActivity(intent);
    }

    public void propertyItem(View view) {
        Intent intent = new Intent(this, ItemsActivity.class);
        intent.putExtra("type", "Property");
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
