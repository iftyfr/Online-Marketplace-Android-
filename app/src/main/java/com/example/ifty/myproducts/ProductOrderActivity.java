package com.example.ifty.myproducts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProductOrderActivity extends AppCompatActivity implements CartItemAdapter.CartInfo{

    private EditText customerName, address, contactNo, contactNoAlt;
    private TextView  shippingFee, totalPrice;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private String sellerUserId;
    private ArrayList<String> postIdList;
    private RecyclerView cartRecyclerView;
    private CartItemAdapter cartItemAdapter;
    private ArrayList <CartItem> cartItemList;
    private ScrollView orderScrollView;
    private MyPreferences myPreferences;
    private Map<String, String> orderInfoMap;
    private Map<String, String> ordersMap;
    private String orderItems = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_order);

        customerName = findViewById(R.id.order_customer_name);
        address = findViewById(R.id.order_customer_address);
        contactNo = findViewById(R.id.order_mobile_customer);
        contactNoAlt = findViewById(R.id.order_mobile_customer2);
        shippingFee = findViewById(R.id.shipping_fee);
        totalPrice = findViewById(R.id.order_total_price);
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        orderScrollView = findViewById(R.id.orderScrollView);

        myPreferences = MyPreferences.getPreferences(this);
        orderInfoMap = new HashMap<>();
        ordersMap = new HashMap<>();

        orderScrollView.setFocusableInTouchMode(true);
        orderScrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        cartItemList= new ArrayList<>();
        postIdList=new ArrayList<>();
        postIdList=getIntent().getStringArrayListExtra("postIdList");

        //postId=getIntent().getStringExtra("postId");

        cartRecyclerView.setNestedScrollingEnabled(false);

        getOrderList(postIdList);

        if (mAuth.getCurrentUser()!=null){
            user_id = mAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Customers").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            Toast.makeText(ProductOrderActivity.this, "exist", Toast.LENGTH_SHORT).show();

                            String user_name = task.getResult().getString("userName");
                            String mobile_number = task.getResult().getString("mobileNumber");
                            String customer_address = task.getResult().getString("address");

                            customerName.setText(user_name);
                            contactNo.setText(mobile_number);
                            address.setText(customer_address);


                        } else {
                            Toast.makeText(ProductOrderActivity.this, "don't exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String error = task.getException().toString();
                        Toast.makeText(ProductOrderActivity.this, "RETRIEVE ERROR : " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }



    }

    private void getOrderList(ArrayList<String> postIdList) {
        for (int i=0; i<postIdList.size();i++){
            firebaseFirestore.collection("Posts").document(postIdList.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (Objects.requireNonNull(task.getResult()).exists()){
                            String product_name = task.getResult().getString("productName");
                            String product_price = task.getResult().getString("price");
                            String company_name = task.getResult().getString("companyName");
                            String postImage = task.getResult().getString("postImage");
                            sellerUserId = task.getResult().getString("userId");

                            cartItemList.add(new CartItem(company_name,product_name,product_price,postImage));

                            cartRecyclerView.setHasFixedSize(true);
                            cartRecyclerView.setLayoutManager(new LinearLayoutManager(ProductOrderActivity.this));
                            cartItemAdapter=new CartItemAdapter(ProductOrderActivity.this,cartItemList,ProductOrderActivity.this);
                            cartRecyclerView.setAdapter(cartItemAdapter);
                        }
                        else {
                            Toast.makeText(ProductOrderActivity.this, "Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(ProductOrderActivity.this, "Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void signIn(View view) {
        Intent intent = new Intent(this,LoginActivity.class);
        intent.putExtra("ifOrder","fromOrder");
        intent.putExtra("customerOrSeller","customer");
        //intent.putExtra("postId",postId);
        finish();
        startActivity(intent);
    }


    @Override
    public void removeFromCart(int position,String uniqueName) {
        Toast.makeText(this, ""+position, Toast.LENGTH_SHORT).show();
        cartItemList= new ArrayList<>();
        postIdList.remove(position);
        myPreferences.setPostIdList(postIdList);/*
        postIdList = new ArrayList<>();
        postIdList=myPreferences.getPostIdList();*/
        //getOrderList(postIdList);

        Intent intent = new Intent(this,ProductOrderActivity.class);
        intent.putStringArrayListExtra("postIdList",postIdList);
        finish();
        startActivity(intent);

        ordersMap.remove(uniqueName);
        orderItems="";
        for(String entry : ordersMap.keySet()) {
            orderItems = orderItems+ordersMap.get(entry)+"\n";
        }
        orderInfoMap.put("orderItems",orderItems);

    }

    @Override
    public void totalPrice(int price) {
        totalPrice.setText(String.valueOf(price));
    }

    @Override
    public void namePriceQty(String uniqueName, String namePriceQty) {
        ordersMap.put(uniqueName,namePriceQty);
        orderItems="";

        for(String entry : ordersMap.keySet()) {
            orderItems = orderItems+ordersMap.get(entry)+"\n";
        }
        orderInfoMap.put("orderItems",orderItems);
    }

    public void confirmOrder(View view) {
        String customer_name = customerName.getText().toString();
        String customer_address = address.getText().toString();
        String customer_contactNo = contactNo.getText().toString();
        String customer_contactNo2 = contactNoAlt.getText().toString();
        if (!TextUtils.isEmpty(customer_name)&&!TextUtils.isEmpty(customer_address)&&!TextUtils.isEmpty(customer_contactNo)){
            orderInfoMap.put("customer_name",customer_name);
            orderInfoMap.put("customer_address",customer_address);
            orderInfoMap.put("customer_contactNo",customer_contactNo);
            orderInfoMap.put("customer_contactNo2",customer_contactNo2);
            orderInfoMap.put("shipping",shippingFee.getText().toString());
            orderInfoMap.put("totalPrice",totalPrice.getText().toString());

            firebaseFirestore.collection("Users").document(sellerUserId).collection("Orders").add(orderInfoMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (task.isSuccessful()){
                        postIdList=new ArrayList<>();
                        Toast.makeText(ProductOrderActivity.this, "Order Confirmed Successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProductOrderActivity.this,MainActivity.class));
                        finish();
                    }
                    else {
                        String error = task.getException().toString();
                        Toast.makeText(ProductOrderActivity.this, "FireStore ERROR : "+error, Toast.LENGTH_SHORT).show();
                        //setupProgressBar.setVisibility(View.INVISIBLE);
                    }

                }
            });
        }
        else {
            Toast.makeText(this, "* User Name\n* Address\n* Contact number\n\nare required.", Toast.LENGTH_SHORT).show();
        }

    }
}
