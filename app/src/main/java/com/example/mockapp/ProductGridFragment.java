package com.example.mockapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;


import com.example.mockapp.network.ProductEntry;
import com.example.mockapp.staggeredgridlayout.StaggeredProductCardRecyclerViewAdapter;

import java.util.List;

import in.slanglabs.platform.ui.SlangUI;

public class ProductGridFragment extends Fragment {

    private final static String TAG = ProductGridFragment.class.getSimpleName();

    StaggeredProductCardRecyclerViewAdapter mAdapter;
    RecyclerView recyclerView;
    List<ProductEntry> productList;
    MaterialButton myAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment with the ProductGrid theme
        View view = inflater.inflate(R.layout.shr_product_grid_fragment, container, false);

        // Set up the tool bar
        setUpToolbar(view);

        myAccount = view.findViewById(R.id.my_orders);
        myAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Toast.makeText(v.getContext(), "IT WORKS OMG OMG", Toast.LENGTH_LONG).show();
                Log.d(TAG, "******* Ready to navigate to new activity");*/
                startActivity(new Intent(getActivity(), OrderListActivity.class));
            }
        });

        // Set up the RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position % 3 == 2 ? 2 : 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        productList = ProductEntry.initProductEntryList(getResources());
        mAdapter = new StaggeredProductCardRecyclerViewAdapter(productList);
        recyclerView.setAdapter(mAdapter);
        int largePadding = getResources().getDimensionPixelSize(R.dimen.shr_staggered_product_grid_spacing_large);
        int smallPadding = getResources().getDimensionPixelSize(R.dimen.shr_staggered_product_grid_spacing_small);
        recyclerView.addItemDecoration(new ProductGridItemDecoration(largePadding, smallPadding));

        // Set cut corner background for API 23+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.findViewById(R.id.product_grid).setBackground(getContext().getDrawable(R.drawable.shr_product_grid_background_shape));
        }

        setClickProduct();

        SlangUI.showTrigger();

        return view;
    }

    private void setClickProduct() {
        mAdapter.setmClickListener(new StaggeredProductCardRecyclerViewAdapter.ProductClickListener() {
            @Override
            public void onProductClick(View view, int position) {
                /*Toast.makeText(view.getContext(), "Item clicked at position " + position + 1, Toast.LENGTH_LONG).show();
                Log.d(TAG, "******** ITEM CLICKED... at position " + position);*/
            }
        });
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
