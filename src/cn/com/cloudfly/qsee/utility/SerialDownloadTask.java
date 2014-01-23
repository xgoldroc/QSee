package cn.com.cloudfly.qsee.utility;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import cn.com.cloudfly.qsee.R;



public class SerialDownloadTask extends AsyncTask<Uri[],Integer,String[]> implements OnCancelListener {
	public interface OnDownloadListener{
		void onFileDownloadDone(String file);
		void onAllDownloadDone(String downloadDir);
	};
	
	private ProgressDialog _progressDialog;
	private String _serialName;
	private OnDownloadListener _downloadListener;
	public SerialDownloadTask(Context a,String serialName,final OnDownloadListener l){
		_progressDialog=new ProgressDialog(a,ProgressDialog.STYLE_HORIZONTAL);
		_progressDialog.setCancelable(true);
		_progressDialog.setMessage(Utility.getResourceString(R.string.WAITING_DOWNLOADING));
		_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		_serialName=serialName;
		_downloadListener=l;
		
	}

	@Override
	protected String[] doInBackground(Uri[]... uris) {
		int sz=uris[0].length;
		String[] fp=new String[sz];
		for(int i=0;i<sz;++i){
			Uri uri=uris[0][i];
			String filePath=downloadFileToMyStore(uri,_serialName,uri.getLastPathSegment(),i,sz);
			if (!filePath.startsWith("ERROR")){
				fp[i]=filePath;
			}else{
				fp[i]=null;
			}
			if (this.isCancelled()){
				break;
			}
		}
		return fp;
	}
	
	private String getDownloadDir(String serialName){
		return String.format("%s/%s",Utility.myStoreDirPath(), serialName);
	}
	
	
	private String  downloadFileToMyStore(Uri uri,String serialName,String fileName,int idx,int count){
		String filePath=String.format("%s/%s",getDownloadDir(serialName),fileName);
		if (!Utility.makeMyStore(serialName)){
			return "ERROR:can not make";
		}
		File f=new File(filePath);
		if (f.exists()){
			setFileDownloadDone(filePath);
			return filePath;
		}
		try {
			URL url = new URL(uri.toString());
	         // 打开连接   
	         URLConnection con = url.openConnection();
	         //获得文件的长度
	         int contentLength = con.getContentLength();
	         InputStream is = con.getInputStream();  
	         byte[] bs = new byte[1024];   
	         // 输出的文件流   
	         OutputStream os = new FileOutputStream(f);   
	         int writenSize=0;
	         this.publishProgress(0,contentLength,idx,count);
	         // 开始读取   
	         int len=0;   
	         while ((len = is.read(bs)) != -1 && !this.isCancelled()) {   
	             os.write(bs, 0, len);
	             writenSize+=len;
	             this.publishProgress(writenSize,contentLength,idx,count);
	         }
	         this.publishProgress(contentLength,contentLength,idx,count);
	         // 完毕，关闭所有链接   
	         os.close(); 
	         is.close();
	         
	         if (!this.isCancelled()){
	        	 setFileDownloadDone(filePath);
	        	 return filePath;   
	         }else{
	        	 if (f.exists()){
	        		 f.delete();
	        	 }
	        	 return "ERROR:User cancelled";
	         }
		} catch (Exception e) {
        	StringWriter sw = new StringWriter(); 
            PrintWriter pw = new PrintWriter(sw); 
            e.printStackTrace(pw); 
            return "ERROR:\n"+sw.toString();
	        //return null;
		}
	}

	@Override
	protected void onPreExecute() {
		_progressDialog.show();
		super.onPreExecute();
	}
	@Override
	protected void onProgressUpdate(Integer... values) {
		int value=values[0].intValue();
		int total=values[1].intValue();
		if (value==0){
			int idx=values[2].intValue();
			int count=values[3].intValue();
			_progressDialog.setMessage(String.format("正在下载第%d张/共%d张...", idx+1,count));
		}
		_progressDialog.setMax(total);
		_progressDialog.setProgress(value);
		super.onProgressUpdate(values);
	}

	
	@Override
	protected void onPostExecute(String[] filePaths) {
		_progressDialog.dismiss();
		for(String filePath:filePaths){
			_downloadListener.onFileDownloadDone(filePath);
		}
		_downloadListener.onAllDownloadDone(this.getDownloadDir(_serialName));
		super.onPostExecute(filePaths);
	}
	
	protected void onCancelled(String[] filePaths){
		for(String filePath:filePaths){
			_downloadListener.onFileDownloadDone(filePath);
		}
		super.onCancelled();
	}
	
	private ArrayList<String> _downloadedFiles=new ArrayList<String>();
	synchronized private void setFileDownloadDone(String filePath){
		_downloadedFiles.add(filePath);
	}
	
	synchronized private String[] getDownloadFiles(){
		String[] fs=new String[_downloadedFiles.size()];
		_downloadedFiles.toArray(fs);
		return fs;
	}
	
	public void onCancel(DialogInterface dialog) {
		this.cancel(false);
		
	}

	@Override
	protected void onCancelled() {
		String[] files=getDownloadFiles();
		this.onCancelled(files);
	}

}
