package com.example.ifty.myproducts;

public class ProductId {

    String ProductId;

    public <T extends ProductId> T withId(String productId){
        this.ProductId=productId;
        return (T) this;
    }
}
