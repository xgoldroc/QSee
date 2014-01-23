package cn.com.cloudfly.qsee.view;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.utility.BitmapThumb;
import cn.com.cloudfly.qsee.utility.Utility;

public abstract class AsyncImageView extends ImageView implements Runnable{
	public AsyncImageView(Context context,FileItem file) {
		super(context);
		_file=file;
		_bitmap=null;
	}

	protected abstract ThreadPoolExecutor pool();
	protected static ThreadPoolExecutor initializeThreadPool(){
		//newFixedThreadPool
		//return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());  
		return new ThreadPoolExecutor(1,3,5, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(15),new ThreadPoolExecutor.DiscardOldestPolicy()); 
	}
	
//	public void reset(FileItem file){
//		_file=file;
//		if (_bitmap!=null){
//			_bitmap=null;
//		}
//		refresh();
//	}
		
	@Override
	protected void onWindowVisibilityChanged( int visibility){
		if (visibility==VISIBLE){
			Log.d("VISIBLE",_file.getName());
		}else if (visibility==GONE){
			Log.d("GONE",_file.getName());
		}else{
			Log.d("INVISIBLE",_file.getName());
		}
	}


	private String getPoolString(){
		ThreadPoolExecutor e=pool();
		return String.format("Finished %d/%d,Threads %d/%d,queue length=%d",
				e.getCompletedTaskCount(),
				e.getTaskCount(),
				e.getPoolSize(),
				e.getMaximumPoolSize(),
				e.getQueue().size()
				);
	}
	
	public void refresh(){
		if (!isBitmapAvailable()){
			try{
				if (pool().isShutdown()){
					Utility.toastDebug("isShutdown");
				}
				pool().execute(this);
				Log.d("HH", getPoolString());
			}catch(RejectedExecutionException e){
				Utility.toastDebug("RejectedExecutionException");
			}
		}else{
			Log.d("HH", "Do not need execute asyncLoadImage");
		}
	} 
	
	synchronized public boolean isBitmapAvailable(){
		return _bitmap !=null && _bitmap.isAvailable();
	}
	
	synchronized public void clearImage(){
		if (_bitmap!=null){
			doClearImage(_bitmap);
			_bitmap=null;
		}
	}
	protected abstract void doClearImage(BitmapThumb thumb);
	
	private BitmapThumb _bitmap=null;
	synchronized protected BitmapThumb getBitmapThumb(){
		return _bitmap;
	}
	synchronized protected void setBitmapThumb(BitmapThumb bt){
		_bitmap=bt;
	}
	
	
	private Handler _handler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			setBitmapThumb(onAsyncLoadedImage((Bitmap) msg.obj));
			AsyncImageView.this.postInvalidate();
		}
	};
	
	
	
	//subclass should implement it 
	protected abstract Bitmap asyncLoadingImage(FileItem file);
	protected abstract BitmapThumb onAsyncLoadedImage(Bitmap bmp);
	
	public void run(){
		if (!isBitmapAvailable()){
			Bitmap bmp=asyncLoadingImage(_file);
			if (bmp!=null){
				Message msg=new Message();
				msg.obj=bmp;
				_handler.sendMessage(msg);
			}
		}
	}
	
	protected final FileItem _file;
	
	static Paint _paint=new Paint();
	
	protected abstract Rect getRect();
	@Override 
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (_bitmap!=null && _bitmap.get() !=null ){
			Bitmap bmp=_bitmap.get();
			canvas.drawBitmap(bmp, null,getRect(), _paint);
		}else{
			refresh();
		}
	}
}
