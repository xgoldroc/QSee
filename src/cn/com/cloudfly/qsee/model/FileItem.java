package cn.com.cloudfly.qsee.model;


import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import cn.com.cloudfly.qsee.utility.SerialDownloadTask;
import cn.com.cloudfly.qsee.utility.Utility;

/**
 * 提供统一的接口， 操作本地文件和网络文件。
 *同时具备：缩略图文件信息以及属性支持，还有bitmap 载入支持
 */
public abstract class FileItem extends PropertySerializable {
	
	//implement function from PropertySerializable
	@Override
	protected String getPropertyFilePath(){
		return String.format("%s/%s/%s.property", this.getParentPath(),THUMBS_DIR_NAME,this.getName());
	}
	
	public static FileItem createInstance(String path,AbstractFolderModel ownerModel){
		if (Utility.isPathUrl(path))
			return new HttpFileItem(path,ownerModel);
		else 
			return new LocalFileItem(path,ownerModel);
	}
	
	protected  final static String THUMBS_DIR_NAME=".thumbs";
	
	static public String getParentPathFrom(String path){
		return FileItem.createInstance(path, null).getParentPath();
	}
	
	static public String getThumbsDirFromDir(String dirPath){
		return FileItem.createInstance(dirPath+"/foo.png",null).getThumbsFileItem().getParentPath();
	}
	
	
	protected FileItem(String path,AbstractFolderModel ownerModel) {
		_filePath=path;
		_ownerModel=ownerModel;
	}

	protected FileItem(File file,AbstractFolderModel ownerModel){
		_filePath=file.getAbsolutePath();
		_ownerModel=ownerModel;
	}
	
	public String getAbsolutePath(){
		return _filePath;
	}
	
	abstract public String getName();
	
	abstract public String getParentPath();
	
	
	/**
	 * 返回FileItem的对应的缩略图FileItem
	 * 注意：本地图片只有一级缩略，而远程http图片有两级缩略（一级是下载图（对应原图），二级是下载图的缩略图））
	 * @return 对应的缩略图的FileItem
	 */
	public FileItem getThumbsFileItem(){
		if (this._filePath.equalsIgnoreCase(getThumbsFilePath()))//自身已经是最后一级缩略图
			return this;
		else
			return createInstance(getThumbsFilePath(),this._ownerModel).getThumbsFileItem();//取最后一级缩略图
	}
	
	/**
	 * 返回指定文件的本地代理文件：如果是远程文件，返回对应本地下载地址；如果是本地文件，返回自身
	 * @return 指定文件的本地代理文件
	 */
	public FileItem getLocalDownloadFileItem(){
		if (Utility.isPathUrl(this._filePath)){
			return createInstance(getThumbsFilePath(),this._ownerModel);
		}else{
			return this;
		}
	}
	
	abstract public void startDownload(Context ctx,SerialDownloadTask.OnDownloadListener listener);
	
	static public boolean makeThumbsDirectory(String imgDirPath){
		String thumbsDirPath=getThumbsDirFromDir(imgDirPath);//createInstance(imgDirPath+"/foo.png",null).getThumbsFile().getParentPath();
		if (!Utility.isPathUrl(imgDirPath)){
			if (!thumbsDirPath.equalsIgnoreCase(imgDirPath+"/"+THUMBS_DIR_NAME)){
				Log.d("ASSERT", thumbsDirPath+"!=" +imgDirPath+"/"+THUMBS_DIR_NAME);
			}
		}
		File thumbsDir=new File(thumbsDirPath);
		if (!thumbsDir.exists()){
			if (!thumbsDir.mkdirs()){
				Log.d("ERROR", "Can not create dir"+thumbsDir.getAbsolutePath());
				return false;
			}
		}
		return true;
	}

	
	abstract protected String getThumbsFilePath();

	protected abstract long lastModified(); 
	
	public boolean isThumbsValid(){
		File thumbFile=new File(getThumbsFilePath());
		final boolean thumbFileExists=thumbFile.exists();
		if (!thumbFileExists){
			return false;
		}
		
		return thumbFile.lastModified()>=lastModified() && thumbFile.length()>0;
	}

	
	
	
	
	
	public boolean delete(){
		return _ownerModel.deleteFile(this);
	}
	
	abstract public boolean rename(String newName);

	abstract public boolean exists();
	
	
	private int _scaleAngle=0;

	private boolean isThumbsItem(){
		return this.getParentPath().endsWith(THUMBS_DIR_NAME);
	}
	
	private Bitmap _bitmap=null;
	synchronized public Bitmap  bitmap(int scaleAngle){//for ImageSwitcherActivity only
		if ( _scaleAngle != scaleAngle ){
			 _bitmap = null;
			_scaleAngle = scaleAngle;
		}
		prepareBitmap(CacheOperate.PREPARE);
		return _bitmap;
	}
	
	public enum CacheOperate {PREPARE,CLEAR} ;
	synchronized public void  prepareBitmap(CacheOperate operate){
		if (operate==CacheOperate.CLEAR ){
			_bitmap=null;
		}else{
			if (_bitmap==null){
				Bitmap btp=Utility.getImageThumbFromOrig(_filePath,(!this.isThumbsItem())?Math.max(Utility.getScreenWidth(),Utility.getScreenHeight()):Utility.getCellSize(),false,Utility.failedBitmap);
				if (_scaleAngle % 360 !=0){
					btp=Utility.rotateBitmap(btp,_scaleAngle);
				}
				if (btp!=null){
					_bitmap=btp;
				}
			}
		}
	}
	
	protected String _filePath;
	private AbstractFolderModel _ownerModel;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileItem other = (FileItem) obj;
		return  (_filePath.equalsIgnoreCase(other._filePath));
	}
	
}
