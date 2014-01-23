package cn.com.cloudfly.qsee.utility;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.model.AbstractFolderModel;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.model.FolderModelManager;
import cn.com.cloudfly.qsee.model.FolderModelManager.Mode;

//async gen thumbs for a dir
//put thumbs to <dir>/.thumbs/

public class ThumbsGenerator implements Runnable, UncaughtExceptionHandler  {
	
	public ThumbsGenerator(int thumbCellSize){
		_cellSize=thumbCellSize;
	}
	
	public void changeDirectory(String dirPath){
		if (_imgFolderModel!=null && _imgFolderModel.getPath().equals(dirPath)){
			return;
		}
		
//		File imgDir=new File(dirPath);
//		if (imgDir.exists()){
			cancel();
			_imgFolderModel=FolderModelManager.getFolderModel(dirPath,Mode.FILE_SYSTEM);
			start();
			if (!isThumbsDirectoryExist(dirPath)){
				Toast.makeText(Utility.getApplicationCtx(), R.string.FIRST_ENTER_WARNING, Toast.LENGTH_LONG).show();
			}
//		}
	}
	
	public void start(){
		cancel();	
		synchronized(this){
			_shouldQuit=false;
		}
		_thread=new Thread(this);
		_thread.setUncaughtExceptionHandler(this);
		_thread.setDaemon(true);
		_thread.start();
	}
	
	private void cancel(){
		synchronized(this){
			_shouldQuit=true;
		}
		try {
			if (_thread!=null){
				_thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private AbstractFolderModel _imgFolderModel=null;
	private Thread _thread=null;
	private boolean _shouldQuit=false;
	private final int _cellSize;

	
	
	public void run() {
		//Log.d("DEBUG","ENTER Gen thumbs thread.");
		if (!FileItem.makeThumbsDirectory(_imgFolderModel.getPath())){			//if thumbs dir does not exist,create it
			return ;
		}
		FileItem[] imgFiles=_imgFolderModel.files();//Utility.listImageFiles();
		if (imgFiles!=null && imgFiles.length>0){
			for (FileItem imgFile:imgFiles){
				synchronized(this){
					if (_shouldQuit)
						break;
				}
				genThumbsFor(imgFile);
			}
		}
		//Log.d("DEBUG","EXIT Gen thumbs thread.");
	}

	public void uncaughtException(Thread thread, Throwable ex) {
		Log.d("ERROR","Gen thumbs thread exception:"+ex.toString());
	}
	
//	public static String getThumbsFilePathOfs(URL url){
//		String[] pathSecs=url.getPath().split("/");
//		if (pathSecs.length>0){
//			return String.format("%s/QSee/Cache/%s/%s",Utility.getDefaultDirPath(),pathSecs[pathSecs.length-1],url.getFile());
//		}else{
//			return String.format("%s/QSee/Cache/%s",Utility.getDefaultDirPath(),url.getFile());
//		}
//	}
//	public static String getThumbsFilePathOf(String imgFilePath){
//		if (imgFilePath.startsWith("http:")){
//			try {
//				return getThumbsFilePathOf(new URL(imgFilePath));
//			} catch (MalformedURLException e) {
//				return null;
//			}	
//		}else{
//			return getThumbsFilePathOf(new File(imgFilePath));
//		}
//	}
//	
//	private static String getThumbsFilePathOf(File imgFile){
//		return String.format("%s%s/%s.PNG", imgFile.getParent(),FileItem.THUMBS_DIR_NAME,imgFile.getName());
//	}
	
	/**
	 * 生成指定文件的预览图文件
	 * @param imgFile
	 */
	private void genThumbsFor(FileItem imgFile){
		if (imgFile.isThumbsValid()){
			return ;
		}
		FileItem thumbFileItem=imgFile.getThumbsFileItem();
		if (thumbFileItem==null)
			return ;
		
		if (thumbFileItem.exists())
			return;
		
		Log.d("THUMBS", "generate "+imgFile.getAbsolutePath()+" to "+thumbFileItem.getAbsolutePath());
		Bitmap bitmap=Utility.getImageThumbFromOrig(imgFile.getAbsolutePath(),_cellSize , !Utility.isPathUrl(imgFile.getAbsolutePath()), null);
		if (bitmap==null){
			return ;
		}
		
		boolean thumbFileExists=thumbFileItem.exists();
		File thumbFile=new File(thumbFileItem.getAbsolutePath());
		if (!thumbFileExists){
			try {
				thumbFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return ;
			}
		}		

		//thumbs file should exists;
		assert(thumbFile.exists());
		assert(bitmap!=null);
		
		try {
			FileOutputStream os = new FileOutputStream(thumbFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
			os.close();
			bitmap.recycle();
			bitmap=null;
			os=null;
			thumbFile=null;
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			return ;
		} catch (IOException e) {
			
			e.printStackTrace();
			return ;
		}
	}
	
	static private boolean isThumbsDirectoryExist(String imgDir){
		if (Utility.isPathUrl(imgDir)){
			return true;
		}
		File thumbsDir=new File(FileItem.getThumbsDirFromDir(imgDir));
		return thumbsDir.exists();
	}
	
}
