package com.example.ifty.myproducts;

import android.widget.ImageView;
import android.widget.TextView;

public class CartItem {

    private String companyName, productName, price,postImage;

    public CartItem() {
    }

    public CartItem(String companyName, String productName, String price, String postImage) {
        this.companyName = companyName;
        this.productName = productName;
        this.price = price;
        this.postImage = postImage;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getProductName() {
        return productName;
    }

    public String getPrice() {
        return price;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }
}
