package com.example.weibo_hezihan;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private WeiboAdapter weiboAdapter;
    private List<WeiboResponse.Record> weiboList;
    private boolean isLoading = false;
    private int currentPage = 1;
    private static final String TOKEN_KEY = "token";
    private static final String BASE_URL = "https://hotfix-service-prod.g.mi.com/";
    private RecyclerView recyclerView;
    private Parcelable recyclerViewState;
    private boolean isFirstLoad = true;
    private FrameLayout loadingScreen;
    private FrameLayout networkErrorScreen;
    private Call<WeiboResponse> weiboCall;

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);

        initializeUI(view);
        initializeRecyclerView();
        initializeSwipeRefreshLayout();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            loadWeiboData(currentPage);
        }

        setupRetryButton();
        setupRecyclerViewScrollListener();

        return view;
    }

    private void initializeUI(View view) {//初始化UI
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        loadingScreen = view.findViewById(R.id.loading_screen);
        networkErrorScreen = view.findViewById(R.id.network_error_screen);
        weiboList = new ArrayList<>();
        weiboAdapter = new WeiboAdapter(getContext(), weiboList);
        recyclerView.setAdapter(weiboAdapter);
    }

    private void initializeRecyclerView() {//初始化RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void initializeSwipeRefreshLayout() {//初始化下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            weiboList.clear();
            loadWeiboData(currentPage);
        });
    }

    private void setupRetryButton() {//设置重试按钮
        Button retryButton = networkErrorScreen.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(v -> {
            showLoadingScreen();
            loadWeiboData(currentPage);
        });
    }

    private void setupRecyclerViewScrollListener() {//设置RecyclerView滚动监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == weiboList.size() - 1) {
                    isLoading = true;
                    currentPage++;
                    loadWeiboData(currentPage);
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void restoreInstanceState(Bundle savedInstanceState) {
        currentPage = savedInstanceState.getInt("currentPage");
        weiboList = (List<WeiboResponse.Record>) savedInstanceState.getSerializable("weiboList");
        recyclerViewState = savedInstanceState.getParcelable("recyclerViewState");
        isFirstLoad = savedInstanceState.getBoolean("isFirstLoad");
        weiboAdapter.notifyDataSetChanged();
        Objects.requireNonNull(recyclerView.getLayoutManager()).onRestoreInstanceState(recyclerViewState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentPage", currentPage);
        outState.putSerializable("weiboList", (Serializable) weiboList);
        outState.putParcelable("recyclerViewState", Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState());
        outState.putBoolean("isFirstLoad", isFirstLoad);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            recyclerViewState = savedInstanceState.getParcelable("recyclerViewState");
            Objects.requireNonNull(recyclerView.getLayoutManager()).onRestoreInstanceState(recyclerViewState);
        }
    }

    private void loadWeiboData(int page) {
        isLoading = true;
        if (isFirstLoad) {
            showLoadingScreen();
        }

        ApiService apiService = RetrofitClient
                .getClient(BASE_URL)
                .create(ApiService.class);
        weiboCall = apiService.getWeiboData(TOKEN_KEY, page, 10);

        weiboCall.enqueue(new Callback<WeiboResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeiboResponse> call, @NonNull Response<WeiboResponse> response) {
                handleResponse(response, page);
            }

            @Override
            public void onFailure(@NonNull Call<WeiboResponse> call, @NonNull Throwable t) {
                handleFailure();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleResponse(Response<WeiboResponse> response, int page) {
        if (response.isSuccessful() && response.body() != null) {
            WeiboResponse weiboResponse = response.body();
            if (page == 1) {
                weiboList.clear();
            }
            weiboList.addAll(weiboResponse.getData().getRecords());

            if (page == 1) {
                Collections.shuffle(weiboList);
            }

            weiboAdapter.notifyDataSetChanged();
            hideNetworkErrorScreen();

            if (weiboResponse.getData().getRecords().isEmpty()) {
                Toast.makeText(getContext(), "无更多内容", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "加载数据失败", Toast.LENGTH_SHORT).show();
            if (isFirstLoad) {
                showNetworkErrorScreen();
            }
        }
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
        isFirstLoad = false;
        hideLoadingScreen();
    }

    private void handleFailure() {
        Toast.makeText(getContext(), "加载数据失败", Toast.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
        if (isFirstLoad) {
            showNetworkErrorScreen();
        } else {
            hideLoadingScreen();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (weiboCall != null) {
            weiboCall.cancel();
        }
    }

    private void showLoadingScreen() {
        loadingScreen.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.GONE);
        networkErrorScreen.setVisibility(View.GONE);
    }

    private void hideLoadingScreen() {
        loadingScreen.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        networkErrorScreen.setVisibility(View.GONE);
    }

    private void showNetworkErrorScreen() {
        loadingScreen.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
        networkErrorScreen.setVisibility(View.VISIBLE);
    }

    private void hideNetworkErrorScreen() {
        networkErrorScreen.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    public void refreshData() {
        currentPage = 1;
        weiboList.clear();
        loadWeiboData(currentPage);
    }
}
