package com.app.courier;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.CustomTypeAdapter;
import com.apollographql.apollo.exception.ApolloException;
import com.app.courier.adapters.DateTimeTypeAdapter;
import com.app.courier.adapters.PlacesAdapter;
import com.app.courier.adapters.TimeTypeAdapter;
import com.app.courier.models.PlaceQueryResult;
import com.app.courier.utils.Constants;
import com.app.courier.utils.SimpleDividerItemDecoration;
import com.app.courier.utils.ViewHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

import apollo.api.PocSearchMethodQuery;
import apollo.api.type.CustomType;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity implements Callback, View.OnClickListener {

    private EditText addressInput;
    private ImageButton cancelButton;
    private ProgressBar progressBar;
    private ProgressBar horizontalProgressBar;
    private RecyclerView recyclerView;

    private OkHttpClient client;
    private ApolloClient apolloClient;

    private View rootView;

    private String lastTextSearched;

    private PlacesAdapter adapter;
    private List<PlaceQueryResult> places = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rootView = findViewById(R.id.home_root);

        client = new OkHttpClient();
        apolloClient = ApolloClient.builder()
                .serverUrl(Constants.GRAPHQL_URL)
                .okHttpClient(this.client)
                .addCustomTypeAdapter(CustomType.DATETIME, new DateTimeTypeAdapter())
                .addCustomTypeAdapter(CustomType.TIME, new TimeTypeAdapter())
                .build();

        progressBar = (ProgressBar) findViewById(R.id.activity_home_progress);
        horizontalProgressBar = (ProgressBar) findViewById(R.id.activity_home_horizontal_pb);

        addressInput = (EditText) findViewById(R.id.activity_home_address_input);
        addressInput.addTextChangedListener(searchTextWatcher);

        cancelButton = (ImageButton) findViewById(R.id.activity_home_cancel_btn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addressInput.setText("");
                places.clear();
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.activity_home_recycler_view);
        recyclerView.setHasFixedSize(true);
        adapter = new PlacesAdapter(places, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
    }

    protected void onStart() {
        super.onStart();
    }


    private void performPlaceQuery(String query) {
        progressBar.setVisibility(View.VISIBLE);
        String url = String.format("%s?input=%s&key=%s", Constants.GOOGLE_AUTOCOMPLETE_URL, query, Constants.GOOLE_API_KEY);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(this);
    }

    private TextWatcher searchTextWatcher = new TextWatcher() {

        private Timer timer;

        @Override
        public void afterTextChanged(Editable arg0) {
            // user typed: start the timer
            int length = arg0.toString().length();

            if(length == 0)
                cancelButton.setVisibility(View.INVISIBLE);
            else
                cancelButton.setVisibility(View.VISIBLE);

            if(length < 3) {
                progressBar.setVisibility(View.INVISIBLE);
                places.clear();
                adapter.notifyDataSetChanged();
                return;
            }

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        lastTextSearched = addressInput.getText().toString();
                        final String query = URLEncoder.encode(lastTextSearched, "UTF-8");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                performPlaceQuery(query);
                            }
                        });
                    } catch (Exception exc) {
                        Log.e("ERR", exc.toString());
                    }
                }
            }, 400); // 400ms delay before the timer executes the „run“ method from TimerTask
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // nothing to do here
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // user is typing: reset already started timer (if existing)
            if (timer != null) {
                timer.cancel();
            }
        }
    };


    @Override
    public void onFailure(Call call, IOException e) {
        progressBar.setVisibility(View.INVISIBLE);
        horizontalProgressBar.setVisibility(View.INVISIBLE);
        Log.e("ERR", e.toString());
        ViewHelper.showSimpleSnackbarMessage(rootView, R.string.default_err_mssg);
    }

    @Override
    public void onResponse(Call call, final Response response) throws IOException {

        String requestUrl = call.request().url().toString();
        if(requestUrl.contains(Constants.GOOGLE_AUTOCOMPLETE_URL)) {
            resolveAutocomplete(response);
        } else if(requestUrl.contains(Constants.GOOGLE_ADDRESS_URL)) {
            resolveAddress(response);
        }

    }

    private void resolveAutocomplete(final Response response) {
        if(!addressInput.getText().toString().equals(this.lastTextSearched)) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resolveRequest(response);
            }
        });
    }

    private void resolveAddress(Response response) {
        try {
            String jsonData = response.body().string();
            JSONObject json = new JSONObject(jsonData);
            JSONObject location = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
            searchPOC(location.getDouble("lat"), location.getDouble("lng"));
        } catch (Exception exc) {
            Log.e("ERR", exc.toString());
            ViewHelper.showSimpleSnackbarMessage(rootView, R.string.default_err_mssg);
        }
    }

    private void resolveRequest(Response response) {
        progressBar.setVisibility(View.INVISIBLE);
        places.clear();
        try {
            String jsonData = response.body().string();
            JSONObject json = new JSONObject(jsonData);
            JSONArray predictions = json.getJSONArray("predictions");
            for(int i = 0; i < predictions.length(); i++) {
                PlaceQueryResult place = new PlaceQueryResult(predictions.getJSONObject(i));
                places.add(place);
            }

        } catch (Exception exc) {
            Log.e("ERR", exc.toString());
        }
        adapter.notifyDataSetChanged();
    }

    private void searchPOC(double lat, double lng) {
        apolloClient.query(PocSearchMethodQuery.builder()
                .lat(Double.toString(lat)).lng(Double.toString(lng)).algorithm("NEAREST").now(new Date()).build())
                .enqueue(new ApolloCall.Callback<PocSearchMethodQuery.Data>() {

                    @Override
                    public void onResponse(@Nonnull com.apollographql.apollo.api.Response<PocSearchMethodQuery.Data> response) {

                        PocSearchMethodQuery.Data data = response.data();
                        if(data.pocSearch().size() > 0) {
                            PocSearchMethodQuery.PocSearch pocSearchResult = data.pocSearch().get(0);
                            Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
                            intent.putExtra(ProductListActivity.ID_EXTRA, pocSearchResult.id());
                            intent.putExtra(ProductListActivity.ADDRESS_EXTRA, pocSearchResult.address().address1());
                            startActivity(intent);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                horizontalProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        horizontalProgressBar.setVisibility(View.INVISIBLE);
                        Log.e("ERR", e.toString());
                        ViewHelper.showSimpleSnackbarMessage(rootView, R.string.default_err_mssg);
                    }
                });
    }

    @Override
    public void onClick(View view) {
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        PlaceQueryResult place = places.get(itemPosition);
        try {
            final String query = URLEncoder.encode(place.getDescription(), "UTF-8");
            String url = String.format("%s?address=%s", Constants.GOOGLE_ADDRESS_URL, query);
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            horizontalProgressBar.setVisibility(View.VISIBLE);
            client.newCall(request).enqueue(this);

        } catch (Exception exc) {
            Log.e("ERR", exc.toString());
            ViewHelper.showSimpleSnackbarMessage(rootView, R.string.default_err_mssg);
        }

    }
}
