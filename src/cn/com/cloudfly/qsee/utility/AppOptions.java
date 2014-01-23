package cn.com.cloudfly.qsee.utility;


import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.utility.Utility.SortPolicy;

public class AppOptions {
	public static final SharedPreferences getApplicationSharedPreferences(String name) {
		return Utility.getApplicationCtx().getSharedPreferences(name, Context.MODE_PRIVATE);
	}
	
	 static final public String readLiveWallpaperFile(){
		return getApplicationSharedPreferences("live_wallpaper").getString("file", null);
	}
	 
	 static final public boolean readShouldLiveWallpaperRefresh(){
		 return getApplicationSharedPreferences("live_wallpaper").getBoolean("refresh", true);
	 }
	
	 static final public void writeLiveWallpaperRefreshDone(){
		 getApplicationSharedPreferences("live_wallpaper").edit()
			.putBoolean("refresh", false).commit();
	 }
	 
	 static final public int readLiveWallpaperScaleAngle(){
		 return getApplicationSharedPreferences("live_wallpaper").getInt("scaleAngle", 0) % 360;
	 }
	 
	 static final public boolean isLiveWallpaperInFavoriteMode(){
		 return getApplicationSharedPreferences("live_wallpaper").getBoolean("isFavorite", false);
	 }
	 
	 static final public void writeLiveWallpaper(String filePath,int scaleAngle,boolean isInFavoriteMode){
		 getApplicationSharedPreferences("live_wallpaper").edit()
			.putString("file", filePath)
			.putBoolean("refresh", true)
			.putBoolean("isFavorite",isInFavoriteMode)
			.putInt("scaleAngle", scaleAngle).commit();		 
	 }
	 
	 
	 static final public int readViewCurrentPos(){
		 return getApplicationSharedPreferences("global").getInt("currPos", 0);
	 }
	 
	 static final public void writeViewCurrentPos(int pos){
		 getApplicationSharedPreferences("global")
			.edit()
			.putInt("currPos", pos)
			.commit();
	 }
	 
	 static final public void saveFavorite(FileItem files[]){
			StringBuilder sb=new StringBuilder();
			for (FileItem f : files){
				sb.append(f.getAbsolutePath());
				sb.append(";");
			}
			String currFavoriteFiles=getApplicationSharedPreferences("global").getString("favorite", "");
			String newFavoriteFiles=sb.toString();
			if (currFavoriteFiles.equals(newFavoriteFiles))
				return; 
			
			SharedPreferences.Editor edit=getApplicationSharedPreferences("global").edit();
			edit.putString("favorite",sb.toString());
			edit.putBoolean("favorite_changed", true);
			edit.commit();
	 }

	 static final public boolean isFavoriteChanged(){
		 return getApplicationSharedPreferences("global").getBoolean("favorite_changed", false);
	 }
	 static final public String[] loadFavorite(){
			String listStr=getApplicationSharedPreferences("global").getString("favorite", "");
			String[] paths=listStr.split(";");
			SharedPreferences.Editor edit=getApplicationSharedPreferences("global").edit();
			edit.putBoolean("favorite_changed", false);
			edit.commit();
			return paths;
	 }
	 
	 static final public void writeTabsViewVisibility(boolean v){
		 SharedPreferences.Editor edit=getApplicationSharedPreferences("global").edit();
		 edit.putBoolean("tabsview_visibility",v);
		 edit.commit();
	 }
	 
	 static final public boolean readTabsViewVisibility(){
		 return getApplicationSharedPreferences("global").getBoolean("tabsview_visibility",true );
	 }
	 
	 static final public String readCurrentPath(){
		 return getApplicationSharedPreferences("global").getString("currPath", Utility.getDefaultDirPath());
	 }
	 
	 static final public void writeCurrentPath(String p){
		 SharedPreferences.Editor edit=getApplicationSharedPreferences("global").edit();
		 edit.putString("currPath", p).commit();
	 }
	 
	 static final public String readRealCurrentPath(){
		 return getApplicationSharedPreferences("global").getString("currRealPath", Utility.getDefaultDirPath());
	 }
	 
	 static final public void writeRealCurrentPath(String p){
		 SharedPreferences.Editor edit=getApplicationSharedPreferences("global").edit();
		 edit.putString("currRealPath", p).commit();
	 }
	 
 
	 static final public SortPolicy readCurrentSortPolicy(){
		 int s= getApplicationSharedPreferences("global").getInt("currSort", SortPolicy.ReverseName.ordinal());
		 if (s==SortPolicy.Name.ordinal()){
			 return SortPolicy.Name;
		 }else if (s==SortPolicy.Time.ordinal()){
			 return SortPolicy.Time;
		 }else if (s==SortPolicy.Size.ordinal()){
			 return SortPolicy.Size;
		 }else if (s==SortPolicy.ReverseName.ordinal()){
			 return SortPolicy.ReverseName;
		 }else if (s==SortPolicy.ReverseSize.ordinal()){
			 return SortPolicy.ReverseSize;
		 }else if (s==SortPolicy.ReverseTime.ordinal()){
			 return SortPolicy.ReverseTime;
		 }else {
			 return SortPolicy.ReverseTime;
		 }
	 }
	 
	 static final public void writeCurrentSortPolicy(SortPolicy s){
		 SharedPreferences.Editor edit=getApplicationSharedPreferences("global").edit();
		 edit.putInt("currSort", s.ordinal()).commit();
	 }
	 
	 static final public void writeAutoSwitchTimerInterval(int a){
		 SharedPreferences.Editor edit=getApplicationSharedPreferences("global").edit();
		 edit.putInt("AutoSwitchInterval",a).commit();
	 }
	 
	 static final public int readAutoSwitchTimerInterval(){
		 return getApplicationSharedPreferences("global").getInt("AutoSwitchInterval", 0);
	 }
	 
	 static final public void setTopStatusBarHeightBy(Activity v){
		 Class<?> c = null;
		 Object obj = null;
		 Field field = null;
		 int x = 0, sbar = 0;
		 try {
			 c = Class.forName("com.android.internal.R$dimen");
			 obj = c.newInstance();
			 field = c.getField("status_bar_height");
			 x = Integer.parseInt(field.get(obj).toString());
			 sbar = v.getResources().getDimensionPixelSize(x);
		 } catch (Exception e1) {
			 e1.printStackTrace();
		 } 		 
		 int statusBarHeight = sbar;
		 //Utility.toastDebug(String.format("TopStatusBarHeight=%d",statusBarHeight));
		 SharedPreferences.Editor edit=getApplicationSharedPreferences("global").edit();
		 edit.putInt("TopStatusBarHeight",statusBarHeight).commit();
	 }
	 
	 static final public int getTopStatusBarHeight(){
		 return getApplicationSharedPreferences("global").getInt("TopStatusBarHeight", 0);
	 }
	 
	 static final public void setLastWallpaperExhibitionCategory(String category){
		 getApplicationSharedPreferences("global").edit().putString("lastExhibionCategory", category).commit(); 
	 }
	 
	 static final public String getLastWallpaperExhibitionCategory(){
		 String c=getApplicationSharedPreferences("global").getString("lastExhibionCategory", "");
		 return c;
	 }
//	 static final public String getLastWallpaperStartupMsg(){
//		 return getApplicationSharedPreferences("global").getString("LastWallpaperStartupMsg", "");
//	 }
//	 
//	 static final public void setLastWallpaperStartupMsg(String  msg){
//		 SharedPreferences.Editor edit=getApplicationSharedPreferences("global").edit();
//		 edit.putString("LastWallpaperStartupMsg",msg).commit();
//	 }
}
