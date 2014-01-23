package cn.com.cloudfly.qsee.model;


import java.io.File;

import android.app.Activity;
import android.util.Log;
import cn.com.cloudfly.qsee.utility.SerialDownloadTask;
import cn.com.cloudfly.qsee.utility.Utility;

public class RealFolderModel extends AbstractFolderModel {

	public RealFolderModel(String path) {
		super(path);
	}

	@Override
	protected FileItem[] createFiles() {
		long beginTics=System.currentTimeMillis();
		File[] files=Utility.listImageFiles(getPath());
		Log.d("HHH",String.valueOf(System.currentTimeMillis()-beginTics));
		return createArray(files,this);
	}
	
	@Override
	protected FileItem[] createSubfolders() {
		return createArray(Utility.listSubFolders(getPath()),this);
	}

	static private FileItem[] createArray(File[] files,AbstractFolderModel ownerModel){
		if (files==null || files.length==0)
			return emptyFileItems;
		Log.d("LOAD", "createArray " + files.length);
	
		FileItem[] items=new FileItem[files.length];
		for(int i=0; i<files.length ; ++i){
			items[i]=FileItem.createInstance(files[i].getAbsolutePath(),ownerModel);
		}
		return items;
	}


	
	@Override
	protected boolean hasParent() {
		return new File(this.getPath()).getParent()!=null;
	}

	@Override
	public boolean deleteFile(FileItem f) {
		if (f==null)
			return false;
		boolean result=new File(f.getAbsolutePath()).delete();
		if (result){
			invalidate();
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public String[] startDownload(Activity ctx, SerialDownloadTask.OnDownloadListener listener){
		String[] paths=super.startDownload(ctx,listener);
		if (paths!=null){
			for (String path:paths){
				listener.onFileDownloadDone(path);
			}
			listener.onAllDownloadDone(new File(this.getPath()).getName());
		}
		return paths;
	}
}
