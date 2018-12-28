package com.example.mockapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.mockapp.network.ImageRequester;
import com.example.mockapp.network.OrderEntry;
import com.example.mockapp.slang.ActivityDetector;

import java.util.List;

import static android.view.View.GONE;


public class OrderEntryFragment extends Fragment {
    private static final String TAG = OrderEntryFragment.class.getSimpleName();

    private MaterialButton featured;
    private MaterialButton myAccount, feedback;
    private String orderNumber;
    private String orderDateString;
    private List<OrderEntry> orderEntries;
    private OrderEntry orderEntry;
    private TextView orderNum;
    private TextView orderDate;
    private String mode;

    private TextView title;
    private TextView brand;
    private TextView price;
    private TextView status;
    private TextView location;
    private TextView deliveryDate;
    private TextView returnDate;
    private TextView pickUpDate;
    private NetworkImageView orderImage;
    private ImageView cancelledOverlay;
    private MaterialButton cancelButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mode = bundle.getString(ActivityDetector.ACTIVITY_MODE);
            Log.d(TAG, "Mode is " + mode);
            orderNumber = bundle.getString(ActivityDetector.ORDER_NUMBER);
            orderDateString = bundle.getString(ActivityDetector.ORDER_DATE);
            //TODO convert this to single item
            orderEntries = bundle.getParcelableArrayList(ActivityDetector.ORDER_ENTRY_LIST);
            orderEntry = orderEntries.get(0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.shr_order_entry_fragment, container, false);

        setUpToolbar(view);

