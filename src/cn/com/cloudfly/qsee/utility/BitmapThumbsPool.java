package cn.com.cloudfly.qsee.utility;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.util.Log;

public class BitmapThumbsPool {
	private final int _poolSize;
	private Map<String,Bitmap> _pool=null;
	private final boolean _hasFrame;
	private final int _maxWidthHeight;
	
	public BitmapThumbsPool(int poolSize,boolean hasFrame,int maxWidthHeight){
		_poolSize=poolSize;
		_hasFrame=hasFrame;
		_maxWidthHeight=maxWidthHeight;
		_pool=Collections.synchronizedMap(new LinkedHashMap<String,Bitmap>(_poolSize+5, 0.75f,true){
			private static final long serialVersionUID = 1L;
			@Override
			protected boolean removeEldestEntry (Map.Entry<String,Bitmap> eldest){
				if (size()>_poolSize){
					return true;
				}else{
					return false;
				}
			}
		});
		
	}
	
	
	
	public Bitmap addThumb(String imgFilePath,Bitmap bmp){
		if (bmp==null){
			bmp=Utility.getImageThumb(imgFilePath,_maxWidthHeight,_hasFrame,Utility.failedBitmap);
		}
		
		_pool.put(imgFilePath, bmp);
		if (!_pool.containsKey(imgFilePath)){
			Log.d("ASSERT", "No exists");	
		}
		return bmp;
	}
	
	public BitmapThumb createBitmapThumb(String imgFilePath){
		Bitmap bmp=_pool.get(imgFilePath);
		if (bmp==null){
			bmp=addThumb(imgFilePath,null);
		}
		return new BitmapThumb(imgFilePath,this);
	}
	
	public BitmapThumb createBitmapThumb(String imgFilePath,Bitmap bmpRaw){
		addThumb(imgFilePath,bmpRaw);
		return new BitmapThumb(imgFilePath,this);
	}
	
	public boolean hasBitmapThumb(BitmapThumb thumb){
		return _pool.containsKey(thumb.path());
	}
		
	
 	public void freeBitmapThumb(BitmapThumb thumb){
		_pool.remove(thumb.path());
	}


	Bitmap getBitmap(BitmapThumb thumb){
		if (thumb.path().length()==0){
			
			return Utility.failedBitmap;
		}
		Bitmap bmp= _pool.get(thumb.path());
		if (bmp==null){
			return Utility.failedBitmap;
		}else{
			return bmp;
		}
	}
	
}
