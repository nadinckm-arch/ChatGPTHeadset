package com.example.headset;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b) { super.onCreate(b); TextView t = new TextView(this); t.setText("ChatGPT Headset diagnostic prototype\n\nThis version does not yet control ChatGPT Voice.\nNext step: test headset button events on HONOR Earbuds 4."); t.setTextSize(20); t.setPadding(32,48,32,32); setContentView(t); }
}
