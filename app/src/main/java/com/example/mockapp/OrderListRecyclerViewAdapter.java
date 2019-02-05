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
import com.example.mockapp.slang.ActivityDetector;

import java.util.ArrayList;
import java.util.List;

public class OrderListRecyclerViewAdapter extends RecyclerView.Adapter<OrderListRecyclerViewAdapter.OrderListCardViewHolder> {
    private List<OrderList> orderList;

    private final static String TAG = OrderListRecyclerViewAdapter.class.getSimpleName();

    private Context context;
    String mode;

    public OrderListRecyclerViewAdapter(Context context, List<OrderList> orderList, String mode) {
        this.orderList = orderList;
        this.context = context;
        this.mode = mode;
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

            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

            orderListCardViewHolder.orderEntryRecyclerView.setLayoutManager(linearLayoutManager);
            orderListCardViewHolder.orderEntryRecyclerView.setHasFixedSize(true);
            orderListCardViewHolder.orderEntryRecyclerView.setAdapter(adapter);

            adapter.setOrderClickListener(new OrderListInnerRecyclerViewAdapter.OrderInnerClickListener() {
                @Override
                public void onOrderClick(View view, int position) {
                    Log.d(TAG, "Click on OrderListRecyclerViewAdapter");
                    Log.d(TAG, "Position clicked is " + (position+1));
                    Bundle bundle = new Bundle();
                    List<OrderEntry> list = new ArrayList<>();
                    list.add(entries.get(position));
                    bundle.putParcelableArrayList(
                            ActivityDetector.ORDER_ENTRY_LIST, (ArrayList<OrderEntry>) list
                    );
                    bundle.putString(ActivityDetector.ORDER_NUMBER, number);
                    bundle.putString(ActivityDetector.ORDER_DATE, date);
                    bundle.putString(ActivityDetector.ACTIVITY_MODE, mode);
                    OrderEntryFragment orderEntryFragment = new OrderEntryFragment();
                    orderEntryFragment.setArguments(bundle);
                    ((NavigationHost) context).navigateTo(orderEntryFragment, true, ActivityDetector.TAG_ORDER_ENTRY);
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

    public class OrderListCardViewHolder extends RecyclerView.ViewHolder {

        public TextView orderNumber;
        public TextView orderDate;
        public RecyclerView orderEntryRecyclerView;

        public OrderListCardViewHolder(@NonNull View itemView) {
            super(itemView);

            orderNumber = itemView.findViewById(R.id.order_number);
            orderDate = itemView.findViewById(R.id.order_date);
            orderEntryRecyclerView = itemView.findViewById(R.id.recycler_view_order_list_card);
        }
    }
}
