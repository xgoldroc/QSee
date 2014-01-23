package cn.com.cloudfly.qsee.utility;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;

/**
 * 对javaTimer 的类QTimer 的封装,使用时实现timeout() 方法即可。
 *
 */
public abstract class CFTimer extends Handler {
	private long _inter_millisec=-1;
	public CFTimer(long inter_millisec) {
		super();
		_inter_millisec=inter_millisec;
	}

	public long getInterval(){
		return _inter_millisec;
	}
	/**
	 * 此方法在计时器时间到达时被调用。
	 */
	abstract public void timeout();
	
	public void cancel(){
		if (_timer!=null){
			_timer.cancel();
			_timer=null;
		}
	}
	public void start(){
		cancel();
		_timer=new Timer();
		_timer.schedule(new TimerTask(){
			@Override
			public void run() {
				//Log.d("HH","run->before send message");
				Message message = new Message();
				message.what=1;
				CFTimer.this.sendMessage(message);   
				//Log.d("HH","run->after send message");
			}
		}, _inter_millisec,_inter_millisec);
	}
	
	private Timer _timer=null;

	@Override
	public void handleMessage(Message msg) {
		if (msg.what==1){
			timeout();
		}
	}
}
