package com.example.mockapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;


import com.example.mockapp.network.OrderEntry;
import com.example.mockapp.network.OrderList;
import com.example.mockapp.slang.ActivityDetector;

import java.util.ArrayList;
import java.util.List;

public class OrderListFragment extends Fragment {

    private static final String TAG = OrderListFragment.class.getSimpleName();

    private OrderListRecyclerViewAdapter mAdapter;
    private RecyclerView recyclerViewOrder;
    private List<OrderList> orderList;

    private MaterialButton featured;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shr_order_list_fragment, container, false);

        // Set up the tool bar
        setUpToolbar(view);

        featured = view.findViewById(R.id.featured);
        featured.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Toast.makeText(v.getContext(),"WE'RE GOING HOME", Toast.LENGTH_LONG).show();
                Log.d(TAG, "******* Ready to go home");*/
                Toast.makeText(v.getContext(),"WE'RE GOING HOME", Toast.LENGTH_LONG).show();
                Intent startMainActivity = new Intent(getContext(), MainActivity.class);
                startMainActivity.putExtra("back", true);
                startActivity(startMainActivity);
            }
        });

        Bundle bundle = getArguments();
        String mode = "";
        if (bundle != null)
            mode = bundle.getString(ActivityDetector.ACTIVITY_MODE);
        if (mode!=null) {
            if (mode.equals(ActivityDetector.MODE_TRACK_PRODUCT)
                    || mode.equals(ActivityDetector.MODE_REFUND_PRODUCT)
                    || mode.equals(ActivityDetector.MODE_RETURN_PRODUCT)) {
                Log.d(TAG, "Mode is ActivityDetector.MODE_TRACK_PRODUCT");
                orderList = bundle.getParcelableArrayList(ActivityDetector.ORDER_LIST);
                //TODO implement multi-modal approach by letting Slang know when user clicks on an item
            } else {
                orderList = OrderList.initOrderList(getResources());
            }
        }
        else {
            orderList = OrderList.initOrderList(getResources());
        }

        recyclerViewOrder = view.findViewById(R.id.recycler_view_order_list);
        recyclerViewOrder.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewOrder.setLayoutManager(linearLayoutManager);
        mAdapter = new OrderListRecyclerViewAdapter(getContext(), orderList);
        recyclerViewOrder.setAdapter(mAdapter);
        mAdapter.setOrderClickListener(new OrderListRecyclerViewAdapter.OrderClickListener() {
            @Override
            public void onOrderClick(View view, int position) {
                Log.d(TAG, "******** Click on OrderListFragment");
                Toast.makeText(getContext(), "From OrderListFragment", Toast.LENGTH_LONG).show();
                OrderList orders = orderList.get(position);
                List<OrderEntry> entries = orders.items;
                Bundle bundle = new Bundle();
                String orderString = "Order#: " + orders.order_number;
                String orderDate = "Order Date: " + orders.order_date;
                bundle.putParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST, (ArrayList<OrderEntry>) entries);
                bundle.putString(ActivityDetector.ORDER_NUMBER, orderString);
                bundle.putString(ActivityDetector.ORDER_DATE, orderDate);
                OrderEntryFragment orderEntryFragment = new OrderEntryFragment();
                orderEntryFragment.setArguments(bundle);
                ((NavigationHost) getActivity()).navigateTo(orderEntryFragment, true);
            }
        });
        return view;
    }

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }

        toolbar.setNavigationOnClickListener(new NavigationIconClickListener(
                getContext(),
                view.findViewById(R.id.product_grid),
                new AccelerateDecelerateInterpolator(),
                getContext().getResources().getDrawable(R.drawable.shr_branded_menu), // Menu open icon
                getContext().getResources().getDrawable(R.drawable.shr_close_menu))); // Menu close icon
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.shr_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }
}
