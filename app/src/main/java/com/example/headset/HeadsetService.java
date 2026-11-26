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
 private final ArrayList<String> sequence=new ArrayList<>();
 private final Map<Integer,Long> downs=new HashMap<>();
 private final Set<Integer> captured=new HashSet<>();
 private final Runnable finish=()->{
 if(sequence.isEmpty())return;
 String pattern=android.text.TextUtils.join(",",sequence);sequence.clear();
 android.content.SharedPreferences prefs=getSharedPreferences("controls",0);
 if(prefs.getBoolean("learning",false)){
 prefs.edit().putString("pattern",pattern).putBoolean("learning",false).apply();log("Команда сохранена: "+pattern);
 }else if(pattern.equals(prefs.getString("pattern","87:S"))){toggle();}
 else log("Не совпало: "+pattern);
 };
 @Override protected boolean onKeyEvent(KeyEvent e){
 int k=e.getKeyCode();
 if(k==KeyEvent.KEYCODE_POWER||k==KeyEvent.KEYCODE_HOME||k==KeyEvent.KEYCODE_BACK||k==KeyEvent.KEYCODE_SLEEP||k==KeyEvent.KEYCODE_WAKEUP)return false;
 android.content.SharedPreferences prefs=getSharedPreferences("controls",0);
 boolean learning=prefs.getBoolean("learning",false);
 String pattern=prefs.getString("pattern","87:S");
 if(e.getAction()==KeyEvent.ACTION_DOWN){
 if(e.getRepeatCount()!=0)return captured.contains(k);
 if(!learning){
 boolean relevant=false;
 for(String token:pattern.split(","))if(token.startsWith(k+":"))relevant=true;
 if(!relevant)return false;
 if(find(root(),true)==null&&(active(TRAIN)||active(MUSIC)))return false;
 }
 handler.removeCallbacks(finish);
 captured.add(k);downs.put(k,e.getEventTime());return true;
 }
 if(e.getAction()==KeyEvent.ACTION_UP&&captured.remove(k)){
 Long start=downs.remove(k);
 sequence.add(k+":"+(start!=null&&e.getEventTime()-start>=600?"L":"S"));
 handler.removeCallbacks(finish);handler.postDelayed(finish,700);return true;
 }
 return false;
 }
}
