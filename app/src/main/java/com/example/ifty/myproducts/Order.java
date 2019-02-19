package com.example.ifty.myproducts;

public class Order  extends ProductId{
    String customer_address, customer_contactNo, customer_contactNo2, customer_name, orderItems, shipping, totalPrice;

    public Order() {
    }

    public Order(String customer_address, String customer_contactNo, String customer_contactNo2, String customer_name, String orderItems, String shipping, String totalPrice) {
        this.customer_address = customer_address;
        this.customer_contactNo = customer_contactNo;
        this.customer_contactNo2 = customer_contactNo2;
        this.customer_name = customer_name;
        this.orderItems = orderItems;
        this.shipping = shipping;
        this.totalPrice = totalPrice;
    }

    public String getCustomer_address() {
        return customer_address;
    }

    public String getCustomer_contactNo() {
        return customer_contactNo;
    }

    public String getCustomer_contactNo2() {
        return customer_contactNo2;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public String getOrderItems() {
        return orderItems;
    }

    public String getShipping() {
        return shipping;
    }

    public String getTotalPrice() {
        return totalPrice;
    }
}
