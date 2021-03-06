package in.slanglabs.shrinemock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.example.mockapp.R;

import in.slanglabs.shrinemock.network.OrderEntry;
import in.slanglabs.shrinemock.network.OrderList;
import in.slanglabs.shrinemock.slang.ActivityDetector;

import java.util.ArrayList;
import java.util.List;

public class OrderListActivity extends AppCompatActivity implements NavigationHost {

    private static final String TAG = OrderListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shr_order_activity);

        //SlangUI.showTrigger();

        Intent intent = getIntent();
        String mode = intent.getStringExtra(ActivityDetector.ACTIVITY_MODE);

        if (savedInstanceState == null) {
            if (mode == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.orderListContainer, new OrderListFragment(), ActivityDetector.TAG_ORDER_LIST)
                        .commit();
            } else {
                Bundle bundle = new Bundle();
                ArrayList<OrderList> orders = intent.getParcelableArrayListExtra(ActivityDetector.ORDER_LIST);
                Log.d(TAG, "Size of orders is up here " + orders.size());
                List<OrderEntry> list;
                switch (mode) {
                    case ActivityDetector.MODE_TRACK_ALL:
                        bundle.putString(ActivityDetector.ACTIVITY_MODE, mode);
                        OrderListFragment orderListFragment = new OrderListFragment();
                        orderListFragment.setArguments(bundle);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.orderListContainer, orderListFragment, ActivityDetector.TAG_ORDER_LIST)
                                .commit();
                        break;
                    case ActivityDetector.MODE_TRACK_DEFAULT:
                    case ActivityDetector.MODE_RETURN_DEFAULT:
                    case ActivityDetector.MODE_REFUND_DEFAULT:
                    case ActivityDetector.MODE_CANCEL_DEFAULT:
                        OrderEntryFragment orderEntryFragment = new OrderEntryFragment();
                        bundle.putString(ActivityDetector.ACTIVITY_MODE, mode);
                        bundle.putString(ActivityDetector.ORDER_NUMBER, "Order#: " + orders.get(0).order_number);
                        bundle.putString(ActivityDetector.ORDER_DATE, "Order Date: " + orders.get(0).order_date);
                        bundle.putParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST,
                                (ArrayList<OrderEntry>) orders.get(0).items);
                        orderEntryFragment.setArguments(bundle);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.orderListContainer, orderEntryFragment, ActivityDetector.TAG_ORDER_ENTRY)
                                .commit();
                        break;
                    case ActivityDetector.MODE_TRACK_PRODUCT:
                    case ActivityDetector.MODE_RETURN_PRODUCT:
                    case ActivityDetector.MODE_REFUND_PRODUCT:
                    case ActivityDetector.MODE_CANCEL_PRODUCT:
                    case ActivityDetector.MODE_TRACK_RETURN:
                        Log.d(TAG, "Size of orders is down here " + orders.size());
                        Log.d(TAG, "Mode is " + mode);
                        bundle.putString(ActivityDetector.ACTIVITY_MODE, mode);
                        if (orders.size() > 1) {
                            orderListFragment = new OrderListFragment();
                            bundle.putParcelableArrayList(ActivityDetector.ORDER_LIST,
                                    (ArrayList<OrderList>) orders);
                            orderListFragment.setArguments(bundle);
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.orderListContainer, orderListFragment, ActivityDetector.TAG_ORDER_LIST)
                                    .commit();
                        } else {
                            orderEntryFragment = new OrderEntryFragment();
                            bundle.putString(ActivityDetector.ORDER_NUMBER, "Order#: " + orders.get(0).order_number);
                            bundle.putString(ActivityDetector.ORDER_DATE, "Order Date: " + orders.get(0).order_date);
                            // truncate the list to show only the first item
                            list = orders.get(0).items;
                            bundle.putParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST, (ArrayList<OrderEntry>) list);
                            orderEntryFragment.setArguments(bundle);
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.orderListContainer, orderEntryFragment, ActivityDetector.TAG_ORDER_ENTRY)
                                    .commit();
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack, String tag) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.orderListContainer, fragment, tag);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}