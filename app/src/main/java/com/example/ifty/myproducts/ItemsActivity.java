package com.example.ifty.myproducts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class ItemsActivity extends AppCompatActivity {

    private ArrayList <Item> itemList;
    private RecyclerView itemRecyclerView;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        itemRecyclerView=findViewById(R.id.item_recycleView);

        itemList= new ArrayList<>();
        type=getIntent().getStringExtra("type");

        switch (type) {
            case "Electronics":
                itemList = new ArrayList<>();
                itemList.add(new Item("Mobile Phones", R.drawable.mobile));
                itemList.add(new Item("Mobile Phone Accessories", R.drawable.mobile_accessories));
                itemList.add(new Item("Computers & Tablets", R.drawable.laptop_tab));
                itemList.add(new Item("TVs", R.drawable.tv));
                break;
            case "Cars & Vehicles":
                itemList = new ArrayList<>();
                itemList.add(new Item("Motorbikes & Scooters", R.drawable.bike));
                itemList.add(new Item("Cars", R.drawable.cars));
                itemList.add(new Item("Trucks", R.drawable.trucks));
                itemList.add(new Item("Vans & Buses", R.drawable.bus));
                break;
            case "Property":
                itemList = new ArrayList<>();
                itemList.add(new Item("Apartment & Flats", R.drawable.flat));
                itemList.add(new Item("Houses", R.drawable.house));
                itemList.add(new Item("Plots & Land", R.drawable.land));
                break;
        }

        itemRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        ItemAdapter itemAdapter=new ItemAdapter(this,itemList);
        itemRecyclerView.setAdapter(itemAdapter);
    }
}
