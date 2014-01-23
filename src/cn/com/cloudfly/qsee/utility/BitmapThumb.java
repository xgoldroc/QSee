package cn.com.cloudfly.qsee.utility;

import android.graphics.Bitmap;

//BitmapThumbPool accessor
public class BitmapThumb {
	private final String  _bitmapPath;
	private final BitmapThumbsPool _pool;
	
	public BitmapThumb(String path,BitmapThumbsPool pool){
		_bitmapPath=path;
		_pool=pool;
	}
	
	public Bitmap get(){
		return _pool.getBitmap(this);
	}
	
	public boolean isAvailable(){
		return _pool.hasBitmapThumb(this);
	}
	
	@Override
	public BitmapThumb clone(){
		return new BitmapThumb(_bitmapPath,_pool);
	}
	
	String path() {
		return _bitmapPath;
	}
}