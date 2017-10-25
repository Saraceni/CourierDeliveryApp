package com.app.courier.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by rafaelgontijo on 25/10/17.
 */

public class ViewHelper {

    public static void showSimpleSnackbarMessage(View v, int text) {
        Snackbar.make(v, text, Snackbar.LENGTH_SHORT).show();
    }
}
