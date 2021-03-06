package com.example.ifty.myproducts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    Context context;
    ArrayList<Product> productList;
    FirebaseFirestore firebaseFirestore;
    private int fav_image = 1;
    private String companyName = null;
    private String companyThumbnail=null;
    private FavouriteCheck favouriteCheck;
    private boolean isFav;

   // private IsFave isFave;

    ProductAdapter(Context context, ArrayList<Product> productList, FavouriteCheck favouriteCheck) {
        this.context = context;
        this.productList = productList;
        this.favouriteCheck = favouriteCheck;
        //this.isFave = isFave;
    }

    public interface FavouriteCheck {
        boolean favouriteCheck (String postId, int numb);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_layout,parent,false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductViewHolder holder, int position) {
        final Product product = productList.get(position);
        String userId = product.getUserId();
        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.getResult().exists()){
                    companyName = task.getResult().getString("companyName");
                    //String image = task.getResult().getString("image");
                    companyThumbnail = task.getResult().getString("logoThumbnail");
                    if (companyName != null && companyThumbnail != null){
                        holder.companyName.setText(companyName);
                        Glide.with(context.getApplicationContext()).load(companyThumbnail).into(holder.companyImage);
                    }
                    else {
                        holder.companyName.setText("User Name");
                        Glide.with(context.getApplicationContext()).load(R.drawable.company_logo).into(holder.companyImage);
                    }
                }
                else {
                    Toast.makeText(context, "not", Toast.LENGTH_SHORT).show();
                }
            }
        });

        isFav=favouriteCheck.favouriteCheck(product.ProductId,0);

        if (isFav){
            Glide.with(context.getApplicationContext()).load(R.drawable.favourite_red).into(holder.favouriteImage);
        }
        else {
            Glide.with(context.getApplicationContext()).load(R.drawable.favourite_gray).into(holder.favouriteImage);
        }

        Glide.with(context).load(product.getPostImage()).into(holder.productImage);
        holder.productName.setText(product.getProductName());
        holder.price.setText(product.getPrice());

        holder.favouriteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFav=favouriteCheck.favouriteCheck(product.ProductId,1);

                if (!isFav) {
                    //create fav doccumnet in firebae
                } else {

                }

                if (!isFav){
                    Glide.with(context.getApplicationContext()).load(R.drawable.favourite_red).into(holder.favouriteImage);
                }
                else {
                    Glide.with(context.getApplicationContext()).load(R.drawable.favourite_gray).into(holder.favouriteImage);

                }
            }
        });


        holder.productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = product.getUserId();
                firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()){
                            companyName = task.getResult().getString("companyName");
                            //String image = task.getResult().getString("image");
                            companyThumbnail = task.getResult().getString("logoThumbnail");
                            Intent intent = new Intent(context,ProductDetailsActivity.class);
                            intent.putExtra("userId",product.getUserId());
                            intent.putExtra("postId",product.ProductId);
                            intent.putExtra("companyName",companyName);
                            intent.putExtra("logoThumbnail",companyThumbnail);
                            ((Activity)context).finish();
                            context.startActivity(intent);
                        }
                        else {
                            Toast.makeText(context, "not", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        holder.companyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,CompanyProfileActivity.class);
                intent.putExtra("userId",product.getUserId());
                context.startActivity(intent);
            }
        });



        /*----------------


        holder.favouriteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                isFave.myFave(isFav, product.ProductId, holder.favouriteImage);
            }
        });

        -----*/
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }


    /*------------------*/
/*
    public void setOnmYFave(IsFave isFave) {
        this.isFaveOn = isFave;
    }


    public interface IsFave {
        void myFave(boolean isFav, String postId, ImageView img);
    }


    --------------------*/ //vuture somossa

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView companyImage, productImage, favouriteImage;
        TextView productName, price, companyName;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            companyName = itemView.findViewById(R.id.product_company_name);
            companyImage = itemView.findViewById(R.id.product_company_image);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            favouriteImage = itemView.findViewById(R.id.favourite_product);
        }
    }
}
