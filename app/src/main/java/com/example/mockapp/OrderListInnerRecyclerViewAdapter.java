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

public class OrderListInnerRecyclerViewAdapter extends RecyclerView.Adapter<OrderListInnerRecyclerViewAdapter.OrderListInnerViewHolder> {

    private List<OrderEntry> orderEntries;
    private ImageRequester imageRequester;
    private OrderInnerClickListener orderClickListener;
    Context context;

    public OrderListInnerRecyclerViewAdapter(Context context, List<OrderEntry> entries) {
        orderEntries = entries;
        this.context = context;
        imageRequester = ImageRequester.getInstance();
    }

    @NonNull
    @Override
    public OrderListInnerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        int layoutId = R.layout.shr_order_list_card_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(layoutId, viewGroup, false);

        return new OrderListInnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderListInnerViewHolder holder, int i) {
        if (orderEntries != null && i < orderEntries.size()) {
            OrderEntry orderEntry = orderEntries.get(i);
            holder.orderTitle.setText(orderEntry.title);
            holder.orderBrand.setText(orderEntry.brand);
            String price = "INR " + orderEntry.price;
            holder.orderPrice.setText(price);
            holder.orderStatus.setText(orderEntry.status);
            /*holder.orderLocation.setText(orderEntry.location);
            holder.orderDelivery.setText(orderEntry.delivery_date);*/
            imageRequester.setImageFromUrl(holder.orderImage, orderEntry.url);
        }
    }

    @Override
    public int getItemCount() {
        if (orderEntries == null)
                return 0;
        else
            return orderEntries.size();
    }

    public class OrderListInnerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public NetworkImageView orderImage;
        public TextView orderTitle;
        public TextView orderBrand;
        public TextView orderPrice;
        public TextView orderStatus;
        /*public TextView orderLocation;
        public TextView orderDelivery;*/

        public OrderListInnerViewHolder(@NonNull View itemView) {
            super(itemView);
            orderImage = itemView.findViewById(R.id.order_image);
            orderTitle = itemView.findViewById(R.id.order_title);
            orderBrand = itemView.findViewById(R.id.order_brand);
            orderPrice = itemView.findViewById(R.id.order_price);
            orderStatus = itemView.findViewById(R.id.order_status);
            /*orderLocation = itemView.findViewById(R.id.order_location);
            orderDelivery = itemView.findViewById(R.id.order_delivery);*/
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            orderClickListener.onOrderClick(v,getAdapterPosition());
        }
    }
    public void setOrderClickListener(OrderInnerClickListener orderClickListener) {
        this.orderClickListener = orderClickListener;
    }
    public interface OrderInnerClickListener {
        void onOrderClick(View view, int position);
    }
}
