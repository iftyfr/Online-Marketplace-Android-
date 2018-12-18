package com.example.ifty.myproducts;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FloatingTextButton addNewBtn;
    private FirebaseFirestore firebaseFirestore;
    private Toolbar mToolbar;
    private DrawerLayout drawer;
    private HomeFragment homeFragment;
    private ImageView navImage;
    private TextView navText;
    String user_id;
    Dialog loginDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        drawer=findViewById(R.id.drawer);
        mToolbar=findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);



        loginDialog= new Dialog(this);

        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();


        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        navImage=hView.findViewById(R.id.nav_image);
        navText=hView.findViewById(R.id.nav_text);
        addNewBtn=findViewById(R.id.postAddBtn);

        if (mAuth.getCurrentUser()!=null){
            user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            firebaseFirestore.collection("Customers").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (task.getResult().exists()){
                            String customer_or_seller = task.getResult().getString("customerOrSeller");
                            if (customer_or_seller.equals("seller")){
                                addNewBtn.setVisibility(View.VISIBLE);
                            }
                            String user_name = task.getResult().getString("userName");
                            String customer_Thumbnail = task.getResult().getString("customerThumbnail");
                               navText.setText(user_name);
                            RequestOptions placeHolderRequest = new RequestOptions();
                            placeHolderRequest.placeholder(R.drawable.default_user_image);
                            Glide.with(MainActivity.this).applyDefaultRequestOptions(placeHolderRequest).load(customer_Thumbnail).into(navImage);
                        }
                    }
                    else {
                        String error = task.getException().toString();
                        Toast.makeText(MainActivity.this, "RETRIEVE ERROR : "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        addNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewPost();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,mToolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        homeFragment = new HomeFragment();
        replaceFragment(homeFragment);

    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    public void addNewPost() {
        if (mAuth.getCurrentUser()==null){
            Intent intent = new Intent(this,LoginActivity.class);
            intent.putExtra("customerOrSeller","seller");
            finish();
            startActivity(intent);
        }
        else {
            firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (!task.getResult().exists()){
                            Intent intent = new Intent(MainActivity.this,SetupActivity.class);
                            intent.putExtra("intentChecker","first_time");
                            startActivity(intent);
                        }
                        else {
                            startActivity(new Intent(MainActivity.this,NewAdPostActivity.class));
                        }
                    }
                    else {
                        String error = task.getException().toString();
                        Toast.makeText(MainActivity.this, "RETRIEVE ERROR : "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    
    public void custProfileImage(View view) {
        if (mAuth.getCurrentUser()==null){
            loginPopupDialog("sign_in");
        }
        else {
            firebaseFirestore.collection("Customers").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (!task.getResult().exists()){
                            loginPopupDialog("sign_up");
                        }
                        else {
                            String customer_or_seller = task.getResult().getString("customerOrSeller");
                            if (customer_or_seller.equals("seller")){
                                Intent intent = new Intent(MainActivity.this,CompanyProfileActivity.class);
                                intent.putExtra("intentChecker","not_first_time");
                                intent.putExtra("userId",user_id);
                                startActivity(intent);
                            }
                            else if (customer_or_seller.equals("customer")){
                                Intent intent = new Intent(MainActivity.this,CustomerSetupActivity.class);
                                intent.putExtra("intentChecker","not_first_time");
                                startActivity(intent);
                            }

                        }
                    }
                    else {
                        String error = task.getException().toString();
                        Toast.makeText(MainActivity.this, "RETRIEVE ERROR : "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void navHome(MenuItem item) {
        Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
    }

    public void myCompanyProfile(MenuItem item) {
        Toast.makeText(this, "My Company", Toast.LENGTH_SHORT).show();
    }

    public void favouriteProducts(MenuItem item) {
        Toast.makeText(this, "Favourite..", Toast.LENGTH_SHORT).show();
    }

    public void logoutCustomer(MenuItem item) {
        mAuth.signOut(); Intent intent = new Intent(MainActivity.this,MainActivity.class);
        Toast.makeText(this, "You just logged out.", Toast.LENGTH_SHORT).show();
        finish();
        startActivity(intent);
    }



    public void loginPopupDialog(final String sign_in_or_up){
        Button customer;
        Button seller;
        loginDialog.setContentView(R.layout.login_popup);
        customer=loginDialog.findViewById(R.id.customer_login);
        seller=loginDialog.findViewById(R.id.seller_login);

        if (sign_in_or_up.equals("sign_in")){
            customer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    intent.putExtra("customerOrSeller","customer");
                    finish();
                    startActivity(intent);
                }
            });

            seller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    intent.putExtra("customerOrSeller","seller");
                    finish();
                    startActivity(intent);
                }
            });
        }

        else if (sign_in_or_up.equals("sign_iup")){
            customer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,CustomerSetupActivity.class);
                    intent.putExtra("customerOrSeller","customer");
                    finish();
                    startActivity(intent);
                }
            });

            seller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,SetupActivity.class);
                    intent.putExtra("customerOrSeller","seller");
                    finish();
                    startActivity(intent);
                }
            });
        }

        Objects.requireNonNull(loginDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loginDialog.show();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }
}
