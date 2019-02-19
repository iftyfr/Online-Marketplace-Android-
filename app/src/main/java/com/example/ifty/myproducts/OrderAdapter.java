package com.example.ifty.myproducts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    Context context;
    ArrayList<Order> orderList;
    makeArchive makeArchive;

    public OrderAdapter(Context context, ArrayList<Order> orderList, makeArchive makeArchive) {
        this.context = context;
        this.orderList = orderList;
        this.makeArchive = makeArchive;
    }


    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_layout,parent,false);
        return new OrderViewHolder(view);
    }

    public interface makeArchive {
        void doArchive(String orderId);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        final Order order = orderList.get(position);
        holder.order.setText(order.getOrderItems());
        holder.name.setText(order.getCustomer_name());
        holder.address.setText(order.getCustomer_address());
        holder.contact.setText(order.getCustomer_contactNo());
        holder.altContact.setText(order.getCustomer_contactNo2());
        holder.totalPrice.setText(order.getTotalPrice());

        holder.doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeArchive.doArchive(order.ProductId);
            }
        });

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        private TextView order, name, address, contact, altContact, totalPrice;
        private Button doneBtn;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            order = itemView.findViewById(R.id.notify_order);
            name = itemView.findViewById(R.id.notify_name);
            address = itemView.findViewById(R.id.notify_address);
            contact = itemView.findViewById(R.id.notify_contact);
            altContact = itemView.findViewById(R.id.notify_contact_alt);
            totalPrice = itemView.findViewById(R.id.notify_total_price);
            doneBtn = itemView.findViewById(R.id.doneBtn);
        }
    }
}
