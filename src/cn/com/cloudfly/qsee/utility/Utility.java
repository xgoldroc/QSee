package cn.com.cloudfly.qsee.utility;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore.MediaColumns;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.activity.SettingsActivity;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.model.FolderModelManager;
import cn.com.cloudfly.qsee.service.QSeeWallpaperService;
import cn.com.cloudfly.qsee.view.ImageFileView;

public class Utility {
	public static String getAppVersion(){
		return Utility.getResourceString(R.string.APP_VER);
	}
	
	public final static long copyFile(File f1, File f2) throws Exception {
		long time = new Date().getTime();
		int length = 2097152;
		FileInputStream in = new FileInputStream(f1);
		FileOutputStream out = new FileOutputStream(f2);
		FileChannel inC = in.getChannel();
		FileChannel outC = out.getChannel();
		while (true) {
			if (inC.position() == inC.size()) {
				inC.close();
				outC.close();
				in.close();
				out.close();
				return new Date().getTime() - time;
			}
			if ((inC.size() - inC.position()) < 20971520)
				length = (int) (inC.size() - inC.position());
			else
				length = 20971520;
			inC.transferTo(inC.position(), length, outC);
			inC.position(inC.position() + length);
		}
	}

	public final static boolean isImageFile(String fileName) {
		int idx=fileName.lastIndexOf('.');
		if (idx==-1)
			return false;
		
		String extName=fileName.substring(idx+1);
		
		return 	extName.equalsIgnoreCase("jpg") ||
					extName.equalsIgnoreCase("jpeg") ||
					extName.equalsIgnoreCase("jpe") ||
					extName.equalsIgnoreCase("png") ||
					extName.equalsIgnoreCase("bmp") ;
		 //final String regExp=("^.*?\\.(jpg|jpeg|png|bmp|jpe)");
		 //return fileName.matches(regExp);
	}

	public final static File[] listImageFiles(String dirPath) {
		return listImageFiles(new File(dirPath));
	}

	private static abstract class SortComparator implements Comparator<File>{
		  @Override
		public boolean equals(Object obj){  
		  		return true;  
		  }  
	}
	
	private static class CompratorByTime extends  SortComparator  {
		  public int compare(File f1, File f2) {  
			  long diff = f1.lastModified()-f2.lastModified();  
		      if(diff>0)  
		         return 1;  
		      else if(diff==0)  
		         return 0;  
		      else 
		         return -1;  
		  }  
	} 

	private static class CompratorByReverseTime extends  SortComparator {  
		  public int compare(File f1, File f2) {  
			  long diff = f2.lastModified()-f1.lastModified();  
		      if(diff>0)  
		         return 1;  
		      else if(diff==0)  
		         return 0;  
		      else 
		         return -1;  
		  }  
	} 
	
	private static class CompratorByName extends  SortComparator {  
		  public int compare(File f1, File f2) {  
			  return f1.getName().compareTo(f2.getName());
		  }
	} 

	private static class CompratorByReverseName extends  SortComparator {  
		  public int compare(File f1, File f2) {  
			  return f2.getName().compareTo(f1.getName());
		  }
	} 
	
	private static class CompratorBySize extends  SortComparator {  
		  public int compare(File f1, File f2) {  
			  long diff = f1.length()-f2.length();  
		      if(diff>0)  
		         return 1;  
		      else if(diff==0)  
		         return 0;  
		      else 
		         return -1;  
		  }
	} 
	
	private static class CompratorByReverseSize extends  SortComparator {  
		  public int compare(File f1, File f2) {  
			  long diff = f2.length()-f1.length();  
		      if(diff>0)  
		         return 1;  
		      else if(diff==0)  
		         return 0;  
		      else 
		         return -1;  
		  }
	} 

	 public enum SortPolicy {
		 Name,
		 ReverseName,
		 Time,
		 ReverseTime,
		 Size,
		 ReverseSize
	 };

	 private static SortComparator[] comparators=initializeSortComparator();
	 static SortComparator[] initializeSortComparator(){
		SortComparator[] sc=new SortComparator[6];
		sc[SortPolicy.Name.ordinal()]=new CompratorByName();
		sc[SortPolicy.Time.ordinal()]=new CompratorByTime();
		sc[SortPolicy.Size.ordinal()]=new CompratorBySize();
		sc[SortPolicy.ReverseName.ordinal()]=new CompratorByReverseName();
		sc[SortPolicy.ReverseTime.ordinal()]=new CompratorByReverseTime();
		sc[SortPolicy.ReverseSize.ordinal()]=new CompratorByReverseSize();
		return sc;		 
	 }
	 
