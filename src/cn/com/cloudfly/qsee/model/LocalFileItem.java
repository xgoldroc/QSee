package cn.com.cloudfly.qsee.model;


import java.io.File;

import android.content.Context;
import cn.com.cloudfly.qsee.utility.SerialDownloadTask;
import cn.com.cloudfly.qsee.utility.Utility;


public class LocalFileItem extends FileItem {

	protected LocalFileItem(String path, AbstractFolderModel ownerModel) {
		super(path, ownerModel);
	}

	@Override
	public String getName(){
		return file().getName();
	}
	
	@Override
	public String getParentPath(){
		return file().getParent();//?什么情况下会返回null?
	}
	
	@Override
	protected String getThumbsFilePath(){
		if (getParentPath().endsWith(THUMBS_DIR_NAME)){//自身已经是缩略图了
			return this.getAbsolutePath();
		}else{
			return String.format("%s/%s/%s.PNG", getParentPath(),THUMBS_DIR_NAME,getName());
		}
	}

	@Override
	public boolean rename(String newName){
		if (Utility.isPathUrl(getAbsolutePath())){
			return false;
		}
		File newFile=new File(getParentPath()+"/"+newName);
		if ( file().renameTo(newFile)){
			_filePath=newName;
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public long lastModified (){
		return file().lastModified();
	}
	
	@Override
	public boolean exists(){
		return file().exists();
	}

	//private File _file=null;
	private File file(){
		return new File(getAbsolutePath());
		//if (_file==null){
		//	_file=new File(getAbsolutePath());
		//}
		//return _file;
	}

	@Override
	public void startDownload(Context ctx,SerialDownloadTask.OnDownloadListener listener) {
		listener.onFileDownloadDone(this.getAbsolutePath());
		listener.onAllDownloadDone(new File(this.getParentPath()).getName());
	}
	
}
