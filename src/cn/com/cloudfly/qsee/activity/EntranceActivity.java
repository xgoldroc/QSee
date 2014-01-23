package cn.com.cloudfly.qsee.activity;

import android.app.Activity;
import android.os.Bundle;
import cn.com.cloudfly.qsee.model.FavoriteFolderModel;
import cn.com.cloudfly.qsee.utility.Clipboard;
import cn.com.cloudfly.qsee.utility.Utility;
import cn.com.cloudfly.qsee.utility.WapsUtility;

public class EntranceActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(this);
	}
	
	static public void initialize(Activity a){
		Utility.initializeApplicationContext(a);
		Clipboard.initialize(a);
		FavoriteFolderModel.getInstance(a);
		WapsUtility.initialize(a);
	}
}
