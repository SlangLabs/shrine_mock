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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.example.mockapp.network.OrderEntry;
import com.example.mockapp.slang.ActivityDetector;

import java.util.List;


public class OrderEntryFragment extends Fragment {
    private static final String TAG = OrderEntryFragment.class.getSimpleName();

    private MaterialButton featured;
    private MaterialButton myAccount;
    private RecyclerView recyclerView;
    private OrderEntryRecyclerViewAdapter mAdapter;
    private String orderNumber;
    private String orderDate;
    private List<OrderEntry> orderEntries;
    private TextView orderNum;
    private TextView date;
    //private String mode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle bundle = getArguments();
        if (bundle != null) {
            //mode = bundle.getString(ActivityDetector.ACTIVITY_MODE);
            orderNumber = bundle.getString(ActivityDetector.ORDER_NUMBER);
            orderDate = bundle.getString(ActivityDetector.ORDER_DATE);
            orderEntries = bundle.getParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.shr_order_entry_fragment, container, false);

        setUpToolbar(view);

        featured = view.findViewById(R.id.featured);
        featured.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startMainActivity = new Intent(getContext(), MainActivity.class);
                startMainActivity.putExtra("back", true);
                startActivity(startMainActivity);
            }
        });
        myAccount = view.findViewById(R.id.my_orders);
        myAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((NavigationHost) getActivity()).navigateTo(new OrderListFragment(), false);
            }
        });

        orderNum = view.findViewById(R.id.order_entry_number);
        date = view.findViewById(R.id.order_entry_date);
        recyclerView = view.findViewById(R.id.recycler_view_order_entry);

        orderNum.setText(orderNumber);
        date.setText(orderDate);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new OrderEntryRecyclerViewAdapter(getContext(), orderEntries);
        recyclerView.setAdapter(mAdapter);
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