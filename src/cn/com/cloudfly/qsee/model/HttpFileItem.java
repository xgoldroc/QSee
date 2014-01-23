package cn.com.cloudfly.qsee.model;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.utility.SerialDownloadTask;
import cn.com.cloudfly.qsee.utility.Utility;
import cn.com.cloudfly.qsee.utility.WapsUtility;


/**
 * 此类只用来表示网络上的远程文件，相对于本地文件而言，却提供统一的接口
 * 
 */
public class HttpFileItem extends FileItem {

	protected HttpFileItem(String path, AbstractFolderModel ownerModel) {
		super(path, ownerModel);
	}

	@Override
	public String getName() {
		return uri().getLastPathSegment();
	}

	@Override
	public String getParentPath() {
		return uri().getScheme()+"://"+uri().getHost()+uri().getPath().replace(uri().getLastPathSegment(), "");
	}

	private String getParentName(){
		List<String> segs=uri().getPathSegments();
		if (segs!=null && segs.size()>=2){
			return segs.get(segs.size()-2);
		}
		return "untitled";
	}
	
	static public String getLocalDownloadDir(){
		return String.format("%s/QSee/Exhibitions",Utility.getDefaultDirPath());
	}
	@Override
	protected String getThumbsFilePath(){
		return String.format("%s/%s/%s", getLocalDownloadDir(),getParentName(),getName());
	}

	@Override
	public boolean rename(String newName) {
		//网络上的文件无法改名
		return false;
	}

	@Override
	public long lastModified() {
		return 0;//0 表示远程文件是很早很早就存在的（不变的）即：本地一旦下载成LocalFileItem后，永远比远程新，不需要重新下载。
	}

	@Override
	public boolean exists() {
		//返回远程文件的本地代理文件(下载下的文件)的存在与否
		//目前返回 true
		return true;
	}

	private Uri _uri=null;
	private Uri uri(){
		if (_uri==null){
			_uri=Uri.parse(_filePath);
		}
		return _uri;
	}

	@Override
	public void startDownload(final Context ctx,final SerialDownloadTask.OnDownloadListener listener) {
		if (this.getLocalDownloadFileItem().exists()){//if the file has been downloaded ,just call onAllDownloadDone();
			listener.onAllDownloadDone(new File(HttpFileItem.this.getThumbsFilePath()).getParent());
			return;
		}
		
		if (WapsUtility.getUserMoney()==-1 || WapsUtility.getUserMoney()>30 ){
			Uri[] uris=(new Uri[1]);
			uris[0]=Uri.parse(HttpFileItem.this.getAbsolutePath());
			new SerialDownloadTask(ctx,new File(HttpFileItem.this.getThumbsFilePath()).getParentFile().getName(),listener).execute(uris);
		}else{
			if (WapsUtility.getUserMoney()<1){
				new AlertDialog.Builder(ctx)
				.setTitle(R.string.WALLPAPER_DOWNLOAD)
				.setMessage(WapsUtility.getMoneyQuestionInfo(1))
				//.setIcon(android.R.drawable.)
				.setNeutralButton(R.string.CLOSE, null)
				.setNegativeButton(WapsUtility.getMoreMoneyDetail(),new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						WapsUtility.startOffersActivity();	
					}
				})
				.show();
				
			}else{
				new AlertDialog.Builder(ctx)
				.setTitle(R.string.WALLPAPER_DOWNLOAD)
				.setMessage(WapsUtility.getMoneyQuestionInfo(1)+"\n确认下载？")
				//.setIcon(android.R.drawable.)
				.setPositiveButton(R.string.OK, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Uri[] uris=(new Uri[1]);
						uris[0]=Uri.parse(HttpFileItem.this.getAbsolutePath());
						new SerialDownloadTask(ctx,new File(HttpFileItem.this.getThumbsFilePath()).getParentFile().getName(),listener).execute(uris);
					}
				})
				.setNeutralButton(R.string.CANCEL, null)
				.setNegativeButton(WapsUtility.getMoreMoneyDetail(),new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						WapsUtility.startOffersActivity();	
					}
				})
				.show();
			}
		}
	}

}
