package cn.com.cloudfly.qsee.activity;


import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import cn.com.cloudfly.qsee.MenuMgr;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.model.AbstractFolderModel;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.model.FolderModelManager;
import cn.com.cloudfly.qsee.model.FolderModelManager.Mode;
import cn.com.cloudfly.qsee.model.VirtualFolderModel;
import cn.com.cloudfly.qsee.utility.AppOptions;
import cn.com.cloudfly.qsee.utility.SerialDownloadTask.OnDownloadListener;
import cn.com.cloudfly.qsee.utility.Utility;
import cn.com.cloudfly.qsee.utility.WapsUtility;
import cn.com.cloudfly.qsee.view.ExhibitionViewSwitcher;

public class WallpaperExhibitionActivity extends EntranceActivity {
	public static void startNewInstance(Activity ctx,String[] files,String title,String category){
		Intent intent=new Intent(ctx, WallpaperExhibitionActivity.class);

		intent.putExtra("files",files )
				.putExtra("title",title)
				.putExtra("category", category);
		
		ctx.startActivityForResult(intent,2);
	}

	public static void startNewInstanceForLocal(Activity ctx,String dirPath,int maxSize){
		Intent intent=new Intent(ctx, WallpaperExhibitionActivity.class);

		File d=new File(dirPath);
		intent.putExtra("files",Utility.getImageFilePaths(dirPath,maxSize))
				.putExtra("title",d.getName())
				.putExtra("category", d.getParent());
		
		ctx.startActivity(intent);
	}

	
	private String _whCategory;
	private String _whTitle;
	private String _whThumb[]=new String[7];
	
	ExhibitionViewSwitcher _viewSwitcher=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Intent intent=this.getIntent();
		_whThumb=intent.getExtras().getStringArray("files");
		_whTitle=intent.getExtras().getString("title");
		_whCategory=intent.getExtras().getString("category");
		
		_viewSwitcher=new ExhibitionViewSwitcher(this,null){
			@Override
			public void onViewClicked(View v, int idx,String url) {
				//Toast.makeText(WallpaperExhibitionActivity.this, String.valueOf(idx)+":"+url, Toast.LENGTH_SHORT).show();
				//if (Utility.isPathUrl(url)){
					ImageSwitcherActivity.startNewInstance(WallpaperExhibitionActivity.this, url);
				//}else{
				//	ImageSwitcherActivity.startNewInstance(WallpaperExhibitionActivity.this, url);
				//}
			}
		};
		_viewSwitcher.loadImageFiles(_whThumb,_whTitle);		
		setContentView(_viewSwitcher);
		
		AbstractFolderModel fm=FolderModelManager.getFolderModel(FileItem.getParentPathFrom(_whThumb[0]), Mode.FILE_SYSTEM);
		if (Utility.isPathUrl(fm.getPath())){
			VirtualFolderModel vfm=(VirtualFolderModel) fm;
			for (int i=0;i<_whThumb.length;++i){
				vfm.addItemPath(_whThumb[i]);
			}
		}
		refreshTitle();
		if (_whCategory!=null){
			AppOptions.setLastWallpaperExhibitionCategory(_whCategory);
		}
		FolderModelManager.getInstance().startThumbsGenerating(fm.getPath());
	}

	private void refreshTitle(){
		this.setTitle(String.format("%s - %s",Utility.getResourceString(R.string.WALLPAPER_EXHIBITION),_whCategory));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuMgr.createWallpaperExhibitionMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.string.MENU_DOWNLOAD_WALLPAPERS_ALL:
				{
					AbstractFolderModel fm=FolderModelManager.getFolderModel(FileItem.getParentPathFrom(_whThumb[0]), Mode.FILE_SYSTEM);
					fm.startDownload(this, new OnDownloadListener(){

						public void onFileDownloadDone(String file) {
							//Utility.toastDebug(String.format("%s 下载完成", file));	
							WapsUtility.spendUserMoney(1);
						}

						public void onAllDownloadDone(String downloadDir) {
							Toast.makeText(WallpaperExhibitionActivity.this, "下载完成,保存到"+downloadDir+"\n可使用快速跳转查看", Toast.LENGTH_LONG).show();	
						}
					});
					break;
				}
//			case R.string.MENU_JUMP_TO_DOWNLOAD_DIR:
//			{
//				this.setResult(2);
//				this.finish();
//				break;
//			}
			
		}
		return super.onOptionsItemSelected(item);
	}


		
	
}
