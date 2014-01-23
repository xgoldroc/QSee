package cn.com.cloudfly.qsee.activity;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;
import cn.com.cloudfly.qsee.MenuMgr;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.model.AbstractFolderModel;
import cn.com.cloudfly.qsee.model.FavoriteFolderModel;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.model.FolderModelIterator;
import cn.com.cloudfly.qsee.model.FolderModelManager;
import cn.com.cloudfly.qsee.model.VirtualFolderModel;
import cn.com.cloudfly.qsee.utility.AppOptions;
import cn.com.cloudfly.qsee.utility.SerialDownloadTask.OnDownloadListener;
import cn.com.cloudfly.qsee.utility.Utility;
import cn.com.cloudfly.qsee.utility.WapsUtility;

public class ImageSwitcherActivity extends EntranceActivity implements ViewFactory, Runnable{
	private TranslateAnimation _leftOutAnim;
	private TranslateAnimation _leftInAnim;
	private TranslateAnimation _rightInAnim;
	private TranslateAnimation _rightOutAnim;
	private ImageSwitcher _imgSwitcher=null;
	private TextView _titleView=null;
	
	private float _x=0;//, y, z;
	private SensorEventListener _lsn=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		LinearLayout layout=new LinearLayout(this);
		layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		layout.setBackgroundColor(Color.BLACK);
		layout.setOrientation(LinearLayout.VERTICAL);
		//layout.setBackgroundResource(R.drawable.app);
		
		this.setContentView(layout);

		final int scrWidth=Utility.getScreenWidth();
		
		
		_titleView=new TextView(this); 
		_titleView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		_titleView.setBackgroundColor(Color.GRAY);
		_titleView.setTextColor(Color.LTGRAY);
		
		_imgSwitcher=new ImageSwitcher(this);
		_imgSwitcher.setFactory(this);
		_imgSwitcher.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		
		_leftOutAnim=new TranslateAnimation(0, -scrWidth,0, 0);
		_leftInAnim=new TranslateAnimation(scrWidth, 0,0, 0);
		 
		_rightOutAnim=new TranslateAnimation(0, scrWidth,0, 0);
		_rightInAnim=new TranslateAnimation(-scrWidth, 0,0, 0);
		
	
		setAnimationDuration(300);
		
		//_imgSwitcher.setOnCreateContextMenuListener(this);
		layout.addView(_titleView);
		layout.addView(_imgSwitcher);
		
		_lsn = new SensorEventListener() {
			public void onSensorChanged(SensorEvent e) {
				_x = e.values[SensorManager.DATA_X];
				//y = e.values[SensorManager.DATA_Y];
				//z = e.values[SensorManager.DATA_Z];
				moveNextOrPrevByX(_x);	
			}
			public void onAccuracyChanged(Sensor s, int accuracy) {
			}
		};
		
		if (SettingsActivity.isSentorSwitchImageEnabled(this)){
			SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
			Sensor sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorMgr.registerListener(_lsn, sensor, SensorManager.SENSOR_DELAY_GAME);
		}
	
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	String getCallerNameFrom(Intent intent){
		String cn=intent.getStringExtra("caller");
		if (cn==null){
			return "unknown";
		}else{
			return cn;
		}
	}
	
	boolean getModelModeIsFavoriteFrom(Intent intent){
		return intent.getBooleanExtra("isFavoriteMode", false);
	}
	
	void saveImagePath(String imagePath){
		SharedPreferences settings=this.getPreferences(Context.MODE_PRIVATE );
		settings.edit().putString(getCallerNameFrom(this.getIntent())+".currImgPath",imagePath).commit();
	}
	
