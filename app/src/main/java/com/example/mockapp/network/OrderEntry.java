package com.example.mockapp.network;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderEntry implements Parcelable {

        public int item_number;
        public String title;
        public String url;
        public int price;
        public String brand;
        public String color;
        public String status;
        public String location;
        public boolean delivered;
        public boolean returned;
        public boolean pickup;
        public boolean cancelled;
        public String delivery_date;
        public String pickup_date;
        public String return_date;
        public String cancel_date;
        public final static Creator<OrderEntry> CREATOR = new Creator<OrderEntry>() {


            @SuppressWarnings({
                    "unchecked"
            })
            public OrderEntry createFromParcel(Parcel in) {
                return new OrderEntry(in);
            }

            public OrderEntry[] newArray(int size) {
                return (new OrderEntry[size]);
            }

        }
                ;

    protected OrderEntry(Parcel in) {
        this.item_number = ((int) in.readValue((int.class.getClassLoader())));
        this.title = ((String) in.readValue((String.class.getClassLoader())));
        this.url = ((String) in.readValue((String.class.getClassLoader())));
        this.price = ((int) in.readValue((int.class.getClassLoader())));
        this.brand = ((String) in.readValue((String.class.getClassLoader())));
        this.color = ((String) in.readValue((String.class.getClassLoader())));
        this.status = ((String) in.readValue((String.class.getClassLoader())));
        this.location = ((String) in.readValue((String.class.getClassLoader())));
        this.delivered = ((boolean) in.readValue((boolean.class.getClassLoader())));
        this.returned = ((boolean) in.readValue((boolean.class.getClassLoader())));
        this.pickup = ((boolean) in.readValue((boolean.class.getClassLoader())));
        this.cancelled = ((boolean) in.readValue((boolean.class.getClassLoader())));
        this.delivery_date = ((String) in.readValue((String.class.getClassLoader())));
        this.pickup_date = ((String) in.readValue((String.class.getClassLoader())));
        this.return_date = ((String) in.readValue((String.class.getClassLoader())));
        this.cancel_date = ((String) in.readValue((String.class.getClassLoader())));
    }

public OrderEntry() {
    }
public OrderEntry(int item_number, String title, String url,
                  int price, String brand, String color, String status, String location,
                  boolean delivered, boolean returned, boolean pickup, boolean cancelled,
                  String delivery_date, String pickup_date, String return_date,
                  String cancel_date) {
        this.item_number = item_number;
        this.title = title;
        this.url = url;
        this.price = price;
        this.brand = brand;
        this.color = color;
        this.status = status;
        this.location = location;
        this.delivered = delivered;
        this.returned = returned;
        this.pickup = pickup;
        this.cancelled = cancelled;
        this.delivery_date = delivery_date;
        this.pickup_date = pickup_date;
        this.return_date = return_date;
        this.cancel_date = cancel_date;
    }


    public int getItem_number() {
        return item_number;
    }

        public void setItem_number(int item_number) {
        this.item_number = item_number;
    }

        public String getTitle() {
        return title;
    }

        public void setTitle(String title) {
        this.title = title;
    }

        public String getUrl() {
        return url;
    }

        public void setUrl(String url) {
        this.url = url;
    }

        public int getPrice() {
        return price;
    }

        public void setPrice(int price) {
        this.price = price;
    }

        public String getBrand() {
        return brand;
    }

        public void setBrand(String brand) {
        this.brand = brand;
    }

        public String getColor() {
        return color;
    }

        public void setColor(String color) {
        this.color = color;
    }

        public String getStatus() {
        return status;
    }

        public void setStatus(String status) {
        this.status = status;
    }

        public String getLocation() {
        return location;
    }

        public void setLocation(String location) {
        this.location = location;
    }

        public boolean isDelivered() {
        return delivered;
    }

        public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

        public String getDelivery_date() {
        return delivery_date;
    }

        public void setDelivery_date(String delivery_date) {
        this.delivery_date = delivery_date;
    }

        public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(item_number);
        dest.writeValue(title);
        dest.writeValue(url);
        dest.writeValue(price);
        dest.writeValue(brand);
        dest.writeValue(color);
        dest.writeValue(status);
        dest.writeValue(location);
        dest.writeValue(delivered);
        dest.writeValue(returned);
        dest.writeValue(pickup);
        dest.writeValue(cancelled);
        dest.writeValue(delivery_date);
        dest.writeValue(pickup_date);
        dest.writeValue(return_date);
        dest.writeValue(cancel_date);
    }

        public int describeContents() {
        return 0;
    }
}