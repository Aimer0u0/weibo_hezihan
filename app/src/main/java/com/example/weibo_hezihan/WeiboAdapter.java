package com.example.weibo_hezihan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;


public class WeiboAdapter extends RecyclerView.Adapter<WeiboAdapter.WeiboViewHolder> {
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";
    private static final String BASE_URL = "https://hotfix-service-prod.g.mi.com/";
    private final List<WeiboResponse.Record> weiboList;
    private final Context context;

    public WeiboAdapter(Context context, List<WeiboResponse.Record> weiboList) {
        this.context = context;
        this.weiboList = weiboList;
    }

    @NonNull
    @Override
    public WeiboViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {//创建ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weibo, parent, false);
        return new WeiboViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WeiboViewHolder holder, int position) {//绑定数据
        WeiboResponse.Record weibo = weiboList.get(position);
        bindData(holder, weibo);
    }

    @Override
    public int getItemCount() {
        return weiboList.size();
    }//返回数据长度

    private void bindData(@NonNull final WeiboViewHolder holder, WeiboResponse.Record weibo) {//绑定数据
        holder.username.setText(weibo.getUsername());
        holder.title.setText(weibo.getTitle());

        Glide.with(context)
                .load(weibo.getAvatar())
                .placeholder(R.mipmap.ic_image_4)
                .into(holder.avatar);

        holder.imagesGrid.removeAllViews();
        resetMediaPlayer(holder);//重置MediaPlayer

        if (weibo.getVideoUrl() != null && !weibo.getVideoUrl().isEmpty()) {
            setupVideo(holder, weibo);
        } else if (weibo.getImages() != null && !weibo.getImages().isEmpty()) {
            setupImages(holder, weibo);
        } else {
            hideVideoViews(holder);
        }

        holder.likeCount.setText(String.valueOf(weibo.getLikeCount()));
        updateLikeButton(holder, weibo.isLikeFlag());//更新点赞按钮

        holder.likeButton.setOnClickListener(v -> handleLikeButtonClick(holder, weibo));//点赞按钮点击事件

        holder.delete.setOnClickListener(v -> handleDeleteButtonClick(holder));

        holder.comment.setOnClickListener(v -> handleCommentButtonClick(holder));
    }

    private void resetMediaPlayer(@NonNull WeiboViewHolder holder) {
        if (holder.mediaPlayer != null) {
            holder.mediaPlayer.reset();
        }
    }

    private void hideVideoViews(@NonNull WeiboViewHolder holder) {//隐藏视频控件
        holder.videoSurfaceView.setVisibility(View.GONE);
        holder.videoPoster.setVisibility(View.GONE);
        holder.videoSeekBar.setVisibility(View.GONE);
        holder.videoPlayTime.setVisibility(View.GONE);
    }

    private void updateLikeButton(@NonNull WeiboViewHolder holder, boolean isLiked) {//更新点赞按钮
        holder.likeButton.setImageResource(isLiked ? R.mipmap.ic_liked : R.mipmap.ic_like);
    }

    private void handleLikeButtonClick(@NonNull WeiboViewHolder holder, WeiboResponse.Record weibo) {//处理点赞按钮点击事件
        if (!checkLoginStatus()) {
            return;
        }
        if (!weibo.isLikeFlag()) {
            animateLike(holder.likeButton);
            likePost(holder, weibo);
        } else {
            animateUnlike(holder.likeButton);
            cancelLike(holder, weibo);
        }
    }