	String restoreImagePath(){
		SharedPreferences settings=this.getPreferences(Context.MODE_PRIVATE );
		return settings.getString(getCallerNameFrom(this.getIntent())+".currImgPath",Utility.getDefaultDirPath());
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent=this.getIntent();
		Uri fileUri=null;
		String action=intent.getAction();
//		if (action!=null && action.length()>0){
//			Toast.makeText(this,action, Toast.LENGTH_SHORT).show();
//		}
		
		if (Intent.ACTION_SEND.equals(action)){
			fileUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
			//Log.d("HAHA", fileUri.toString());
			if (!fileUri.getScheme().equals("file")){
				fileUri=Utility.getFileUriFromContentUri(fileUri);
			}
		}else{	
			//if (action == null ||Intent.ACTION_VIEW.equals(action)){
			fileUri=intent.getData();
			//Log.d("HAHA", fileUri.toString());
		}
			
		if (fileUri!=null && !fileUri.equals(Uri.EMPTY) && intent.getBooleanExtra("used",false)==false){
				String currImagePath=fileUri.toString();
				if (!Utility.isPathUrl(currImagePath)){
					currImagePath=fileUri.getPath();
				}
				//Log.d("HAHA", currImagePath);
				saveImagePath(currImagePath);
				this.getIntent().putExtra("used",true);
		}else{
				//Log.d("HAHA", "No data URI in ACTION");
				//Toast.makeText(this,"No data URI in ACTION", Toast.LENGTH_SHORT).show();
		}
	
		//Log.d("HAHA", "onStart");
		String currImagePath=restoreImagePath();
		if (currImagePath==null || currImagePath.length()==0){
			//Log.d("HAHA", "NULL");
			Toast.makeText(this,"Dta URI is empty", Toast.LENGTH_SHORT).show();
			this.finish();
			return;
		}
		
		//Log.d("HAHA", currImagePath);
		
		//Toast.makeText(this,currImagePath ,Toast.LENGTH_SHORT);
		//this.finish();
		//Utility.toastDebug("ImageSiwtcher");
		if (getModelModeIsFavoriteFrom(this.getIntent())){
			Utility.toastDebug("ImageSiwtcher load favorite");
			FavoriteFolderModel.getInstance().invalidate();
			FolderModelManager.getInstance().setCurrentPath(FavoriteFolderModel.getInstance().getPath());
		}else{
			if (FolderModelManager.getInstance().mode()==FolderModelManager.Mode.FAVORITE_SYSTEM){
				FolderModelManager.getInstance().setCurrentPath(new File(currImagePath).getParent());
			}
		}
		
		
		refresh(currImagePath);
		if (SettingsActivity.isSentorSwitchImageEnabled(this)){
			Utility.unlockScreenPower(this);
		}
	}

	private Toast _modeToast=null;
	
	@Override
	protected void onStop() {
		if (this.currentImageFileItem()!=null){
			saveImagePath(currentImageFileItem().getAbsolutePath());
		}
	
		if (SettingsActivity.isSentorSwitchImageEnabled(this)){
			SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
			sensorMgr.unregisterListener(_lsn);
		}
		if (SettingsActivity.isSentorSwitchImageEnabled(this)){
			Utility.lockScreenPower(this);
		}
		if (_modeToast!=null){
			_modeToast.cancel();
		}
		super.onStop();
	}
	
	@Override
	protected void onPause() {
		if (this._currentIterator!=null){
			AppOptions.writeViewCurrentPos(this._currentIterator.getIndex());
		}
	
		super.onPause();
	}

	private boolean _adapterMode=true;
	private void switchViewMode(){
		if (_modeToast==null){
			_modeToast=Toast.makeText(this,R.string.ENTER_SINGLE_IMG_ZOOMIN_MODE, Toast.LENGTH_SHORT);
		}
		_adapterMode=!_adapterMode;
		ImageView currView=(ImageView) _imgSwitcher.getCurrentView();
		if (_adapterMode){
			currView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			currView.scrollTo(0,0);
			_titleView.setVisibility(View.VISIBLE);
			_modeToast.cancel();
		}else{
			_modeToast.show();
			currView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			_titleView.setVisibility(View.GONE);
		}
	}
	

	public void returnBack(){
		this.setResult(_needRefresh);
		_needRefresh=0;
		this.finish();
	}
	
