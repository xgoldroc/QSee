package cn.com.cloudfly.qsee.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class CommandReceiver extends BroadcastReceiver {
	public final static String ACTION_SEND_COMMAND="cn.com.cloudfly.qsee.intent.action.SEND_COMMAND";
	
	CommandReceiver(ICommandListener l){
		_listener=l;
	}
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		if (_listener!=null){
			final int cmdId=intent.getIntExtra("cmd",0);
			final String params=intent.getStringExtra("params");
			_listener.onCommand(cmdId, params);
		}
	}
	
	private ICommandListener _listener=null;
}
