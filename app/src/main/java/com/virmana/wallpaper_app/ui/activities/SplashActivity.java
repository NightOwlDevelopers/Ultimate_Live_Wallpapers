package com.virmana.wallpaper_app.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.virmana.wallpaper_app.R;
import com.virmana.wallpaper_app.api.apiClient;
import com.virmana.wallpaper_app.api.apiRest;
import com.virmana.wallpaper_app.config.Config;
import com.virmana.wallpaper_app.entity.ApiResponse;
import com.virmana.wallpaper_app.entity.Category;
import com.virmana.wallpaper_app.manager.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class SplashActivity extends AppCompatActivity {

    private ProgressBar intro_progress;
    private PrefManager prf;


    IInAppBillingService mService;


    private static final String LOG_TAG = "iabv3";
// PUT YOUR MERCHANT KEY HERE;
    // put your Google merchant id here (as stated in public profile of your Payments Merchant Center)
    // if filled library will provide protection against Freedom alike Play Market simulators
    private static final String MERCHANT_ID=null;

    private BillingProcessor bp;
    private boolean readyToPurchase = false;



    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBuy();


        setContentView(R.layout.activity_splash);
        getSection();
        prf= new PrefManager(getApplicationContext());

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // If you want to modify a view in your Activity
                SplashActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        checkAccount();
                    }
                });
            }
        }, 1000);



    }
    public static void adapteActivity(Activity activity){
        activity.finish();
    }
    private void checkAccount() {

        Integer version = -1;
        try {

            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        }

        if (version!=-1){


            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            Call<ApiResponse> call = service.check(version);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    updateTextViews();
                    apiClient.FormatData(SplashActivity.this,response);
                    if (response.isSuccessful()){
                        if (response.body().getCode().equals(200)) {
                            if (!prf.getString("first").equals("true")){
                                Intent intent = new Intent(SplashActivity.this,IntroActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                                finish();
                                prf.setString("first","true");
                            }else{

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (
                                            ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                                            ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                                            ) {
                                        Intent intent = new Intent(SplashActivity.this,PermissionActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.enter, R.anim.exit);
                                        finish();
                                    }else{
                                        Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.enter, R.anim.exit);
                                        finish();
                                    }
                                }else{
                                    Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.enter, R.anim.exit);
                                    finish();
                                }

                            }
                        }else if (response.body().getCode().equals(202)) {


                            String title_update=response.body().getValues().get(0).getValue();
                            String featurs_update=response.body().getMessage();

                            View v = (View)  getLayoutInflater().inflate(R.layout.update_message,null);
                            TextView update_text_view_title=(TextView) v.findViewById(R.id.update_text_view_title);
                            TextView update_text_view_updates=(TextView) v.findViewById(R.id.update_text_view_updates);
                            update_text_view_title.setText(title_update);
                            update_text_view_updates.setText(featurs_update);
                            AlertDialog.Builder builder;
                            builder = new AlertDialog.Builder(SplashActivity.this);
                            builder.setTitle("New Update")
                                    //.setMessage(response.body().getValue())
                                    .setView(v)
                                    .setPositiveButton(getResources().getString(R.string.update_now), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            final String appPackageName=getApplication().getPackageName();
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }
                                            finish();
                                        }
                                    })
                                    .setNegativeButton(getResources().getString(R.string.skip), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!prf.getString("first").equals("true")){
                                                Intent intent = new Intent(SplashActivity.this,IntroActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.enter, R.anim.exit);
                                                finish();
                                                prf.setString("first","true");
                                            }else{
                                                Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.enter, R.anim.exit);
                                                finish();
                                            }
                                        }
                                    })
                                    .setCancelable(false)
                                    .setIcon(R.drawable.ic_update)
                                    .show();

                        } else {
                            if (!prf.getString("first").equals("true")){
                                Intent intent = new Intent(SplashActivity.this,IntroActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                                finish();
                                prf.setString("first","true");
                            }else{
                                Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                               finish();
                            }

                        }
                    }else {
                        if (!prf.getString("first").equals("true")){
                            Intent intent = new Intent(SplashActivity.this,IntroActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                           finish();
                            prf.setString("first","true");
                        }else{
                            Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                           finish();
                        }

                    }
                  /*  if (response.body().getName().equals("true")){


                        if (!prf.getString("first").equals("true")){
                            Intent intent = new Intent(IntroActivity.this,SlideActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                            finish();
                            prf.setString("first","true");
                        }else{
                            Intent intent = new Intent(IntroActivity.this,MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                            finish();
                        }




                    }else if(response.body().getName().equals("false")){
                    */

                    /*
                    }else {
                        if (!prf.getString("first").equals("true")){
                            Intent intent = new Intent(IntroActivity.this,SlideActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                            finish();
                            prf.setString("first","true");
                        }else{
                            Intent intent = new Intent(IntroActivity.this,MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                            finish();
                        }

                    }*/


                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {

                    if (!prf.getString("first").equals("true")){
                        Intent intent = new Intent(SplashActivity.this,IntroActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                        finish();
                        prf.setString("first","true");
                    }else{
                        Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                        finish();
                    }

                }
            });
        }else{
            if (!prf.getString("first").equals("true")){
                Intent intent = new Intent(SplashActivity.this,IntroActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
                prf.setString("first","true");
            }else{
                Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
            }

        }

    }
    private void getSection() {
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Category>> call = service.categoryAll();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {


            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {

            }
        });
    }

    private void initBuy() {
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);


        if(!BillingProcessor.isIabServiceAvailable(this)) {
            //  showToast("In-app billing service is unavailable, please upgrade Android Market/Play to version >= 3.9.16");
        }

        bp = new BillingProcessor(this, Config.LICENSE_KEY, MERCHANT_ID, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
                updateTextViews();
            }
            @Override
            public void onBillingError(int errorCode, @Nullable Throwable error) {
            }
            @Override
            public void onBillingInitialized() {
                readyToPurchase = true;
                updateTextViews();
            }
            @Override
            public void onPurchaseHistoryRestored() {
                for(String sku : bp.listOwnedProducts())
                    Log.d(LOG_TAG, "Owned Managed Product: " + sku);
                for(String sku : bp.listOwnedSubscriptions())
                    Log.d(LOG_TAG, "Owned Subscription: " + sku);
                updateTextViews();
            }
        });
        bp.loadOwnedPurchasesFromGoogle();
    }

    private void updateTextViews() {
        PrefManager prf= new PrefManager(getApplicationContext());
        bp.loadOwnedPurchasesFromGoogle();
        if(isSubscribe(Config.SUBSCRIPTION_ID)){
            prf.setString("SUBSCRIBED","TRUE");
            showToast("SUBSCRIBED");

        }
        else{
            prf.setString("SUBSCRIBED","FALSE");
            showToast("NOT SUBSCRIBED");
        }
    }

    private void showToast(String message) {
       // Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public Bundle getPurchases(){
        if (!bp.isInitialized()) {
            return null;
        }
        try{

            return  mService.getPurchases(Constants.GOOGLE_API_VERSION, getApplicationContext().getPackageName(), Constants.PRODUCT_TYPE_SUBSCRIPTION, null);
        }catch (Exception e) {
            // Toast.makeText(this, "ex", Toast.LENGTH_SHORT).show();

            e.printStackTrace();
        }
        return null;
    }
    public Boolean isSubscribe(String SUBSCRIPTION_ID_CHECK){

        if (!bp.isSubscribed(Config.SUBSCRIPTION_ID))
            return false;


        Bundle b =  getPurchases();
        if (b==null)
            return  false;
        if( b.getInt("RESPONSE_CODE") == 0){
            ArrayList<String> ownedSkus =
                    b.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            ArrayList<String>  purchaseDataList =
                    b.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
            ArrayList<String>  signatureList =
                    b.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
            String continuationToken =
                    b.getString("INAPP_CONTINUATION_TOKEN");


            if(purchaseDataList == null){
                return  false;

            }
            if(purchaseDataList.size()==0){
                return  false;
            }
            for (int i = 0; i < purchaseDataList.size(); ++i) {
                String purchaseData = purchaseDataList.get(i);
                String signature = signatureList.get(i);
                String sku_1 = ownedSkus.get(i);
                //Long tsLong = System.currentTimeMillis()/1000;

                try {
                    JSONObject rowOne = new JSONObject(purchaseData);
                    String  productId =  rowOne.getString("productId") ;

                    if (productId.equals(SUBSCRIPTION_ID_CHECK)){

                        Boolean  autoRenewing =  rowOne.getBoolean("autoRenewing");
                        if (autoRenewing){
                            Long tsLong = System.currentTimeMillis()/1000;
                            Long  purchaseTime =  rowOne.getLong("purchaseTime")/1000;
                            return  true;
                        }else{
                            // Toast.makeText(this, "is not autoRenewing ", Toast.LENGTH_SHORT).show();
                            Long tsLong = System.currentTimeMillis()/1000;
                            Long  purchaseTime =  rowOne.getLong("purchaseTime")/1000;
                            if (tsLong > (purchaseTime + (Config.SUBSCRIPTION_DURATION*86400)) ){
                                //   Toast.makeText(this, "is Expired ", Toast.LENGTH_SHORT).show();
                                return  false;
                            }else{
                                return  true;
                            }
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }else{
            return false;
        }

        return  false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConn);
    }
}
