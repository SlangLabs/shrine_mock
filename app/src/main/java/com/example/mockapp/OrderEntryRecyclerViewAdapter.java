package com.example.mockapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.mockapp.network.ImageRequester;
import com.example.mockapp.network.OrderEntry;


import java.util.List;

public class OrderEntryRecyclerViewAdapter extends RecyclerView.Adapter<OrderEntryRecyclerViewAdapter.OrderEntryViewHolder> {
    private List<OrderEntry> orderEntries;

    private Context context;

    public OrderEntryRecyclerViewAdapter(Context context, List<OrderEntry> orderEntries) {
        this.context = context;
        this.orderEntries = orderEntries;
    }

    @NonNull
    @Override
    public OrderEntryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        int layoutId = R.layout.shr_order_entry_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutId, viewGroup, false);

        OrderEntryViewHolder orderEntryViewHolder = new OrderEntryViewHolder(view);
        return orderEntryViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull OrderEntryViewHolder orderEntryViewHolder, int i) {
        if (orderEntries != null && i < orderEntries.size()) {
            OrderEntry entry = orderEntries.get(i);
            ImageRequester imageRequester = ImageRequester.getInstance();
            imageRequester.setImageFromUrl(orderEntryViewHolder.orderImage, entry.url);
            orderEntryViewHolder.title.setText(entry.title);
            orderEntryViewHolder.brand.setText(entry.brand);
            String p = "INR " + entry.price;
            orderEntryViewHolder.price.setText(p);
            orderEntryViewHolder.status.setText(entry.status);
            String location = "Currently at: " + entry.location;
            orderEntryViewHolder.location.setText(location);
            if (entry.delivered) {
                String deliveryDate = "Delivery Date: " + entry.delivery_date;
                orderEntryViewHolder.delivery.setText(deliveryDate);
            } else
                orderEntryViewHolder.delivery.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (orderEntries == null)
            return 0;
        else
            return orderEntries.size();
    }


    public class OrderEntryViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView brand;
        private TextView price;
        private TextView status;
        private TextView location;
        private TextView delivery;
        private NetworkImageView orderImage;

        public OrderEntryViewHolder(@NonNull View view) {
            super(view);

            orderImage = view.findViewById(R.id.order_entry_image);
            title = view.findViewById(R.id.order_entry_title);
            brand = view.findViewById(R.id.order_entry_brand);
            price = view.findViewById(R.id.order_entry_price);
            status = view.findViewById(R.id.order_entry_status);
            location = view.findViewById(R.id.order_entry_location);
            delivery = view.findViewById(R.id.order_entry_delivery);
           /*

            title.setText(orderEntries.title);

            brand.setText(orderEntries.brand);

            String p = "INR" + orderEntries.price;
            price.setText(p);

            status.setText(orderEntries.status);

            location.setText(orderEntries.location);

            if (orderEntries.delivered) {
                String deliveryDate = "Delivery Date: " + orderEntries.delivery_date;
                delivery.setText(deliveryDate);
            } else
                delivery.setVisibility(View.GONE);*/
        }
    }
}