	 static SortComparator getComparator(SortPolicy p){
	 	assert(comparators!=null);
		 return comparators[p.ordinal()];
	}
	 
	public final static File[] listImageFiles(File dir) {
		File[] fs= dir.listFiles(new FileFilter() {
			public boolean accept(File path) {
				if (path.isDirectory() || path.isHidden())
					return false;

				return isImageFile(path.getName());
			}
		});
		if (fs!=null){
			SortPolicy s=AppOptions.readCurrentSortPolicy();
			Arrays.sort(fs, getComparator(s));
		}
		return fs;
	}

	public final static File[] listSubFolders(String dirPath){
		File[] dirs=new File(dirPath).listFiles(new FileFilter(){
			public boolean accept(File file) {
				return file.isDirectory() && !file.getName().startsWith(".");
			}
		});
		if (dirs!=null){
			SortPolicy s=AppOptions.readCurrentSortPolicy();
			Arrays.sort(dirs, getComparator(s));
		}
		return dirs;
	}
	
	public final static String[] getImageFilePaths(String dir,int count){
		File[] fs=listImageFiles(dir);
		int sz=Math.min(fs.length,count);
		String[] ps=new String[sz];
		for (int i=0;i<sz;++i){
			ps[i]=fs[i].getAbsolutePath();
		}
		return ps;
	}
	
	public final static File getFirstImageFile(File dir) {
		File[] files = dir.listFiles(new FileFilter() {
			private boolean _hasFirst = false;

			public boolean accept(File pathname) {
				if (pathname.isDirectory() || pathname.isHidden())
					return false;

				if (_hasFirst) {
					return false;
				} else {
					_hasFirst = isImageFile(pathname.getName());
					return true;
				}
			}
		});

		if (files == null || files.length == 0) {
			return null;
		} else {
			return files[0];
		}
	}

//	public final static Rect getScreenWidths(String imgFile) {
//		BitmapFactory.Options opt = new BitmapFactory.Options();
//		opt.inJustDecodeBounds = true;
//		BitmapFactory.decodeFile(imgFile, opt);
//		return new Rect(0, 0, opt.outWidth, opt.outHeight);
//	}

	public static Bitmap failedBitmap = null;

	public static Bitmap getBitmapFromResource(int resId) {
		return BitmapFactory.decodeResource(_appCtx.getResources(), resId);
	}

