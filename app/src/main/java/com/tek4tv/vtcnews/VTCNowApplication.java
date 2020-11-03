package com.tek4tv.vtcnews;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.tek4tv.vtcnews.common.SharedPreferencesUtil;
import com.tek4tv.vtcnews.network.ApiService;
import com.tek4tv.vtcnews.network.ApiUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class VTCNowApplication extends Application {
    private static ApiService sApiService;
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
       // FirebaseMessaging.getInstance().subscribeToTopic("HotNews").addOnSuccessListener(new OnSuccessListener<Void>() {
         //   @Override
           // public void onSuccess(Void aVoid) {
//                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
            //}
        //});
        SharedPreferencesUtil.initialize(getApplicationContext(),MODE_PRIVATE);
        sApiService = ApiUtil.getApiService();
    }
    public static ApiService getApiService(){
        return sApiService;
    }
}
