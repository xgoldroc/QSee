package cn.com.cloudfly.qsee.view;


import java.util.concurrent.ThreadPoolExecutor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.activity.ImageSwitcherActivity;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.model.FolderModelManager;
import cn.com.cloudfly.qsee.utility.BitmapThumb;
import cn.com.cloudfly.qsee.utility.BitmapThumbsPool;
import cn.com.cloudfly.qsee.utility.Clipboard;
import cn.com.cloudfly.qsee.utility.Utility;

public class ImageFileView extends AsyncImageView implements OnMenuItemClickListener {
	static public int verticalSpacing=10; 
	
	public ImageFileView(Context ctx, FileItem file,int cellSize) {
		super(ctx,file);
 		this.setLayoutParams(new GridView.LayoutParams(Utility.getCellSize(), Utility.getCellSize()));
	}
	
	public void setupContextMenu(ContextMenu menu){
		//menu.setHeaderView(ImageFileView.createThumbView(_currSelectView));
		if (getBitmapThumb()!=null){
			menu.setHeaderIcon(new BitmapDrawable(getBitmapThumb().get()));
		}
		menu.setHeaderTitle(this.getImageHintString());	
		menu.add(0,R.string.MENU_COPY,2,R.string.MENU_COPY).setOnMenuItemClickListener(this);
		menu.add(0,R.string.MENU_CUT,3,R.string.MENU_CUT).setOnMenuItemClickListener(this);
		menu.add(0,R.string.MENU_DELETE,4,R.string.MENU_DELETE).setOnMenuItemClickListener(this);
		menu.add(0,R.string.MENU_RENAME,5,R.string.MENU_RENAME).setOnMenuItemClickListener(this);
		
		menu.add(0,R.string.MENU_PROPERTY,6,R.string.MENU_PROPERTY).setIcon(android.R.drawable.ic_dialog_info).setOnMenuItemClickListener(this);
		
	}
	
	public boolean onMenuItemClick(MenuItem item) {
        if (_file==null)
        	return false;

		switch(item.getItemId()){
		case R.string.MENU_RENAME://重命名
			this.renameImage();
			break;
		case R.string.MENU_PROPERTY://属性
			hintImageProperty(Toast.LENGTH_LONG );
			break;
//		case R.string.MENU_SET_AS_WALLPAPER: //设为壁纸
//			SetWallpaperActivity.startNewInstance(this.getContext(),imgFilePath(),0);
//			break;
		case R.string.MENU_DELETE://删除
			try{
				new AlertDialog.Builder(this.getContext())
				.setTitle(R.string.CONFIRM_DELETE)
				.setMessage(getDeleteConfirmText())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						if (_file.delete()){
							//Bitmap bitmap=BitmapFactory.decodeResource(ImageFileView.this.getContext().getResources(), android.R.drawable.ic_delete);
							//ImageFileView.this.setImageBitmap(bitmap);
							///FavoriteFolderModel.getInstance().save();
						}else{
							String tt=Utility.getResourceString(R.string.ERROR_CAN_NOT_DELETE)+ imgFilePath();
							Toast.makeText(ImageFileView.this.getContext(),tt, Toast.LENGTH_SHORT).show();
						}
					}
				})
				.setNegativeButton(R.string.CANCEL, null)
				.show();
				
			}catch(SecurityException e){
				Toast.makeText(ImageFileView.this.getContext(),R.string.NO_DELETE_PERMISION, Toast.LENGTH_SHORT).show();
			}
			break;
