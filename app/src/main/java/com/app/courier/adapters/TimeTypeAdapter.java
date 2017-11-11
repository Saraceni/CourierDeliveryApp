package com.app.courier.adapters;

import com.apollographql.apollo.CustomTypeAdapter;

/**
 * Created by rafaelgontijo on 25/10/17.
 */

public class TimeTypeAdapter implements CustomTypeAdapter<String> {

    @Override
    public String decode(String value) {
        return value;
    }

    @Override
    public String encode(String value) {
        return value;
    }
}
