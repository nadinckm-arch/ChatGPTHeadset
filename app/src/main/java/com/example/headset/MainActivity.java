package com.example.headset;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
 private TextView log;
 private final BroadcastReceiver receiver = new BroadcastReceiver() {
  @Override public void onReceive(Context context, Intent intent) {
   String msg = intent.getStringExtra("code") + " " + intent.getStringExtra("action");
   log.setText(msg + "\n" + log.getText());
  }
 };
 @Override public void onCreate(Bundle b) {
  super.onCreate(b);
  LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,40,32,32);
  TextView info = new TextView(this);
  info.setText("ChatGPTHeadset 0.3 — управление голосом и медиакнопками. Включите специальные возможности и доступ к уведомлениям.");
  info.setTextSize(18); root.addView(info);
  Button enable = new Button(this); enable.setText("Open accessibility settings");
  enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); root.addView(enable);
  Button media = new Button(this); media.setText("Доступ к уведомлениям"); media.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))); root.addView(media);
  Button learn=new Button(this); learn.setText("Обучить команду"); root.addView(learn);
  learn.setOnClickListener(v->{getSharedPreferences("controls",0).edit().putBoolean("learning",true).apply();log.setText("Выполни комбинацию. Пауза 700 мс завершает запись.");});
  Button cancel=new Button(this);cancel.setText("Отменить обучение");root.addView(cancel);cancel.setOnClickListener(v->{getSharedPreferences("controls",0).edit().putBoolean("learning",false).apply();log.setText("Обучение отменено");});
  log = new TextView(this); log.setText("No headset key events captured yet."); log.setTextSize(17); root.addView(log);
  setContentView(root);
 }
 @Override protected void onStart() { super.onStart(); IntentFilter filter = new IntentFilter(HeadsetService.ACTION_KEY); if(android.os.Build.VERSION.SDK_INT>=33) registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED); else registerReceiver(receiver, filter); }
 @Override protected void onStop() { unregisterReceiver(receiver); super.onStop(); }
}
