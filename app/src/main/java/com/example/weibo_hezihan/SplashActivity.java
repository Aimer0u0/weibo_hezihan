package com.example.weibo_hezihan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);//获取SharedPreferences对象
        boolean isPrivacyAgreed = sharedPreferences.getBoolean("isPrivacyAgreed", false);

        if (isPrivacyAgreed) {
            proceedToMain();
        } else {
            showPrivacyDialog();
        }
    }

    private void proceedToMain() {//跳转到主界面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    private void showPrivacyDialog() {//显示隐私政策对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_privacy, null);
        builder.setView(dialogView)
                .setCancelable(false);//点击外部不可取消

        AlertDialog dialog = builder.create();//创建对话框
        dialog.show();

        TextView agreeText = dialogView.findViewById(R.id.agree_text);
        TextView disagreeText = dialogView.findViewById(R.id.disagree_text);
        TextView textView = dialogView.findViewById(R.id.privacy_content);

        String text = "欢迎使用 iH微博 ，我们将严格遵守相关法律和隐私政策保护您的个人隐私，请您阅读并同意《用户协议》与《隐私政策》";
        SpannableString spannableString = new SpannableString(text);

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(SplashActivity.this, "查看用户协议", Toast.LENGTH_SHORT).show();
            }
        };
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(SplashActivity.this, "查看隐私政策", Toast.LENGTH_SHORT).show();
            }
        };

        spannableString.setSpan(clickableSpan1, 43, 49, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(clickableSpan2, 50, 56, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        agreeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isPrivacyAgreed", true);
                editor.apply();
                dialog.dismiss();
                proceedToMain();
            }
        });

        disagreeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finishAffinity();
            }
        });
    }
}
