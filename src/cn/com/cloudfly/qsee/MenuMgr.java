package cn.com.cloudfly.qsee;

import android.view.Menu;
import android.view.MenuItem;
import cn.com.cloudfly.qsee.activity.ImageSwitcherActivity;
import cn.com.cloudfly.qsee.model.FolderModelManager;
import cn.com.cloudfly.qsee.utility.Utility;
import cn.com.cloudfly.qsee.utility.WapsUtility;

public class MenuMgr {

	static public void createMainViewMenu(Menu menu){
		menu.add(0,R.string.MENU_ONLINE_WALLPAPER,10,R.string.MENU_ONLINE_WALLPAPER).setIcon(android.R.drawable.ic_menu_slideshow);
		menu.add(0, R.string.MENU_REFRESH, 15, R.string.MENU_REFRESH).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, R.string.SELECT_CURRENT_DIR, 20, R.string.SELECT_CURRENT_DIR).setIcon(android.R.drawable.ic_menu_compass);

		menu.add(0, R.string.MENU_SETTINGS, 30, R.string.MENU_SETTINGS).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, R.string.MENU_SORT, 40, R.string.MENU_SORT).setIcon(android.R.drawable.ic_menu_sort_alphabetically);
		
		if (WapsUtility.isOn()){
			menu.add(0,R.string.MENU_RECOMMEND_APPS,50,R.string.MENU_RECOMMEND_APPS).setIcon(android.R.drawable.ic_menu_info_details);
		}
		menu.add(0, R.string.MENU_SHOW_HIDE_TAB, 60, R.string.MENU_SHOW_HIDE_TAB).setIcon(android.R.drawable.ic_menu_view);
		menu.add(1, R.string.MENU_CLEANUP, 70, R.string.MENU_CLEANUP).setIcon(android.R.drawable.ic_menu_search);
		
		
		menu.add(1, R.string.MENU_ABOUT, 80, R.string.MENU_ABOUT).setIcon(android.R.drawable.ic_menu_info_details);
		//menu.add(0,R.string.MENU_TAOBAO_STORE,90,R.string.MENU_TAOBAO_STORE).setIcon(android.R.drawable.btn_star_big_on);
		menu.add(0, R.string.MENU_EXIT, 100, R.string.MENU_EXIT).setIcon(android.R.drawable.ic_lock_power_off);
	}
	
	static public void prepareMainViewMenu(Menu menu){
		MenuItem menuShowSelectDirDialog=menu.findItem(7);
		MenuItem menuSort=menu.findItem(9);
		
		if (menuShowSelectDirDialog != null){
			if (FolderModelManager.getInstance().mode() == FolderModelManager.Mode.FILE_SYSTEM) {
				menuShowSelectDirDialog.setVisible(true);
				menuShowSelectDirDialog.setTitle(R.string.SELECT_CURRENT_DIR);
			} else {
				menuShowSelectDirDialog.setVisible(false);
			}
		}
		
		if (menuSort!=null){
			menuSort.setVisible(FolderModelManager.getInstance().mode() == FolderModelManager.Mode.FILE_SYSTEM);
		}
	}
	
	static public void createImageSwitcherMenu(Menu menu,ImageSwitcherActivity activity){
		menu.add(0,R.string.MENU_MAKE_IMAGE_PAD,0,R.string.MENU_MAKE_IMAGE_PAD).setIcon(android.R.drawable.ic_menu_gallery);
		menu.add(2,R.string.MENU_ROTATION_RIGHT,2,R.string.MENU_ROTATION_RIGHT).setIcon(android.R.drawable.ic_menu_rotate);
		
		menu.add(3,R.string.MENU_ZOOM,3,R.string.MENU_ZOOM).setIcon(android.R.drawable.ic_menu_zoom);
		if (FolderModelManager.getInstance().mode()==FolderModelManager.Mode.FAVORITE_SYSTEM){
			menu.add(4,R.string.MENU_DELETE,4,R.string.MENU_REMOVE).setIcon(android.R.drawable.ic_menu_delete);
		}else{
			menu.add(4,R.string.MENU_DELETE,4,R.string.MENU_DELETE).setIcon(android.R.drawable.ic_menu_delete);
		}
		menu.add(5,R.string.MENU_SET_AS_WALLPAPER,5,R.string.MENU_SET_AS_WALLPAPER).setIcon(android.R.drawable.  ic_menu_set_as);
		MenuItem menuItemAddFavorite=menu.add(6,R.string.MENU_ADD_TO_FAVORITE,6,R.string.MENU_ADD_TO_FAVORITE).setIcon(android.R.drawable.star_big_on);
		if (FolderModelManager.getInstance().mode()==FolderModelManager.Mode.FAVORITE_SYSTEM){
			menuItemAddFavorite.setEnabled(false);
		}else{
			menuItemAddFavorite.setEnabled(true);
			if (Utility.isPathUrl(activity.currentImageFileItem().getAbsolutePath())){
				menu.add(7,R.string.MENU_DOWNLOAD_WALLPAPER,7,R.string.MENU_DOWNLOAD_WALLPAPER).setIcon(android.R.drawable.stat_sys_download);
			}
		}
	}
	
	static public void prepareImageSwitcherMenu(Menu menu,ImageSwitcherActivity activity){
		MenuItem mnuItemRotation=menu.findItem(R.string.MENU_ROTATION_RIGHT);
		MenuItem mnuItemDelete=menu.findItem(R.string.MENU_DELETE);
		MenuItem mnuItemAddFavorite=menu.findItem(R.string.MENU_ADD_TO_FAVORITE);
		MenuItem mnuItemZoom=menu.findItem(R.string.MENU_ZOOM);
		
		
		if (Utility.isPathUrl(activity.currentImageFileItem().getAbsolutePath())){
			mnuItemRotation.setVisible(false);
			mnuItemDelete.setVisible(false);
			mnuItemAddFavorite.setVisible(false);
			mnuItemZoom.setVisible(false);
		}else{
			mnuItemRotation.setVisible(true);
			mnuItemDelete.setVisible(true);
			mnuItemAddFavorite.setVisible(true);	
			mnuItemZoom.setVisible(true);
		}
	
	}
	
	
	static public void createWallpaperExhibitionMenu(Menu menu){
		menu.add(1,R.string.MENU_DOWNLOAD_WALLPAPERS_ALL,1,R.string.MENU_DOWNLOAD_WALLPAPERS_ALL).setIcon(android.R.drawable.stat_sys_download);
		//menu.add(2,R.string.MENU_JUMP_TO_DOWNLOAD_DIR,2,R.string.MENU_JUMP_TO_DOWNLOAD_DIR).setIcon(android.R.drawable.ic_menu_directions);
	}
}
