package com.tek4tv.vtcnews;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.tek4tv.vtcnews.common.Common;
import com.tek4tv.vtcnews.location.LocationCustom;
import com.tek4tv.vtcnews.location.LocationTracker;
import com.tek4tv.vtcnews.model.Version;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, LocationTracker.onLocationChange, LocationTracker.checkPermision {
    private RxPermissions rxPermissions;
    private LocationTracker locationTracker;
    private WebView wv;
    private SwipeRefreshLayout swipeRefreshLayout;
    public LocationCustom location;
    private String address;
    private String urlWv = "";
    public Version version = null;

    public LocationCustom getLocation() {
        return location;
    }

    public void setLocation(LocationCustom location) {
        this.location = location;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rxPermissions = new RxPermissions(this);
        locationTracker = new LocationTracker(getApplicationContext(), this);
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        try {
                            if (locationTracker.canGetLocation()) {
                                locationTracker.getLocation();
                                if (locationTracker.getLocation() != null) {
                                    Geocoder gcd = new Geocoder(this, Locale.getDefault());
                                    List<Address> addresses = gcd.getFromLocation(locationTracker.getLocation().getLatitude(), locationTracker.getLocation().getLongitude(), 1);
                                    if (addresses.size() > 0) {
                                        address = addresses.get(0).getAdminArea();
                                    } else {
                                        // do your stuff
                                    }
                                    location = new LocationCustom(locationTracker.getLocation().getLatitude(), locationTracker.getLocation().getLongitude(), address);
                                }
                            } else {
                                locationTracker.showSettingsAlert();
                            }
                        } catch (Exception e) {

                        }

                    } else {
                        locationTracker.showSettingsAlert();
                    }
                });
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        wv = (WebView) findViewById(R.id.webView);
        urlWv = Common.URL;


        setUpWV();

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().flush();
                } else {
                    CookieSyncManager.getInstance().sync();
                }
            }
        });

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;
            version = new Version();
            version.setVersionCode(versionNumber + "");
            version.setVersionName(versionName + "");
        } catch (Exception e) {

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setUpWV() {
        wv.getSettings().setLoadsImagesAutomatically(true);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        wv.getSettings().setMediaPlaybackRequiresUserGesture(false);
        wv.setScrollbarFadingEnabled(true);
        wv.setScrollContainer(false);
        wv.setWebChromeClient(new MainViewClient());
        wv.addJavascriptInterface(new WebViewJavaScriptInterface(this), "MainActivity");
        //wv.getSettings().setAppCacheEnabled(false);
        //wv.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //wv.clearCache(true);
        wv.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(wv, true);
        }
        if (!Common.checkInternet(this)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("");
            alertDialog.setMessage(getString(R.string.eror_network));
            alertDialog.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    MainActivity.this.finish();
                }
            });
            alertDialog.show();
        } else {
            wv.loadUrl(Common.URL);
//            wv.loadUrl("file:///android_asset/test.html");
        }


    }


    @Override
    public void checkPermission() {
        try {
            rxPermissions
                    .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    .subscribe(granted -> {
                        if (granted) { // Always true pre-M
                            if (locationTracker.canGetLocation()) {
                                Geocoder gcd = new Geocoder(this, Locale.getDefault());
                                List<Address> addresses = gcd.getFromLocation(locationTracker.getLocation().getLatitude(), locationTracker.getLocation().getLongitude(), 1);
                                if (addresses.size() > 0) {
                                    address = addresses.get(0).getAdminArea();
                                } else {
                                    // do your stuff
                                }
                                location = new LocationCustom(locationTracker.getLocation().getLatitude(), locationTracker.getLocation().getLongitude(), address);
                            } else {
                                locationTracker.showSettingsAlert();
                            }
                        } else {
                            locationTracker.showSettingsAlert();
                        }
                    });
        } catch (Exception e) {

        }
    }

    @Override
    public void onLocationChanged(Location locationimpl) {
        try {
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(locationimpl.getLatitude(), locationimpl.getLongitude(), 1);
            if (addresses.size() > 0) {
                address = addresses.get(0).getAdminArea();
            } else {
                // do your stuff
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        location = new LocationCustom(locationimpl.getLatitude(), locationimpl.getLongitude(), address);
    }

    @Override
    public void onRefresh() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            wv.evaluateJavascript("pullToRefresh();", null);
        } else {
            wv.loadUrl("javascript:pullToRefresh();");
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private class MainViewClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            result.confirm();
            Log.d("TAG", "messageCategory" + message);
            if (message != null && !message.equals("")) {
                if (message.contains("warning_")) {
                    Toast.makeText(MainActivity.this, message.replace("warning_", ""), Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("");
                    alertDialog.setMessage(message);
                    alertDialog.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
//                    alertDialog.setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    });
                    alertDialog.show();
                }
            }
            return true;
        }
    }

    public String saveData(String json) {
        Context ctx = getApplicationContext();
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
        // Put the json format string to SharedPreferences object.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(this.getPackageName(), json);
        editor.commit();
        return "true";
    }

    public String loadData() {
        Context ctx = getApplicationContext();
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
        // Get saved string data in it.
        String userInfoListJsonString = sharedPreferences.getString(this.getPackageName(), "");
        return userInfoListJsonString;
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
//        if(wv.canGoBack()){
//            wv.goBack();
//            return;
//        }else{
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.confirm_back), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
//        }

    }

    public class WebViewJavaScriptInterface {

        private Context context;

        /*
         * Need a reference to the context in order to sent a post message
         */
        public WebViewJavaScriptInterface(Context context) {
            this.context = context;
        }

        /*
         * This method can be called from Android. @JavascriptInterface
         * required after SDK version 17.
         */
        @JavascriptInterface
        public void goToDetail(String url, String urlDetail, String urlWvHeader, int height, boolean isLive, int type) {
            if (DetailActivity.instance == null) {
                Bundle mBundle = new Bundle();
                Intent intent = new Intent(context, DetailActivity.class);
                mBundle.putString("url", url);
                mBundle.putString("urlDetail", urlDetail);
                mBundle.putString("urlWvHeader", urlWvHeader);
                mBundle.putInt("height", height);
                mBundle.putInt("type", type);
                mBundle.putBoolean("isLive", isLive);
                intent.putExtra("bundle", mBundle);
                startActivity(intent);
            }

        }

        @JavascriptInterface
        public void goToDetail(String url, String urlDetail, String urlWvHeader, int height, boolean isLive) {
            if (DetailActivity.instance == null) {
                Bundle mBundle = new Bundle();
                Intent intent = new Intent(context, DetailActivity.class);
                mBundle.putString("url", url);
                mBundle.putString("urlDetail", urlDetail);
                mBundle.putString("urlWvHeader", urlWvHeader);
                mBundle.putInt("height", height);
                mBundle.putBoolean("isLive", isLive);
                intent.putExtra("bundle", mBundle);
                startActivity(intent);
            }

        }

        @JavascriptInterface
        public String getVersionApp() {
            if (version != null) {
                return new Gson().toJson(version);
            }
            return "";
        }

        @JavascriptInterface
        public String getLocation() {
            if (location != null) {
                return new Gson().toJson(location);
            }
            return "";
        }

        @JavascriptInterface
        public String saveDataFromWeb(String json) {
            return saveData(json);
        }

        @JavascriptInterface
        public String loadDataFromApp() {
            return loadData();
        }

        @JavascriptInterface
        public void goToFullScreen(String url, String isOriention, String urlOverlay, boolean isLive) {
            if (FullActivity.instance == null) {
                Bundle mBundle = new Bundle();
                Intent intent = new Intent(context, FullActivity.class);
                mBundle.putString("url", url);
                mBundle.putString("isOriention", isOriention);
                mBundle.putString("urlOverlay", urlOverlay);
                mBundle.putBoolean("isLive", isLive);
                intent.putExtra("bundle", mBundle);
                startActivity(intent);
            }
        }

        @JavascriptInterface
        public void goToFullScreen(String url, String isOriention, String urlOverlay, boolean isLive, int type) {
            if (FullActivity.instance == null) {
                Bundle mBundle = new Bundle();
                Intent intent = new Intent(context, FullActivity.class);
                mBundle.putString("url", url);
                mBundle.putString("isOriention", isOriention);
                mBundle.putString("urlOverlay", urlOverlay);
                mBundle.putBoolean("isLive", isLive);
                mBundle.putInt("type", type);
                intent.putExtra("bundle", mBundle);
                startActivity(intent);
            }
        }

        @JavascriptInterface
        public void shareApp(String message) {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "VTC News");
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(shareIntent, ""));
            } catch (Exception e) {
            }
        }

        @JavascriptInterface
        public void openUrl(String message) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(message));
                startActivity(browserIntent);
            } catch (Exception e) {
            }
        }


    }
}
