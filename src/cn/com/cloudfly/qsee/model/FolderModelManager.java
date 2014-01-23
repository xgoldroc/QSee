package cn.com.cloudfly.qsee.model;


import java.io.File;

import cn.com.cloudfly.qsee.utility.AppOptions;
import cn.com.cloudfly.qsee.utility.ThumbsGenerator;
import cn.com.cloudfly.qsee.utility.Utility;

public class FolderModelManager {
	public FolderModelManager() {
		super();
		_currFolderModel=new RealFolderModel(Utility.getDefaultDirPath());
		_thumbsGenerator=new ThumbsGenerator(Utility.getCellSize());
	}

	public static FolderModelManager getInstance(){
		if (_instance==null){
			_instance=new FolderModelManager();
		}
		return _instance;
	}
	
	
	public static AbstractFolderModel getFolderModel(String path,Mode mode){
		if (path.endsWith("/..")){
			path=new File(path.substring(0,path.length()-3)).getParent();
		}
		if (mode==Mode.FILE_SYSTEM){
			if (Utility.isPathUrl(path)){
				return new VirtualFolderModel(path);
			}else{
				return  new RealFolderModel(path);
			}
		}else{
			return FavoriteFolderModel.getInstance();
		}
		
	}
	private static FolderModelManager _instance=null;

	private ThumbsGenerator _thumbsGenerator=null;
	public void setCurrentPath(String path){
		if (path.endsWith("/..")){
			path=new File(path.substring(0,path.length()-3)).getParent();
		}
		
		if (path.equals(FavoriteFolderModel.getInstance().getPath())){
			_mode=Mode.FAVORITE_SYSTEM;
		}else {
			_mode=Mode.FILE_SYSTEM;
			_currFolderModel= FolderModelManager.getFolderModel(path,_mode);
			startThumbsGenerating(path);
		}
	}
	
	public void startThumbsGenerating(String path){
		_thumbsGenerator.changeDirectory(path);
	}
	
	public AbstractFolderModel getCurrentModel(){
		if (_mode==Mode.FILE_SYSTEM)
			return _currFolderModel;
		else
			return FavoriteFolderModel.getInstance();
	}
	
	public enum Mode {FILE_SYSTEM,FAVORITE_SYSTEM};
	public void switchMode(){
		if (_mode==Mode.FILE_SYSTEM)
			_mode=Mode.FAVORITE_SYSTEM;
		else
			_mode=Mode.FILE_SYSTEM;
	}
	public Mode mode(){
		return _mode;
	}
	
	private Mode _mode=Mode.FILE_SYSTEM;
	private AbstractFolderModel _currFolderModel=null;
	
	public void save(){
		AppOptions.writeCurrentPath(getCurrentModel().getPath());
		AppOptions.writeRealCurrentPath(_currFolderModel.getPath());
	}
	
	public String load(){
		String path=AppOptions.readCurrentPath();
		if (!path.equalsIgnoreCase(getCurrentModel().getPath())){
			setCurrentPath(path);
		}
		String p=AppOptions.readRealCurrentPath();
		if (_mode!=Mode.FILE_SYSTEM && !p.equalsIgnoreCase(_currFolderModel.getPath())){
			_currFolderModel= new RealFolderModel(p);
		}
		startThumbsGenerating(p);
		return  getCurrentModel().getPath();
	}
}