	int _needRefresh=0;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case R.string.MENU_MAKE_IMAGE_PAD:
			new AlertDialog.Builder(this)
			.setTitle(R.string.MENU_TAOBAO_STORE)
			.setIcon(android.R.drawable.ic_menu_gallery)
			.setMessage("特喜欢一张图片？\n定制成鼠标垫放手边吧。")
			.setPositiveButton("去看看先", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
						Utility.startWebBrowserActivity(ImageSwitcherActivity.this, WapsUtility.getMousePadCustomUrl());
				}
			})
			.setNegativeButton(R.string.CLOSE, null)
			.show();
			
			break;
		case R.string.MENU_ROTATION_RIGHT:
			new AlertDialog.Builder(this)
			.setTitle(R.string.SELECT_ROTATION_MODE)
			.setIcon(android.R.drawable.ic_menu_rotate)
			.setItems(R.array.rotation_mode, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					if (which==0){	
						_scaleAngle-=90;
					}else if (which==1){
						_scaleAngle+=90;
					}else if (which==2){
						_scaleAngle+=180;
					}else{
						_scaleAngle=0;
					}
					currentImageFileItem().setProperty("scaleAngle", String.valueOf(_scaleAngle));
					_needRefresh=1;
					refreshCurrent();
				}
					
			})
			.show();
	
			break;
		case R.string.MENU_ZOOM://zoom in/out
			switchViewMode();
			break;
		case R.string.MENU_REMOVE:
		case 	R.string.MENU_DELETE:
			new AlertDialog.Builder(this)
			.setTitle(R.string.CONFIRM_DELETE)
			.setMessage(getDeleteConfirmText())
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					FileItem fileItem=ImageSwitcherActivity.this.currentImageFileItem();
					if (fileItem.delete()){
//						if (FolderModelManager.getInstance().mode()==FolderModelManager.Mode.FAVORITE_SYSTEM){
//							FavoriteFolderModel.getInstance().save();
//							
//						}
						_needRefresh=1;
						refreshCurrent();
					}else{
						String tmt=Utility.getResourceString(R.string.ERROR_CAN_NOT_DELETE)+fileItem.getAbsolutePath()+".";
						Toast.makeText(ImageSwitcherActivity.this, tmt, Toast.LENGTH_SHORT).show();
					}
				}
			})
			.setNegativeButton(R.string.CANCEL, null)
			.show();
			break;
		case R.string.MENU_ADD_TO_FAVORITE://add to favorite
			if (FavoriteFolderModel.getInstance().containsItemPath(currentImageFileItem().getAbsolutePath())){
				Toast.makeText(this, R.string.FAVORITE_ALREADY_CONTAINS, Toast.LENGTH_SHORT).show();
			}else{
				FavoriteFolderModel.getInstance().addItemPath(currentImageFileItem().getAbsolutePath());
				String tmt=Utility.getResourceString(R.string.SUCCESS_TO_ADD_FAVORITE)
								+"("+Utility.getResourceString(R.string.HAS)
								+FavoriteFolderModel.getInstance().size()
								+Utility.getResourceString(R.string.MANY_IMAGE)+")";
				Toast.makeText(this, tmt, Toast.LENGTH_SHORT).show();
				///FavoriteFolderModel.getInstance().save();
			}
			break;
		case R.string.MENU_DOWNLOAD_WALLPAPER:
		{
			FileItem currImgFileItem=currentImageFileItem();
			currImgFileItem.startDownload(this,new OnDownloadListener(){

				public void onFileDownloadDone(String file) {
					Utility.toastDebug(String.format("%s 下载完成", file));	
					WapsUtility.spendUserMoney(1);
				}

				public void onAllDownloadDone(String downloadDir) {
					Toast.makeText(ImageSwitcherActivity.this, "下载完成,保存到"+downloadDir, Toast.LENGTH_LONG).show();	
				}
			});
			break;
		}
		case R.string.MENU_SET_AS_WALLPAPER://set wallpaper
			final FileItem currImgFileItem=currentImageFileItem();
			currImgFileItem.startDownload(this,new OnDownloadListener(){

				public void onFileDownloadDone(String file) {
					Utility.toastDebug(String.format("%s 下载完成", file));	
					WapsUtility.spendUserMoney(1);
				}

				public void onAllDownloadDone(String downloadDir) {
					SetWallpaperActivity.startNewInstance(ImageSwitcherActivity.this,currImgFileItem.getLocalDownloadFileItem().getAbsolutePath(),_scaleAngle);
				}
				
			});
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	String getDeleteConfirmText(){
		if (FolderModelManager.getInstance().mode()!=FolderModelManager.Mode.FAVORITE_SYSTEM){
			return currentImageFileItem().getAbsolutePath()+"?";
		}else{
			return currentImageFileItem().getAbsolutePath()+"?\n"+Utility.getResourceString(R.string.FAVORITE_DELETE_COMMENT);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuMgr.createImageSwitcherMenu(menu,this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuMgr.prepareImageSwitcherMenu(menu,this);
		return super.onPrepareOptionsMenu(menu);
	}
	
	
	void moveNextOrPrevByX(float x){
		final int MIN_X_EDGE=4;
		if (x>MIN_X_EDGE ){
			if (!_sensotTriggered){
				moveNext();
				_sensotTriggered=true;
			}
		}else if (x<-MIN_X_EDGE ){
			if (!_sensotTriggered){
				movePrev();
				_sensotTriggered=true;
			}
		}else{
			_sensotTriggered=false;
		}
	}
	private void  setAnimationDuration(int d){
		_leftOutAnim.setDuration(d);
		_leftInAnim.setDuration(d);
		_rightOutAnim.setDuration(d);
		_rightInAnim.setDuration(d);
	}
	private boolean _sensotTriggered=false;

	public FileItem currentImageFileItem(){
		if (_currentIterator==null)
			return null;
		
		return _currentIterator.getCurrent();	
	}
	
	private void refresh(String imagePath) {
		AbstractFolderModel _folderModel=FolderModelManager.getInstance().getCurrentModel();
		if (_folderModel!=FavoriteFolderModel.getInstance()){
			if (_folderModel==null || !_folderModel.isParentOf(imagePath)){
				FolderModelManager.getInstance().setCurrentPath(FileItem.getParentPathFrom(imagePath));
				_folderModel=FolderModelManager.getInstance().getCurrentModel();
				if (Utility.isPathUrl(imagePath)){
					((VirtualFolderModel) _folderModel).addItemPath(imagePath);
				}
			}
		}
		
		_currentIterator=_folderModel.find(imagePath);
		if (_currentIterator!=null){
			refreshCurrent();
		}else{
			this.finish();
		}
	
	}

	private FolderModelIterator _currentIterator=null;
	
	public View makeView() {
		ImageView view=new ImageView(this);
		view.setScaleType(ImageView.ScaleType.FIT_CENTER);
		view.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		return view;
	}
	
	public void run(){
		if (this._currentIterator!=null){
			FolderModelIterator iterator=this._currentIterator.getDistanceIterator(2);
			iterator.getCurrent().prepareBitmap(FileItem.CacheOperate.CLEAR);//2
			iterator.goPrev();
			iterator.getCurrent().prepareBitmap(FileItem.CacheOperate.PREPARE);//1
			iterator.goPrev(); 
			//0: skip current item
			iterator.goPrev(); 
			iterator.getCurrent().prepareBitmap(FileItem.CacheOperate.PREPARE);//-1
			iterator.goPrev(); 
			iterator.getCurrent().prepareBitmap(FileItem.CacheOperate.CLEAR);//-2
		}
	}
	
	private int _scaleAngle=0;
	private static ExecutorService _pool=Executors.newSingleThreadExecutor();
	
	void refreshCurrent(){
		if (_currentIterator!=null && _currentIterator.getCurrent()!=null){
			FileItem currFile=_currentIterator.getCurrent();
			String scaleAngle=currFile.getProperty("scaleAngle", "0");
			if (scaleAngle==null){
				this._scaleAngle=0;
			}else{
				this._scaleAngle=Integer.parseInt(scaleAngle);
			}

			Bitmap bmp=currFile.bitmap(_scaleAngle);
			if (bmp!=null){
				_imgSwitcher.setImageDrawable(new BitmapDrawable(bmp));
			}else{
				Toast.makeText(this,R.string.FAILED_LOAD_IMAGE, Toast.LENGTH_SHORT).show();
			}
			_pool.execute(this);//asyncLoadingImages --> call run() in work thread
			updateTitle();	
		}
		ImageView currView=(ImageView) _imgSwitcher.getCurrentView();
		currView.setScaleType(_adapterMode?ImageView.ScaleType.FIT_CENTER:ImageView.ScaleType.CENTER_CROP);
		currView.scrollTo(0,0);
	}
	
	void updateTitle(){
		if (_titleView.getVisibility()==View.VISIBLE){
			FileItem file=currentImageFileItem().getLocalDownloadFileItem();
			if (file!=null){
				Rect rect=Utility.getBitmapRect(file.getAbsolutePath());
				String titleString=null;
				if (rect.isEmpty()){
					titleString=String.format(" %s ",  _currentIterator.getPositionString());
				}else{
					titleString=String.format(" %s - %d X %d %s",  _currentIterator.getPositionString(),
							rect.width(),rect.height(),file.getName());
				}
				_titleView.setText(titleString);
			}else{
					
			}
		}
	}
	
	
	void moveNext(){
		if (_imgSwitcher.getInAnimation()!=_leftInAnim){
			_imgSwitcher.setInAnimation(_leftInAnim);
		}
		if (_imgSwitcher.getOutAnimation()!=_leftOutAnim){
			_imgSwitcher.setOutAnimation(_leftOutAnim); 
		}
		_scaleAngle=0;
		_currentIterator.goNext();
		refreshCurrent();
	}
	
	void movePrev(){
		if (_imgSwitcher.getInAnimation()!=_rightInAnim){
			_imgSwitcher.setInAnimation(_rightInAnim);
		}
		if (_imgSwitcher.getOutAnimation()!=_rightOutAnim){
			_imgSwitcher.setOutAnimation(_rightOutAnim); 
		}
		_scaleAngle=0;
		_currentIterator.goPrev();
		refreshCurrent();
	}
	
	private boolean _touched=false;
	private float _downX=0;
	private long _downTimeMillis=System.currentTimeMillis();
	
	private void onDblClicked(){
		this.switchViewMode();
	}
	
	private final int MIN_OFFEST=10;
	private final int MIN_DBL_CLICK_INTERVAL=300;
	private final int MIN_DBL_CLICK_RECT=20;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		
		float x=event.getX();
		switch(event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			_touched=true;
			if (System.currentTimeMillis()-_downTimeMillis<MIN_DBL_CLICK_INTERVAL && Math.abs(_downX-x)<MIN_DBL_CLICK_RECT){
				onDblClicked();
			}
			_downX=x;
			_downTimeMillis=  System.currentTimeMillis()  ;
			break;
		case MotionEvent.ACTION_UP:
			if (_touched){
				_touched=false;
				float offset=x-_downX;
				if (offset==0 ||!this._adapterMode)
					break;
			
				int duration=(int)(System.currentTimeMillis()-_downTimeMillis);
				double unitDurcation=Math.abs(duration/offset)/2;
				if (offset<-MIN_OFFEST){
					setAnimationDuration((int) ((Utility.getScreenWidth()+offset)*unitDurcation));
					moveNext();
				}else if (offset>MIN_OFFEST){
					setAnimationDuration((int) ((Utility.getScreenWidth()-offset)*unitDurcation));
					movePrev();
				}
			}

			
			break;
		case MotionEvent.ACTION_MOVE:
			ImageView imgView=(ImageView) _imgSwitcher.getCurrentView();
			if (this._adapterMode){
				imgView.scrollTo((int) (_downX-x), 0);
			}else{
				imgView.scrollBy((int) (_downX-x), 0);
				_downX=x;
			}
			break;
		}
		
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode){
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_VOLUME_UP:
			movePrev();
			return false;
			
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			moveNext();
			return false;
			
		case KeyEvent.KEYCODE_BACK:
			this.setResult(_needRefresh);
			_needRefresh=0;
			this.finish();
			return false;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	
	static public void startNewInstance(Context ctx,String imgFilePath){
		Intent intent=new Intent();
		intent.setClass(ctx,ImageSwitcherActivity.class);
		//intent.setAction(android.content.Intent.ACTION_VIEW);
		if (!Utility.isPathUrl(imgFilePath)){
			intent.setDataAndType(Uri.fromFile(new File(imgFilePath)), "image/*");
		}else{
			intent.setDataAndType(Uri.parse(imgFilePath), "image/*");
		}
		intent.putExtra("caller","QSee");
		intent.putExtra("isFavoriteMode", FolderModelManager.getInstance().mode()==FolderModelManager.Mode.FAVORITE_SYSTEM);
		ctx.startActivity(intent);
	}

	static public void startNewInstanceForResult(Activity ctx,String imgFilePath){
		Intent intent=new Intent();
		intent.setClass(ctx,ImageSwitcherActivity.class);
		//intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(imgFilePath)), "image/*");
		intent.putExtra("caller","QSee");
		intent.putExtra("isFavoriteMode", FolderModelManager.getInstance().mode()==FolderModelManager.Mode.FAVORITE_SYSTEM);
		ctx.startActivityForResult(intent,1);
		
	}
}
