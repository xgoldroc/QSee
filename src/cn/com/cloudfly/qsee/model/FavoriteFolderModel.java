package cn.com.cloudfly.qsee.model;

import android.content.Context;
import cn.com.cloudfly.qsee.utility.Utility;

public class FavoriteFolderModel extends VirtualFolderModel {
	private static FavoriteFolderModel _ff=null;
	public static FavoriteFolderModel getInstance(Context ctx){
		if (_ff==null){
			_ff=new FavoriteFolderModel(ctx);
		}
		return _ff;
	}
	public static FavoriteFolderModel getInstance(){
		return _ff;
	}
	
	private FavoriteFolderModel(Context ctx){
		super(Utility.getFavoriteFolderName());
	}

}