	public final static int getScreenWidth() {
		Display screen = ((WindowManager) _appCtx
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		return screen.getWidth();
	}

	public final static int getScreenHeight() {
		Display screen = ((WindowManager) _appCtx
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		return screen.getHeight();
	}

	static PowerManager.WakeLock _wakeLock = null;

	public final static void lockScreenPower(Context ctx) {
		if (_wakeLock == null) {
			PowerManager pm = (PowerManager) ctx
					.getSystemService(Context.POWER_SERVICE);
			_wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
					"ImageSwitcherActivity");
		}
		_wakeLock.acquire();
	}

	public final static void unlockScreenPower(Context ctx) {
		if (_wakeLock != null) {
			_wakeLock.release();
		}
	}

	private final static int CLEAR_VIEWS_BUFFER = 11;

	private static long last_low_memory_ticks = System.currentTimeMillis();

	private static boolean _isMyPhone=false;

	public final static boolean isApplicationContextInitialized() {
		return _appCtx != null;
	}

	//private static Toast _lowMemoryToast = null;

	public final static void initializeApplicationContext(Context ctx) {
		if (_appCtx == null) {
			_appCtx = ctx.getApplicationContext();
			failedBitmap = BitmapFactory.decodeResource(_appCtx.getResources(),R.drawable.failed);
			_uiThreadHandler = new Handler() {
				@Override
				public void handleMessage(android.os.Message msg) {
					switch (msg.what) {
					case CLEAR_VIEWS_BUFFER: {
						if (System.currentTimeMillis() - last_low_memory_ticks > 5000) {
							Utility.toastDebug( String.format("Catch Outof Mem ,refresh"));
							ViewsBuffer.clearAllBuffers();
							FolderModelManager.getInstance().getCurrentModel().invalidate();
							last_low_memory_ticks = System.currentTimeMillis();
						}
						break;
					}
					default:
						break;
					}
				}
			};

			TelephonyManager phoneMgr=(TelephonyManager)_appCtx.getSystemService(Context.TELEPHONY_SERVICE);
			if (phoneMgr!=null){
				_phoneId=phoneMgr.getDeviceId();
				if (_phoneId!=null){
					_isMyPhone=(_phoneId.equals("354957030999240"));
				}
			}else{
				_isMyPhone=false;
			}
			_isMyPhone= (_isMyPhone || Build.MODEL.equals("sdk"));
		}
	}

	public static String getPhondId(){
		return _phoneId;
	}
	private static String _phoneId=null;
	private static Handler _uiThreadHandler = null;

	private static int cellSize = 0;
	public static int getCellSize(){
		if (cellSize==0)
			cellSize=Utility.getScreenWidth()/ getImageCellCount()- ImageFileView.verticalSpacing;
		return cellSize;
	}
	private static int _imageCellCount=0;
	public static int getImageCellCount() {
		if (_imageCellCount==0){
			Display screen = ((WindowManager) _appCtx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			int displayRotation = screen.getOrientation();
			if (displayRotation == Surface.ROTATION_0	|| displayRotation == Surface.ROTATION_180) {
				_imageCellCount = 3;
			} else {
				_imageCellCount = 5;
			}
		}
		return _imageCellCount;
			
	}
	
	
//	public static void initializeUIContext(Context ctx) {
//		Display screen = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//		cellSize = screen.getWidth() / SettingsActivity.imageCellCount- ImageFileView.verticalSpacing;
//	}

	static final public int doubleClickActionLineX() {
		int wp = SettingsActivity.dblClickPrevAreaWidthPercent(_appCtx);
		return Utility.getScreenWidth() / wp;
	}

	static private Context _appCtx = null;

	static public Bitmap rotateBitmap(Bitmap bmp, int scaleAngle) {
		Matrix matrix = new Matrix();
		matrix.setRotate(scaleAngle);
		return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),matrix, true);
	}

	static public String getDefaultDirPath() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)|| Environment.getExternalStorageState().equals(	Environment.MEDIA_MOUNTED_READ_ONLY)) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		} else {
			return "/";
		}
	}

	
	/**
	 * 姝ゅ嚱鏁版ā鎷熷疄鐜�android 2.2 涓殑ThumbnailUtils.extractThumbnail(bmp,width,height);
	 * 浠ヤ究鍙互鍦�.1骞冲彴涓婅繍琛�
	 * @param bmp
	 * @param width
	 * @param height
	 * @return
	 */
	static public Bitmap extractThumbnail(Bitmap bmp,int width,int height){
		if (bmp==null)
			return bmp;
		int bmpWidth=bmp.getWidth();
		int bmpHeight=bmp.getHeight();
		
		int x=0;
		int y=0;
		//鎵撳紑涓嬭堪浠ｇ爜琛ㄧず鍙栧嚭鍥剧墖涓棿閮ㄥ垎锛屽睆钄借〃绀哄彇鍑哄浘鐗囧乏涓婇儴鍒�
//		if (bmpWidth>bmpHeight){
//			y=0;
//			x=(bmpWidth-bmpHeight)/2;
//		}else{
//			x=0;
//			y=(bmpHeight-bmpWidth)/2;
//		}
		int sz=Math.min(bmpWidth, bmpHeight);
		Bitmap bp=Bitmap.createBitmap(bmp, x, y, sz,sz);//鍙栧嚭鍥剧墖涓棿閮ㄥ垎, 鍙兘瀵艰嚧Out Of Memory
		return Bitmap.createScaledBitmap(bp, width, height,true);//鎶婂緱鍒扮殑鐭╁舰鍖哄煙鐨勯儴鍒嗚繘琛宻cale
	}

