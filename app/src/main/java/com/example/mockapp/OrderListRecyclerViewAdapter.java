package com.example.mockapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.mockapp.network.OrderEntry;
import com.example.mockapp.network.OrderList;

import java.util.ArrayList;
import java.util.List;

public class OrderListRecyclerViewAdapter extends RecyclerView.Adapter<OrderListRecyclerViewAdapter.OrderListCardViewHolder> {
    private List<OrderList> orderList;

    private final static String TAG = OrderListRecyclerViewAdapter.class.getSimpleName();

    private OrderClickListener orderClickListener;
    private Context context;

    public OrderListRecyclerViewAdapter(Context context, List<OrderList> orderList) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderListCardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        int layoutId = R.layout.shr_order_list_card;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutId, viewGroup, false);

        OrderListCardViewHolder orderListCardViewHolder = new OrderListCardViewHolder(view);
        return orderListCardViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull OrderListCardViewHolder orderListCardViewHolder, int i) {
        if (orderList != null && i < orderList.size()) {
            OrderList order = orderList.get(i);
            final String number = "Order#: " + order.order_number;
            orderListCardViewHolder.orderNumber.setText(number);
            final String date = "Order Date: " + order.order_date;
            orderListCardViewHolder.orderDate.setText(date);

            final List<OrderEntry> entries = order.items;
            OrderListInnerRecyclerViewAdapter adapter = new OrderListInnerRecyclerViewAdapter(context, entries);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

            orderListCardViewHolder.orderEntryRecyclerView.setLayoutManager(linearLayoutManager);
            orderListCardViewHolder.orderEntryRecyclerView.setHasFixedSize(true);
            orderListCardViewHolder.orderEntryRecyclerView.setAdapter(adapter);

            adapter.setOrderClickListener(new OrderListInnerRecyclerViewAdapter.OrderInnerClickListener() {
                @Override
                public void onOrderClick(View view, int position) {
                    Log.d(TAG, "******** Click on OrderListRecyclerViewAdapter");
                    Toast.makeText(context, "From OrderListRecyclerViewAdapter", Toast.LENGTH_LONG).show();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("entry", (ArrayList<OrderEntry>) entries);
                    bundle.putString("number", number);
                    bundle.putString("date", date);
                    OrderEntryFragment orderEntryFragment = new OrderEntryFragment();
                    orderEntryFragment.setArguments(bundle);
                    ((NavigationHost) context).navigateTo(orderEntryFragment, true);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (orderList == null)
            return 0;
        else
            return orderList.size();
    }

    public class OrderListCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView orderNumber;
        public TextView orderDate;
        public RecyclerView orderEntryRecyclerView;

        public OrderListCardViewHolder(@NonNull View itemView) {
            super(itemView);

            orderNumber = itemView.findViewById(R.id.order_number);
            orderDate = itemView.findViewById(R.id.order_date);
            orderEntryRecyclerView = itemView.findViewById(R.id.recycler_view_order_list_card);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            orderClickListener.onOrderClick(v,getAdapterPosition());
        }
    }

    public void setOrderClickListener(OrderClickListener orderClickListener) {
        this.orderClickListener = orderClickListener;
    }

    public interface OrderClickListener {
        void onOrderClick(View view, int position);
    }
}
