package in.slanglabs.shrinemock;

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


import com.example.mockapp.R;

import in.slanglabs.shrinemock.network.OrderList;
import in.slanglabs.shrinemock.slang.ActivityDetector;

import java.util.List;

public class OrderListFragment extends Fragment {

    private static final String TAG = OrderListFragment.class.getSimpleName();

    private OrderListRecyclerViewAdapter mAdapter;
    private RecyclerView recyclerViewOrder;
    private List<OrderList> orderList;
    //private boolean modeActivity;
    private MaterialButton featured, feedback;

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
                Intent startMainActivity = new Intent(getContext(), MainActivity.class);
                startMainActivity.putExtra("back", true);
                startActivity(startMainActivity);
            }
        });

        feedback = view.findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), FeedbackActivity.class));
            }
        });

        Bundle bundle = getArguments();
        String mode = "";
        if (bundle != null)
            mode = bundle.getString(ActivityDetector.ACTIVITY_MODE);
        else
            mode = ActivityDetector.MODE_NONE;
        if (!mode.isEmpty()) {
            if (mode.equals(ActivityDetector.MODE_TRACK_PRODUCT)
                    || mode.equals(ActivityDetector.MODE_REFUND_PRODUCT)
                    || mode.equals(ActivityDetector.MODE_RETURN_PRODUCT)
                    || mode.equals(ActivityDetector.MODE_CANCEL_PRODUCT)
                    || mode.equals(ActivityDetector.MODE_TRACK_RETURN)) {
                Log.d(TAG, "Mode is " + mode);
                orderList = bundle.getParcelableArrayList(ActivityDetector.ORDER_LIST);
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
        mAdapter = new OrderListRecyclerViewAdapter(getContext(), orderList, mode);
        recyclerViewOrder.setAdapter(mAdapter);
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
