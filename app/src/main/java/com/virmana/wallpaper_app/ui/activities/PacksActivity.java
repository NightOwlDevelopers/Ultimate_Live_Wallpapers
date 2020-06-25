package com.virmana.wallpaper_app.ui.activities;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.virmana.wallpaper_app.R;
import com.virmana.wallpaper_app.adapter.PackAdapter;
import com.virmana.wallpaper_app.api.apiClient;
import com.virmana.wallpaper_app.api.apiRest;
import com.virmana.wallpaper_app.entity.Pack;
import com.virmana.wallpaper_app.manager.PrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PacksActivity extends AppCompatActivity {

    private PackAdapter packAdapter;
    private List<Pack> packList= new ArrayList<>();
    private GridLayoutManager gridLayoutManager;


    private RelativeLayout relative_layout_packs_activity;
    private SwipeRefreshLayout swipe_refreshl_packs_activity;
    private ImageView image_view_empty;
    private RecyclerView recycle_view_packs_activity;
    private RelativeLayout relative_layout_load_more;
    private LinearLayout linear_layout_page_error;
    private Button button_try_again;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packs);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Packs list");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        showAdsBanner();
        initAction();
        loadPacks();
    }
    @Override
    public void onBackPressed(){
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
            return;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                    super.onBackPressed();
                    overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showAdsBanner() {
        PrefManager prefManager= new PrefManager(getApplicationContext());
        if (getString(R.string.AD_MOB_ENABLED_BANNER).equals("true")) {
            if (prefManager.getString("SUBSCRIBED").equals("FALSE")) {
                final AdView mAdView = (AdView) findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder()
                        .build();
                mAdView.loadAd(adRequest);

                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        mAdView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }
    private void initView() {
        
        this.relative_layout_packs_activity=(RelativeLayout) findViewById(R.id.relative_layout_packs_activity);
        this.swipe_refreshl_packs_activity=(SwipeRefreshLayout) findViewById(R.id.swipe_refreshl_packs_activity);
        this.image_view_empty=(ImageView) findViewById(R.id.image_view_empty);
        this.recycle_view_packs_activity=(RecyclerView) findViewById(R.id.recycle_view_packs_activity);
        this.linear_layout_page_error=(LinearLayout) findViewById(R.id.linear_layout_page_error);
        this.button_try_again=(Button) findViewById(R.id.button_try_again);

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);

            if (tabletSize) {
                this.gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,GridLayoutManager.VERTICAL,false);
            } else {
                this.gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,GridLayoutManager.VERTICAL,false);
            }


        this.packAdapter =new PackAdapter(packList,this );
        recycle_view_packs_activity.setHasFixedSize(true);
        recycle_view_packs_activity.setAdapter(packAdapter);
        recycle_view_packs_activity.setLayoutManager(gridLayoutManager);
    }
    private void initAction() {
        this.swipe_refreshl_packs_activity.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                packList.clear();
                packAdapter.notifyDataSetChanged();
                loadPacks();

            }
        });
        button_try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                packList.clear();
                packAdapter.notifyDataSetChanged();
                loadPacks();
            }
        });
    }

    private void loadPacks() {
        recycle_view_packs_activity.setVisibility(View.GONE);
        image_view_empty.setVisibility(View.GONE);
        linear_layout_page_error.setVisibility(View.GONE);
        swipe_refreshl_packs_activity.setRefreshing(true);

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Pack>> call = service.PacksList();
        call.enqueue(new Callback<List<Pack>>() {
            @Override
            public void onResponse(Call<List<Pack>> call, Response<List<Pack>> response) {
                apiClient.FormatData(PacksActivity.this,response);
                if(response.isSuccessful()){
                    if (response.body().size()!=0){

                        for (int i=0;i<response.body().size();i++){
                            packList.add(response.body().get(i));
                        }
                        packAdapter.notifyDataSetChanged();
                        recycle_view_packs_activity.setVisibility(View.VISIBLE);
                        image_view_empty.setVisibility(View.GONE);
                        linear_layout_page_error.setVisibility(View.GONE);
                    }else {
                        recycle_view_packs_activity.setVisibility(View.GONE);
                        image_view_empty.setVisibility(View.VISIBLE);
                        linear_layout_page_error.setVisibility(View.GONE);
                    }
                }else{
                    recycle_view_packs_activity.setVisibility(View.GONE);
                    image_view_empty.setVisibility(View.GONE);
                    linear_layout_page_error.setVisibility(View.VISIBLE);
                }
                swipe_refreshl_packs_activity.setRefreshing(false);

            }
            @Override
            public void onFailure(Call<List<Pack>> call, Throwable t) {
                recycle_view_packs_activity.setVisibility(View.GONE);
                image_view_empty.setVisibility(View.GONE);
                linear_layout_page_error.setVisibility(View.VISIBLE);
                swipe_refreshl_packs_activity.setRefreshing(false);

            }
        });
    }
}
