package com.app.courier.adapters;

import com.apollographql.apollo.CustomTypeAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rafaelgontijo on 25/10/17.
 */

public class DateTimeTypeAdapter implements CustomTypeAdapter<Date> {

    @Override
    public Date decode(String value) {
        try {
            Date decodedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(value);
            return decodedDate;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String encode(Date value) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(value);
    }
}
