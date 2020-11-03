package com.tek4tv.vtcnews.network;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

import java.util.Map;

public interface ApiService {
    @POST(ApiUtil.URL_INSERT)
    Observable<ResponseBody> getDailyForecast(@QueryMap Map<String,String> params);
}
