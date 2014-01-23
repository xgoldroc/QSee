package cn.com.cloudfly.qsee.model;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import cn.com.cloudfly.qsee.utility.AppOptions;
import cn.com.cloudfly.qsee.utility.SerialDownloadTask;


public class VirtualFolderModel extends AbstractFolderModel {

	public VirtualFolderModel(String path) {
		super(path);
	}
	
	private String pathKey(){
		return getPath().replace("/","_");
		//return getPath().endsWith("/")?getPath().substring(0, getPath().length()-2):getPath();
	}
	
	private long _savedTicks=0;
	
	private void saveFilesString(String files){
		_savedTicks= System.currentTimeMillis();
		SharedPreferences.Editor edit=AppOptions.getApplicationSharedPreferences(pathKey()).edit();
		edit.putString("body",files)
			.putLong("changedTicks",_savedTicks)
			.commit();
	}
	
	@Override
	public void invalidate() {
		long changedTicks=AppOptions.getApplicationSharedPreferences(pathKey()).getLong("changedTicks",_savedTicks+1);
		if (changedTicks>_savedTicks){
			super.invalidate();
		}
	}

	private String loadFilesString(){
		_savedTicks=AppOptions.getApplicationSharedPreferences(pathKey()).getLong("changedTicks",_savedTicks+1);
		return AppOptions.getApplicationSharedPreferences(pathKey()).getString("body", "");
	}
	
	public void addItemPath(String  path){
		if (!containsItemPath(path)){
			saveFilesString(loadFilesString()+";"+path+";");
		}
		super.invalidate();
	}
	
	public boolean removeItemPath(String filePath){
		
		boolean  ret=false;
		StringBuilder sb=new StringBuilder();
		for (String f : loadFilesString().split(";")){
			if (f.length()==0)
				continue;
			if (f.equals(filePath)){
				ret=true;
			}else{
				sb.append(f);
				sb.append(";");
			}
		}
		saveFilesString(sb.toString());
		super.invalidate();
		return ret;
	}
	
	public boolean containsItemPath(String path){
		return loadFilesString().contains(path+";");
	}

	@Override
	protected FileItem[] createFiles() {
		String listStr=loadFilesString();
		String[] paths=listStr.split(";");
		ArrayList<FileItem> files=new ArrayList<FileItem>();
		//File[] files=new File[paths.length];
		for (int i=0; i<paths.length ; ++i){
			if (paths[i]!=null && paths[i].length()>0){
				//Log.d("LOAD", "Load favorite file "+i+" "+paths[i]);
				files.add(FileItem.createInstance(paths[i],this));
			}
		}
		FileItem[] fileArray=new FileItem[files.size()];
		files.toArray(fileArray);
		return fileArray;
	}
	
	@Override
	protected FileItem[] createSubfolders() {
		return emptyFileItems;
	}
	
	@Override
	protected boolean hasParent() {
		return false;
	}

	@Override
	public boolean deleteFile(FileItem f) {
		if (f==null)
			return false;
		
		return removeItemPath(f.getAbsolutePath());
	}
	
	@Override
	public String[] startDownload(Activity ctx, SerialDownloadTask.OnDownloadListener listener){
		String[] paths=super.startDownload(ctx,listener);
		if (paths!=null){

			Uri[] uris=new Uri[paths.length];
			for (int i=0;i<paths.length;++i){
				uris[i]=Uri.parse(paths[i]);
			}

			new SerialDownloadTask(ctx,new File(this.getPath()).getName(),listener).execute(uris);
		}
		return paths;
	}
}
