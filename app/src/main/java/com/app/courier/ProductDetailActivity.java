package com.app.courier;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.NumberFormat;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_IMG_URL = "EXTRA_IMG_URL";
    public static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    public static final String EXTRA_PRICE = "EXTRA_PRICE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Double price = getIntent().getDoubleExtra(EXTRA_PRICE, 0);
        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        String img = getIntent().getStringExtra(EXTRA_IMG_URL);

        NumberFormat formatter = NumberFormat.getCurrencyInstance();

        TextView header = (TextView) findViewById(R.id.activity_detail_header);
        header.setText(description);

        TextView details = (TextView) findViewById(R.id.activity_detail_details);
        details.setText(formatter.format(price));

        ImageView imgView = (ImageView) findViewById(R.id.activity_detail_img);
        Picasso.with(this).load(img).into(imgView);

        findViewById(R.id.activity_detail_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
