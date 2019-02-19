package com.example.ifty.myproducts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class ProductDetailsActivity extends AppCompatActivity implements ProductAdapter.FavouriteCheck {
    private BottomNavigationView mainBottomNav;
    private String userId;
    private String postId;
    private String companyName=null;
    private String logoThumbnail=null;
    private ImageView productImage, companyLogo, favimage, shoppingCart;
    private TextView priceTv,companyNameTv, companyNameAllProTv, productNameTv,productDescription, cartQty;
    private RecyclerView otherProductsList;
    private Toolbar proDetailsToolbar;

    private ArrayList<Product> productList;
    private FirebaseFirestore firebaseFirestore;
    private ProductAdapter productAdapter = null;
    private FirebaseAuth mAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isNewPostFirstLoad = true;
    private ScrollView scrollView;
    private String product_image;
    private String product_price;
    private String product_name;
    private MyPreferences myPreferences;
    private ArrayList<String> postIdList;
    private boolean isPostId = false;
    private String customer_id;

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
        scrollView=findViewById(R.id.scrollView);
        proDetailsToolbar=findViewById(R.id.pro_details_toolbar);
        shoppingCart=findViewById(R.id.shopping_cart);
        cartQty=findViewById(R.id.cartQty);

        setSupportActionBar(proDetailsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Product Details");

        scrollView.setFocusableInTouchMode(true);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        otherProductsList.setNestedScrollingEnabled(false);


        myPreferences = MyPreferences.getPreferences(this);

        postIdList = new ArrayList<>();
        postIdList = myPreferences.getPostIdList();

        if (postIdList!=null){
            cartQty.setText(String.valueOf(postIdList.size()));
        }

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

        if (mAuth.getCurrentUser()!=null){
            customer_id = mAuth.getCurrentUser().getUid();
        }

        firebaseFirestore.collection("Users").document(userId).collection("Posts").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot>task) {
                if (task.isSuccessful()){
                    if (Objects.requireNonNull(task.getResult()).exists()){
                        product_name = task.getResult().getString("productName");
                        product_price = task.getResult().getString("price");
                        String product_desc = task.getResult().getString("description");
                        product_image = task.getResult().getString("postImage");
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

                        if (postIdList==null){
                            postIdList = new ArrayList<>();
                        }
                        postIdList.add(postId);
                        myPreferences.setPostIdList(postIdList);
                        postIdList = new ArrayList<>();
                        postIdList=myPreferences.getPostIdList();
                        cartQty.setText(String.valueOf(postIdList.size()));

                        Toast.makeText(ProductDetailsActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.bottom_action_buy_now:

                        postIdList = new ArrayList<>();
                        postIdList.add(postId);
                        myPreferences.setPostIdList(postIdList);
                        postIdList = new ArrayList<>();
                        postIdList=myPreferences.getPostIdList();

                        Intent intent = new Intent(ProductDetailsActivity.this,ProductOrderActivity.class);
                        intent.putStringArrayListExtra("postIdList",postIdList);
                        startActivity(intent);
                        Toast.makeText(ProductDetailsActivity.this, "Buy now", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        return false;
                }
            }
        });

        productList=new ArrayList<>();
        productAdapter = new ProductAdapter(this,productList,this);
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

    public void companyProfile(View view) {
        Intent intent = new Intent(this,CompanyProfileActivity.class);
        intent.putExtra("userId",userId);
        startActivity(intent);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void checkOutCart(View view) {
        postIdList=myPreferences.getPostIdList();
        if (postIdList==null){
            Toast.makeText(this, "Your Shopping Cart is empty!", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(this,ProductOrderActivity.class);
            intent.putStringArrayListExtra("postIdList",postIdList);
            startActivity(intent);
        }
    }



   /* @Override
    public boolean favouriteCheck(final String post_id, int numb) {

        if (mAuth.getCurrentUser() != null) {
            firebaseFirestore.collection("Customers").document(userId).collection("Favourites").document(post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            isPostId = true;
                            Toast.makeText(ProductDetailsActivity.this, "yes", Toast.LENGTH_SHORT).show();

                        } else {
                            //Log.d(TAG, "No such document");
                            isPostId = false;
                            Toast.makeText(ProductDetailsActivity.this, "no", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            return isPostId;

        } else {
            return false;
        }
    }

    @Override
    public void myFave(boolean isFav, String postId, ImageView img) {

    }*/




    @Override
    public boolean favouriteCheck(final String post_id, int numb) {

        if (mAuth.getCurrentUser() != null) {
            firebaseFirestore.collection("Customers").document(customer_id).collection("Favourites").document(post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            isPostId = true;
                            //Toast.makeText(ProductDetailsActivity.this, "yes", Toast.LENGTH_SHORT).show();

                        } else {
                            //Log.d(TAG, "No such document");
                            isPostId = false;
                            //Toast.makeText(ProductDetailsActivity.this, "no", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            if (!isPostId) {

                if (numb == 1) {
                    firebaseFirestore.collection("Posts").document(post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (Objects.requireNonNull(task.getResult()).exists()) {
                                    String product_name = task.getResult().getString("productName");
                                    String price = task.getResult().getString("price");
                                    String desc = task.getResult().getString("description");
                                    String check_item = task.getResult().getString("checkItem");
                                    String user_id = task.getResult().getString("userId");
                                    String product_image = task.getResult().getString("postImage");
                                    String thumb_image = task.getResult().getString("thumbImage");

                                    Map<String, String> favMap = new HashMap<>();
                                    favMap.put("productName", Objects.requireNonNull(product_name));
                                    favMap.put("price", Objects.requireNonNull(price));
                                    favMap.put("description", Objects.requireNonNull(desc));
                                    favMap.put("postId", post_id);
                                    favMap.put("checkItem", Objects.requireNonNull(check_item));
                                    favMap.put("userId", Objects.requireNonNull(user_id));
                                    favMap.put("postImage", Objects.requireNonNull(product_image));
                                    favMap.put("thumbImage", Objects.requireNonNull(thumb_image));

                                    firebaseFirestore.collection("Customers").document(customer_id).collection("Favourites").document(post_id).set(favMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //Toast.makeText(ProductDetailsActivity.this, "Added to Favourite list!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ProductDetailsActivity.this, "ERROR : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    Toast.makeText(ProductDetailsActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ProductDetailsActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

                return false;

            } else {
                if (numb == 1) {
                    firebaseFirestore.collection("Customers").document(customer_id).collection("Favourites").document(post_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Toast.makeText(ProductDetailsActivity.this, "removed from favourite.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
                //isPostId=false;
                return true;
            }
        } else {
            return false;
        }
    }
}


