/*package com.example.mockapp.network;


import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


import com.example.mockapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class OrderList implements Parcelable {
    public final String order_number;
    //TODO change to Date datatype
    public final String order_date;
    public List<OrderEntry> items = null;
    public final static Creator<OrderList> CREATOR = new Creator<OrderList>() {


        @SuppressWarnings({
                "unchecked"
        })
        public OrderList createFromParcel(Parcel in) {
            return new OrderList(in);
        }

        public OrderList[] newArray(int size) {
            return (new OrderList[size]);
        }

    }
            ;

    public OrderList(String order_number, String order_date, List<OrderEntry> items) {
        this.order_number = order_number;
        this.order_date = order_date;
        this.items = items;
    }

    public static List<OrderList> initOrderList(Resources resources) {
        InputStream inputStream = resources.openRawResource(R.raw.order);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int pointer;
            while ((pointer = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, pointer);
            }
        } catch (IOException exception) {
            Log.e(TAG, "Error writing/reading from the JSON file.", exception);
        } finally {
            try {
                inputStream.close();
            } catch (IOException exception) {
                Log.e(TAG, "Error closing the input stream.", exception);
            }
        }
        String jsonProductsString = writer.toString();
        Gson gson = new Gson();
        Type orderListType = new TypeToken<ArrayList<OrderList>>() {
        }.getType();
        return gson.fromJson(jsonProductsString, orderListType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(order_number);
        dest.writeValue(order_date);
        dest.writeList(items);
    }

    protected OrderList(Parcel in) {
        this.order_number = ((String) in.readValue((String.class.getClassLoader())));
        this.order_date = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.items, (com.example.mockapp.network.OrderEntry.class.getClassLoader()));
    }
}*/
package com.example.mockapp.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Type;
import android.util.Log;

import com.example.mockapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class OrderList implements Parcelable {

    public String order_number;
    public String order_date;
    public List<OrderEntry> items = new ArrayList<>();
    private static final String TAG = OrderList.class.getSimpleName();
    public final static Parcelable.Creator<OrderList> CREATOR = new Creator<OrderList>() {


        @SuppressWarnings({
                "unchecked"
        })
        public OrderList createFromParcel(Parcel in) {
            return new OrderList(in);
        }

        public OrderList[] newArray(int size) {
            return (new OrderList[size]);
        }

    };

    public OrderList(String order_number, String order_date, List<OrderEntry> items) {
        this.order_number = order_number;
        this.order_date = order_date;
        this.items = items;
    }

    protected OrderList(Parcel in) {
        this.order_number = ((String) in.readValue((String.class.getClassLoader())));
        this.order_date = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.items, (com.example.mockapp.network.OrderEntry.class.getClassLoader()));
    }

    public OrderList() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(order_number);
        dest.writeValue(order_date);
        dest.writeList(items);
    }

    public int describeContents() {
        return 0;
    }

    public static List<OrderList> initOrderList(Resources resources) {
        InputStream inputStream = resources.openRawResource(R.raw.order);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int pointer;
            while ((pointer = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, pointer);
            }
        } catch (IOException exception) {
            Log.e(TAG, "Error writing/reading from the JSON file.", exception);
        } finally {
            try {
                inputStream.close();
            } catch (IOException exception) {
                Log.e(TAG, "Error closing the input stream.", exception);
            }
        }
        String jsonProductsString = writer.toString();
        Gson gson = new Gson();
        Type orderListType = new TypeToken<ArrayList<OrderList>>() {
        }.getType();
        return gson.fromJson(jsonProductsString, orderListType);
    }
}
