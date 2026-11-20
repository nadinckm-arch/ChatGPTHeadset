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
  info.setText("HONOR Earbuds 4 — gesture diagnostics\n\nEnable ChatGPT Headset accessibility service, return here and test double/triple taps on either earbud. Yandex Music controls should remain functional.\n\nThis prototype does NOT start ChatGPT Voice.");
  info.setTextSize(18); root.addView(info);
  Button enable = new Button(this); enable.setText("Open accessibility settings");
  enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); root.addView(enable);
  log = new TextView(this); log.setText("No headset key events captured yet."); log.setTextSize(17); root.addView(log);
  setContentView(root);
 }
 @Override protected void onStart() { super.onStart(); IntentFilter filter = new IntentFilter(HeadsetService.ACTION_KEY); registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED); }
 @Override protected void onStop() { unregisterReceiver(receiver); super.onStop(); }
}
