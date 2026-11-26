package com.example.headset;
import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.media.session.*;
import android.os.*;
import android.view.KeyEvent;
import android.view.accessibility.*;
import java.util.*;

public class HeadsetService extends AccessibilityService {
 public static final String ACTION_KEY="com.example.headset.KEY_EVENT";
 private static final String CHAT="com.openai.chatgpt", MUSIC="ru.yandex.music", TRAIN="ru.nadin.pulsetimer";
 private final Handler handler=new Handler(Looper.getMainLooper());
 private String foreground="";
 private boolean pending=false, consumed=false;
 private long last=0;
 private void log(String s){Intent i=new Intent(ACTION_KEY).setPackage(getPackageName());i.putExtra("code",s);i.putExtra("action","");sendBroadcast(i);}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){
 if(e.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && e.getPackageName()!=null) foreground=e.getPackageName().toString();
 if(pending) attempt();
 }
 @Override public void onInterrupt(){pending=false;}
 private boolean active(String pkg){
 if(pkg.equals(foreground))return true;
 try{
 MediaSessionManager m=(MediaSessionManager)getSystemService(MEDIA_SESSION_SERVICE);
 for(MediaController c:m.getActiveSessions(new ComponentName(this,SessionListener.class))){
 if(!pkg.equals(c.getPackageName()))continue;
 PlaybackState s=c.getPlaybackState();
 if(s!=null && s.getState()!=PlaybackState.STATE_NONE && s.getState()!=PlaybackState.STATE_STOPPED && s.getState()!=PlaybackState.STATE_ERROR)return true;
 }
 }catch(SecurityException e){log("Нет доступа к медиасессиям");return true;}
 return false;
 }
 private AccessibilityNodeInfo find(AccessibilityNodeInfo n,boolean close){
 if(n==null)return null;
 String t=(String.valueOf(n.getContentDescription())+" "+String.valueOf(n.getText())).toLowerCase(Locale.ROOT);
 String[] labels=close?new String[]{"end voice","end conversation","exit voice","завершить голос","завершить разговор","выйти из голос","close voice"}:new String[]{"start voice","voice mode","start a voice","голосовой режим","начать голос","голосовой помощник"};
 for(String l:labels)if(t.contains(l)&&n.isClickable()&&n.isEnabled())return n;
 for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo a=find(n.getChild(i),close);if(a!=null)return a;}
 return null;
 }
 private AccessibilityNodeInfo root(){AccessibilityNodeInfo n=getRootInActiveWindow();return n!=null&&CHAT.contentEquals(n.getPackageName())?n:null;}
 private void attempt(){
 if(!pending)return;
 AccessibilityNodeInfo n=find(root(),false);
 if(n!=null&&n.performAction(AccessibilityNodeInfo.ACTION_CLICK)){pending=false;log("Запуск голосового режима");}
 }
 private void toggle(){
 AccessibilityNodeInfo n=find(root(),true);
 if(n!=null&&n.performAction(AccessibilityNodeInfo.ACTION_CLICK)){pending=false;log("Завершение голосового режима");return;}
 pending=true;
 Intent i=getPackageManager().getLaunchIntentForPackage(CHAT);
 if(i==null){pending=false;log("ChatGPT не найден");return;}
 try{startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(Exception e){pending=false;log("Запуск заблокирован");return;}
 handler.postDelayed(()->attempt(),500);
 handler.postDelayed(()->attempt(),1500);
 handler.postDelayed(()->{if(pending){pending=false;log("Кнопка голосового режима не найдена");}},5000);
 }
 @Override protected boolean onKeyEvent(KeyEvent e){
 int k=e.getKeyCode();
 if(k!=KeyEvent.KEYCODE_HEADSETHOOK&&k!=KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE&&k!=KeyEvent.KEYCODE_MEDIA_NEXT)return false;
 if(e.getAction()==KeyEvent.ACTION_UP)return consumed;
 if(e.getRepeatCount()!=0)return consumed;
 boolean voice=find(root(),true)!=null;
 if(!voice&&(active(TRAIN)||active(MUSIC))){consumed=false;last=0;return false;}
 consumed=true;
 long now=SystemClock.elapsedRealtime();
 // Earbuds may encode double tap as MEDIA_NEXT; other devices send two play/pause events.
 if(k==KeyEvent.KEYCODE_MEDIA_NEXT||(last!=0&&now-last<=650)){last=0;toggle();}
 else{last=now;log("Ожидание второго нажатия");}
 return true;
 }
}
