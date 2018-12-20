package com.example.mockapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.mockapp.network.ImageRequester;
import com.example.mockapp.network.OrderEntry;
import com.example.mockapp.slang.ActivityDetector;


import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class OrderEntryRecyclerViewAdapter extends RecyclerView.Adapter<OrderEntryRecyclerViewAdapter.OrderEntryViewHolder> {
    private List<OrderEntry> orderEntries;
    public String mode;
    private Context context;

    public OrderEntryRecyclerViewAdapter(Context context, List<OrderEntry> orderEntries, String mode) {
        this.context = context;
        this.orderEntries = orderEntries;
        this.mode = mode;
    }

    @NonNull
    @Override
    public OrderEntryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        int layoutId = R.layout.shr_order_entry_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutId, viewGroup, false);

        return new OrderEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderEntryViewHolder orderEntryViewHolder, int i) {
        if (orderEntries != null && i < orderEntries.size()) {

            orderEntryViewHolder.returnDate.setVisibility(GONE);
            orderEntryViewHolder.pickUpDate.setVisibility(GONE);
            final String date = String.valueOf(new java.sql.Date(System.currentTimeMillis()));

            final OrderEntry entry = orderEntries.get(i);
            ImageRequester imageRequester = ImageRequester.getInstance();
            imageRequester.setImageFromUrl(orderEntryViewHolder.orderImage, entry.url);
            orderEntryViewHolder.title.setText(entry.title);
            orderEntryViewHolder.brand.setText(entry.brand);
            if (entry.delivered)
                orderEntryViewHolder.cancelButton.setText("Return");
            else
                orderEntryViewHolder.cancelButton.setText("Cancel");
            if (entry.returned) {
                orderEntryViewHolder.cancelButton.setVisibility(GONE);
                orderEntryViewHolder.status.setTextColor(Color.rgb(255, 102, 0));
                orderEntryViewHolder.location.setVisibility(GONE);
                String return_date = "Return Date: " + entry.return_date;
                orderEntryViewHolder.returnDate.setText(return_date);
                orderEntryViewHolder.returnDate.setVisibility(View.VISIBLE);
                String pickup;
                if (entry.pickup) {
                    pickup = "Pick-up Date: " + entry.pickup_date;
                }
                else {
                    pickup = "Estimated pick-up: " + entry.pickup_date;
                }
                orderEntryViewHolder.pickUpDate.setText(pickup);
                orderEntryViewHolder.pickUpDate.setVisibility(View.VISIBLE);
            } else if (entry.cancelled) {
                orderEntryViewHolder.cancelButton.setVisibility(GONE);
                orderEntryViewHolder.status.setTextColor(Color.RED);
                orderEntryViewHolder.cancelledOverlay.setVisibility(View.VISIBLE);
                orderEntryViewHolder.location.setVisibility(GONE);
                String cancellation_date = "Cancellation Date: " + entry.cancel_date;
                orderEntryViewHolder.returnDate.setText(cancellation_date);
                orderEntryViewHolder.returnDate.setVisibility(View.VISIBLE);
            }
            orderEntryViewHolder.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirmation for " + entry.title +" by " + entry.brand);
                    builder.setMessage("Are you sure you want to proceed?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (entry.delivered) {
                                orderEntryViewHolder.status.setText(R.string.returned);
                                orderEntryViewHolder.status.setTextColor(Color.rgb(255, 102, 0));
                                orderEntryViewHolder.cancelButton.setEnabled(false);
                                String return_date = "Return Date: " + date;
                                orderEntryViewHolder.returnDate.setText(return_date);
                                orderEntryViewHolder.returnDate.setVisibility(View.VISIBLE);
                            } else {
                                orderEntryViewHolder.cancelledOverlay.setVisibility(View.VISIBLE);
                                orderEntryViewHolder.status.setText(R.string.cancelled);
                                orderEntryViewHolder.status.setTextColor(Color.RED);
                                orderEntryViewHolder.cancelButton.setEnabled(false);
                                String cancellation_date = "Cancellation Date: " + date;
                                orderEntryViewHolder.returnDate.setText(cancellation_date);
                                orderEntryViewHolder.returnDate.setVisibility(View.VISIBLE);
                                orderEntryViewHolder.location.setVisibility(View.GONE);
                                entry.cancelled = true;
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
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.darkRed));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.darkGreen));
                }
            });

            String p = "INR " + entry.price;
            orderEntryViewHolder.price.setText(p);
            if (mode.equals(ActivityDetector.MODE_CANCEL_PRODUCT)
                    || mode.equals(ActivityDetector.MODE_CANCEL_DEFAULT)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirmation for " + entry.title +" by " + entry.brand);
                builder.setMessage("Are you sure you want to proceed?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        orderEntryViewHolder.cancelledOverlay.setVisibility(View.VISIBLE);
                        orderEntryViewHolder.status.setText(R.string.cancelled);
                        orderEntryViewHolder.status.setTextColor(Color.RED);
                        orderEntryViewHolder.cancelButton.setEnabled(false);
                        String cancellation_date = "Cancellation Date: " + date;
                        orderEntryViewHolder.returnDate.setText(cancellation_date);
                        orderEntryViewHolder.returnDate.setVisibility(View.VISIBLE);
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
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.darkRed));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.darkGreen));
            } else if(mode.equals(ActivityDetector.MODE_RETURN_PRODUCT)
                    || mode.equals(ActivityDetector.MODE_RETURN_DEFAULT)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to proceed?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        orderEntryViewHolder.status.setText(R.string.returned);
                        orderEntryViewHolder.status.setTextColor(Color.rgb(255, 102, 0));
                        String return_date = "Return Date: " + date;
                        orderEntryViewHolder.returnDate.setText(return_date);
                        orderEntryViewHolder.cancelButton.setEnabled(false);
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
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.darkRed));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.darkGreen));
            } else {
                orderEntryViewHolder.status.setText(entry.status);
            }
            String location = "Currently at: " + entry.location;
            orderEntryViewHolder.location.setText(location);
            if (entry.delivered && !entry.returned) {
                String deliveryDate = "Delivery Date: " + entry.delivery_date;
                orderEntryViewHolder.deliveryDate.setText(deliveryDate);
            } else
                orderEntryViewHolder.deliveryDate.setVisibility(GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (orderEntries == null)
            return 0;
        else
            return orderEntries.size();
    }


    public class OrderEntryViewHolder extends RecyclerView.ViewHolder {

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

        public OrderEntryViewHolder(@NonNull View view) {
            super(view);

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
        }
    }
}
