package cn.com.cloudfly.qsee.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import cn.com.cloudfly.qsee.model.AbstractFolderModel;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.model.IFolderModelListener;
import cn.com.cloudfly.qsee.utility.ViewsBuffer;

public abstract class AbstractFolderModelAdapter extends android.widget.BaseAdapter implements IFolderModelListener {

	public void setFolderModel(AbstractFolderModel folderModel){
		if (_folderModel!=folderModel){
			onBeforeFolderModelChanged();
			_viewBuffer.clear();
			_folderModel=folderModel;
			_folderModel.setListener(this);
			onAfterFolderModelChanged();
			this.notifyDataSetChanged();
		}
	}
	
	public int getCount() {
		FileItem[] fs=modelItems();
		if (fs==null)
			return 0;
		else
			return fs.length;
	}

	public void invalidate(){
		_folderModel.invalidate();
		this.notifyDataSetChanged();
	}
	
	protected void onBeforeFolderModelChanged(){
	}
	
	protected void onAfterFolderModelChanged(){
	}
	
	public void onModelChanged() {
		_viewBuffer.clear();
		this.notifyDataSetChanged();
	}

	public Object getItem(int position) {
		FileItem[] fs=modelItems();
		if (fs!=null && position<fs.length){
			return fs[position]; 
		}
		return null;
	}

	public long getItemId(int position) {
		FileItem[] fs=modelItems();
		if (fs!=null && position<fs.length){
			return position; 
		}
		return -1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		FileItem[] fs=modelItems();
		if (fs!=null && position<fs.length){
			View v=_viewBuffer.getView(position);
			if (v==null){
				v= createView(fs[position],parent.getContext());
				_viewBuffer.addView(position,v);
			}
			return v;
		}
		return null;
	}
	
	protected abstract FileItem[] modelItems();
	protected abstract View createView(FileItem f,Context ctx);
	
	protected AbstractFolderModel _folderModel=null;
	private ViewsBuffer _viewBuffer=new ViewsBuffer();
}
