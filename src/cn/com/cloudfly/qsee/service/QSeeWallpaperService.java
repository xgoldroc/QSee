package cn.com.cloudfly.qsee.service;


import java.io.File;

import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.activity.SettingsActivity;
import cn.com.cloudfly.qsee.model.AbstractFolderModel;
import cn.com.cloudfly.qsee.model.FavoriteFolderModel;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.model.FolderModelIterator;
import cn.com.cloudfly.qsee.model.FolderModelManager;
import cn.com.cloudfly.qsee.model.FolderModelManager.Mode;
import cn.com.cloudfly.qsee.utility.AppOptions;
import cn.com.cloudfly.qsee.utility.CFTimer;
import cn.com.cloudfly.qsee.utility.Clipboard;
import cn.com.cloudfly.qsee.utility.Utility;
import cn.com.cloudfly.qsee.utility.WapsUtility;

public class QSeeWallpaperService extends WallpaperService implements ICommandListener{

	private CommandReceiver _cmdReceiver=null;
	@Override
	public void onCreate() {
		super.onCreate();
		try{
			Utility.initializeApplicationContext(this);
			Clipboard.initialize(this);
			FavoriteFolderModel.getInstance(this);//.load();
			WapsUtility.initialize(this);
			
			_cmdReceiver=new CommandReceiver(this);
			IntentFilter filter=new IntentFilter();
			filter.addAction(CommandReceiver.ACTION_SEND_COMMAND);
			this.registerReceiver(_cmdReceiver, filter);
			
			registerTimer(0);
			
			QSeeWallpaperService.screenTopBarHeight=AppOptions.getTopStatusBarHeight();
		}
		catch(Exception e)
		{
			Utility.toastDebug("Error onCreate()");
			//Toast.makeText(this,"Error onCreate()",Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onDestroy(){
		this.unregisterReceiver(_cmdReceiver);
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}

	WallpaperEngine _currentEngine=null;
	
	class Wallpaper {
		private Bitmap _bitmap=null;
		private String _lastError=null;
		
		Wallpaper(Bitmap bitmap){
			_bitmap=bitmap;
			_lastError="success";
		}
		
		Wallpaper(String errInfo){
			_bitmap=null;
			_lastError=errInfo;
		}
		
		String lastError(){
			return _lastError;
		}
		
		Bitmap image(){
			return _bitmap;
		}
	}
	
	
	enum DblClickDirection{DA_PREV,DA_NEXT};
	class WallpaperEngine extends Engine {
		long _latestTouchTime=0;
		float _latestTouchX=0,_latestTouchY=0;
		
        @Override
		public void onTouchEvent(MotionEvent event) {
        	if (event.getAction()==MotionEvent.ACTION_UP){
	    		float x=event.getRawX();
	    		float y=event.getRawY();
        		if (Math.abs(x-_latestTouchX)<20 && Math.abs(y-_latestTouchY)<20){
		        	if (event.getEventTime()-_latestTouchTime<500){
		        		if (x<=Utility.doubleClickActionLineX())
		        			this.onDblClick(DblClickDirection.DA_PREV);
		        		else
		        			this.onDblClick(DblClickDirection.DA_NEXT);
		        		
		        		return;
		        	}
        		}
	        	_latestTouchTime=event.getEventTime();
	        	_latestTouchX=x;
	        	_latestTouchY=y;
        	}
        	super.onTouchEvent(event);
		}

		private final Paint _paint = new Paint();
        private float _offset=0;
        private final Paint _linePaint=new Paint();
        

        WallpaperEngine() {
            // Create a Paint to draw the lines for our cube
//            final Paint paint = _paint;
//            paint.setColor(0xffffffff);
            _paint.setAntiAlias(true);
            _paint.setStrokeWidth(1);
            _paint.setStrokeCap(Paint.Cap.ROUND);
            _paint.setStyle(Paint.Style.STROKE);
            _paint.setTextSize(20);
            _paint.setColor(Color.GRAY);
            
            _linePaint.setColor(Color.GRAY);
            this.setTouchEventsEnabled(true);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
        	if (_currentEngine==this){
        		_currentEngine=null;
        		Utility.toastDebug("destory Engine but it is current visible engine");
        	}
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
            	if (!this.isPreview()){
            		_currentEngine=this;
            	}
                drawFrame(true,DblClickDirection.DA_NEXT);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawFrame(true,DblClickDirection.DA_NEXT);
        }

//        @Override
//        public void onSurfaceCreated(SurfaceHolder holder) {
//            super.onSurfaceCreated(holder);
//        }
//
//        @Override
//        public void onSurfaceDestroyed(SurfaceHolder holder) {
//            super.onSurfaceDestroyed(holder);
//        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,float xStep, float yStep, int xPixels, int yPixels) {
        	_offset = xOffset;
            drawFrame(true,DblClickDirection.DA_NEXT);
        }

        
        public void onDblClick(DblClickDirection act){
        	if (SettingsActivity.isDblClickToNextImage(QSeeWallpaperService.this)){
	        	drawFrame(false,act);
	        	if (act==DblClickDirection.DA_NEXT){
	        		nextLiveWallpaper();
	        	}else{
	        		prevLiveWallpaper();
	        	}
	        	drawFrame(true,act);
        	}
        }
        
        void drawFrame(boolean isDraw,DblClickDirection a) {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                	if (isDraw){
                		drawImage(c);
                	}else{
                		clearImage(c,a);
                	}
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
        }
        
        void clearImage(Canvas c,DblClickDirection a){
        	c.drawColor(Color.BLACK);	
        	if (a==DblClickDirection.DA_PREV){
        		//c.drawRect(0, Utility.screenTopBarHeight, Utility.doubleClickActionLineX(), Utility.getScreenHeight(), _linePaint);
        		c.drawLine(Utility.doubleClickActionLineX(), screenTopBarHeight, Utility.doubleClickActionLineX(), Utility.getScreenHeight(), _linePaint);
        		final String text= QSeeWallpaperService.this.getResources().getString(cn.com.cloudfly.qsee.R.string.PREV);
        		final float textPixelLength=_paint.measureText(text);
        		c.drawText(text, (Utility.doubleClickActionLineX()-textPixelLength)/2, 100, _paint);
        	}else{
        		//c.drawRect(Utility.doubleClickActionLineX(), Utility.screenTopBarHeight,Utility.getScreenWidth() , Utility.getScreenHeight(), _linePaint);
        		c.drawLine(Utility.doubleClickActionLineX(), screenTopBarHeight, Utility.doubleClickActionLineX(), Utility.getScreenHeight(), _linePaint);
        		final String text= QSeeWallpaperService.this.getResources().getString(cn.com.cloudfly.qsee.R.string.NEXT);
        		final float textPixelLength=_paint.measureText(text);
        		c.drawText(text, (Utility.getScreenWidth()-Utility.doubleClickActionLineX()-textPixelLength)/2+Utility.doubleClickActionLineX(), 100, _paint);
        	}
	
        }
        void drawImage(Canvas c) {
        	c.drawColor(Color.BLACK);	
            _latestTouchTime=0;
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
        		final String text= QSeeWallpaperService.this.getResources().getString(cn.com.cloudfly.qsee.R.string.SETTING_WALLPAPER);
        		final float textPixelLength=_paint.measureText(text);
    			c.drawText(text, (Utility.getScreenWidth()-textPixelLength)/2,100, _paint);
    			return;
            }
            
        	try{
        		if (_liveWallpaper==null || _liveWallpaper.image()==null){
	        		Wallpaper wallpaper=getLiveWallpaper(false/*true*/);
	        		if ( wallpaper.image()==null){
	        			final String text= wallpaper.lastError();//QSeeWallpaperService.this.getResources().getString(cn.com.cloudfly.qsee.R.string.NOT_SET_WALLPAPER);
	            		final float textPixelLength=_paint.measureText(text);
	        			c.drawText(text, (Utility.getScreenWidth()-textPixelLength)/2,100, _paint);
	        			return;
	        		}
        		}
//        		if (wallpaper.getHeight()*wallpaper.getWidth()==1){
//        			final String text=QSeeWallpaperService.this.getResources().getString(cn.com.cloudfly.qsee.R.string.SETTING_WALLPAPER);
//            		final float textPixelLength=_paint.measureText(text);
//        			c.drawText(text, (Utility.getScreenWidth()-textPixelLength)/2,100, _paint);
//        			return;
//        		}
        		
        		final int screenWidth=Utility.getScreenWidth();
        		int wallpaperWidth=_liveWallpaper.image().getWidth();
        			
        		if (_liveWallpaper.image().getWidth()<=screenWidth){
        			///c.drawColor(0xff000000);
        			c.drawBitmap(_liveWallpaper.image(),0,screenTopBarHeight,_paint);
        			c.drawBitmap(_liveWallpaper.image(),(screenWidth-wallpaperWidth),screenTopBarHeight,_paint);
        			c.drawBitmap(_liveWallpaper.image(),(screenWidth-wallpaperWidth)/2,screenTopBarHeight,_paint);
        		}else{
        			float x=(screenWidth-wallpaperWidth)*_offset;//*wallpaper.getWidth()/this.getDesiredMinimumWidth();
        			c.drawBitmap(_liveWallpaper.image(),x,screenTopBarHeight , _paint)	;
        		}
        	}catch(Exception s){
    			//Toast.makeText(QSeeWallpaperService.this,"Error drawImage() line:"+codeLine+","+(++count),Toast.LENGTH_SHORT).show();
        		final String text= QSeeWallpaperService.this.getResources().getString(cn.com.cloudfly.qsee.R.string.FAILED_DRAW_WALLPAPER);
        		final float textPixelLength=_paint.measureText(text);
	   			c.drawText(text, (Utility.getScreenWidth()-textPixelLength)/2,100, _paint);
    			return;
	
        	}
        }
	}

	
	static private int screenTopBarHeight = 0;
	private Wallpaper _liveWallpaper = null;
	public Wallpaper getLiveWallpaper(boolean reset) {
		if (Utility.getApplicationCtx() == null) {
			return null;
		}

		if (reset){
			_liveWallpaper = null;
			///FavoriteFolderModel.getInstance().load();
		}
		if (_liveWallpaper == null/*|| AppOptions.readShouldLiveWallpaperRefresh()*/) {
			String wallpaperFilePath = AppOptions.readLiveWallpaperFile();
			if (wallpaperFilePath == null) {
				_liveWallpaper = new Wallpaper("读取壁纸配置失败,请重新设置");
				//_liveWallpaper = BitmapFactory.decodeResource(	Utility.getApplicationCtx().getResources(), R.drawable.app);
				return _liveWallpaper;
			} else {
				FileItem f=FileItem.createInstance(wallpaperFilePath, null).getLocalDownloadFileItem();
				if (!f.exists()) {
					//_liveWallpaper = null;
					Utility.toastDebug("No such image "+wallpaperFilePath);
					_liveWallpaper = new Wallpaper("壁纸文件已经不存在,请重新设置");
					//return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
					return _liveWallpaper;
				}

				wallpaperFilePath=f.getAbsolutePath();
				final int wallpaperWidth = Utility.getApplicationCtx().getWallpaperDesiredMinimumWidth();
				final int wallpaperHeight = Utility.getApplicationCtx().getWallpaperDesiredMinimumHeight()- screenTopBarHeight;
				Bitmap wallpaper = Utility.getImageThumbFromOrig(wallpaperFilePath,	Math.max(wallpaperWidth,wallpaperHeight), false,null);
				//Bitmap wallpaper = Utility.getImage(wallpaperFilePath,	wallpaperWidth, wallpaperHeight);
				if (wallpaper == null) {
					//_liveWallpaper = null;
					Utility.toastDebug("Load image "+wallpaperFilePath+" failed");
					_liveWallpaper =new Wallpaper("无法载入壁纸或图片太大");
				} else {
					//FileItem fis=FileItem.createInstance(wallpaperFilePath,null);
					int scaleAngle=Integer.parseInt(f.getProperty("scaleAngle", "0"));
					if (scaleAngle != 0) {
						wallpaper = Utility.rotateBitmap(wallpaper, scaleAngle);
					}
					double srcWidth = wallpaper.getWidth();
					double srcHeight = wallpaper.getHeight();
					int dstWidth = (int) (srcWidth * wallpaperHeight / srcHeight);
					Bitmap liveWallpaper = Bitmap.createScaledBitmap(wallpaper,	dstWidth, wallpaperHeight, true);
					if (liveWallpaper==null){
						_liveWallpaper = new Wallpaper("旋转壁纸失败");
					}else{
						_liveWallpaper=new Wallpaper(liveWallpaper);
					}
				}
			}
			AppOptions.writeLiveWallpaperRefreshDone();
		}
		return _liveWallpaper;
	}
	
	
	private boolean _imgComeFromFavorite = false;
	private AbstractFolderModel _wallpaperModel=null;
	private FolderModelIterator _wallpaperIterator=null;

	private boolean prepareLiveWallpaperList() {
		WapsUtility.pushAd();
		
		String wallpaperFilePath = AppOptions.readLiveWallpaperFile();
		
		if (wallpaperFilePath != null ) {
			String dirPath = new File(wallpaperFilePath).getParent();
			if (dirPath == null || dirPath.length() == 0)
				return false;

			boolean imgComeFromFavorite= AppOptions.isLiveWallpaperInFavoriteMode();
			if (_wallpaperModel==null || _wallpaperIterator==null||(!imgComeFromFavorite && !dirPath.equals(_wallpaperModel.getPath())) || imgComeFromFavorite!=_imgComeFromFavorite) {
				//Toast.makeText(Utility.getApplicationCtx(), String.format("%s\n%s", dirPath,_wallpaperModel==null?"NULL":_wallpaperModel.path()),	Toast.LENGTH_LONG).show();
				_imgComeFromFavorite=imgComeFromFavorite;
				_wallpaperModel=FolderModelManager.getFolderModel(dirPath,_imgComeFromFavorite?Mode.FAVORITE_SYSTEM:Mode.FILE_SYSTEM);
				_wallpaperIterator=new FolderModelIterator(_wallpaperModel);
				Utility.toastDebug( String.format("刷新文件列表,发现%d文件",_wallpaperModel.size() ));
				return _wallpaperIterator.setCurrent(FileItem.createInstance(wallpaperFilePath,_wallpaperModel));
			}

			if (_wallpaperIterator.getCurrent()==null || !wallpaperFilePath.equalsIgnoreCase(_wallpaperIterator.getCurrent().getAbsolutePath())){
				if (! _wallpaperIterator.setCurrent(FileItem.createInstance(wallpaperFilePath,_wallpaperModel))){
					_imgComeFromFavorite=imgComeFromFavorite;
					Utility.toastDebug( String.format("发现当前设置壁纸不在缓存目录内，重新刷新列表..."));
					_wallpaperModel=FolderModelManager.getFolderModel(dirPath,_imgComeFromFavorite?Mode.FAVORITE_SYSTEM:Mode.FILE_SYSTEM);
					_wallpaperIterator=new FolderModelIterator(_wallpaperModel);
					Utility.toastDebug( String.format("刷新文件列表,发现%d文件",_wallpaperModel.size() ));
					return _wallpaperIterator.setCurrent(FileItem.createInstance(wallpaperFilePath,_wallpaperModel));
				}
			}
			return true;
		}else{
			Utility.toastDebug("No wallpaperFile in AppOptions");
		}
		return false;
	}

	public void nextLiveWallpaper() {
		if (prepareLiveWallpaperList()) {
			_wallpaperIterator.goNext();
			FileItem f=_wallpaperIterator.getCurrent();
			if (f!=null){
				if (!f.exists()){
					if (!new File(f.getParentPath()).exists()){
						Toast.makeText(this, R.string.FOUND_WALLPAPER_DIR_DELETED, Toast.LENGTH_LONG).show();
						return;
					}
					
					Toast.makeText(this,R.string.FOUND_WALLPAPER_DELETED,Toast.LENGTH_SHORT).show();
					_wallpaperModel=null;
					nextLiveWallpaper(); //什么情况下会出现StackOverflow？删除整个文件夹
				}else{
					AppOptions.writeLiveWallpaper(f.getAbsolutePath(), 0,_imgComeFromFavorite);
					_liveWallpaper=null;
				}
			}
		}
	}

	public void prevLiveWallpaper() {
		if (prepareLiveWallpaperList()) {
			_wallpaperIterator.goPrev();
			FileItem f=_wallpaperIterator.getCurrent();
			if (f!=null){
				if (!f.exists()){
					if (!new File(f.getParentPath()).exists()){
						Toast.makeText(this, R.string.FOUND_WALLPAPER_DIR_DELETED, Toast.LENGTH_LONG).show();
						return;
					}
					Toast.makeText(this,R.string.FOUND_WALLPAPER_DELETED,Toast.LENGTH_SHORT).show();
					_wallpaperModel=null;
					prevLiveWallpaper();
				}else{
					AppOptions.writeLiveWallpaper(f.getAbsolutePath(), 0,_imgComeFromFavorite);
					_liveWallpaper=null;
				}
			}
		}
	}
	
	public void onCommand(int cmdId, String params) {
		Log.d("HH","onCommand("+cmdId+","+params+")");
		//Toast.makeText(Utility.getApplicationCtx(), String.format("onCommand:%d\n%s", cmdId,params),	Toast.LENGTH_LONG).show();
		int interval=AppOptions.readAutoSwitchTimerInterval();
		if (cmdId==1){
			//_wallpaperModel=null;
			if (!this.prepareLiveWallpaperList()){
				Utility.toastDebug("Failed to prepareLiveWallpaperList");
			}
			this.getLiveWallpaper(true);
			Toast.makeText(this, R.string.SETTING_WALLPAPER_DONE, Toast.LENGTH_SHORT).show();	
		}else if (cmdId==2){//set timer interval
			interval=Integer.parseInt(params);
			
		}
		registerTimer(interval);
		QSeeWallpaperService.screenTopBarHeight=AppOptions.getTopStatusBarHeight();
	}
	
	
	private	CFTimer _timer=null;
	
    private void registerTimer(int interval){
        if (_timer!=null){
    		_timer.cancel();
    		_timer=null;
        }

        if (interval==0){
			Utility.toastDebug("Stop Dynamic Wallpaper changed Timer");
        	return;
        }
        
        _timer=new CFTimer(interval){
			@Override
			public void timeout() {
				Utility.toastDebug(_timer.getInterval()+" done,Auto switch the next wallpaper");
				QSeeWallpaperService.this.nextLiveWallpaper();
				if (_currentEngine!=null){
					_currentEngine. drawFrame(true,DblClickDirection.DA_NEXT);
				}else{
					Utility.toastDebug("No current Engine to redraw next wallpaper");
				}
				if (_timer!=null && _timer.getInterval()!=AppOptions.readAutoSwitchTimerInterval()){
					Utility.toastDebug("Auto switch Interval changed to "+SettingsActivity.getTimerSwitchInterval(Utility.getApplicationCtx()));
					_timer.cancel();
					_timer=null;
					registerTimer(AppOptions.readAutoSwitchTimerInterval());
				}
			}
		};
		Utility.toastDebug("start timer "+_timer.getInterval()/1000+" s");
		Log.d("HH","start timer "+_timer.getInterval());
        _timer.start();
    }

}