//	static public int getBitmapByteSize(Bitmap bitmap) {
//		return bitmap.getRowBytes() * bitmap.getHeight();
//	}

	static public final Rect getBitmapRect(String imgFile) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imgFile, opt);
		return new Rect(0, 0, opt.outWidth, opt.outHeight);
	}

	static public final boolean isBitmapHorizontal(String imgFile){
		Rect rect=getBitmapRect(imgFile);
		return rect.width()>rect.height();
	}
	
	static public final BitmapFactory.Options getPerfectImageLoadOption(String imgFile, int maxWidthHeight) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		int width, height;
		opt.inJustDecodeBounds = true;
		Utility.decordBitmapFileOrUrl(imgFile, opt);
		int _bitmapWidth = opt.outWidth;
		int _bitmapHeight = opt.outHeight;
		if (_bitmapWidth > _bitmapHeight) {
			opt.inSampleSize = (_bitmapWidth / maxWidthHeight) + 1;
			// width = maxWidthHeight;
		} else {
			opt.inSampleSize = (_bitmapHeight / maxWidthHeight) + 1;
			// height = maxWidthHeight;
		}
		width = _bitmapWidth / opt.inSampleSize;
		height = _bitmapHeight / opt.inSampleSize;
		if (width==0){
			width++;
		}
		if (height==0){
			height++;
		}
		opt.outWidth = width;
		opt.outHeight = height;
		assert (width < maxWidthHeight);
		assert (height < maxWidthHeight);
		opt.inJustDecodeBounds = false;
		return opt;
	}

	static public final ImageView createImageView(Context ctx,String imgFilePath, int width,int height) {
		int maxWidthHeight = Math.max(width, height);
		BitmapFactory.Options opt = getPerfectImageLoadOption(imgFilePath,maxWidthHeight);
		opt.inPurgeable = true;
		ImageView iv=new ImageView(ctx);
		try {
			iv.setImageBitmap(BitmapFactory.decodeFile(imgFilePath, opt));
		} catch (OutOfMemoryError e) {
			Utility._uiThreadHandler.sendEmptyMessage(CLEAR_VIEWS_BUFFER);
		}
		return iv;
	}

