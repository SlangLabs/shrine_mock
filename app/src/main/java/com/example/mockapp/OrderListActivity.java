package com.example.mockapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


import com.example.mockapp.network.OrderEntry;
import com.example.mockapp.network.OrderList;
import com.example.mockapp.slang.ActivityDetector;

import java.util.ArrayList;
import java.util.List;

public class OrderListActivity extends AppCompatActivity implements NavigationHost {

    private static final String TAG = OrderListActivity.class.getSimpleName();

    private List<OrderList> orderList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shr_order_activity);

        Intent intent = getIntent();
        String mode = intent.getStringExtra(ActivityDetector.ACTIVITY_MODE);

        if (savedInstanceState == null) {
            if (mode == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.orderListContainer, new OrderListFragment())
                        .commit();
            }
            else {
                Bundle bundle = new Bundle();
                orderList = OrderList.initOrderList(getResources());
                List<OrderEntry> list;
                switch (mode) {
                    case ActivityDetector.MODE_TRACK_DEFAULT:
                        OrderEntryFragment fragment = new OrderEntryFragment();
                        //bundle.putString(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_DEFAULT);
                        bundle.putString(ActivityDetector.ORDER_NUMBER, "Order#: " + orderList.get(0).order_number);
                        bundle.putString(ActivityDetector.ORDER_DATE, "Order Date: " + orderList.get(0).order_date);
                        // truncate the list to show only the first item
                        list = orderList.get(0).items;
                        bundle.putParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST, (ArrayList<OrderEntry>) list);
                        fragment.setArguments(bundle);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.orderListContainer, fragment)
                                .commit();
                        break;
                    case ActivityDetector.MODE_TRACK_PRODUCT:
                        String productName = intent.getStringExtra(ActivityDetector.ENTITY_PRODUCT);
                        String productColor = intent.getStringExtra(ActivityDetector.ENTITY_COLOR);
                        String productBrand = intent.getStringExtra(ActivityDetector.ENTITY_BRAND);
                        int num = 0;
                        List<OrderList> orders = new ArrayList<>();
                        for(int i = 0; i < orderList.size(); i++) {
                            list = orderList.get(i).items;
                            List<OrderEntry> orderEntries = new ArrayList<>();
                            boolean storeOut = false;
                            for(int j = 0; j < list.size(); j++) {
                                OrderEntry entry = list.get(j);
                                String name = entry.title;
                                String color = entry.color;
                                String brand = entry.brand;
                                boolean store = false;

                                if (name.toLowerCase().contains(productName.toLowerCase())) {
                                    if (productColor != null) {
                                        if (productColor.equalsIgnoreCase(color)) {
                                            //If color is present from user input we compare and
                                            // only validate if color and name match
                                            if (productBrand != null) {
                                                if(productBrand.equalsIgnoreCase(brand)) {
                                                    num++;
                                                    store = true;
                                                    storeOut = true;
                                                }
                                            }
                                            else {
                                                num++;
                                                store = true;
                                                storeOut = true;
                                            }
                                        }
                                    }
                                    else {
                                        if (productBrand != null) {
                                            if(productBrand.equalsIgnoreCase(brand)) {
                                                num++;
                                                store = true;
                                                storeOut = true;
                                            }
                                        }
                                        else {
                                            num++;
                                            store = true;
                                            storeOut = true;
                                        }
                                    }
                                }
                                if (store) {
                                    orderEntries.add(entry);
                                    Log.d(TAG, "Adding orderEntry to list of OrderEntries");
                                }
                            }
                            if (storeOut) {
                                orders.add(new OrderList(orderList.get(i).order_number,
                                        orderList.get(i).order_date, orderEntries));
                                Log.d(TAG, "Adding list of OrderEntries to list of orderList");
                            }
                        }
                        switch (num) {
                            case 0:
                                // TODO show a message no matching products matching found
                                Toast.makeText(this, "Sorry, no matching products found", Toast.LENGTH_LONG).show();
                                break;
                            case 1:
                                Toast.makeText(this, "It is the unique case, showing product", Toast.LENGTH_LONG).show();
                                fragment = new OrderEntryFragment();
                                bundle.putString(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_PRODUCT);
                                bundle.putString(ActivityDetector.ORDER_NUMBER, "Order#: " + orders.get(0).order_number);
                                bundle.putString(ActivityDetector.ORDER_DATE, "Order Date: " + orders.get(0).order_date);
                                bundle.putParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST,
                                        (ArrayList<OrderEntry>) orders.get(0).items);
                                fragment.setArguments(bundle);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .add(R.id.orderListContainer, fragment)
                                        .commit();
                                break;
                            default:
                                //Open OrderListFragment with matching products
                                Toast.makeText(this, "Multiple products found", Toast.LENGTH_LONG).show();
                                OrderListFragment orderListFragment = new OrderListFragment();
                                bundle.putString(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_PRODUCT);
                                bundle.putParcelableArrayList(ActivityDetector.ORDER_LIST,
                                        (ArrayList<OrderList>) orders);
                                orderListFragment.setArguments(bundle);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .add(R.id.orderListContainer, orderListFragment)
                                        .commit();
                                break;
                        }
                        break;
                    case ActivityDetector.MODE_REFUND_DEFAULT:
                        int index = -1;
                        orders = new ArrayList<>();
                        for(int i = 0; i < orderList.size(); i++) {
                            list = orderList.get(i).items;
                            List<OrderEntry> orderEntries = new ArrayList<>();
                            for(int j = 0; j < list.size(); j++) {
                                OrderEntry entry = list.get(j);
                                if (entry.returned) {
                                    index = i;
                                    orderEntries.add(entry);
                                    break;
                                }
                            }
                            if (index > 0) {
                                orders.add(new OrderList(orderList.get(i).order_number,
                                        orderList.get(i).order_date, orderEntries));
                                break;
                            }
                        }
                        fragment = new OrderEntryFragment();
                        bundle.putString(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_PRODUCT);
                        bundle.putString(ActivityDetector.ORDER_NUMBER, "Order#: " + orders.get(0).order_number);
                        bundle.putString(ActivityDetector.ORDER_DATE, "Order Date: " + orders.get(0).order_date);
                        bundle.putParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST,
                                (ArrayList<OrderEntry>) orders.get(0).items);
                        fragment.setArguments(bundle);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.orderListContainer, fragment)
                                .commit();
                        break;
                    case ActivityDetector.MODE_REFUND_PRODUCT:
                        productName = intent.getStringExtra(ActivityDetector.ENTITY_PRODUCT);
                        productColor = intent.getStringExtra(ActivityDetector.ENTITY_COLOR);
                        productBrand = intent.getStringExtra(ActivityDetector.ENTITY_BRAND);
                        num = 0;
                        orders = new ArrayList<>();
                        for(int i = 0; i < orderList.size(); i++) {
                            list = orderList.get(i).items;
                            List<OrderEntry> orderEntries = new ArrayList<>();
                            boolean storeOut = false;
                            for (int j = 0; j < list.size(); j++) {
                                OrderEntry entry = list.get(j);
                                String name = entry.title;
                                String color = entry.color;
                                String brand = entry.brand;
                                if(!entry.returned) {
                                    // Do nothing
                                }
                                else {
                                    boolean store = false;

                                    if (name.toLowerCase().contains(productName.toLowerCase())) {
                                        if (productColor != null) {
                                            if (productColor.equalsIgnoreCase(color)) {
                                                //If color is present from user input we compare and
                                                // only validate if color and name match
                                                if (productBrand != null) {
                                                    if(productBrand.equalsIgnoreCase(brand)) {
                                                        num++;
                                                        store = true;
                                                        storeOut = true;
                                                    }
                                                }
                                                else {
                                                    num++;
                                                    store = true;
                                                    storeOut = true;
                                                }
                                            }
                                        }
                                        else {
                                            if (productBrand != null) {
                                                if(productBrand.equalsIgnoreCase(brand)) {
                                                    num++;
                                                    store = true;
                                                    storeOut = true;
                                                }
                                            }
                                            else {
                                                num++;
                                                store = true;
                                                storeOut = true;
                                            }
                                        }
                                    }
                                    if (store) {
                                        orderEntries.add(entry);
                                        Log.d(TAG, "Adding orderEntry to list of OrderEntries");
                                    }
                                }
                            }
                            if (storeOut) {
                                orders.add(new OrderList(orderList.get(i).order_number,
                                        orderList.get(i).order_date, orderEntries));
                                Log.d(TAG, "Adding list of OrderEntries to list of orderList");
                            }
                        }
                        switch (num) {
                            case 0:
                                // TODO show a message no matching products matching found
                                Toast.makeText(this, "Sorry, no matching products found", Toast.LENGTH_LONG).show();
                                break;
                            case 1:
                                Toast.makeText(this, "It is the unique case, showing product", Toast.LENGTH_LONG).show();
                                fragment = new OrderEntryFragment();
                                bundle.putString(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_REFUND_PRODUCT);
                                bundle.putString(ActivityDetector.ORDER_NUMBER, "Order#: " + orders.get(0).order_number);
                                bundle.putString(ActivityDetector.ORDER_DATE, "Order Date: " + orders.get(0).order_date);
                                bundle.putParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST,
                                        (ArrayList<OrderEntry>) orders.get(0).items);
                                fragment.setArguments(bundle);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .add(R.id.orderListContainer, fragment)
                                        .commit();
                                break;
                            default:
                                //Open OrderListFragment with matching products
                                Toast.makeText(this, "Multiple products found", Toast.LENGTH_LONG).show();
                                OrderListFragment orderListFragment = new OrderListFragment();
                                bundle.putString(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_REFUND_PRODUCT);
                                bundle.putParcelableArrayList(ActivityDetector.ORDER_LIST,
                                        (ArrayList<OrderList>) orders);
                                orderListFragment.setArguments(bundle);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .add(R.id.orderListContainer, orderListFragment)
                                        .commit();
                                break;
                        }
                        break;
                    case ActivityDetector.MODE_RETURN_DEFAULT:
                        index = -1;
                        orders = new ArrayList<>();
                        for(int i = 0; i < orderList.size(); i++) {
                            list = orderList.get(i).items;
                            List<OrderEntry> orderEntries = new ArrayList<>();
                            for(int j = 0; j < list.size(); j++) {
                                OrderEntry entry = list.get(j);
                                if (entry.delivered) {
                                    index = i;
                                    orderEntries.add(entry);
                                    break;
                                }
                            }
                            if (index > 0) {
                                orders.add(new OrderList(orderList.get(i).order_number,
                                        orderList.get(i).order_date, orderEntries));
                                break;
                            }
                        }
                        fragment = new OrderEntryFragment();
                        bundle.putString(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_PRODUCT);
                        bundle.putString(ActivityDetector.ORDER_NUMBER, "Order#: " + orders.get(0).order_number);
                        bundle.putString(ActivityDetector.ORDER_DATE, "Order Date: " + orders.get(0).order_date);
                        bundle.putParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST,
                                (ArrayList<OrderEntry>) orders.get(0).items);
                        fragment.setArguments(bundle);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.orderListContainer, fragment)
                                .commit();
                        break;
                    case ActivityDetector.MODE_RETURN_PRODUCT:
                        productName = intent.getStringExtra(ActivityDetector.ENTITY_PRODUCT);
                        productColor = intent.getStringExtra(ActivityDetector.ENTITY_COLOR);
                        productBrand = intent.getStringExtra(ActivityDetector.ENTITY_BRAND);
                        num = 0;
                        orders = new ArrayList<>();
                        for(int i = 0; i < orderList.size(); i++) {
                            list = orderList.get(i).items;
                            List<OrderEntry> orderEntries = new ArrayList<>();
                            boolean storeOut = false;
                            for (int j = 0; j < list.size(); j++) {
                                OrderEntry entry = list.get(j);
                                String name = entry.title;
                                String color = entry.color;
                                String brand = entry.brand;
                                if(entry.returned || !entry.delivered) {
                                    // Do nothing
                                }
                                else {
                                    boolean store = false;

                                    if (name.toLowerCase().contains(productName.toLowerCase())) {
                                        if (productColor != null) {
                                            if (productColor.equalsIgnoreCase(color)) {
                                                //If color is present from user input we compare and
                                                // only validate if color and name match
                                                if (productBrand != null) {
                                                    if(productBrand.equalsIgnoreCase(brand)) {
                                                        num++;
                                                        store = true;
                                                        storeOut = true;
                                                    }
                                                }
                                                else {
                                                    num++;
                                                    store = true;
                                                    storeOut = true;
                                                }
                                            }
                                        }
                                        else {
                                            if (productBrand != null) {
                                                if(productBrand.equalsIgnoreCase(brand)) {
                                                    num++;
                                                    store = true;
                                                    storeOut = true;
                                                }
                                            }
                                            else {
                                                num++;
                                                store = true;
                                                storeOut = true;
                                            }
                                        }
                                    }
                                    if (store) {
                                        orderEntries.add(entry);
                                        Log.d(TAG, "Adding orderEntry to list of OrderEntries");
                                    }
                                }
                            }
                            if (storeOut) {
                                orders.add(new OrderList(orderList.get(i).order_number,
                                        orderList.get(i).order_date, orderEntries));
                                Log.d(TAG, "Adding list of OrderEntries to list of orderList");
                            }
                        }
                        switch (num) {
                            case 0:
                                // TODO show a message no matching products matching found
                                //TODO for the case where customer has asked to return an order
                                // not yet delivered, ask user to cancel order
                                Toast.makeText(this, "Sorry, no matching products found", Toast.LENGTH_LONG).show();
                                finish();
                                break;
                            case 1:
                                Toast.makeText(this, "Your item will be returned", Toast.LENGTH_LONG).show();
                                fragment = new OrderEntryFragment();
                                bundle.putString(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_RETURN_PRODUCT);
                                bundle.putString(ActivityDetector.ORDER_NUMBER, "Order#: " + orders.get(0).order_number);
                                bundle.putString(ActivityDetector.ORDER_DATE, "Order Date: " + orders.get(0).order_date);
                                bundle.putParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST,
                                        (ArrayList<OrderEntry>) orders.get(0).items);
                                fragment.setArguments(bundle);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .add(R.id.orderListContainer, fragment)
                                        .commit();
                                break;
                            default:
                                //Open OrderListFragment with matching products
                                Toast.makeText(this, "Multiple products found", Toast.LENGTH_LONG).show();
                                OrderListFragment orderListFragment = new OrderListFragment();
                                bundle.putString(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_RETURN_PRODUCT);
                                bundle.putParcelableArrayList(ActivityDetector.ORDER_LIST,
                                        (ArrayList<OrderList>) orders);
                                orderListFragment.setArguments(bundle);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .add(R.id.orderListContainer, orderListFragment)
                                        .commit();
                                break;
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.orderListContainer, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}
