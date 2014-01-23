package cn.com.cloudfly.qsee;


import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.cloudfly.qsee.activity.EntranceActivity;
import cn.com.cloudfly.qsee.activity.SettingsActivity;
import cn.com.cloudfly.qsee.activity.WallpaperExhibitionActivity;
import cn.com.cloudfly.qsee.adapter.AbstractFolderModelAdapter;
import cn.com.cloudfly.qsee.adapter.FilesFolderModelAdapter;
import cn.com.cloudfly.qsee.adapter.FoldersFolderModelAdapter;
import cn.com.cloudfly.qsee.model.AbstractFolderModel;
import cn.com.cloudfly.qsee.model.FavoriteFolderModel;
import cn.com.cloudfly.qsee.model.FileItem;
import cn.com.cloudfly.qsee.model.FolderModelManager;
import cn.com.cloudfly.qsee.model.HttpFileItem;
import cn.com.cloudfly.qsee.utility.AppOptions;
import cn.com.cloudfly.qsee.utility.Clipboard;
import cn.com.cloudfly.qsee.utility.Utility;
import cn.com.cloudfly.qsee.utility.Utility.SortPolicy;
import cn.com.cloudfly.qsee.utility.WapsUtility;
import cn.com.cloudfly.qsee.view.FolderView;
import cn.com.cloudfly.qsee.view.ImageFileView;

import com.waps.AdView;
import com.waps.AppConnect;

public class MainViewActivity extends TabActivity implements	MenuItem.OnMenuItemClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		//getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
		
		EntranceActivity.initialize(this);
		
		///FavoriteFolderModel.getInstance().load();

		TabHost tabHost = getTabHost();

		TabHost.TabSpec favoriteTabSpec = tabHost.newTabSpec("FAVORITE")
				.setIndicator(	getResources().getString(R.string.FAVORITE_FOLDER_NAME),
									getResources().getDrawable(android.R.drawable.star_big_on))
				.setContent(R.id.MainLayout);
		TabHost.TabSpec sdCardTabSpec = tabHost	.newTabSpec("SDCARD")
				.setIndicator(	getResources().getString(R.string.SDCARD_NAME),
									getResources().getDrawable(android.R.drawable.stat_notify_sdcard))
				.setContent(R.id.MainLayout)	;

		tabHost.addTab(sdCardTabSpec);
		tabHost.addTab(favoriteTabSpec);

		
		GridView imgsView = (GridView) this.findViewById(R.id.GridViewImgs);
