package com.example.ifty.myproducts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class FavouriteActivity extends AppCompatActivity implements ProductAdapter.FavouriteCheck{

    private RecyclerView favRecyclerView;
    private ArrayList<Product> favList;
    private ProductAdapter productAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String userId;
    private Toolbar favToolbar;
    private boolean isPostId = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);


        favRecyclerView = findViewById(R.id.favRecyclerView);
        favToolbar = findViewById(R.id.fav_toolbar);
        favList = new ArrayList<>();

        setSupportActionBar(favToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Favourite Products");


        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();


        firebaseFirestore.collection("Customers").document(userId).collection("Favourites").addSnapshotListener(this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null){
                    if(!queryDocumentSnapshots.isEmpty()){
                        for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                            if (doc.getType()==DocumentChange.Type.ADDED){
                                String blogPostId = doc.getDocument().getId();
                                Product product = doc.getDocument().toObject(Product.class).withId(blogPostId);
                                favList.add(product);
                            }
                        }
                    }
                }

            }
        });

        productAdapter = new ProductAdapter(this,favList,this);
        favRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        favRecyclerView.setAdapter(productAdapter);


    }

    @Override
    public boolean favouriteCheck(final String post_id, int numb) {

        if (auth.getCurrentUser() != null) {
            firebaseFirestore.collection("Customers").document(userId).collection("Favourites").document(post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                           // Toast.makeText(ProductDetailsActivity.this, "no", Toast.LENGTH_SHORT).show();
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

                                    firebaseFirestore.collection("Customers").document(userId).collection("Favourites").document(post_id).set(favMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {
                                            if (task.isSuccessful()) {
                                               // Toast.makeText(ProductDetailsActivity.this, "Added to Favourite list!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(FavouriteActivity.this, "ERROR : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    Toast.makeText(FavouriteActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(FavouriteActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

                return false;

            } else {
                if (numb == 1) {
                    firebaseFirestore.collection("Customers").document(userId).collection("Favourites").document(post_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                               // Toast.makeText(FavouriteActivity.this, "removed from favourite.", Toast.LENGTH_SHORT).show();

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        finish();
        startActivity(intent);
    }
}
