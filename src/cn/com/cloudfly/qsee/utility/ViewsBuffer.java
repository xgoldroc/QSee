package cn.com.cloudfly.qsee.utility;


import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import android.view.View;
import cn.com.cloudfly.qsee.view.AsyncImageView;

public class ViewsBuffer {
	static final int _maxSize=Utility.BUFFER_SIZE;
	
	 
	static private ArrayList<SoftReference<ViewsBuffer>> viewBuffers=new ArrayList<SoftReference<ViewsBuffer>>();
	static public void clearAllBuffers(){
		for (SoftReference<ViewsBuffer> viewBuffer:viewBuffers){
			ViewsBuffer vb=viewBuffer.get();
			if (vb!=null){
				vb.clear();
			}
		}
	}
	
	
	public ViewsBuffer(){
		_bufferMap=new LinkedHashMap<Integer,View>(_maxSize+20, 0.75f,true){
			private static final long serialVersionUID = -4287846661937960252L;
			@Override
			protected boolean removeEldestEntry (Map.Entry<Integer,View> eldest){
				if (size()>_maxSize){
					AsyncImageView view=(AsyncImageView)eldest.getValue();
					view.clearImage();//free bitmap from BitmapThumbsPool
					return true;
				}else{
					return false;
				}
			}
		};
		viewBuffers.add(new SoftReference<ViewsBuffer>(this));
	}
	private LinkedHashMap<Integer,View> _bufferMap=null;
	
	public View getView(int pos){
		View v=_bufferMap.get(pos);
		if (v!=null){
			AsyncImageView view=(AsyncImageView)v;
			if (view.isBitmapAvailable())
				return v;
			else
				_bufferMap.remove(pos);
		}
		return null;
	}
	
	public void addView(int pos,View v){
		_bufferMap.put(pos,v);
	}
	
	public void clear(){
		_bufferMap.clear();
	}
}
