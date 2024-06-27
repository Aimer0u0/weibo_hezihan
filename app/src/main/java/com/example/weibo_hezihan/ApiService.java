package com.example.weibo_hezihan;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {//定义接口
    @Headers("Content-Type: application/json")
    @POST("weibo/api/auth/sendCode")
    Call<SendCodeResponse> sendCode(@Body SendCodeRequest request);//发送验证码

    @Headers("Content-Type: application/json")
    @POST("weibo/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);//登录

    @Headers("Content-Type: application/json")
    @GET("weibo/api/user/info")
    Call<UserInfoResponse> getUserInfo(@Header("Authorization") String token);//获取用户信息

    @GET("weibo/homePage")
    Call<WeiboResponse> getWeiboData(
            @Header("Authorization") String token,
            @Query("current") int current,
            @Query("size") int size
    );//获取微博数据

    @POST("weibo/like/up")
    Call<LikeResponse> likePost(
            @Header("Authorization") String authHeader,
            @Body LikeRequest likeRequest
    );//点赞

    @POST("weibo/like/down")
    Call<CancelLikeResponse> cancelLike(
            @Header("Authorization") String authHeader,
            @Body CancelLikeRequest cancelLikeRequest
    );//取消点赞
}
