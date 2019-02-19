package com.example.ifty.myproducts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class CompanyProfileActivity extends AppCompatActivity implements ProductAdapter.FavouriteCheck {

    private ImageView profileImage;
    private TextView companyName;
    private TextView email;
    private TextView ratings;
    private TextView address;
    private TextView contuct;
    private ImageButton editProfile;
    private RecyclerView profileRecyclerProducts;
    private ScrollView scrollView;
    private Toolbar companyProToolbar;

    private String userId;
    private ArrayList<Product> productList;
    private FirebaseFirestore firebaseFirestore;
    private ProductAdapter productAdapter = null;
    private FirebaseAuth mAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isNewPostFirstLoad = true;
    private String currentUserId;
    private boolean isPostId = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_profile);

        profileImage=findViewById(R.id.profile_image);
        companyName=findViewById(R.id.profile_company_name);
        email=findViewById(R.id.profile_email);
        ratings=findViewById(R.id.profile_company_rating);
        address=findViewById(R.id.profile_company_address);
        contuct=findViewById(R.id.profile_mobile);
        editProfile=findViewById(R.id.profile_edit);
        profileRecyclerProducts=findViewById(R.id.profile_recycler);
        scrollView=findViewById(R.id.scrollViewProfile);
        companyProToolbar=findViewById(R.id.companyProToolbar);

        setSupportActionBar(companyProToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        scrollView.setFocusableInTouchMode(true);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        profileRecyclerProducts.setNestedScrollingEnabled(false);


        userId=getIntent().getStringExtra("userId");
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null){
            currentUserId=mAuth.getUid();
            if (currentUserId.equals(userId)){
                editProfile.setVisibility(View.VISIBLE);
            }
        }

        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        Toast.makeText(CompanyProfileActivity.this, "exist", Toast.LENGTH_SHORT).show();

                        String company_name = task.getResult().getString("companyName");
                        String mobile_number = task.getResult().getString("mobileNumber");
                        String company_address = task.getResult().getString("companyAddress");
                        String company_email = task.getResult().getString("email");
                        String company_logo = task.getResult().getString("companyLogo");
                        String company_thumbnail = task.getResult().getString("logoThumbnail");

                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.company_logo);

                        Glide.with(CompanyProfileActivity.this).applyDefaultRequestOptions(placeHolderRequest).load(company_logo).into(profileImage);

                        companyName.setText(company_name);
                        email.setText(company_email);
                        address.setText(company_address);
                        contuct.setText(mobile_number);

                    }
                    else {
                        Intent intent = new Intent(CompanyProfileActivity.this,SetupActivity.class);
                        intent.putExtra("intentChecker","first_time");
                        finish();
                        startActivity(intent);
                    }
                }
                else {
                    String error = task.getException().toString();
                    Toast.makeText(CompanyProfileActivity.this, "RETRIEVE ERROR : "+error, Toast.LENGTH_SHORT).show();
                }
            }
        });


        productList=new ArrayList<>();
        productAdapter = new ProductAdapter(this,productList,this);
        profileRecyclerProducts.setLayoutManager(new GridLayoutManager(this,2));
        profileRecyclerProducts.setAdapter(productAdapter);


        profileRecyclerProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                if (reachedBottom){
                    Toast.makeText(CompanyProfileActivity.this, "Reached"+lastVisible.getString("desc"), Toast.LENGTH_SHORT).show();
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

    public void editProfile(View view) {
        Intent intent = new Intent(CompanyProfileActivity.this,SetupActivity.class);
        intent.putExtra("intentChecker","not_first_time");
        startActivity(intent);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
                            Toast.makeText(CompanyProfileActivity.this, "yes", Toast.LENGTH_SHORT).show();

                        } else {
                            //Log.d(TAG, "No such document");
                            isPostId = false;
                            Toast.makeText(CompanyProfileActivity.this, "no", Toast.LENGTH_SHORT).show();
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
    }*/


    @Override
    public boolean favouriteCheck(final String post_id, int numb) {

        if (mAuth.getCurrentUser() != null) {
            firebaseFirestore.collection("Customers").document(currentUserId).collection("Favourites").document(post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            isPostId = true;
                            Toast.makeText(CompanyProfileActivity.this, "yes", Toast.LENGTH_SHORT).show();

                        } else {
                            //Log.d(TAG, "No such document");
                            isPostId = false;
                            Toast.makeText(CompanyProfileActivity.this, "no", Toast.LENGTH_SHORT).show();
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

                                    firebaseFirestore.collection("Customers").document(currentUserId).collection("Favourites").document(post_id).set(favMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(CompanyProfileActivity.this, "Added to Favourite list!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(CompanyProfileActivity.this, "ERROR : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    Toast.makeText(CompanyProfileActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(CompanyProfileActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

                return false;

            } else {
                if (numb == 1) {
                    firebaseFirestore.collection("Customers").document(currentUserId).collection("Favourites").document(post_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CompanyProfileActivity.this, "removed from favourite.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
                isPostId=false;
                return true;
            }
        } else {
            return false;
        }
    }

   /* @Override
    public void myFave(boolean isFav, String postId, ImageView img) {

    }*/
}
