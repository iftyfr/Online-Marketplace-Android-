package com.example.ifty.myproducts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CartItemAdapter extends RecyclerView.Adapter <CartItemAdapter.CartItemViewHolder> {

    private Context context;
    private ArrayList <CartItem> cartItemList;
    private int total_price = 50;
    private CartInfo cartInfo;
    private int count;
    private String price;
    private String namePriceQty;
    private String uniqueName;

    public CartItemAdapter(Context context, ArrayList<CartItem> cartItemList, CartInfo cartInfo) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.cartInfo = cartInfo;
    }


    public interface CartInfo {
        void removeFromCart(int position, String uniqueName);
        void totalPrice (int price);
        void namePriceQty (String uniqueName, String namePriceQty);
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_items_layout,parent,false);
        return new CartItemViewHolder (view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CartItemViewHolder holder, final int position) {

        final CartItem cartItems = cartItemList.get(position);
        Toast.makeText(context, ""+cartItems.getProductName(), Toast.LENGTH_SHORT).show();

        holder.product_name.setText(cartItems.getProductName());
        price = cartItems.getPrice();
        holder.company_name.setText(cartItems.getCompanyName());
        String postImage = cartItems.getPostImage();

        holder.product_price.setText(price.replaceAll("\\D+",""));
        total_price = total_price+Integer.parseInt(price.replaceAll("\\D+",""));
        cartInfo.totalPrice(total_price);
        Glide.with(context).load(postImage).into(holder.pruduct_image);

        count=Integer.valueOf(holder.quantity.getText().toString());

        namePriceQty = cartItems.getProductName()+"    "+"Qty "+"("+count+")   Price: "+cartItems.getPrice().replaceAll("\\D+","")+" tk";

        uniqueName = namePriceQty.replaceAll("\\s+","").replaceAll("[\\d.]", "");
        cartInfo.namePriceQty(uniqueName,namePriceQty);

        holder.qtyAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count=Integer.valueOf(holder.quantity.getText().toString());
                count++;
                total_price=(total_price+Integer.parseInt(cartItems.getPrice().replaceAll("\\D+","")));
                holder.quantity.setText(String.valueOf(count));
                cartInfo.totalPrice(total_price);
                String qtyPrice = String.valueOf(Integer.valueOf(cartItems.getPrice().replaceAll("\\D+",""))*count);

                namePriceQty = cartItems.getProductName()+"    "+"Qty "+"("+count+")   Price: "+qtyPrice+" tk";
                uniqueName = namePriceQty.replaceAll("\\s+","").replaceAll("[\\d.]", "");
                cartInfo.namePriceQty(uniqueName,namePriceQty);
            }
        });
        holder.qtyRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count=Integer.valueOf(holder.quantity.getText().toString());
                if (count>1){
                    count--;
                    total_price=(total_price-Integer.parseInt(cartItems.getPrice().replaceAll("\\D+","")));
                    holder.quantity.setText(String.valueOf(count));
                    cartInfo.totalPrice(total_price);

                    String qtyPrice = String.valueOf(Integer.valueOf(cartItems.getPrice().replaceAll("\\D+",""))*count);

                    namePriceQty = cartItems.getProductName()+"    "+"Qty "+"("+count+")   Price: "+qtyPrice+" tk";
                    uniqueName = namePriceQty.replaceAll("\\s+","").replaceAll("[\\d.]", "");
                    cartInfo.namePriceQty(uniqueName,namePriceQty);
                }
                else {
                    Toast.makeText(context, "Atleast one product have to order !", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.remove_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qtyPrice = String.valueOf(Integer.valueOf(cartItems.getPrice().replaceAll("\\D+",""))*count);
                namePriceQty = cartItems.getProductName()+"    "+"Qty "+"("+count+")   Price: "+qtyPrice+" tk";
                uniqueName = namePriceQty.replaceAll("\\s+","").replaceAll("[\\d.]", "");
                cartInfo.removeFromCart(position,uniqueName);
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class CartItemViewHolder extends RecyclerView.ViewHolder {
         TextView company_name, product_name, product_price, quantity;
         ImageView pruduct_image, remove_product;
         ImageButton qtyAdd, qtyRemove;
        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            company_name = itemView.findViewById(R.id.cart_company_name);
            product_name = itemView.findViewById(R.id.cart_product_name);
            product_price = itemView.findViewById(R.id.cart_product_price);
            pruduct_image = itemView.findViewById(R.id.cart_product_image);
            remove_product = itemView.findViewById(R.id.cart_product_remove);
            qtyAdd = itemView.findViewById(R.id.qty_add);
            qtyRemove = itemView.findViewById(R.id.qty_remove);
            quantity = itemView.findViewById(R.id.quantity);
        }
    }
}