//		Display screen = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//		int displayRotation = screen.getOrientation();
//		if (displayRotation == Surface.ROTATION_0	|| displayRotation == Surface.ROTATION_180) {
//			SettingsActivity.imageCellCount = 3;
//		} else {
//			SettingsActivity.imageCellCount = 5;
//		}
		imgsView.setVerticalSpacing(10);
		imgsView.setHorizontalSpacing(10);
		ImageFileView.verticalSpacing=10;

		//Utility.initializeUIContext(this);
		
		imgsView.setNumColumns(Utility.getImageCellCount());
		imgsView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View itemView,int position, long arg3) {
				ImageFileView v = (ImageFileView)itemView;// (av.getAdapter().getView(position, null, null));
				v.startViewActivity(MainViewActivity.this);
			}
		});
		
		imgsView.setOnItemLongClickListener(new OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> view, View itemView,int arg2, long arg3) {
				_currSelectView=(ImageFileView)itemView;
				
				return false;
			}
			
		});
		imgsView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu,	View gridView, ContextMenuInfo menuInfo) {
				_currSelectView.setupContextMenu(menu);
			}
		});

		imgsView.setFocusable(true);
		imgsView.setAdapter(new FilesFolderModelAdapter());
		
		getGalleryDirView();
		
		View tab1View=tabHost.getTabWidget().getChildTabViewAt(0);
		tab1View.setOnTouchListener(new View.OnTouchListener(){

			public boolean onTouch(View v, MotionEvent event) {
				if (FolderModelManager.getInstance().mode() == FolderModelManager.Mode.FILE_SYSTEM){
					//Toast.makeText(MainViewActivity.this, v.toString(), Toast.LENGTH_SHORT).show();
					showDialog(DIALOG_SELECT_DIR_ID);
				}
				return false;
			}
			
		});

		tabHost.setCurrentTab(0);
		
		if (!Utility.isMyPhone()){
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
	            public void uncaughtException(Thread thread, final Throwable ex) { 
	            	StringWriter sw = new StringWriter(); 
	                PrintWriter pw = new PrintWriter(sw); 
	                ex.printStackTrace(pw); 
	                 
	                String info=String.format("QSee version :%s\nAndroid version code :%d\nDevice :%s\n%s\n%s",
	                		Utility.getResourceString(cn.com.cloudfly.qsee.R.string.APP_VER),
	                		Build.VERSION.SDK_INT,
	                		Build.MODEL,
	                		Utility.getDeviceDetailInfo(),
	                		sw.toString()
	                		);

	                Utility.startSendReportEmailActivity(MainViewActivity.this,info);
	                finish(); 
	            } 		
			});
		}
		
		WapsUtility.initialize(this);
		LinearLayout container =(LinearLayout)findViewById(R.id.AdLinearLayout);
		new AdView(this,container).DisplayAd();
		//new MiniAdView(this, container).DisplayAd(10);
	}
	

	@Override
	protected void onDestroy() {
		WapsUtility.finialize();
		super.onDestroy();
	}


	ImageFileView _currSelectView=null;
	
	LinearLayout _dirViewLayout = null;
	TextView _textDirView=null;
	GridView _gridDirView = null;

	private View getGalleryDirView() {
		if (_dirViewLayout == null) {
			_dirViewLayout = new LinearLayout(this);
			_dirViewLayout.setOrientation(LinearLayout.VERTICAL);
			_textDirView=new TextView(this);
			
			_gridDirView = new GridView(this);
			_gridDirView.setLayoutParams(new GridView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			Display screen = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			_gridDirView.setNumColumns(screen.getWidth()/(FolderView.THUMB_SIZE + 10));
			_gridDirView.setScrollingCacheEnabled(true);
			_gridDirView.setVerticalSpacing(10);
			_gridDirView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> av, View arg1, int pos,long id) {
					AbstractFolderModelAdapter dirAdapter = (AbstractFolderModelAdapter) (av.getAdapter());
					FileItem file = (FileItem) dirAdapter.getItem(pos);
					changeCurrentPath(file.getAbsolutePath());
				}
			});
			_gridDirView.setFocusable(true);
			_gridDirView.setFocusableInTouchMode(true);
			
			_gridDirView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
						public void onCreateContextMenu(ContextMenu menu,	View v, ContextMenuInfo menuInfo) {
							//GridView dirView=(GridView)v;
							
							if (_currImgDirView!=null) {
								String sht=MainViewActivity.this.getResources().getString(R.string.FOLDER)+":"+ _currImgDirView.getDirPath();
								menu	.setHeaderTitle(sht);
							}
							menu.add(0, R.string.MENU_OPEN, 0, R.string.MENU_OPEN).setOnMenuItemClickListener(MainViewActivity.this);
							menu.add(1, R.string.MENU_RENAME, 1, R.string.MENU_RENAME)	.setOnMenuItemClickListener(MainViewActivity.this);
							menu.add(2, R.string.MENU_DELETE, 2, R.string.MENU_DELETE).setOnMenuItemClickListener(MainViewActivity.this);
							if (Clipboard.hasText()){
								final String clipFilePath = Clipboard.getText();
								if ( Utility.isImageFile(clipFilePath)) {
									String mt=MainViewActivity.this.getResources().getString(R.string.PASTE_IMAGE) 
													+new File(clipFilePath).getName() 
													+ MainViewActivity.this.getResources().getString(R.string.TO);
									menu.add(	3,R.string.PASTE_IMAGE_TO,3,mt)
											.setOnMenuItemClickListener(	MainViewActivity.this);
								}
							} else {
								menu.add(3, R.string.PASTE_IMAGE_TO, 3, R.string.PASTE_IMAGE_TO)
										.setOnMenuItemClickListener(MainViewActivity.this)
										.setEnabled(false);
							}
							menu.add(4,R.string.WALLPAPER_EXHIBITION_LOCAL,4,R.string.WALLPAPER_EXHIBITION_LOCAL).setOnMenuItemClickListener(MainViewActivity.this);
						}

					});
			
			_gridDirView.setOnItemLongClickListener(new OnItemLongClickListener() {
						public boolean onItemLongClick(AdapterView<?> g,View imgDirView, int pos, long id) {
							_currImgDirView = (FolderView) imgDirView;
							if (_currImgDirView.getDirPath().endsWith(".."))
								return true;
							imgDirView.requestFocus();
							return false;
						}

					});
			_gridDirView.setAdapter(new FoldersFolderModelAdapter());
			
			_dirViewLayout.addView(_textDirView);
			_dirViewLayout.addView(_gridDirView);

		}
		return _dirViewLayout;
	}

	AbstractFolderModelAdapter getGalleryDirViewAdapter() {
		return (AbstractFolderModelAdapter) _gridDirView.getAdapter();
	}

