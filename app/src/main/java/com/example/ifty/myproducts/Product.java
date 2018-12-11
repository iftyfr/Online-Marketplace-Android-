package com.example.ifty.myproducts;

import java.util.Date;

public class Product extends ProductId {
    String productName,price, location, description, checkItem, postImage,thumbImage,userId;
    Date timestamp;

    public Product() {
    }

    public Product(String productName, String price, String location, String description, String checkItem, String postImage, String thumbImage, String userId, Date timestamp) {
        this.productName = productName;
        this.price = price;
        this.location = location;
        this.description = description;
        this.checkItem = checkItem;
        this.postImage = postImage;
        this.thumbImage = thumbImage;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getProductName() {
        return productName;
    }

    public String getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getCheckItem() {
        return checkItem;
    }

    public String getPostImage() {
        return postImage;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public String getUserId() {
        return userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCheckItem(String checkItem) {
        this.checkItem = checkItem;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
