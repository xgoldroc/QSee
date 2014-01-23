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
 * ����ֻ������ʾ�����ϵ�Զ���ļ�������ڱ����ļ����ԣ�ȴ�ṩͳһ�Ľӿ�
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
		//�����ϵ��ļ��޷�����
		return false;
	}

	@Override
	public long lastModified() {
		return 0;//0 ��ʾԶ���ļ��Ǻ������ʹ��ڵģ�����ģ���������һ�����س�LocalFileItem����Զ��Զ���£�����Ҫ�������ء�
	}

	@Override
	public boolean exists() {
		//����Զ���ļ��ı��ش����ļ�(�����µ��ļ�)�Ĵ������
		//Ŀǰ���� true
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
				.setMessage(WapsUtility.getMoneyQuestionInfo(1)+"\nȷ�����أ�")
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
