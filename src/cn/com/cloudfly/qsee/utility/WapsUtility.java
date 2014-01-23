package cn.com.cloudfly.qsee.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import cn.com.cloudfly.qsee.MainViewActivity;
import cn.com.cloudfly.qsee.QSeeGGActivity;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.activity.SettingsActivity;
import cn.com.cloudfly.qsee.activity.WallpaperExhibitionActivity;

import com.waps.AppConnect;
import com.waps.UpdatePointsNotifier;

/**
 * 用于waps平台的相关api调用的封装。
 *
 */
public class WapsUtility {
	private static final String wapsPid=
														"waps";
														//"hiapk";
														//"qq";
														//"gfan";
														//"eoe";
														//"goapk";
														//"apkChina";

	private static final String wapsId="c1d687ea987609c090e9c80a478c7a5d";
	private static Context _ctx=null;
	public static void initialize(Context ctx){
		_ctx=ctx;
		AppConnect.getInstance(wapsId,wapsPid,_ctx);	
		AppConnect.getInstance(_ctx).setAdViewClassName(QSeeGGActivity.class.getName());// "cn.com.cloudfly.qsee.QSeeGGActivity" 
		operateUserMoney(UserMoneyOperate.QUERY,0);
	}
	
	public static void finialize(){
		AppConnect.getInstance(_ctx).finalize();
		_ctx=null;
	}
	
	
	/**
	 * @return 返回waps平台的开启状态
	 */
	public static boolean isOn(){
		if (!SettingsActivity.isAdEnabled(_ctx))
			return false;
		
		if (!Utility.isNetworkAvailable()){
			return false;
		}
		
		String whEnabled=getConfig(wapsPid+"OnOff","on");
		return whEnabled.equals("on");
	}
	
	
	public static void pushAd(){
		if (isOn()){
			//AppConnect.getInstance(_ctx).getPushAd(_ctx);
		}
	}
	
	public static void checkVersionUpdate(){
		//AppConnect.getInstance(_ctx).checkUpdate(_ctx);
	}
	
	public static void startOffersActivity(){
		if (isOn()){
			AppConnect.getInstance(_ctx).showOffers(_ctx);
		}
	}

	private static String _moneyName=null;
	private static int _money=-1;
	
	public static int getUserMoney(){
			return _money;
	}
	
	public static String getMoneyDetail(int m){
		return String.format("%d %s",m,_moneyName);
	}
	
	public static String getMoreMoneyDetail(){
		return String.format("%s%s",Utility.getResourceString(R.string.GET_MORE_MONEY),_moneyName);
	}

	public static String getUserMoneyDetail(){
		if (_moneyName!=null)
			return getMoneyDetail(_money);
		else
			return Utility.getResourceString(R.string.ERROR_GET_USER_INFO);
	}
	
	public static String getMoneyQuestionInfo(int d){
		return String.format("下载需要花费 %s,您当前有 %s",WapsUtility.getMoneyDetail(d),WapsUtility.getUserMoneyDetail());
	}
	
	public static void spendUserMoney(int amount){
		operateUserMoney(UserMoneyOperate.SPEND,amount);
	}
	
	public static void awardUserMoney(int amount){
		operateUserMoney(UserMoneyOperate.AWARD,amount);
	}
	
	enum UserMoneyOperate{QUERY,SPEND,AWARD};
	private static void operateUserMoney(UserMoneyOperate o,int amount){
		UpdatePointsNotifier n=new UpdatePointsNotifier(){
			public void getUpdatePoints(String name, int points) {
				_moneyName=name;
				_money=points;
			}
			public void getUpdatePointsFailed(String error) {
				_moneyName="画展券";
				_money=-1;
				//we does not call Toast,because the callback will be called in work thread insteads of GUI thread
				//Toast.makeText(_ctx, error, Toast.LENGTH_SHORT).show();
			}};
			
		if (o==UserMoneyOperate.QUERY){
			AppConnect.getInstance(_ctx).getPoints(n); 
		}else if (o==UserMoneyOperate.SPEND){
			AppConnect.getInstance(_ctx).spendPoints(amount,n); 
		}else{
			AppConnect.getInstance(_ctx).awardPoints(amount,n); 
		}
	}
	
	public static boolean isWallpaperExhibitionEnabled(){
		if (isOn()){
			String whEnabled=getConfig("whEnabled","false");
			return whEnabled.equals("true");
		}else{
			return false;
		}
	}
	
	public static void showWallpaperExhibitionDisabledDialog(){
		String errInfo=Utility.getResourceString(R.string.ERROR_NETWORK);
		String title= getConfig("whDisabledMsg",errInfo);
		if (title.length()==0){
			title="网络访问出错，请确认有效的网络连接，然后稍候重试。";
		}
		new AlertDialog.Builder(_ctx)
			.setMessage(title)
			.setNeutralButton(R.string.CLOSE,null)
			.create()
			.show();
	}
	
	public static int getWallpaperCount(){
		String whCount=getConfig("whCount","0");
		return Integer.valueOf(whCount);
	}
	
	public static String[] getWallpaperExhibitionUrls(){
		int whCount=getWallpaperCount();
		String whBeginUrl=com.waps.AppConnect.getInstance(_ctx).getConfig("whBeginUrl");
		if (whBeginUrl!=null){
			String uris[]=new String[whCount]; 
			for (int i=1;i<=whCount;++i){
				uris[i-1]=String.format(whBeginUrl,i);
			}
			return uris;
		}else{
			return null;
		}
	
	}
	
	private static String getConfig(String name,String defaultValue){
		try{
			String v=com.waps.AppConnect.getInstance(_ctx).getConfig(name);
			if (v==null || v.equals("NULL") || v.length()==0){
				return defaultValue;
			}else{
				return v;
			}
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	private static String getWhStartupMsg(){
		String whTitle=getConfig("whTitle","");
		String whCount=getConfig("whCount","");
		return String.format("%s (%s 张)\n%s",whTitle,whCount,getConfig("whStartupMsg",""));
		
	}
	
	public static String getWhCategroy(){
		return getConfig("whCategory","");
	}
	
	public static String getMousePadCustomUrl(){
		return getConfig("mousePadCustom","http://a.m.taobao.com/i15189907566.htm");
	}
	
	public static void startWallpaperExhibition(final MainViewActivity a){
		if (!isOn()){
			return ;
		}
		String startupMsg=getWhStartupMsg();
		if (startupMsg.length()>0){
				AlertDialog dialog= new AlertDialog.Builder(a)
				.setTitle(String.format("%s: %s  \n%s", Utility.getResourceString(R.string.WALLPAPER_EXHIBITION),getWhCategroy(),startupMsg))
				.setMessage(R.string.EXHIBITION_TIPS)
				.setPositiveButton(R.string.OK, new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						String whTitle=getConfig("whTitle","");
						WallpaperExhibitionActivity.startNewInstance(a,getWallpaperExhibitionUrls(),whTitle,getWhCategroy());
					}})
				.setNeutralButton(R.string.CANCEL,null)
				.setNegativeButton("提意见", new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						com.waps.AppConnect.getInstance(a).showFeedback();
					}
					
				})	
				.create();
				dialog.show();
				return;
		}
	}
}
