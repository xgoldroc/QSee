package cn.com.cloudfly.qsee.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.service.CommandReceiver;
import cn.com.cloudfly.qsee.utility.AppOptions;
import cn.com.cloudfly.qsee.utility.Utility;
import cn.com.cloudfly.qsee.utility.WapsUtility;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.settings);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		Log.d("HH", key +" changed");
		if (key.equals("timerSwitchImage") || key.equals("timerInterval")){
			int interval=getTimerSwitchInterval(SettingsActivity.this);
			AppOptions.writeAutoSwitchTimerInterval(interval);	
			Utility.toastDebug("changed :"+interval/1000+" s");
			Intent intent = new Intent(CommandReceiver.ACTION_SEND_COMMAND);
			intent.putExtra("cmd", 2);
			intent.putExtra("params", String.valueOf(interval));
			Utility.getApplicationCtx().sendBroadcast(intent);
		}
	}
	
	@Override
	protected void onPause(){
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}

	public static boolean hasHints(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("hints", false);
	}

	public static boolean isShowDirContainsImagesCount(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("showDirContainsImagesCount", true);
	}

	public static boolean isShowDirThumb(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("showDirThumb", true);
	}

	public static boolean isSentorSwitchImageEnabled(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("sentorSwitchImage", false);
	}
	
	public static boolean isDblClickToNextImage(Context ctx){
		return PreferenceManager.getDefaultSharedPreferences(ctx)
				.getBoolean("dblClickToNextImage", true);
	}
	
	public static int dblClickPrevAreaWidthPercent(Context ctx){
		String s= PreferenceManager.getDefaultSharedPreferences(ctx)
				.getString("dblClickPrevAreaWidth", "4");
		return Integer.valueOf(s);
	}
	
	public static boolean isTimerSwitchImageEnabled(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("timerSwitchImage", false);
	}
	
	public static int getTimerSwitchInterval(Context context){
		if (!isTimerSwitchImageEnabled(context)){
			return 0;
		}
		String interval=PreferenceManager.getDefaultSharedPreferences(context)
				.getString("timerInterval", "15");
		return Integer.valueOf(interval)*60*1000;
	}
	//public static int imageCellCounts = 0;
	
	public static void startNewInstance(Context ctx){
		ctx.startActivity(new Intent(ctx, SettingsActivity.class));
	}
	
	public static boolean isExhibitionNewEnabled(Context ctx){
		return PreferenceManager.getDefaultSharedPreferences(ctx)
				.getBoolean("exhibitionNewEnabled", true) && WapsUtility.isOn();
	}

	public static boolean isAdEnabled(Context ctx){
		return true;
//		return PreferenceManager.getDefaultSharedPreferences(ctx)
//				.getBoolean("adEnabled", true);
	}
	
}