        orderImage = view.findViewById(R.id.order_entry_image);
        title = view.findViewById(R.id.order_entry_title);
        brand = view.findViewById(R.id.order_entry_brand);
        price = view.findViewById(R.id.order_entry_price);
        status = view.findViewById(R.id.order_entry_status);
        location = view.findViewById(R.id.order_entry_location);
        deliveryDate = view.findViewById(R.id.order_entry_delivery);
        returnDate = view.findViewById(R.id.order_entry_return);
        pickUpDate = view.findViewById(R.id.order_entry_pickup);
        cancelledOverlay = view.findViewById(R.id.cancelled);
        cancelledOverlay.setVisibility(View.INVISIBLE);
        cancelButton = view.findViewById(R.id.cancel_button);

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
                ((NavigationHost) getActivity()).navigateTo(new OrderListFragment(), false, ActivityDetector.TAG_ORDER_LIST);
            }
        });

        feedback = view.findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), FeedbackActivity.class));
            }
        });

        orderNum = view.findViewById(R.id.order_entry_number);
        orderDate = view.findViewById(R.id.order_entry_date);
        //recyclerView = view.findViewById(R.id.recycler_view_order_entry);

        orderNum.setText(orderNumber);
        orderDate.setText(orderDateString);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                ActivityDetector.PREFERENCES, Context.MODE_PRIVATE
        );
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        returnDate.setVisibility(GONE);
        pickUpDate.setVisibility(GONE);
        final String date = String.valueOf(new java.sql.Date(System.currentTimeMillis()));

        final OrderEntry entry = orderEntry;
        ImageRequester imageRequester = ImageRequester.getInstance();
        imageRequester.setImageFromUrl(orderImage, entry.url);
        title.setText(entry.title);
        brand.setText(entry.brand);

        boolean present = sharedPreferences.getBoolean(entry.title + ActivityDetector.PREF_KEY_BOOL, false);
        String pref_mode = sharedPreferences.getString(entry.title, "");
        String pref_date = sharedPreferences.getString(entry.title + ActivityDetector.PREF_KEY_DATE, "");

        if (entry.delivered)
            cancelButton.setText("Return");
        else
            cancelButton.setText("Cancel");
        if (entry.returned || (pref_mode.equals(ActivityDetector.RETURN_PREF))) {
            cancelButton.setVisibility(GONE);
            status.setTextColor(Color.rgb(255, 102, 0));
            location.setVisibility(GONE);
            String return_date;
            if(present)
                return_date = "Return Date: " + pref_date;
            else
                return_date = "Return Date: " + entry.return_date;
            returnDate.setText(return_date);
            returnDate.setVisibility(View.VISIBLE);
            String pickup;
            if (entry.pickup) {
                pickup = "Pick-up Date: " + entry.pickup_date;
            }
            else {
                pickup = "Estimated pick-up: " + entry.pickup_date;
            }
            pickUpDate.setText(pickup);
            pickUpDate.setVisibility(View.VISIBLE);
        } else if (entry.cancelled || (pref_mode.equals(ActivityDetector.CANCEL_PREF))) {
            cancelButton.setVisibility(GONE);
            status.setTextColor(Color.RED);
            cancelledOverlay.setVisibility(View.VISIBLE);
            location.setVisibility(GONE);
            String cancellation_date;
            if(present)
                cancellation_date = "Cancellation Date: " + pref_date;
            else
                cancellation_date = "Cancellation Date: " + entry.cancel_date;
            returnDate.setText(cancellation_date);
            returnDate.setVisibility(View.VISIBLE);
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOnClickListener(entry, date);
            }
        });

        String p = "INR " + entry.price;
        price.setText(p);
        if (mode.equals(ActivityDetector.MODE_CANCEL_PRODUCT)
                || mode.equals(ActivityDetector.MODE_CANCEL_DEFAULT)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirmation for " + entry.title +" by " + entry.brand);
            builder.setMessage("Are you sure you want to proceed?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancelledOverlay.setVisibility(View.VISIBLE);
                    status.setText(R.string.cancelled);
                    status.setTextColor(Color.RED);
                    //cancelButton.setEnabled(false);
                    cancelButton.setVisibility(GONE);
                    String cancellation_date = "Cancellation Date: " + date;
                    returnDate.setText(cancellation_date);
                    returnDate.setVisibility(View.VISIBLE);
                    editor.putBoolean(entry.title + ActivityDetector.PREF_KEY_BOOL, true);
                    editor.putString(entry.title, ActivityDetector.CANCEL_PREF);
                    editor.putString(entry.title + ActivityDetector.PREF_KEY_DATE, date);
                    editor.apply();
                    entry.cancelled = true;
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.darkRed));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.darkGreen));
        } else if(mode.equals(ActivityDetector.MODE_RETURN_PRODUCT)
                || mode.equals(ActivityDetector.MODE_RETURN_DEFAULT)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirmation");
            builder.setMessage("Are you sure you want to proceed?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    status.setText(R.string.returned);
                    status.setTextColor(Color.rgb(255, 102, 0));
                    //cancelButton.setEnabled(false);
                    cancelButton.setVisibility(GONE);
                    String return_date = "Return Date: " + date;
                    returnDate.setText(return_date);
                    location.setVisibility(View.GONE);
                    editor.putBoolean(entry.title + ActivityDetector.PREF_KEY_BOOL, true);
                    editor.putString(entry.title, ActivityDetector.RETURN_PREF);
                    editor.putString(entry.title + ActivityDetector.PREF_KEY_DATE, date);
                    editor.apply();
                    entry.returned = true;
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.darkRed));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.darkGreen));
        } else {
            if(!pref_mode.equals(""))
                status.setText(pref_mode);
            else
                status.setText(entry.status);
        }
        String locationText = "Currently at: " + entry.location;
        location.setText(locationText);
        if (entry.delivered && !entry.returned) {
            String deliveryDateText = "Delivery Date: " + entry.delivery_date;
            deliveryDate.setText(deliveryDateText);
        } else
            deliveryDate.setVisibility(GONE);

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

    public void setOnClickListener(final OrderEntry entry, final String date) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                ActivityDetector.PREFERENCES, Context.MODE_PRIVATE
        );
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirmation for " + entry.title +" by " + entry.brand);
        builder.setMessage("Are you sure you want to proceed?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (entry.delivered) {
                    status.setText(R.string.returned);
                    status.setTextColor(Color.rgb(255, 102, 0));
                    //cancelButton.setEnabled(false);
                    cancelButton.setVisibility(GONE);
                    String return_date = "Return Date: " + date;
                    returnDate.setText(return_date);
                    returnDate.setVisibility(View.VISIBLE);
                    location.setVisibility(View.GONE);
                    editor.putBoolean(entry.title + ActivityDetector.PREF_KEY_BOOL, true);
                    editor.putString(entry.title, ActivityDetector.RETURN_PREF);
                    editor.putString(entry.title + ActivityDetector.PREF_KEY_DATE, date);
                    editor.apply();
                } else {
                    cancelledOverlay.setVisibility(View.VISIBLE);
                    status.setText(R.string.cancelled);
                    status.setTextColor(Color.RED);
                    //cancelButton.setEnabled(false);
                    cancelButton.setVisibility(GONE);
                    String cancellation_date = "Cancellation Date: " + date;
                    returnDate.setText(cancellation_date);
                    returnDate.setVisibility(View.VISIBLE);
                    location.setVisibility(View.GONE);
                    entry.cancelled = true;
                    editor.putBoolean(entry.title + ActivityDetector.PREF_KEY_BOOL, true);
                    editor.putString(entry.title, ActivityDetector.CANCEL_PREF);
                    editor.putString(entry.title + ActivityDetector.PREF_KEY_DATE, date);
                    editor.apply();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.darkRed));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.darkGreen));
    }

    public OrderEntry getOrderEntry() {
        return orderEntry;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getOrderDateString() {
        return orderDateString;
    }
}