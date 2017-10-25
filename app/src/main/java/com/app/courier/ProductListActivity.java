package com.app.courier;

import android.content.Context;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.app.courier.adapters.DateTimeTypeAdapter;
import com.app.courier.adapters.TimeTypeAdapter;
import com.app.courier.utils.Constants;
import com.app.courier.utils.ViewHelper;

import java.util.List;

import javax.annotation.Nonnull;

import apollo.api.AllCategoriesSearchQuery;
import apollo.api.PocCategorySearchQuery;
import apollo.api.type.CustomType;
import okhttp3.OkHttpClient;

public class ProductListActivity extends AppCompatActivity {

    public static final String ID_EXTRA = "ID_EXTRA";
    public static final String ADDRESS_EXTRA = "ADDRESS_EXTRA";

    private View rootView;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private OkHttpClient client;
    private ApolloClient apolloClient;

    private String pocId;

    List<AllCategoriesSearchQuery.AllCategory> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        rootView = findViewById(R.id.activity_prod_root);
        viewPager = (ViewPager) findViewById(R.id.activity_prod_view_pager);
        tabLayout = (TabLayout) findViewById(R.id.activity_prod_tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        findViewById(R.id.activity_prod_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        client = new OkHttpClient();
        apolloClient = ApolloClient.builder()
                .serverUrl(Constants.GRAPHQL_URL)
                .okHttpClient(this.client)
                .build();

        String address = getIntent().getStringExtra(ADDRESS_EXTRA);
        TextView title = (TextView) findViewById(R.id.activity_prod_title);
        title.setText(address);

        this.pocId = getIntent().getStringExtra(ID_EXTRA);

        getCategories();


    }

    private void getCategories() {
        apolloClient.query(AllCategoriesSearchQuery.builder().build())
                .enqueue(new ApolloCall.Callback<AllCategoriesSearchQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<AllCategoriesSearchQuery.Data> response) {
                        AllCategoriesSearchQuery.Data data = response.data();
                        categories = data.allCategory();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setViewPager(categories);
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

    private void setViewPager(List<AllCategoriesSearchQuery.AllCategory> categories) {
        viewPager.setAdapter(new ProductsFragmentAdapter(getSupportFragmentManager(), this, categories));
        tabLayout.setupWithViewPager(viewPager);
    }


    public class ProductsFragmentAdapter extends FragmentPagerAdapter {

        private Context context;
        private List<AllCategoriesSearchQuery.AllCategory> categories;
        private CategoryFragment[] fragments;

        public ProductsFragmentAdapter(FragmentManager fm, Context context, List<AllCategoriesSearchQuery.AllCategory> categories) {
            super(fm);
            this.context = context;
            this.categories = categories;
            this.fragments = new CategoryFragment[categories.size()];
        }

        @Override
        public Fragment getItem(int position) {
            if(fragments[position] == null) {
                fragments[position] = CategoryFragment.newInstance(Integer.parseInt(categories.get(position).id()), pocId);
            }
            return fragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categories.get(position).title();
        }

        @Override
        public int getCount() {
            return categories.size();
        }
    }
}
