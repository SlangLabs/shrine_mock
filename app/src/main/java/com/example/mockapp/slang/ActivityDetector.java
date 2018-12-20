package com.example.mockapp.slang;

import javax.sql.StatementEvent;

public class ActivityDetector {
    public static final String ACTIVITY_MODE = "activity_mode";

    public static final String MODE_NONE = "none";
    public static final String MODE_TRACK_DEFAULT = "track_default";
    public static final String MODE_TRACK_PRODUCT = "track_product";
    public static final String MODE_TRACK_ALL = "track_all";
    public static final String MODE_TRACK_RETURN = "track_return";
    public static final String MODE_CANCEL_PRODUCT = "cancel";
    public static final String MODE_CANCEL_DEFAULT = "cancel_default";
    public static final String MODE_REFUND_DEFAULT = "refund_default";
    public static final String MODE_REFUND_PRODUCT = "refund_product";
    public static final String MODE_RETURN_DEFAULT = "return_default";
    public static final String MODE_RETURN_PRODUCT = "return_product";

    public static final String ORDER_DATE = "date";
    public static final String ORDER_NUMBER = "number";
    public static final String ORDER_ENTRY_LIST = "entry";
    public static final String ORDER_LIST = "orders";

    public static final String INTENT_TRACK_DEFAULT = "track_order_default";
    public static final String INTENT_TRACK_PRODUCT = "track_order_product";
    public static final String INTENT_TRACK_RETURN = "track_order_return";
    public static final String INTENT_TRACK_ALL = "track_order_all";
    public static final String INTENT_TRACK_REFUND_DEFAULT = "track_refund_default";
    public static final String INTENT_TRACK_REFUND_PRODUCT = "track_refund_product";
    public static final String INTENT_CANCEL_DEFAULT = "cancel_order_default";
    public static final String INTENT_CANCEL_PRODUCT = "cancel_order_product";
    public static final String INTENT_RETURN_PRODUCT = "return_order_product";
    public static final String INTENT_RETURN_DEFAULT = "return_order_default";

    public static final String ENTITY_PRODUCT = "product";
    public static final String ENTITY_BRAND = "brand";
    public static final String ENTITY_COLOR = "color";
}
