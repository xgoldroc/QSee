package cn.com.cloudfly.qsee.model;

import android.os.Looper;
import android.widget.Toast;
import cn.com.cloudfly.qsee.utility.Utility;

public class FolderModelIterator {
	public FolderModelIterator(AbstractFolderModel model){
		_model=model;
	}
	
	private AbstractFolderModel _model;
	private int _currentIdx=0;
	
	final public boolean setCurrent(FileItem file){
		if (_model==null || file==null)
			return false;
		
		FileItem[] fs=_model.files();
		for (int i=0,isize=fs.length; i<isize;++i){
			if (file.equals(fs[i])){
				_currentIdx=i;
				return true;
			}
		}
		_currentIdx=0;
		return false;
	}
	
	final public FileItem getCurrent(){
		FileItem[] items=_model.files();
		if (items.length==0){
			return null;
		}
		if (_currentIdx>=items.length)
			return items[0];
		return items[_currentIdx];
	}
	
	final public void goNext(){
		move(1);
	}
	
	final public void goPrev(){
		move(-1);
	}

	final private void move(int step){
		if (_model.files().length==0){
			Utility.toastDebug("No file in dir");
			return;
		}
		_currentIdx+=step;
		
		if (_currentIdx>=_model.files().length){
			if (Looper.myLooper()!=null && Looper.getMainLooper()==Looper.myLooper()){ 
				Toast.makeText(Utility.getApplicationCtx(),"已经是最末张，将转向第一张" ,Toast.LENGTH_SHORT).show();
			}
			_currentIdx=0;
		}
		
		if (_currentIdx<0){
			if (Looper.myLooper()!=null && Looper.getMainLooper()==Looper.myLooper()){ 
				Toast.makeText(Utility.getApplicationCtx(),"已经是第一张，将转向最末张" ,Toast.LENGTH_SHORT).show();
			}
			_currentIdx=(_model.files().length-1);
		}
	}
	
	final public FolderModelIterator getDistanceIterator(int step){
		FolderModelIterator i = new FolderModelIterator(this._model);
		i._currentIdx=this._currentIdx;
		i.move(step);
		return i;
	}
	
	final public int getIndex(){
		return _currentIdx;
	}
	
	final public String getPositionString(){
		return String.format("%d/%d", _currentIdx+1,_model.size());	}
}
