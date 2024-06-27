package com.example.weibo_hezihan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://hotfix-service-prod.g.mi.com/";
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";

    private EditText phoneNumberEditText;
    private EditText verificationCodeEditText;
    private Button getVerificationCodeButton;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化UI组件
        phoneNumberEditText = findViewById(R.id.phone_number);
        verificationCodeEditText = findViewById(R.id.verification_code);
        getVerificationCodeButton = findViewById(R.id.get_verification_code);
        loginButton = findViewById(R.id.login_button);
        TextView backButton = findViewById(R.id.back_button);

        loginButton.setEnabled(false); // 初始化时禁用登录按钮

        // 添加文本变化监听器
        phoneNumberEditText.addTextChangedListener(textWatcher);
        verificationCodeEditText.addTextChangedListener(textWatcher);

        // 设置返回按钮点击事件
        backButton.setOnClickListener(v -> finish());

        // 获取 FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        RecommendFragment recommendFragment = (RecommendFragment) fragmentManager.findFragmentByTag("recommend");

        // 设置获取验证码按钮点击事件
        getVerificationCodeButton.setOnClickListener(v -> handleGetVerificationCode());

        // 设置登录按钮点击事件
        loginButton.setOnClickListener(v -> handleLogin(recommendFragment));
    }

    // 文本变化监听器
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            checkInputs();
        }
    };

    // 检查输入内容是否有效
    private void checkInputs() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String verificationCode = verificationCodeEditText.getText().toString().trim();
        boolean isPhoneNumberValid = phoneNumber.length() == 11;
        boolean isVerificationCodeValid = !verificationCode.isEmpty();
        loginButton.setEnabled(isPhoneNumberValid && isVerificationCodeValid);
    }

    // 处理获取验证码的逻辑
    private void handleGetVerificationCode() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        if (phoneNumber.length() == 11) {
            sendVerificationCode(phoneNumber);
        } else {
            Toast.makeText(LoginActivity.this, "请输入完整手机号", Toast.LENGTH_SHORT).show();
        }
    }

    // 发送验证码请求
    private void sendVerificationCode(String phoneNumber) {
        SendCodeRequest request = new SendCodeRequest(phoneNumber);
        ApiService apiService = RetrofitClient.getClient(BASE_URL).create(ApiService.class);
        apiService.sendCode(request).enqueue(new Callback<SendCodeResponse>() {
            @Override
            public void onResponse(@NonNull Call<SendCodeResponse> call, @NonNull Response<SendCodeResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isData()) {
                    Toast.makeText(LoginActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                    startCountdown();
                } else {
                    Toast.makeText(LoginActivity.this, "验证码发送失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SendCodeResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 处理登录的逻辑
    private void handleLogin(RecommendFragment recommendFragment) {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String verificationCode = verificationCodeEditText.getText().toString().trim();
        if (phoneNumber.length() == 11 && !verificationCode.isEmpty()) {
            performLogin(phoneNumber, verificationCode, recommendFragment);
        } else {
            Toast.makeText(LoginActivity.this, "请输入完整信息", Toast.LENGTH_SHORT).show();
        }
    }

    // 执行登录请求
    private void performLogin(String phoneNumber, String verificationCode, RecommendFragment recommendFragment) {
        LoginRequest request = new LoginRequest(phoneNumber, verificationCode);
        ApiService apiService = RetrofitClient.getClient(BASE_URL).create(ApiService.class);
        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    String token = response.body().getData();
                    saveToken(token);
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                    if (recommendFragment != null) {
                        recommendFragment.refreshData();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 开始验证码获取倒计时
    private void startCountdown() {
        getVerificationCodeButton.setEnabled(false);
        new CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                getVerificationCodeButton.setText("获取验证码(" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                getVerificationCodeButton.setText("获取验证码");
                getVerificationCodeButton.setEnabled(true);
            }
        }.start();
    }

    // 保存登录token
    private void saveToken(String token) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    // 跳转到主界面
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
