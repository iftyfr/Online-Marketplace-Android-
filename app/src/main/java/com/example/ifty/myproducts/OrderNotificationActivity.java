package com.example.ifty.myproducts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
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

public class OrderNotificationActivity extends AppCompatActivity implements OrderAdapter.makeArchive {

    private RecyclerView orderRecyclerView;
    private ArrayList<Order> orderList;
    private OrderAdapter orderAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String userId;
    private Toolbar orderToolbar;
    private Map<String, String> orderInfoMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_notification);

        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        orderToolbar = findViewById(R.id.order_toolbar);
        orderList = new ArrayList<>();

        setSupportActionBar(orderToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Product Details");


        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users").document(userId).collection("Orders").addSnapshotListener(this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null){
                    if(!queryDocumentSnapshots.isEmpty()){
                        for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                            if (doc.getType()==DocumentChange.Type.ADDED){
                                String orderId = doc.getDocument().getId();
                                Order order = doc.getDocument().toObject(Order.class).withId(orderId);
                                orderList.add(order);
                            }
                        }
                    }
                }

            }
        });

        orderAdapter = new OrderAdapter(this,orderList,this);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderRecyclerView.setAdapter(orderAdapter);

    }

    @Override
    public void doArchive(final String orderId) {
        firebaseFirestore.collection("Users").document(userId).collection("Orders").document(orderId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (Objects.requireNonNull(task.getResult()).exists()) {
                        String customer_name = task.getResult().getString("customer_name");
                        String customer_address = task.getResult().getString("customer_address");
                        String customer_contactNo = task.getResult().getString("customer_contactNo");
                        String customer_contactNo2 = task.getResult().getString("customer_contactNo2");
                        String shipping = task.getResult().getString("shipping");
                        String totalPrice = task.getResult().getString("totalPrice");
                        String orderItems = task.getResult().getString("orderItems");

                        orderInfoMap = new HashMap<>();
                        orderInfoMap.put("customer_name", Objects.requireNonNull(customer_name));
                        orderInfoMap.put("customer_address", Objects.requireNonNull(customer_address));
                        orderInfoMap.put("customer_contactNo", Objects.requireNonNull(customer_contactNo));
                        orderInfoMap.put("customer_contactNo2", Objects.requireNonNull(customer_contactNo2));
                        orderInfoMap.put("shipping", Objects.requireNonNull(shipping));
                        orderInfoMap.put("totalPrice", Objects.requireNonNull(totalPrice));
                        orderInfoMap.put("orderItems", Objects.requireNonNull(orderItems));

                        firebaseFirestore.collection("Users").document(userId).collection("Archive").add(orderInfoMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()){
                                    firebaseFirestore.collection("Users").document(userId).collection("Orders").document(orderId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                startActivity(new Intent(OrderNotificationActivity.this,OrderNotificationActivity.class));
                                                finish();
                                                Toast.makeText(OrderNotificationActivity.this, "Data saved in Archive.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                else {
                                    String error = task.getException().toString();
                                    Toast.makeText(OrderNotificationActivity.this, "FireStore ERROR : "+error, Toast.LENGTH_SHORT).show();
                                    //setupProgressBar.setVisibility(View.INVISIBLE);
                                }

                            }
                        });

                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        finish();
        startActivity(intent);
    }
}
