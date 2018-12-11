package com.example.ifty.myproducts;

public class Item {
    private String itemName;
    private int itemImage;

    public Item(String itemName, int itemImage) {
        this.itemName = itemName;
        this.itemImage = itemImage;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemImage() {
        return itemImage;
    }
}
