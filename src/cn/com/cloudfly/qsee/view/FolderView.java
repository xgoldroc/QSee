package cn.com.cloudfly.qsee.view;


import java.io.File;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.widget.GridView;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.activity.SettingsActivity;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.utility.BitmapThumb;
import cn.com.cloudfly.qsee.utility.BitmapThumbsPool;
import cn.com.cloudfly.qsee.utility.Utility;

public class FolderView extends IconView{
	public final static int THUMB_SIZE=90;
	private final static int THUMB_HEIGHT=120;
	private final static int THUMB_LEFT=12;
	
	private boolean _isParentFolderView=false;
	public FolderView(Context ctx,FileItem file){
		super(ctx,file);
		
		_dirPath=file.getAbsolutePath();
		if (file.getName().endsWith("..")){
			this.setText(Utility.getResourceString(R.string.BACK_TO_PARENT));
			this.setImageResource(R.drawable.fold_back);
			_isParentFolderView=true;
		}else{
			this.setText(file.getName());
			this.setImageResource(R.drawable.fold);
			if (SettingsActivity.isShowDirThumb(this.getContext()) || SettingsActivity.isShowDirContainsImagesCount(this.getContext())){
				refresh();
			}
			_isParentFolderView=false;
		}
		//_foldDrawable=this.getDrawable();
		this.setLayoutParams(new GridView.LayoutParams(THUMB_SIZE, THUMB_HEIGHT));
		this.invalidate();
	}
	//private Drawable _foldDrawable=null;
	private static ThreadPoolExecutor _pool=initializeThreadPool();///*Executors.newSingleThreadExecutor();*/Executors.newFixedThreadPool(1);
	@Override
	protected ThreadPoolExecutor pool() {
		return _pool;
	}
	
	public static void cancelAllLoadingThreads(){
		_pool.purge();
		_pool.shutdown();
		_pool=initializeThreadPool();
	}
	
//	protected boolean shouldLoadA(){
//		return this.getDrawable()==_foldDrawable;
//	}
	
	//Paint _p=new Paint();
	
	private static BitmapThumbsPool _thumbsPool=new BitmapThumbsPool(Utility.BUFFER_SIZE,false,THUMB_SIZE/2);
	
	@Override
	protected void doClearImage(BitmapThumb thumb){
		//_thumbsPool.freeBitmapThumb(thumb);
	}
	

	private static Bitmap _foldIcon=null;
	private static Bitmap getFoldIconBitmap(){
		if (_foldIcon==null){
			_foldIcon=Utility.getBitmapFromResource(R.drawable.fold);
		}
		return Bitmap.createScaledBitmap(_foldIcon,THUMB_SIZE, THUMB_SIZE, false);
	}
	
	@Override
	protected BitmapThumb onAsyncLoadedImage(Bitmap bmp){
		return _thumbsPool.createBitmapThumb(_file.getAbsolutePath(),bmp);
	}
	
	@Override
	protected Bitmap asyncLoadingImage(FileItem dir) {
		if (! SettingsActivity.isShowDirThumb(this.getContext()) && !SettingsActivity.isShowDirContainsImagesCount(this.getContext()))
			return null;
		

		if (_isParentFolderView){
			return null;
		}
		
		File[] imgFiles=Utility.listImageFiles(dir.getAbsolutePath());
		if (imgFiles==null ||imgFiles.length==0)
			return null;
		
		
		Bitmap bitmap=getFoldIconBitmap();

		final int filesCount=imgFiles.length;
		
		int left=THUMB_SIZE/4;
		int top=16;
		String imgFilePath="defaultFoldIcon";
		if ( SettingsActivity.isShowDirThumb(this.getContext())){
				Bitmap 	thumbBitmap=null;
				for (int i=0;i<filesCount && thumbBitmap==null;++i){
					imgFilePath=imgFiles[i].getAbsolutePath();
					thumbBitmap=Utility.getImageThumbFromOrig(imgFilePath, THUMB_SIZE/2, false, null);
				}
				if (thumbBitmap==null){
					thumbBitmap=Utility.failedBitmap;
				}
				if (thumbBitmap!=null){
					Paint p=new Paint();
					Canvas canvas=new Canvas();
					canvas.setBitmap(bitmap);
					canvas.drawBitmap(thumbBitmap, left, top+THUMB_LEFT, p);
					p.setColor(Color.WHITE);
					p.setStyle(Style.STROKE);
					canvas.drawRect(new Rect(left,top+THUMB_LEFT,left+thumbBitmap.getWidth(),top+THUMB_LEFT+thumbBitmap.getHeight()), p);
				}
		}
		if (SettingsActivity.isShowDirContainsImagesCount(this.getContext())){
				String countStr=String.valueOf(filesCount);
				Paint p=new Paint();
		        p.setAntiAlias(true);
		        p.setStyle(Style.FILL);
				p.setColor(Color.WHITE);
		        float w=calcTextWidth(countStr,p);
		        Rect rect=new Rect( left,top,(int) (left+w),top+THUMB_LEFT);
				Canvas canvas=new Canvas();
				canvas.setBitmap(bitmap);
		        canvas.drawRect(rect, p);
		        p.setColor(Color.BLACK);
				canvas.drawText(countStr, rect.left,rect.top+THUMB_LEFT-1, p);
		}
		 return bitmap;
	}
	
	public String getDirPath(){
		return _dirPath;
	}
	private String _dirPath;
	
	static Rect _rect=null;
	@Override
	protected Rect getRect(){
		if (_rect==null){
			_rect=new Rect(0,14+1,THUMB_SIZE,THUMB_SIZE+14+1);
		}
		return _rect;
	}


}
