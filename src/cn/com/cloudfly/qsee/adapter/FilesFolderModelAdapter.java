package cn.com.cloudfly.qsee.adapter;

import android.content.Context;
import android.view.View;
import cn.com.cloudfly.qsee.model.AbstractFolderModel;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.view.ImageFileView;

public class FilesFolderModelAdapter extends AbstractFolderModelAdapter {
	@Override
	protected FileItem[] modelItems(){
		if (_folderModel!=null){
			return  _folderModel.files();
		}else
			return AbstractFolderModel.emptyFileItems;
	}

	@Override
	protected View createView(FileItem f,Context ctx){
		return new ImageFileView(ctx,f,0);
	}

	@Override
	protected void onBeforeFolderModelChanged(){
		ImageFileView.cancelAllLoadingThreads();
	}

}
