package cn.com.cloudfly.qsee.utility;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public abstract class SerialImageLoadTask extends AsyncTask<String, Integer, Bitmap> {
	private int _thumbSz;
	public SerialImageLoadTask(int thumbSz){
		_thumbSz=thumbSz;
	}
	
	abstract public void onBitmapLoaded(Bitmap bmp);
	
	@Override
	protected Bitmap doInBackground(String... path) {
		Bitmap thumb=null;
		if (Utility.isPathUrl(path[0])){
			thumb=Utility.getImageThumb(path[0], _thumbSz, false, null);
		}else{
			thumb=Utility.getImageThumbFromOrig(path[0], _thumbSz, false, null);
		}
		//int sz=Math.min(Math.min(thumb.getWidth(), thumb.getHeight()),_thumbSz);
		return Utility.extractThumbnail(thumb, _thumbSz, _thumbSz);
		//Bitmap thumb=BitmapFactory.decodeFile(path[0]);
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (result!=null){
			onBitmapLoaded(result);
		}
		super.onPostExecute(result);
	}
	
}
