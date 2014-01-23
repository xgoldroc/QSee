package cn.com.cloudfly.qsee.adapter;

import android.content.Context;
import android.view.View;
import cn.com.cloudfly.qsee.model.AbstractFolderModel;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.view.FolderView;

public class FoldersFolderModelAdapter extends AbstractFolderModelAdapter {
	@Override
	protected FileItem[] modelItems() {
		if (_folderModel!=null)
			return _folderModel.subfolders();
		else
			return AbstractFolderModel.emptyFileItems;
	}

	@Override
	protected View createView(FileItem f, Context ctx) {
		return new FolderView(ctx,f);
	}
	
	@Override
	protected void onBeforeFolderModelChanged(){
		FolderView.cancelAllLoadingThreads();
	}

}
