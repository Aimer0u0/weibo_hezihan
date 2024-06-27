package com.example.weibo_hezihan;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class FullscreenImageActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private List<String> imageUrls;
    private int currentIndex;
    private TextView imageCount;
    private ImageView userAvatar;
    private TextView userName;
    private TextView download;
    private long downloadID;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        initializeViews();
        setupViewPager();
        setupUserDetails();
        hideSystemUI();
        setEventListeners();
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED);
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.viewPager);
        imageCount = findViewById(R.id.image_count);
        userAvatar = findViewById(R.id.user_avatar);
        userName = findViewById(R.id.user_name);
        download = findViewById(R.id.download);
    }

    private void setupViewPager() {
        Intent intent = getIntent();
        imageUrls = intent.getStringArrayListExtra("imageUrls");
        currentIndex = intent.getIntExtra("currentIndex", 0);

        FullscreenImageAdapter adapter = new FullscreenImageAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentIndex);
        updateImageCount(currentIndex);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                updateImageCount(position);
            }
        });

        adapter.setOnItemClickListener(position -> finish());
    }

    private void setupUserDetails() {
        userAvatar.setImageResource(R.mipmap.ic_image_4);
        userName.setText("大王叫我来巡山");
    }

    private void hideSystemUI() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void setEventListeners() {
        download.setOnClickListener(v -> downloadImage(imageUrls.get(currentIndex)));
    }

    private void updateImageCount(int position) {
        String countText = (position + 1) + "/" + imageUrls.size();
        imageCount.setText(countText);
    }

    private void downloadImage(String imageUrl) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("Image Download")
                .setDescription("Downloading image...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "image_" + System.currentTimeMillis() + ".jpg");

        downloadID = downloadManager.enqueue(request);
    }

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == downloadID) {
                Toast.makeText(FullscreenImageActivity.this, "图⽚下载完成，请相册查看", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }
}
