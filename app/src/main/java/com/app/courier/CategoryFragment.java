package com.app.courier;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.app.courier.adapters.ProductAdapter;
import com.app.courier.utils.Constants;
import com.app.courier.utils.GridSpacingItemDecoration;
import com.app.courier.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import apollo.api.AllCategoriesSearchQuery;
import apollo.api.PocCategorySearchQuery;
import okhttp3.OkHttpClient;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_CATEGORY_ID = "ARG_CATEGORY_ID";
    private static final String ARG_POC_ID = "ARG_POC_ID";

    private View rootView;
    private TextView contentTextView;
    private RecyclerView recyclerView;

    private OkHttpClient client;
    private ApolloClient apolloClient;

    private int categoryId;
    private String pocId;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param categoryId Parameter 1.
     * @param pocId Parameter 2
     * @return A new instance of fragment CategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryFragment newInstance(int categoryId, String pocId) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_POC_ID, pocId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getInt(ARG_CATEGORY_ID);
            pocId = getArguments().getString(ARG_POC_ID);
        }

        client = new OkHttpClient();
        apolloClient = ApolloClient.builder()
                .serverUrl(Constants.GRAPHQL_URL)
                .okHttpClient(this.client)
                .build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_category, container, false);
        contentTextView = rootView.findViewById(R.id.fragment_cat_text_content);
        recyclerView = rootView.findViewById(R.id.fragment_cat_recycler_view);
        recyclerView.setHasFixedSize(true);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_space);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, false));
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void loadData() {
        apolloClient.query(PocCategorySearchQuery.builder().id(pocId).categoryId(categoryId).search("").build())
                .enqueue(new ApolloCall.Callback<PocCategorySearchQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<PocCategorySearchQuery.Data> response) {
                        final List<PocCategorySearchQuery.Product> products = response.data().poc().products();
                        if(getActivity() == null) { return; }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(products.size() == 0) {
                                    contentTextView.setText(R.string.no_products_of_category);
                                } else {
                                    final List<PocCategorySearchQuery.ProductVariant> variants = new ArrayList<>();
                                    for(int i = 0; i < products.size(); i++) {
                                        PocCategorySearchQuery.Product current = products.get(i);
                                        for(int j = 0; j < current.productVariants().size(); j++) {
                                            variants.add(current.productVariants().get(j));
                                        }
                                    }

                                    populateRecyclerView(variants);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e("ERR", e.toString());
                        ViewHelper.showSimpleSnackbarMessage(rootView, R.string.default_err_mssg);
                    }
                });
    }

    private void populateRecyclerView(List<PocCategorySearchQuery.ProductVariant> variants) {
        ProductAdapter adapter = new ProductAdapter(getContext(), variants, this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        PocCategorySearchQuery.ProductVariant selectedProduct = ((ProductAdapter) recyclerView.getAdapter()).getItem(itemPosition);
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_DESCRIPTION, selectedProduct.title());
        intent.putExtra(ProductDetailActivity.EXTRA_IMG_URL, selectedProduct.imageUrl());
        intent.putExtra(ProductDetailActivity.EXTRA_PRICE, selectedProduct.price());
        getActivity().startActivity(intent);
    }
}
