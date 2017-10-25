package com.app.courier.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.courier.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import apollo.api.PocCategorySearchQuery;

/**
 * Created by rafaelgontijo on 25/10/17.
 */

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {


    private Context context;
    private List<PocCategorySearchQuery.ProductVariant> products;
    private NumberFormat formatter;
    private View.OnClickListener listener;

    public ProductAdapter(Context context, List<PocCategorySearchQuery.ProductVariant> products, View.OnClickListener listener) {
        this.context = context;
        this.products = products;
        this.formatter = NumberFormat.getCurrencyInstance();
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_product, parent, false);
        view.setOnClickListener(listener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PocCategorySearchQuery.ProductVariant variant = products.get(0);
        Picasso.with(this.context).load(variant.imageUrl()).into(holder.img);
        holder.price.setText(formatter.format(variant.price()));
        holder.name.setText(variant.title());
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public PocCategorySearchQuery.ProductVariant getItem(int position) {
        return this.products.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView img;
        private TextView price;
        private TextView name;

        public ViewHolder(View v) {
            super(v);
            img = v.findViewById(R.id.adapter_prod_image);
            name = v.findViewById(R.id.adapter_prod_desc1);
            price = v.findViewById(R.id.adapter_prod_price);
        }

    }
}