    private void handleDeleteButtonClick(@NonNull WeiboViewHolder holder) {//处理删除按钮点击事件
        int position = holder.getAdapterPosition();
        weiboList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, weiboList.size());
    }

    private void handleCommentButtonClick(@NonNull WeiboViewHolder holder) {//处理评论按钮点击事件
        int position = holder.getAdapterPosition() + 1;
        Toast.makeText(context, "点击第" + position + "条数据评论按钮", Toast.LENGTH_SHORT).show();
    }

    private void setupVideo(@NonNull final WeiboViewHolder holder, WeiboResponse.Record weibo) {//设置视频
        holder.videoSurfaceView.setVisibility(View.VISIBLE);
        holder.videoPoster.setVisibility(View.VISIBLE);
        holder.videoSeekBar.setVisibility(View.VISIBLE);
        holder.videoPlayTime.setVisibility(View.VISIBLE);

        Glide.with(context)
                .load(weibo.getPoster())
                .placeholder(R.mipmap.ic_image)
                .into(holder.videoPoster);

        initializeMediaPlayer(holder);//初始化MediaPlayer
        setupSurfaceHolder(holder, weibo.getVideoUrl());//设置SurfaceHolder
    }

    private void initializeMediaPlayer(@NonNull final WeiboViewHolder holder) {//初始化MediaPlayer
        if (holder.mediaPlayer == null) {
            holder.mediaPlayer = new MediaPlayer();
            holder.mediaPlayer.setLooping(true);
            holder.mediaPlayer.setVolume(0, 0);
        }

        holder.mediaPlayer.setOnPreparedListener(mp -> {//设置准备监听器
            holder.videoSeekBar.setMax(holder.mediaPlayer.getDuration());
            updateSeekBar(holder);
        });

        holder.mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> adjustVideoSize(holder));//设置视频尺寸变化监听器

        holder.videoSurfaceView.setOnClickListener(v -> toggleVideoPlayback(holder));//设置视频播放暂停监听器
    }

    private void adjustVideoSize(@NonNull WeiboViewHolder holder) {//调整视频尺寸
        int videoWidth = holder.mediaPlayer.getVideoWidth();
        int videoHeight = holder.mediaPlayer.getVideoHeight();
        float ratio = (float) videoWidth / videoHeight;

        int newWidth, newHeight;
        if (videoWidth > videoHeight) {
            newWidth = holder.videoSurfaceView.getWidth();
            newHeight = (int) (newWidth / ratio);
        } else {
            newHeight = holder.videoSurfaceView.getHeight();
            newWidth = (int) (newHeight * ratio);
        }
        holder.videoSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(newWidth, newHeight));
    }

    private void toggleVideoPlayback(@NonNull WeiboViewHolder holder) {//切换视频播放暂停
        if (holder.mediaPlayer.isPlaying()) {
            holder.mediaPlayer.pause();
            holder.videoPoster.setVisibility(View.VISIBLE);
        } else {
            holder.mediaPlayer.start();
            holder.videoPoster.setVisibility(View.GONE);
            updateSeekBar(holder);
        }
    }

    private void setupSurfaceHolder(@NonNull WeiboViewHolder holder, String videoUrl) {//设置SurfaceHolder
        SurfaceHolder surfaceHolder = holder.videoSurfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {//Surface创建时
                holder.mediaPlayer.setDisplay(surfaceHolder);
                try {
                    holder.mediaPlayer.setDataSource(context, Uri.parse(videoUrl));
                    holder.mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {}
        });
    }

    private void setupImages(@NonNull final WeiboViewHolder holder, WeiboResponse.Record weibo) {//设置图片
        hideVideoViews(holder);

        for (int i = 0; i < weibo.getImages().size(); i++) {//遍历图片
            String imageUrl = weibo.getImages().get(i);
            ImageView imageView = createImageView();
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_image)
                    .into(imageView);
            holder.imagesGrid.addView(imageView);

            int finalI = i;
            imageView.setOnClickListener(v -> openFullscreenImage(weibo.getImages(), finalI));
        }
    }

    private ImageView createImageView() {//创建ImageView
        ImageView imageView = new ImageView(context);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }

    private void openFullscreenImage(List<String> imageUrls, int index) {//打开全屏图片
        Intent intent = new Intent(context, FullscreenImageActivity.class);
        intent.putStringArrayListExtra("imageUrls", new ArrayList<>(imageUrls));
        intent.putExtra("currentIndex", index);
        context.startActivity(intent);
    }

    private void animateLike(ImageView likeButton) {//点赞动画
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(likeButton, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(likeButton, "scaleY", 1f, 1.2f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(likeButton, "rotationY", 0f, 360f);

        startAnimation(scaleUpX, scaleUpY, rotation, likeButton, R.mipmap.ic_liked);
    }

    private void animateUnlike(ImageView likeButton) {//取消点赞动画
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(likeButton, "scaleX", 1f, 0.8f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(likeButton, "scaleY", 1f, 0.8f, 1f);

        startAnimation(scaleDownX, scaleDownY, null, likeButton, R.mipmap.ic_like);
    }

    private void startAnimation(ObjectAnimator scaleX, ObjectAnimator scaleY, ObjectAnimator rotation, ImageView likeButton, int resourceId) {
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);

        scaleX.start();
        scaleY.start();
        if (rotation != null) {
            rotation.setDuration(1000);
            rotation.start();
            rotation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    likeButton.setImageResource(resourceId);
                }
            });
        } else {
            scaleX.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    likeButton.setImageResource(resourceId);
                }
            });
        }
    }

    private void likePost(@NonNull WeiboViewHolder holder, WeiboResponse.Record weibo) {//点赞
        RetrofitClient.getClient(BASE_URL)
                .create(ApiService.class)
                .likePost("Bearer " + getToken(), new LikeRequest(weibo.getId()))
                .enqueue(new retrofit2.Callback<LikeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LikeResponse> call, @NonNull retrofit2.Response<LikeResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isData()) {
                            weibo.setLikeFlag(true);
                            weibo.setLikeCount(weibo.getLikeCount() + 1);
                            holder.likeCount.setText(String.valueOf(weibo.getLikeCount()));
                        } else {
                            showErrorToast(response, "点赞失败");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LikeResponse> call, @NonNull Throwable t) {
                        logError(t, "点赞请求失败");
                        Toast.makeText(context, "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelLike(@NonNull WeiboViewHolder holder, WeiboResponse.Record weibo) {//取消点赞
        RetrofitClient.getClient(BASE_URL)
                .create(ApiService.class)
                .cancelLike("Bearer " + getToken(), new CancelLikeRequest(weibo.getId()))
                .enqueue(new retrofit2.Callback<CancelLikeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CancelLikeResponse> call, @NonNull retrofit2.Response<CancelLikeResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isData()) {
                            weibo.setLikeFlag(false);
                            weibo.setLikeCount(weibo.getLikeCount() - 1);
                            holder.likeCount.setText(String.valueOf(weibo.getLikeCount()));
                        } else {
                            showErrorToast(response, "取消点赞失败");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CancelLikeResponse> call, @NonNull Throwable t) {
                        logError(t, "取消点赞请求失败");
                        Toast.makeText(context, "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showErrorToast(retrofit2.Response<?> response, String defaultMessage) {//显示错误信息
        String errorMessage = defaultMessage;
        if (response.errorBody() != null) {
            try {
                errorMessage = response.errorBody().string();
            } catch (IOException e) {
                logError(e, "读取错误信息失败");
            }
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void logError(Throwable t, String message) {
        Log.e("WeiboAdapter", message, t);
    }//记录错误信息


    private void updateSeekBar(final WeiboViewHolder holder) {//更新SeekBar
        if (holder.mediaPlayer != null && holder.mediaPlayer.isPlaying()) {
            int currentPosition = holder.mediaPlayer.getCurrentPosition();
            holder.videoSeekBar.setProgress(currentPosition);
            holder.videoPlayTime.setText(stringForTime(currentPosition));
            holder.updateSeekBarHandler.postDelayed(holder.updateSeekBarRunnable, 1000);
        }
    }

    private static String stringForTime(int timeMillis) {//时间格式化
        int totalSeconds = timeMillis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        return formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    private String getToken() {//获取Token
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(TOKEN_KEY, null);
    }

    private boolean checkLoginStatus() {//检查登录状态
        String token = getToken();
        if (token == null) {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            });
            return false;
        }
        return true;
    }

    public static class WeiboViewHolder extends RecyclerView.ViewHolder {//ViewHolder
        ImageView avatar;
        TextView username;
        TextView title;
        GridLayout imagesGrid;
        TextView likeCount;
        ImageView likeButton;
        SurfaceView videoSurfaceView;
        ImageView videoPoster;
        SeekBar videoSeekBar;
        TextView videoPlayTime;
        ImageView delete;
        ImageView comment;
        MediaPlayer mediaPlayer;
        Handler updateSeekBarHandler;
        Runnable updateSeekBarRunnable;

        public WeiboViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            title = itemView.findViewById(R.id.title);
            imagesGrid = itemView.findViewById(R.id.images_grid);
            likeCount = itemView.findViewById(R.id.like_count);
            likeButton = itemView.findViewById(R.id.like);
            videoSurfaceView = itemView.findViewById(R.id.video_surface_view);
            videoPoster = itemView.findViewById(R.id.video_poster);
            videoSeekBar = itemView.findViewById(R.id.video_seek_bar);
            videoPlayTime = itemView.findViewById(R.id.video_play_time);
            delete = itemView.findViewById(R.id.delete);
            comment = itemView.findViewById(R.id.comment);
            updateSeekBarHandler = new Handler(Looper.getMainLooper());
            updateSeekBarRunnable = this::updateSeekBar;
        }

        private void updateSeekBar() {//更新SeekBar
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                videoSeekBar.setProgress(currentPosition);
                videoPlayTime.setText(stringForTime(currentPosition));
                updateSeekBarHandler.postDelayed(updateSeekBarRunnable, 1000);
            }
        }
    }
}
