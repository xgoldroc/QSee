package cn.com.cloudfly.qsee.utility;

import android.content.Context;
import android.text.ClipboardManager;

public class Clipboard {
	static public void initialize(Context c){
		if (_clipboardManager==null){
			_clipboardManager=(ClipboardManager)c.getSystemService(Context.CLIPBOARD_SERVICE);
		}
	}

	static public void setText(String t){
		_clipboardManager.setText(t);
	}
	
	static public boolean hasText(){
		return _clipboardManager.hasText();
	}
	
	static public String getText(){
		try{
			return (String) _clipboardManager.getText();
		}catch(Exception e){
			return "";
		}
	}
	
	private static ClipboardManager _clipboardManager=null;
	
	public enum Operate {NONE,COPY,CUT};
	
	static public Operate currentOperate=Operate.NONE; 
}