//		case 43: //还原默认壁纸
//			try {
//				getContext().clearWallpaper();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			break;
		case R.string.MENU_COPY://copy
			Clipboard.currentOperate=Clipboard.Operate.COPY;
			Clipboard.setText(imgFilePath());
			break;
		case R.string.MENU_CUT://cut
			Clipboard.currentOperate=Clipboard.Operate.CUT;
			Clipboard.setText(imgFilePath());
			break;
		}
		return true;
		
	}
	
	
	private String imgFilePath(){
		return _file.getAbsolutePath();
	}
	
	private static ThreadPoolExecutor _pool=initializeThreadPool();
	@Override
	protected ThreadPoolExecutor pool() {
		return _pool;
	}
	
	public static void  cancelAllLoadingThreads(){
		_pool.purge();
		_pool.shutdown();
		_pool=initializeThreadPool();
	}

	private static BitmapThumbsPool _thumbsPool=new BitmapThumbsPool(Utility.BUFFER_SIZE,true,Utility.getCellSize());
	
	@Override
	protected void doClearImage(BitmapThumb thumb){
		_thumbsPool.freeBitmapThumb(thumb);
	}
	
	@Override
	protected BitmapThumb onAsyncLoadedImage(Bitmap bmp){
		return _thumbsPool.createBitmapThumb(imgFilePath(),bmp);
	}
	@Override
	protected Bitmap asyncLoadingImage(FileItem file) {
		try {
			Log.d("T","begin asyncLoadingImage "+this.toString());
	
			Bitmap bmp=Utility.getImageThumb(file.getAbsolutePath(),Utility.getCellSize(),true,Utility.failedBitmap);
			int scaleAngle=Integer.parseInt(file.getProperty("scaleAngle", "0"));
			if (scaleAngle!=0){
				bmp=Utility.rotateBitmap(bmp,scaleAngle);
			}
			//BitmapThumb thumb= _thumbsPool.createBitmapThumb(imgFilePath());
			Log.d("T","end asyncLoadingImage "+this.toString());
			//return thumb;
			return bmp;
		}catch (OutOfMemoryError e){
			//Utility.toastDebug( String.format("Catch OutOfMemory " ));
			Log.d("OME",e.toString());
			
			//System.gc();
			return null;
		}
	}
	
	private String getImagePropertyString(){
		Rect imgRect=Utility.getBitmapRect(imgFilePath());
		String txt=String.format("%s:%s\n%s:%d*%d %s\n%s:%s",
				Utility.getResourceString(R.string.FILE_NAME),
				_file.getName(),
				
				Utility.getResourceString(R.string.IMAGE_AREA_SIZE),
				imgRect.width(),imgRect.height(),
				Utility.getResourceString(R.string.PIXEL),
				
				
				Utility.getResourceString(R.string.POSITION),
				_file.getParentPath()
				);
		return txt;	
	}
	
	private String getImageHintString(){
		return _file.getAbsolutePath();
//		String txt=String.format("%s:%s\n%s:%s",
//				Utility.getResourceString(R.string.FILE_NAME),
//				_file.getName(),
//				Utility.getResourceString(R.string.POSITION),
//				_file.getParent()
//				);
//		return txt;	
	}
	

	public void startViewActivity(Activity a){
		cancelAllLoadingThreads();
		ImageSwitcherActivity.startNewInstanceForResult(a, imgFilePath());
	}

	
	String getDeleteConfirmText(){
		if (FolderModelManager.getInstance().mode()!=FolderModelManager.Mode.FAVORITE_SYSTEM){
			return imgFilePath()+"?";
		}else{
			return imgFilePath()+"?\n"+Utility.getResourceString(R.string.FAVORITE_DELETE_COMMENT);
		}
	}

	private void hintImageProperty(int delay){
		new AlertDialog.Builder(this.getContext())
			.setTitle(R.string.MENU_PROPERTY)
			.setMessage(getImagePropertyString())
			.setNeutralButton(R.string.CLOSE,null)
			.setPositiveButton(R.string.MENU_RENAME,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ImageFileView.this.renameImage();
				}
			})
			.show();
	}

	private void renameImage(){
		final EditText newNameEdit = new EditText(this.getContext());
		newNameEdit.setText(_file.getName());
		newNameEdit.selectAll();
		new AlertDialog.Builder(this.getContext())
		.setTitle(R.string.MENU_RENAME)
		.setView(newNameEdit)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				if (_file.rename(newNameEdit.getText().toString())){
					Toast.makeText(ImageFileView.this.getContext(), R.string.SUCCESS_TO_RENAME,Toast.LENGTH_SHORT).show();						
				}else{
					Toast.makeText(ImageFileView.this.getContext(), R.string.FAILED_TO_RENAME,Toast.LENGTH_SHORT).show();						
				}
			}
		})
		.setNegativeButton(R.string.CANCEL, null)
		.show();
	}

	private static Paint _p=initializePaint();
	private static Paint initializePaint(){
		Paint p=new Paint();
		p.setStrokeWidth(2);
		p.setStyle(Style.STROKE);
		p.setColor(Color.DKGRAY);
		return p;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(1, 1, this.getWidth()-1, this.getHeight()-1, _p);
		int scaleAngle=Integer.parseInt(this._file.getProperty("scaleAngle", "0"));
		if (scaleAngle!=0){
			Bitmap rotateBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.rotate);
			
			canvas.drawBitmap(rotateBitmap,getWidth()-rotateBitmap.getWidth()-2,2,null);
		}
	}
	
	static Rect _rect=null;
	@Override
	protected Rect getRect(){
		if (_rect==null){
			_rect=new Rect(0,0,this.getWidth(),this.getHeight());
		}
		return _rect;
	}
}
