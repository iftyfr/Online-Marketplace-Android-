package com.example.ifty.myproducts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

public class ProductDetailsActivity extends AppCompatActivity {
    private BottomNavigationView mainBottomNav;
    private String userId;
    private String postId;
    private String companyName=null;
    private String logoThumbnail=null;
    private ImageView productImage, companyLogo, favimage;
    private TextView priceTv,companyNameTv, companyNameAllProTv, productNameTv,productDescription;
    private RecyclerView otherProductsList;

    private ArrayList<Product> productList;
    private FirebaseFirestore firebaseFirestore;
    private ProductAdapter productAdapter = null;
    private FirebaseAuth mAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isNewPostFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        userId=getIntent().getStringExtra("userId");
        postId=getIntent().getStringExtra("postId");
        companyName=getIntent().getStringExtra("companyName");
        logoThumbnail=getIntent().getStringExtra("logoThumbnail");

        productImage = findViewById(R.id.pro_details_image);
        companyLogo = findViewById(R.id.pro_details_comp_logo);
        favimage = findViewById(R.id.pro_details_favourite);
        priceTv = findViewById(R.id.pro_details_price);
        companyNameTv = findViewById(R.id.pro_details_comp_name);
        productNameTv = findViewById(R.id.pro_details_name);
        productDescription = findViewById(R.id.product_description);
        companyNameAllProTv = findViewById(R.id.all_pro_company_name);
        otherProductsList = findViewById(R.id.other_products);

        otherProductsList.setNestedScrollingEnabled(false);

        if (companyName != null && logoThumbnail != null){
            companyNameTv.setText(companyName);
            companyNameAllProTv.setText(companyName);
            Glide.with(this).load(logoThumbnail).into(companyLogo);
        }
        else {
            companyNameTv.setText("User Name");
            companyNameAllProTv.setText("");
            Glide.with(this).load(R.drawable.company_logo).into(companyLogo);
        }

        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();

        firebaseFirestore.collection("Users").document(userId).collection("Posts").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot>task) {
                if (task.isSuccessful()){
                    if (Objects.requireNonNull(task.getResult()).exists()){
                        String product_name = task.getResult().getString("productName");
                        String product_price = task.getResult().getString("price");
                        String product_desc = task.getResult().getString("description");
                        String product_image = task.getResult().getString("postImage");

                        productNameTv.setText(product_name);
                        priceTv.setText(product_price);
                        productDescription.setText(product_desc);
                        Glide.with(ProductDetailsActivity.this).load(product_image).into(productImage);
                    }
                    else {
                        Toast.makeText(ProductDetailsActivity.this, "Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(ProductDetailsActivity.this, "Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mainBottomNav=findViewById(R.id.mainBottomNavView);

        mainBottomNav.setItemIconTintList(null);
        mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.bottom_action_add_cart:
                        Toast.makeText(ProductDetailsActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.bottom_action_buy_now:
                        Toast.makeText(ProductDetailsActivity.this, "Buy now", Toast.LENGTH_SHORT).show();
                        return true;


                    default:
                        return false;
                }
            }
        });

        productList=new ArrayList<>();
        productAdapter = new ProductAdapter(this,productList);
        otherProductsList.setLayoutManager(new GridLayoutManager(this,2));
        otherProductsList.setAdapter(productAdapter);


        otherProductsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                if (reachedBottom){
                    Toast.makeText(ProductDetailsActivity.this, "Reached"+lastVisible.getString("desc"), Toast.LENGTH_SHORT).show();
                    loadMorePost();
                }
            }
        });

        firebaseFirestore=FirebaseFirestore.getInstance();
        Query firstQuery = firebaseFirestore.collection("Users").document(userId).collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
        firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                    if (isNewPostFirstLoad){
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() -1);
                    }
                    for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                        if (doc.getType()==DocumentChange.Type.ADDED){
                            String blogPostId = doc.getDocument().getId();
                            Product product = doc.getDocument().toObject(Product.class).withId(blogPostId);

                            if (isNewPostFirstLoad){
                                productList.add(product);
                            }
                            else {
                                productList.add(0,product);
                            }

                            productAdapter.notifyDataSetChanged();
                        }
                    }
                    isNewPostFirstLoad=false;
                }
            }
        });
    }
    public void loadMorePost(){
        Query nextQuery = firebaseFirestore.collection("Users").document(userId).collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null){
                    if(!queryDocumentSnapshots.isEmpty()){
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() -1);
                        for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                            if (doc.getType()==DocumentChange.Type.ADDED){
                                String blogPostId = doc.getDocument().getId();
                                Product product = doc.getDocument().toObject(Product.class).withId(blogPostId);
                                productList.add(product);

                                productAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ProductDetailsActivity.this,MainActivity.class);
        finish();
        startActivity(intent);
    }
}