//	 @Override 
//	 public boolean onTouchEvent(MotionEvent event) {
//		 if (event.getAction()==MotionEvent.ACTION_DOWN){
//			 showDialog(DIALOG_SELECT_DIR_ID);
//		 }
//		 return super.onTouchEvent(event); 
//	}
	 
	private FolderView _currImgDirView = null;

	private String currentPath() {
		return FolderModelManager.getInstance().getCurrentModel().getPath();
	}

	private void refresh() {
		FolderModelManager.getInstance().getCurrentModel().invalidate();
		changeCurrentPath(currentPath());
	}

	public boolean onMenuItemClick(MenuItem item) {
		if (null == _currImgDirView) {
			return false;
		}

		final String imgDirPath = _currImgDirView.getDirPath();
		switch (item.getItemId()) {
		case R.string.MENU_OPEN: // open
			changeCurrentPath(imgDirPath);
			break;
		case R.string.MENU_RENAME: // rename
			final EditText newNameEdit = new EditText(this);
			final File dir = new File(imgDirPath);
			newNameEdit.setText(dir.getName());
			new AlertDialog.Builder(this)
					.setTitle(R.string.RENAME_FOLDER)
					.setMessage(R.string.INPUT_NEW_NAME)
					.setView(newNameEdit)
					//.setIcon(android.R.drawable.)
					.setPositiveButton(R.string.OK, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							File newDir = new File(currentPath() + "/"+ newNameEdit.getText());
							if (dir.renameTo(newDir)) {
								refresh();
							} else {
								Toast.makeText(MainViewActivity.this,R.string.ERROR_CAN_NOT_RENAME_FILE, Toast.LENGTH_SHORT).show();
							}
						}
					})
					.setNegativeButton(R.string.CANCEL, null)
					.show();
			break;
		case R.string.MENU_DELETE: // delete
			new AlertDialog.Builder(this)
					.setTitle(R.string.CONFIRM_DELETE)
					.setMessage(MainViewActivity.this.getResources().getString(R.string.DELETE_FOLDER) + imgDirPath + "?")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton(R.string.OK, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (new File(imgDirPath).delete()) {
								refresh();
							} else {
								String tt=MainViewActivity.this.getResources().getString(R.string.ERROR_CAN_NOT_DELETE)
											+ _currImgDirView.getText()
											+","
											+MainViewActivity.this.getResources().getString(R.string.ERROR_FOLDER_IS_NOT_EMPTY);
								Toast.makeText(MainViewActivity.this,tt,Toast.LENGTH_SHORT).show();
							}
						}
					})
					.setNegativeButton(R.string.CANCEL, null)
					.show();
			break;
		case R.string.PASTE_IMAGE_TO:// paste
			final String clipFilePath = Clipboard.getText();
			final String targetFilePath = imgDirPath + "/"+ new File(clipFilePath).getName();
			if (Clipboard.currentOperate.equals(Clipboard.Operate.COPY)|| Clipboard.currentOperate.equals(Clipboard.Operate.CUT)) {
				File srcFile = new File(clipFilePath);
				try {
					File targetFile = new File(targetFilePath);
					if (!targetFile.createNewFile()) {
						String tt=MainViewActivity.this.getResources().getString(R.string.ERROR_FAILED_CREATE_FILE)+ targetFilePath;
						Toast.makeText(MainViewActivity.this,tt , Toast.LENGTH_SHORT)	.show();
						break;
					}
					if (Utility.copyFile(srcFile, targetFile) <= 0) {
						String tt=MainViewActivity.this.getResources().getString(R.string.ERROR_FAILED_COPY_FILE)+ targetFilePath;
						Toast.makeText(MainViewActivity.this,tt, Toast.LENGTH_SHORT)	.show();
						break;
					}
					AbstractFolderModelAdapter dirAdapter = this.getGalleryDirViewAdapter();
					dirAdapter.invalidate();
					// ((GridView)this.getGalleryDirView()).setAdapter(dirAdapter);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				if (Clipboard.currentOperate == Clipboard.Operate.CUT) {
					srcFile.delete();
					refresh();
				}
			} else {
				Toast.makeText(MainViewActivity.this,"Error " + clipFilePath + "=" + targetFilePath,Toast.LENGTH_SHORT).show();
			}
			break;
		case R.string.WALLPAPER_EXHIBITION_LOCAL:
			WallpaperExhibitionActivity.startNewInstanceForLocal(this, imgDirPath,2000);
			break;
		}

		return true;

	}

	@Override
	protected void onStart() {
		super.onStart();

		String currPath = FolderModelManager.getInstance().load();
		changeCurrentPath(currPath);

		int pos =AppOptions.readViewCurrentPos();
		GridView imgsView = (GridView) this.findViewById(R.id.GridViewImgs);
		imgsView.setSelection(pos);
		
		if (FolderModelManager.getInstance().mode() == FolderModelManager.Mode.FILE_SYSTEM)
			getTabHost().setCurrentTab(0);
		else
			getTabHost().setCurrentTab(1);
		
		getTabHost().setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				MainViewActivity.this.switchMode();
			}
		});
	
	            
	
		getTabHost().getTabWidget().setVisibility(AppOptions.readTabsViewVisibility()?View.VISIBLE:View.GONE);
		//Toast.makeText(this, "Load done...", Toast.LENGTH_SHORT).show();
		AppOptions.setTopStatusBarHeightBy(this);
		
		if (SettingsActivity.isExhibitionNewEnabled(this)){
			String whCategory=WapsUtility.getWhCategroy();
			if (!AppOptions.getLastWallpaperExhibitionCategory().equals(whCategory)){
				Toast.makeText(this, "发现在线高清壁纸更新至 "+whCategory, Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onStop() {
		
		GridView imgsView = (GridView) this.findViewById(R.id.GridViewImgs);
		int pos = imgsView.getFirstVisiblePosition();

		FolderModelManager.getInstance().save();
		AppOptions.writeViewCurrentPos( pos);
		
		getTabHost().setOnTabChangedListener(null);
		
		super.onStop();
	}

	@Override
	protected void onResume() {
//		if (AppOptions.isFavoriteChanged()){
				FavoriteFolderModel.getInstance().invalidate();
//		}
		super.onResume();
	}

	private void changeCurrentPath(String path) {
		AbstractFolderModel folderModel = FolderModelManager.getInstance().getCurrentModel();
		if (folderModel == null || folderModel.getPath() != path) {
			FolderModelManager.getInstance().setCurrentPath(path);
			folderModel = FolderModelManager.getInstance().getCurrentModel();
		}

		AbstractFolderModelAdapter foldersAdapter = this	.getGalleryDirViewAdapter();
		foldersAdapter.setFolderModel(folderModel);
		
		_textDirView.setText(folderModel.detail());
		
		GridView imgsView = (GridView) this.findViewById(R.id.GridViewImgs);
		AbstractFolderModelAdapter filesAdapter = (AbstractFolderModelAdapter) imgsView.getAdapter();
		filesAdapter.setFolderModel(folderModel);

		if (SettingsActivity.hasHints(this)) {
			StringBuilder sb = new StringBuilder();
			if (filesAdapter.getCount() > 0) {
				sb.append(Utility.getResourceString(R.string.FIND));
				sb.append(" ");
				sb.append(filesAdapter.getCount());
				sb.append(" ");
				sb.append(Utility.getResourceString(R.string.MANY_IMAGE));
			}
			if (foldersAdapter.getCount() > 1) {
				if (sb.length() > 0) {
					sb.append(", ");
				} else {
					sb.append(Utility.getResourceString(R.string.FIND));
					sb.append(" ");
				}
				sb.append(foldersAdapter.getCount() - 1);
				sb.append(" ");
				sb.append(Utility.getResourceString(R.string.FOLDER));
			}
			if (sb.length() == 0) {
				sb.append(Utility.getResourceString(R.string.ERROR_NOT_FOUND_ANY_IMAGE));
			}
			Toast.makeText(MainViewActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
		}
		updateTitle();
	}

	private void updateTitle() {
		//String t=String.format("%s %s [ %s ]", this.getResources().getString(R.string.APP_NAME),Utility.isMyPhone()?"=":"-",new File(currentPath()).getName());
		AbstractFolderModel folderModel = FolderModelManager.getInstance().getCurrentModel();
		TextView titleTV = (TextView) findViewById(R.id.title);
		titleTV.setText(folderModel.title());

				
		int imgCount=FolderModelManager.getInstance().getCurrentModel().files().length;
		int folderCount=FolderModelManager.getInstance().getCurrentModel().subfolders().length-1;
		
		StringBuilder sb=new StringBuilder(); 
		if (imgCount>0){
			sb.append(Utility.getResourceString(R.string.IMAGE));
			sb.append(":");
			sb.append(imgCount);
		}
		sb.append(' ');
		if (folderCount>0){
			sb.append(Utility.getResourceString(R.string.SUBFOLDER));
			sb.append(":");
			sb.append(folderCount);
		}
		
		TextView titleRight = (TextView) findViewById(R.id.title_right);
		titleRight.setText(sb);
		sb=null;	
	}


	static final int DIALOG_ABOUT_ID = 0;
	static final int DIALOG_SELECT_DIR_ID = 1;
	static final int DIALOG_EXIT_CONFIRM_ID=2;
	static final int DIALOG_PHONE_INFO =3;
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_SELECT_DIR_ID:
			return new AlertDialog.Builder(MainViewActivity.this)
					.setTitle(R.string.SELECT_CURRENT_DIR)
					.setView(getGalleryDirView())
					//.setCustomTitle(_textDirView)
					.setPositiveButton(R.string.QUICK_JUMP,new OnClickListener(){
						public void onClick(final DialogInterface chgPathDialog, int arg1) {
							new AlertDialog.Builder(MainViewActivity.this)
							.setTitle(R.string.QUICK_JUMP)
							.setIcon(android.R.drawable.ic_menu_directions)
							.setOnCancelListener(new OnCancelListener(){
								public void onCancel(DialogInterface dialog) {
									((Dialog) chgPathDialog).show();
								}
							})
							.setItems(R.array.go_to, new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog, int which) {
									switch (which){
									case 0://to SDcard
											MainViewActivity.this.changeCurrentPath(Utility.getDefaultDirPath());
										break;
									case 1://to DCIM
										String path=Utility.getDefaultDirPath()+"/DCIM";
										if (new File(path).exists()){
											MainViewActivity.this.changeCurrentPath(path);
										}else{
											Toast.makeText(MainViewActivity.this, R.string.ERROR_NOT_FOUND_ANY_IMAGE,Toast.LENGTH_LONG).show();
										}
										break;
									case 2://to QSee/Exhibitions
										MainViewActivity.this.changeCurrentPath(HttpFileItem.getLocalDownloadDir());
										break;
									};
									dialog.dismiss();
									((Dialog) chgPathDialog).show();
								}
									
							})
							.show();
						}
					})
					//.setNeutralButton(text, listener)
					.setNegativeButton(R.string.OK,null)
					
					.create();

		case DIALOG_EXIT_CONFIRM_ID:
			return new AlertDialog.Builder(MainViewActivity.this)
				.setTitle(R.string.CONFIRM_EXIT)
				.setMessage(R.string.EXIT_CONFIRM_INFO)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.OK,  new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						MainViewActivity.this.finish();
						MainViewActivity.this.onStop();
						System.exit(0);
					}
				} )
				.setNegativeButton(R.string.CANCEL, null)
				.create();
		case DIALOG_PHONE_INFO:
			return new AlertDialog.Builder(MainViewActivity.this)
						.setTitle("设备信息")
						.setMessage(Utility.getDeviceDetailInfo())
						.setPositiveButton(R.string.CLOSE,null)
						.create();
		default:
			break;
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuMgr.createMainViewMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuMgr.prepareMainViewMenu(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	public Handler _messageHandler=new Handler(new Handler.Callback() {
		@SuppressWarnings("unchecked")
		public boolean handleMessage(Message msg) {
				_deleteFileItems=(List<Pair<FileItem, FileItem>>) msg.obj;
				cleanDuplicatedFile();
				return true;
		}
	});
	
	
		
	private void onMenuAboutClicked() {
		AlertDialog d = new AlertDialog.Builder(MainViewActivity.this)
				.setPositiveButton(R.string.OK, null)
				.setNeutralButton(R.string.USER_FEEDBACK,new OnClickListener() {
							public void onClick(DialogInterface dialog,int which) {
								AppConnect.getInstance(MainViewActivity.this)	.showFeedback();
							}
						})
				.setMessage(	Html.fromHtml(Utility.getResourceString(R.string.APP_ABOUT_INFO)))
				.create();
		d.show();
		TextView msgTextView = ((TextView) d.findViewById(android.R.id.message));
		MovementMethod lmm = LinkMovementMethod.getInstance();
		msgTextView.setMovementMethod(lmm);
	}
	
	private void onMenuShowHideTabClicked(){
		View tabsView = getTabHost().getTabWidget();//this.findViewById(android.R.id.tabs);
		if (tabsView.getVisibility() == View.GONE) {
			tabsView.setVisibility(View.VISIBLE);
			AppOptions.writeTabsViewVisibility(true);
		} else {
			tabsView.setVisibility(View.GONE);
			AppOptions.writeTabsViewVisibility(false);
		}
	}
	
	private void onMenuSelectCurrentDir(){
		if (FolderModelManager.getInstance().mode() == FolderModelManager.Mode.FILE_SYSTEM){
			showDialog(DIALOG_SELECT_DIR_ID);
		}else{
			showDialog(DIALOG_PHONE_INFO);
		}
	}
	
	private void onMenuSortClicked(){
		new AlertDialog.Builder(this)
		.setTitle(R.string.MENU_SORT)
		.setIcon(android.R.drawable.ic_menu_sort_alphabetically)
		.setSingleChoiceItems(R.array.sort_policy, AppOptions.readCurrentSortPolicy().ordinal(),new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case 0:
					AppOptions.writeCurrentSortPolicy(SortPolicy.Name);
					break;
				case 1:
					AppOptions.writeCurrentSortPolicy(SortPolicy.ReverseName);
					break;
				case 2:
					AppOptions.writeCurrentSortPolicy(SortPolicy.Time);
					break;
				case 3:
					AppOptions.writeCurrentSortPolicy(SortPolicy.ReverseTime);
					break;
				case 4:
					AppOptions.writeCurrentSortPolicy(SortPolicy.Size);
					break;
				case 5:
					AppOptions.writeCurrentSortPolicy(SortPolicy.ReverseSize);
					break;
				default:
					AppOptions.writeCurrentSortPolicy(SortPolicy.ReverseTime);
				};
				dialog.dismiss();
				refresh();
			}
				
		})
		.show();
	}
	
	private void onMenuOnlinWallpaperClicked(){
		if (!Utility.isNetworkAvailable()){
			Toast.makeText(this, R.string.ERROR_NETWORK, Toast.LENGTH_LONG).show();
			return;
		}
		
		if ( !WapsUtility.isOn()){
			Toast.makeText(this, R.string.ERROR_EXHIBITION, Toast.LENGTH_LONG).show();
			return;
		}
		
		if (!WapsUtility.isWallpaperExhibitionEnabled()){
			WapsUtility.showWallpaperExhibitionDisabledDialog();
			return;
		}
		
		WapsUtility.startWallpaperExhibition(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.string.MENU_SETTINGS:
			SettingsActivity.startNewInstance(this);
			break;
		case R.string.MENU_REFRESH:
			this.refresh();
			break;
		case R.string.MENU_ABOUT:
			this.onMenuAboutClicked();
			break;
		case R.string.MENU_EXIT:
			exit();
			break;
		case R.string.MENU_SHOW_HIDE_TAB:
			this.onMenuShowHideTabClicked();
			break;
		case R.string.SELECT_CURRENT_DIR:
			this.onMenuSelectCurrentDir();
			break;
		case R.string.MENU_CLEANUP://clean up
			FolderModelManager.getInstance().getCurrentModel().cleanup(this,_messageHandler);
			break;
		case R.string.MENU_SORT:
			this.onMenuSortClicked();
			break;	
		case R.string.MENU_RECOMMEND_APPS:
			WapsUtility.startOffersActivity();
			break;
		case R.string.MENU_ONLINE_WALLPAPER:
			onMenuOnlinWallpaperClicked();
			break;
		case R.string.MENU_TAOBAO_STORE:
			Utility.startWebBrowserActivity(this,Utility.getResourceString(R.string.MENU_TAOBAO_STORE_URL));
			break;
		}
			
			
		return true;
	}

	
	
	
	void cleanDuplicatedFile(){
		if (_deleteFileItems!=null ){
			if (this._deleteFileItems.size()>0){
				String info=String.format(Utility.getResourceString(R.string.FOUND_DUPLICATED_IMAGES_DETAIL), this._deleteFileItems.size())+"，是否进行清理？";
				new AlertDialog.Builder(this)
				.setMessage(info)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.CLEANUP,new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						deleteFirstDuplicatedFile();	
					}
				})
				//.setNeutralButton(text, listener)
				.setNegativeButton(R.string.SKIP, null)
				.show();
			}else{
				Toast.makeText(this, R.string.NOT_FOUND_DUPLICATED_IMAGES, Toast.LENGTH_SHORT).show();	
			}
		}
	}
	
	List<Pair<FileItem,FileItem> > _deleteFileItems=null;
	private void deleteFirstDuplicatedFile(){
		if (_deleteFileItems.isEmpty()){
			refresh();
			return;
		}
		
		final Pair<FileItem,FileItem> pair =_deleteFileItems.get(0);
		_deleteFileItems.remove(0);
		ImageView v1=Utility.createImageView(this,pair.first.getAbsolutePath(), 100, 100);
		ImageView v2=Utility.createImageView(this,pair.second.getAbsolutePath(), 100, 100);
//		
		LinearLayout l=new LinearLayout(Utility.getApplicationCtx());
		l.addView(v1);
		TextView vs=new TextView(this);
		vs.setText("  VS  ");
		l.addView(vs);
		l.addView(v2);
		
		LinearLayout.LayoutParams layout=new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		l.setLayoutParams(layout);
		l.setGravity(Gravity.CENTER);
		new AlertDialog.Builder(this)
		.setMessage(R.string.FOUND_DUPLICATED_IMAGES)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setView(l)
		.setPositiveButton(R.string.DELETE_ONE,  new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				pair.second.delete();
				if (!_deleteFileItems.isEmpty()){
					deleteFirstDuplicatedFile();
				}
			}
		} )
		.setNeutralButton(R.string.SKIP, new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				deleteFirstDuplicatedFile();
			}
		} )
		.setNegativeButton(R.string.CANCEL,new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				refresh();				
			}
		}) 
		.show();
		
	}

	private void switchMode() {
		FolderModelManager.getInstance().switchMode();
		changeCurrentPath(FolderModelManager.getInstance().getCurrentModel().getPath());
	}

	private void exit(){
		this.showDialog(DIALOG_EXIT_CONFIRM_ID);
	}
	
	boolean cdUp() {
		if (currentPath().equalsIgnoreCase(Utility.getDefaultDirPath())){
			return false;
		}
		
		File parentFile = new File(currentPath()).getParentFile();
		if (parentFile != null) {
			changeCurrentPath(parentFile.getAbsolutePath());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (cdUp()){
				Toast.makeText(this, R.string.BACK_TO_PARENT_DIR, Toast.LENGTH_SHORT).show();
			}else{
				exit();
			}
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==1){
			if (resultCode>0){
				this.refresh();	
			}
		}else if (requestCode==2){
			if (resultCode>0){
				String path=null;
				if (data!=null){
					path=data.getExtras().getString("path");
					this.changeCurrentPath(path);
					if (!path.equals(this.currentPath())){
						Toast.makeText(MainViewActivity.this, this.currentPath(),Toast.LENGTH_LONG).show();
					}
				}else{
					this.changeCurrentPath(HttpFileItem.getLocalDownloadDir());
				}
				FolderModelManager.getInstance().save();
			}
		}
	}
}