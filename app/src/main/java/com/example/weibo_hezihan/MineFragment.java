package com.example.weibo_hezihan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MineFragment extends Fragment {
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";
    private static final String BASE_URL = "https://hotfix-service-prod.g.mi.com/";

    private TextView usernameText;
    private ImageView defaultAvatar;
    private TextView loginText;
    private Button logoutButton;
    private TextView noDataText1;
    private TextView noDataText2;

    private ApiService apiService;
    private Call<UserInfoResponse> userInfoCall;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", usernameText.getText().toString());
        outState.putBoolean("isLoggedIn", logoutButton.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String username = savedInstanceState.getString("username");
            boolean isLoggedIn = savedInstanceState.getBoolean("isLoggedIn");
            usernameText.setText(username);
            if (isLoggedIn) {
                updateUIWithUserInfo();
            } else {
                showLoginUI();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        initializeUI(view);
        apiService = RetrofitClient.getClient(BASE_URL).create(ApiService.class);

        String token = getToken();
        if (token != null) {
            fetchUserInfo(token);
        } else {
            showLoginUI();
        }

        setupEventListeners();
        return view;
    }

    private void initializeUI(View view) {
        usernameText = view.findViewById(R.id.username_text);
        defaultAvatar = view.findViewById(R.id.default_avatar);
        loginText = view.findViewById(R.id.login_text);
        logoutButton = view.findViewById(R.id.logout_button);
        noDataText1 = view.findViewById(R.id.no_data_text1);
        noDataText2 = view.findViewById(R.id.no_data_text2);
    }

    private void setupEventListeners() {
        defaultAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            clearToken();
            showLoginUI();
            Toast.makeText(getActivity(), "已退出登录", Toast.LENGTH_SHORT).show();
        });
    }

    private String getToken() {
        SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(TOKEN_KEY, null);
    }

    private void fetchUserInfo(String token) {
        userInfoCall = apiService.getUserInfo("Bearer " + token);
        userInfoCall.enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserInfoResponse> call, @NonNull Response<UserInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfoResponse userInfoResponse = response.body();
                    if (userInfoResponse.getCode() == 200 && userInfoResponse.getData() != null) {
                        updateUIWithUserInfo();
                    } else {
                        Toast.makeText(getActivity(), "获取用户信息失败：" + userInfoResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        showLoginUI();
                    }
                } else {
                    Toast.makeText(getActivity(), "网络请求失败", Toast.LENGTH_SHORT).show();
                    showLoginUI();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserInfoResponse> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "网络请求失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                showLoginUI();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userInfoCall != null) {
            userInfoCall.cancel();
        }
    }

    private void updateUIWithUserInfo() {
        usernameText.setText("大王叫我来巡山");
        loginText.setVisibility(View.GONE);
        logoutButton.setVisibility(View.VISIBLE);
        noDataText1.setVisibility(View.GONE);
        noDataText2.setVisibility(View.VISIBLE);
        defaultAvatar.setEnabled(false);
        defaultAvatar.setImageResource(R.mipmap.ic_image_4);
    }

    private void showLoginUI() {
        usernameText.setText("请先登录");
        loginText.setVisibility(View.VISIBLE);
        logoutButton.setVisibility(View.GONE);
        noDataText1.setVisibility(View.VISIBLE);
        noDataText2.setVisibility(View.GONE);
        defaultAvatar.setImageResource(R.drawable.default_avatar);
    }

    private void clearToken() {
        SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(TOKEN_KEY);
        editor.apply();
    }
}
