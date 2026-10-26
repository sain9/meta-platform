package com.hussain.data_api_client.model;

import java.util.LinkedHashMap;

/**
 * Convenience type alias — one row from a READ.
 * Preserves column order from the SELECT.
 */
public class Row extends LinkedHashMap<String, Object> {
    public Row() { super(); }
}