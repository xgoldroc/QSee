package cn.com.cloudfly.qsee.model;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.utility.SerialDownloadTask;
import cn.com.cloudfly.qsee.utility.Utility;
import cn.com.cloudfly.qsee.utility.WapsUtility;


/**
 * 抽象文件夹模型类，表示文件夹的概念。具有通用的基本操作接口：
 * 如，包含若干文件和子文件夹，文件操作（删除，整理重复文件），文件查找，Listener 通知机制等 
 * @author roc
 */
public abstract class AbstractFolderModel {
	private ProgressDialog _progressDlg=null;

	/**
	 * 此文件夹的路径或名称
	 */
	private String _path="";
	
	private FileItem[] _files=null;
	
	private FileItem[] _subfolder=null;

	static public final FileItem[] emptyFileItems=new FileItem[0];
	
	public AbstractFolderModel(String path) {
		if (path.endsWith("/..")){
			_path=new File(path.substring(0, path.length()-3)).getParent();
		}else{
			_path=path;
		}
	}
	
	/**
	 * 整理重复文件
	 * @param ctx
	 * @param handler 通知发现的重复图片，message 中的 obj 为 List<Pair<FileItem, FileItem>> 表示重复的两个FileItem 
	 */
	public void cleanup(Context ctx,final Handler handler){
		final Thread _cleanupThread=new Thread(){
			@Override
			public void run(){
				final List<Pair<FileItem,FileItem> > deleteFileItems=new ArrayList<Pair<FileItem,FileItem> >();
				Map<String,FileItem> itemMap=new TreeMap<String,FileItem>();
				int idx=0;
				for(FileItem item:files()){
					if (isInterrupted()){
						deleteFileItems.clear();
						_progressDlg.dismiss();
						return;
					}
					String md5sum=Utility.getMD5Sum(item.getAbsolutePath());
					if (!itemMap.containsKey(md5sum)){
						itemMap.put(md5sum, item);
					}else{
						deleteFileItems.add(new Pair<FileItem,FileItem>(itemMap.get(md5sum),item));
						handler.post(new Runnable(){
							public void run() {
								String info=String.format(Utility.getResourceString(R.string.FOUND_DUPLICATED_IMAGES_DETAIL), deleteFileItems.size());
								_progressDlg. setMessage(info);
							}
						});
					}
					_progressDlg.setProgress(idx++);

				}
				_progressDlg.dismiss();
				Message msg=Message.obtain();
				msg.what=1;
				msg.obj=deleteFileItems;
				handler.sendMessage(msg);
			}
		};
		
		if (_progressDlg==null){
			_progressDlg=new ProgressDialog(ctx);
			_progressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			_progressDlg.setTitle(R.string.MENU_CLEANUP);
			String info=String.format(Utility.getResourceString(R.string.FOUND_DUPLICATED_IMAGES_DETAIL), 0);

			_progressDlg.setMessage(info);
			_progressDlg.setIndeterminate(false);
			_progressDlg.setCancelable(true);
			_progressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					_cleanupThread.interrupt();
					Log.d("HAHA", "_cleanupThread.interrupt()");
				}
			});
		}
		_progressDlg.setMax(files().length);
		_progressDlg.setProgress(0);
		_progressDlg.show();
		
		_cleanupThread.start();
	
	}
	
	public abstract boolean deleteFile(FileItem f);
	
	final public String detail(){
		String dvTitle="\t"+Utility.getResourceString(R.string.CURRENT_PATH)+":"+this.getPath()+"\n\t"
				+this.files().length+" "+Utility.getResourceString(R.string.MANY_IMAGE)+" , "+(this.subfolders().length-1)+" "
				+ Utility.getResourceString(R.string.SUBFOLDER);
		return dvTitle;
	}

	final public FileItem[] files(){
		if (_files==null){
			_files=createFiles();
		}
		return _files;
	}
	
	final public FolderModelIterator find(String filePath){
		FileItem[] items=files();
		if (items!=null && items.length>0){
			for (int i=0 ; i<items.length ; ++i){
				if (items[i].getAbsolutePath().equalsIgnoreCase(filePath)){
					FolderModelIterator iterator =new FolderModelIterator(this);
					if (iterator.setCurrent(items[i]))
						return iterator;
				}
			}
		}
		return null;
	}
	
	final public int findIndex(String filePath){
		FileItem[] items=files();
		if (items!=null && items.length>0){
			for (int i=0 ; i<items.length ; ++i){
				if (items[i].getAbsolutePath().equalsIgnoreCase(filePath)){
					return i;
				}
			}
		}
		return -1;
	}
	
	public void invalidate(){
		_files=null;
		_subfolder=null;
		if (_listener!=null){
			_listener.onModelChanged();
		}
	}
	
	public boolean isParentOf(String filePath){
		return getPath().equals(FileItem.createInstance(filePath,null).getParentPath());
	}
	
	final public  String getPath(){
		return _path;
	}

	private IFolderModelListener _listener=null;
	
	final public void setListener(IFolderModelListener listener){
		_listener=listener;
	}
	final public  int size(){
		return files().length;
	}
	
	final public FileItem[] subfolders(){
		if (_subfolder==null){
			if (!this.hasParent()){
				_subfolder=createSubfolders();
			}else{
				FileItem[] subfolders=createSubfolders();
				_subfolder=new FileItem[subfolders.length+1];
				_subfolder[0]=FileItem.createInstance(_path+"/..",this);
				for (int i=0;i<subfolders.length ; ++i){
					_subfolder[i+1]=subfolders[i];
				}
				subfolders=null;
			}
		}
		return _subfolder;
	}
	final public String title() {
		return String.format("%s %s [ %s ]", Utility.getResourceString(R.string.APP_TITLE),Utility.isMyPhone()?"=":"-",new File(this.getPath()).getName());
	}
	protected abstract FileItem[] createFiles() ;
	protected abstract FileItem[] createSubfolders() ;

	protected abstract boolean hasParent();
	
	
	private String[] getDownloadList(){
		//排除已经下载过的文件（存在的）
		FileItem[] fs=this.files();
		ArrayList<String> paths=new ArrayList<String>();
		for(FileItem i : fs){
			if (!i.getLocalDownloadFileItem().exists()){
				paths.add(i.getAbsolutePath());
			}
		}
		if (paths.size()==0){
			return null;
		}
		String[] ret=new String[paths.size()];
		paths.toArray(ret);
		return ret;
	}
	
	public  String[] startDownload(final Activity ctx, SerialDownloadTask.OnDownloadListener listener){
		String[] paths=getDownloadList();
		if (paths==null){
			new AlertDialog.Builder(ctx)
			.setTitle("全部文件已经下载过了")
			.setMessage(R.string.MENU_JUMP_TO_DOWNLOAD_DIR)
			.setNeutralButton(R.string.CLOSE, null)
			.setNegativeButton(R.string.QUICK_JUMP,new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					Intent t=new Intent();
					FileItem[] fs=AbstractFolderModel.this.files();
					if (fs!=null && fs[0]!=null){
						t.putExtra("path", fs[0].getLocalDownloadFileItem().getParentPath());
					}
					ctx.setResult(2,t);
					ctx.finish();
				}
			})
			.show();
			return null;
		}else{
			if (WapsUtility.getUserMoney()==-1 || WapsUtility.getUserMoney()>=paths.length){
				return paths;
			}else{
				new AlertDialog.Builder(ctx)
				.setTitle(R.string.WALLPAPER_DOWNLOAD)
				.setMessage(WapsUtility.getMoneyQuestionInfo(paths.length))
				//.setIcon(android.R.drawable.)
				.setNeutralButton(R.string.CLOSE, null)
				.setNegativeButton(WapsUtility.getMoreMoneyDetail(),new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						WapsUtility.startOffersActivity();	
					}
				})
				.show();
				return null;
			}
		}
	}
	
}

