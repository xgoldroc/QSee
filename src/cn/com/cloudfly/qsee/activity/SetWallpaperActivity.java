/**
 * 
 */
package cn.com.cloudfly.qsee.activity;


import java.io.File;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.model.FolderModelManager;
import cn.com.cloudfly.qsee.model.FolderModelManager.Mode;
import cn.com.cloudfly.qsee.service.CommandReceiver;
import cn.com.cloudfly.qsee.utility.AppOptions;
import cn.com.cloudfly.qsee.utility.Utility;

public class SetWallpaperActivity extends EntranceActivity implements DialogInterface.OnClickListener {
	private Uri _wallpaperFileUri=null;
	private int _scaleAngle=0;
	@Override
	protected void onStart() {
		super.onStart();
		
		Intent intent=this.getIntent();
		String action=intent.getAction();
		
		if (!Intent.ACTION_SEND.equals(action)){
			Utility.toastDebug("Intent is not ACTION_SEND,ignore");
			this.finish();
			return;
		}
		
		_wallpaperFileUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
		if (!_wallpaperFileUri.getScheme().equals("file")){
			//from 3rdParty app ,not from QSee
			_wallpaperFileUri=Utility.getFileUriFromContentUri(_wallpaperFileUri);
		}

		if (_wallpaperFileUri==null){
			this.finish();
			return;
		}

		
		if (!intent.getExtras().getBoolean("fromQSee")){
			new AlertDialog.Builder(this)
			.setMessage(R.string.CONFIRM_SET_WALLPAPER)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(R.string.OK, this)
			.setNegativeButton(R.string.CANCEL, this)
			.show();
		}else{
			_scaleAngle=intent.getExtras().getInt("scaleAngle");
			setWallpaper();
		}
		
	}

	public void onClick(DialogInterface dialog, int which) {
		if (which==DialogInterface.BUTTON_POSITIVE){
			this.setWallpaper();
		}else if (which==DialogInterface.BUTTON_NEGATIVE){
			this.finish();
		}
	}
	
	private void setWallpaper(){
		setWallpaper(_wallpaperFileUri.getPath(),_scaleAngle);
	}
	private void setWallpaper(String imageFile,int rotationAngle/*90,180,-90,270,...*/){
		if (WallpaperManager.getInstance(this)==null){
			Toast.makeText(this, R.string.SORRY_FOR_NOT_SUPPORT_DYNAMIC_WALLPAPER, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		setLiveWallpaperToService(imageFile,rotationAngle,FolderModelManager.getInstance().mode()==Mode.FAVORITE_SYSTEM);
	
		if (!Utility.isWallpaperQSee()){
			new AlertDialog.Builder(this)
			.setMessage(R.string.WALLPAPER_START_QUERY)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					try{
						Intent i=new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
						SetWallpaperActivity.this.startActivity(i);
					}catch(ActivityNotFoundException e){
						Toast.makeText(SetWallpaperActivity.this, R.string.SORRY_FOR_NOT_SUPPORT_DYNAMIC_WALLPAPER, Toast.LENGTH_LONG).show();	
					}
					finish();
				}
			})
			.setNegativeButton(R.string.CANCEL,  this)
			.show();
		}else{
			//Toast.makeText(SetWallpaperActivity.this, R.string.SETTING_WALLPAPER_DONE, Toast.LENGTH_SHORT).show();	
			finish();
		}
	}

	private static void setLiveWallpaperToService(String filePath, int scaleAngle,boolean isFavorite) {
		AppOptions.writeLiveWallpaper(filePath, scaleAngle, isFavorite);

		Intent intent = new Intent(CommandReceiver.ACTION_SEND_COMMAND);
		intent.putExtra("cmd", 1);
		intent.putExtra("params", filePath);
		Utility.getApplicationCtx().sendBroadcast(intent);
	}
	
	public static void startNewInstance(Context ctx,String imgFilePath,int scaleAngle){
		Intent intent=new Intent(android.content.Intent.ACTION_SEND);
		intent.setClass(ctx,SetWallpaperActivity.class);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imgFilePath)));
		intent.putExtra("fromQSee", true);
		intent.putExtra("scaleAngle",0);
		ctx.startActivity(intent);
	}
	
}
