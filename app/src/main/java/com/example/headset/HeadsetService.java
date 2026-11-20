package com.example.headset;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.content.Intent;
import android.os.SystemClock;

public class HeadsetService extends AccessibilityService {
    public static final String ACTION_KEY = "com.example.headset.KEY_EVENT";
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override protected boolean onKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();
        if (key == KeyEvent.KEYCODE_HEADSETHOOK || key == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
            key == KeyEvent.KEYCODE_MEDIA_NEXT || key == KeyEvent.KEYCODE_MEDIA_PREVIOUS ||
            key == KeyEvent.KEYCODE_MEDIA_PLAY || key == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            Intent i = new Intent(ACTION_KEY);
            i.setPackage(getPackageName());
            i.putExtra("code", KeyEvent.keyCodeToString(key));
            i.putExtra("action", event.getAction() == KeyEvent.ACTION_DOWN ? "DOWN" : "UP");
            i.putExtra("time", SystemClock.elapsedRealtime());
            sendBroadcast(i);
        }
        return false; // Never consume media buttons: keep Yandex Music working.
    }
}