//	static public final Bitmap getImage(String imgFilePath,int width,int height){
//		int maxWidthHeight = Math.max(width, height);
//		BitmapFactory.Options opt = getPerfectImageLoadOption(imgFilePath,maxWidthHeight);
//		opt.inPurgeable = true;
//		return BitmapFactory.decodeFile(imgFilePath, opt);
//	}
	public static boolean isPathUrl(String path){
		return path.startsWith("http://");
	}
	
	
	private static Bitmap decordBitmapFileOrUrl(String imgFilePath,BitmapFactory.Options opt){
		if (Utility.isPathUrl(imgFilePath)){
			FileItem f=FileItem.createInstance(imgFilePath, null).getLocalDownloadFileItem();
			if (!f.exists()){
				try{
					URL url=new URL(imgFilePath);
					InputStream is = url.openConnection().getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);
					return BitmapFactory.decodeStream(bis,null,opt);
				}catch (MalformedURLException e) {
		            e.printStackTrace();
				}catch (IOException e) {
		            e.printStackTrace();
				}
				return null;
			}else{
				imgFilePath=f.getAbsolutePath();
			}
		}
		return BitmapFactory.decodeFile(imgFilePath, opt);
	}
	

	private static Bitmap readImageFrom(String imgFilePath,int maxWidthHeight, Bitmap failedBitmap){
		BitmapFactory.Options opt = getPerfectImageLoadOption(imgFilePath,maxWidthHeight);
		int width = opt.outWidth;
		int height = opt.outHeight;
		opt.inPurgeable = true;
		Bitmap bitmap=decordBitmapFileOrUrl(imgFilePath,opt);
		if (bitmap != null) {
			if (width>0 && height>0 && bitmap.getWidth()>0 && bitmap.getHeight()>0){
				bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
			}else{
				bitmap=null;
			}
		}
		if (bitmap == null) {
			bitmap = failedBitmap;
		}
		return bitmap;
	}
	

	public final static Bitmap getImageThumbFromOrig(String imgFilePath,	int maxWidthHeight, boolean hasFrame, Bitmap failedBitmap) {
		Bitmap bitmap=readImageFrom(imgFilePath,maxWidthHeight,failedBitmap);
		if (!hasFrame) {
			return bitmap;
		}
		
		Bitmap _bitmap = Bitmap.createBitmap(maxWidthHeight,maxWidthHeight, Bitmap.Config.ARGB_8888);
		//Log.d("INFO", "framed Scaled bitmap size " + getBitmapSize(_bitmap)	+ " bytes");
		if (_bitmap == null)
			return failedBitmap;
		Canvas canvas = new Canvas(_bitmap);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		canvas.drawBitmap(bitmap, (maxWidthHeight - bitmap.getWidth()) / 2,
				(maxWidthHeight - bitmap.getHeight()) / 2, paint);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(0, 0, maxWidthHeight - 1, maxWidthHeight - 1, paint);
		// bitmap.recycle();
		return _bitmap;
	}

	public final static Bitmap getImageThumb(String imgFilePath, int maxWidthHeight,boolean hasFrame, Bitmap failedBitmap) {
		FileItem thumbsFileItem=FileItem.createInstance(imgFilePath, null).getThumbsFileItem();
		if (thumbsFileItem==null)
			return null;
		
		String thumbImgFilePath = thumbsFileItem.getAbsolutePath();//ThumbsGenerator.getThumbsFilePathOf(imgFilePath);
		try {
			Bitmap bitmap = BitmapFactory.decodeFile(thumbImgFilePath);
			if (bitmap == null) {
				Log.d("INFO", "Failed to load thumbs,load orig file " + imgFilePath);
				return getImageThumbFromOrig(imgFilePath, maxWidthHeight,hasFrame, failedBitmap);
			} else {
				Log.d("INFO", "Load thumbs from thumbs file "+ thumbImgFilePath + ",size is " + bitmap.getRowBytes());
				return bitmap;
			}
		} catch (OutOfMemoryError e) {
			Log.d("INFO", String.format("getImageThumb failed from %s",imgFilePath ));
			Utility._uiThreadHandler.sendEmptyMessage(CLEAR_VIEWS_BUFFER);
			return null;
		}catch(Error e){
			Log.d("INFO", String.format("getImageThumb ERROR from %s",imgFilePath ));
			return null;
		}
	}

	
	/**
	 * 鐢ㄤ簬澶栭儴绋嬪簭鎵撳紑缇庡浘椋炲害鏃讹紝浠嶮ediaContent 鐨剈ri涓幏寰楁枃浠惰矾寰剈ri
	 * @param MediaContent 鐨剈ri
	 * @return 鏂囦欢璺緞uri
	 */
	public static Uri getFileUriFromContentUri(Uri cu) {
		String[] proj = { MediaColumns.DATA };
		ContentResolver cp = _appCtx.getContentResolver();
		Cursor cursor = cp.query(cu, proj, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			return Uri.parse(cursor.getString(column_index));
		} else {
			return null;
		}

	}

	public final static int BUFFER_SIZE = 50;

	public static String getFavoriteFolderName(){
		return Utility.getResourceString(R.string.FAVORITE_FOLDER_NAME);
	}
	
	public static String getResourceString(int i) {
		return _appCtx.getString(i);
	}

	public static Context getApplicationCtx() {
		return _appCtx;
	}

	public static boolean isMyPhone(){
		return _isMyPhone;
	}
	
	public static void toastDebug(String i){
		String txt=String.format("%d\n%s%s", System.currentTimeMillis(),getFileLineMethod(), i);
		Log.d("toastDebug",txt);
		if (_isMyPhone){
			//Toast.makeText(Utility.getApplicationCtx(),txt,Toast.LENGTH_LONG).show();
		}
	}
	
	private static String getFileLineMethod() { 
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
		return String.format("[%s:%d]\n[%s]\n",traceElement.getFileName(),traceElement.getLineNumber(),traceElement.getMethodName());
	} 

	@SuppressWarnings("unused")
	private void toast(Context ctx,String info){
		Toast toast = Toast.makeText(ctx, info, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);//	璁剧疆Toast淇℃伅鎻愮ず妗嗘樉绀虹殑浣嶇疆锛堝湪灞忓箷灞呬腑鏄剧ず锛�
		try{
			//  浠嶵oast瀵硅薄涓幏寰梞TN鍙橀噺
		    Field field = toast.getClass().getDeclaredField("mTN");
		    field.setAccessible(true);
            Object obj = field.get(toast);
		    //  TN瀵硅薄涓幏寰椾簡show鏂规硶
            Method method =  obj.getClass().getDeclaredMethod("show", null);
		    //  璋冪敤show鏂规硶鏉ユ樉绀篢oast淇℃伅鎻愮ず妗�
            method.invoke(obj, null);
		}catch(Exception e){
			
		}
	}
	
	public static String getDeviceDetailInfo(){
		String info=String.format(	"BOARD=%s\nBRAND=%s\nCPU_ABI=%s\nDEVICE=%s\nDISPLAY=%s\nFINGERPRINT=%s\nHOST=%s\nID=%s\nMANUFACTURER=%s\nMODEL=%s\nPRODUCT=%s\nTAGS=%s\nTIME=%s\nTYPE=%s\nUSER=%s\nVERSION.CODENAME=%s\nVERSION.INCREMENTAL=%s\nVERSION.RELEASE=%s\nVERSION.SDK=%s",
				android.os.Build.BOARD,
				android.os.Build.BRAND,
				android.os.Build.CPU_ABI,
				android.os.Build.DEVICE,
				android.os.Build.DISPLAY,
				android.os.Build.FINGERPRINT,
				android.os.Build.HOST,
				android.os.Build.ID,
				android.os.Build.MANUFACTURER,
				android.os.Build.MODEL,
				android.os.Build.PRODUCT,
				android.os.Build.TAGS,
				android.os.Build.TIME,
				android.os.Build.TYPE,
				android.os.Build.USER,
				android.os.Build.VERSION.CODENAME,
				android.os.Build.VERSION.INCREMENTAL,
				android.os.Build.VERSION.RELEASE,
				android.os.Build.VERSION.SDK
				);
		return info;	
	}
	
	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','A', 'B', 'C', 'D', 'E', 'F' };
	
	private static String toHexString(byte[] b) {

		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}
		return sb.toString();
	}
	
	public static String getMD5Sum(String filename){
		InputStream fis;
		byte[] buffer = new byte[1024];
		int numRead = 0;
		MessageDigest md5;
		try{
			fis = new FileInputStream(filename);
			md5 = MessageDigest.getInstance("MD5");
			while((numRead=fis.read(buffer)) > 0) {
				md5.update(buffer,0,numRead);
			}
			fis.close();
			return toHexString(md5.digest());
		} catch (Exception e) {
			Utility.toastDebug("MD5 sum error");
			//System.out.println("error");
			return "";
		}
	}

	public static  boolean isWallpaperQSee(){
		WallpaperInfo wallpaperInfo=WallpaperManager.getInstance(Utility.getApplicationCtx()).getWallpaperInfo();
		return wallpaperInfo!=null && QSeeWallpaperService.class.getName().equals(wallpaperInfo.getServiceName());
	}

	static public String myStoreDirPath(){
		return Environment.getExternalStorageDirectory().getAbsolutePath()+"/QSee/Exhibitions";
	}
	static public boolean makeMyStore(String subDir){
		if (subDir==null){
			File d=new File(myStoreDirPath());
			if (!d.exists()){
				return d.mkdirs();
			}else{
				return true;
			}
		}else{
			makeMyStore(null);
			File d=new File(myStoreDirPath()+"/"+subDir);
			if (!d.exists()){
				return d.mkdirs();
			}else{
				return true;
			}
		}
	}
	
	static public boolean isNetworkAvailable(){
		boolean flag = false;
		ConnectivityManager manager = (ConnectivityManager)_appCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(manager.getActiveNetworkInfo() != null)
		{
			flag = manager.getActiveNetworkInfo().isAvailable();
		}
		return flag;
	}
	
	static private void startSendEmailActivity(Activity a,String text,String email){
		try{
	        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
	        sendIntent.setData(Uri.parse("mailto:"+email));
	        sendIntent.putExtra(Intent.EXTRA_SUBJECT, String.format("QSee %s Crash Report",Utility.getAppVersion())); 
	        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
	        a.startActivity(sendIntent); 
		}catch(ActivityNotFoundException e){
			Toast.makeText(a, R.string.ERROR_SEND_EMAIL, Toast.LENGTH_LONG).show();
		}
	}

	static public void startSendReportEmailActivity(Activity a,String text){
		startSendEmailActivity(a,text,"cloudfly@live.cn");
	}

	static public void startWebBrowserActivity(Activity a,String url){
		try{
	        Intent sendIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(url)); 
	        a.startActivity(sendIntent); 
		}catch(ActivityNotFoundException e){
			Toast.makeText(a, R.string.ERROR_OPEN_WEB_URL, Toast.LENGTH_LONG).show();
		}
	}
}
