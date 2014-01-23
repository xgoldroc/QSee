package cn.com.cloudfly.qsee.view;


import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import cn.com.cloudfly.qsee.R;
import cn.com.cloudfly.qsee.utility.SerialImageLoadTask;
import cn.com.cloudfly.qsee.utility.Utility;


public abstract class ExhibitionViewSwitcher extends ViewSwitcher implements android.view.View.OnClickListener, android.view.View.OnTouchListener {
	private TranslateAnimation _leftOutAnim;
	private TranslateAnimation _leftInAnim;
	private TranslateAnimation _rightInAnim;
	private TranslateAnimation _rightOutAnim;


	public ExhibitionViewSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
		LayoutInflater mInflater;
		mInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		
		this.addView(mInflater.inflate(R.layout.exhibition, null));
		this.addView(mInflater.inflate(R.layout.exhibition, null));
		
		
		final int scrWidth=Utility.getScreenWidth();

		_leftOutAnim=new TranslateAnimation(0, -scrWidth,0, 0);
		_leftInAnim=new TranslateAnimation(scrWidth, 0,0, 0);
		 
		_rightOutAnim=new TranslateAnimation(0, scrWidth,0, 0);
		_rightInAnim=new TranslateAnimation(-scrWidth, 0,0, 0);
		
		setAnimationDuration(300);
    }
	
	private String _title=null;
	
    public void loadImageFiles(String[] files,String title){
    	_files=files;
    	_currentScreenIdx=0;
    	_title=title;
    	setViewData(0);
    	this.showNext();
    }
	
    public void showPreviousScreen() { 
        if (_currentScreenIdx > 0) { 
        	_currentScreenIdx--; 
    		if (getInAnimation()!=_rightInAnim){
    			setInAnimation(_rightInAnim);
    		}
    		if (getOutAnimation()!=_rightOutAnim){
    			setOutAnimation(_rightOutAnim); 
    		}
        } else { 
            return; 
        } 
         
        if (setViewData(_currentScreenIdx)){ 
        	showPrevious(); 
        }else{
        	_currentScreenIdx++;
        }
    }    

    public void showNextScreen() { 
       	_currentScreenIdx++; 
       	
		if (getInAnimation()!=_leftInAnim){
			setInAnimation(_leftInAnim);
		}
		if (getOutAnimation()!=_leftOutAnim){
			setOutAnimation(_leftOutAnim); 
		}
   	
         
        if (setViewData(_currentScreenIdx)){
        	showNext(); 
        }else{
        	_currentScreenIdx--;
        }
    } 
 
    public int currentPageIndex(){
    	return _currentScreenIdx;
    }

    public int totalPageCount(){
    	return (_files.length/IMAGE_COUNT)+ (_files.length % IMAGE_COUNT==0?0:1);
    }
 	
    public  void  setAnimationDuration(int d){
		_leftOutAnim.setDuration(d);
		_leftInAnim.setDuration(d);
		_rightOutAnim.setDuration(d);
		_rightInAnim.setDuration(d);
	}
	
    
    
    private String[] _files=null;
	private int _currentScreenIdx=0; 
    
    private String[]  getSectionFiles(int sectionIdx){
    	int idx=(IMAGE_COUNT*sectionIdx);
    	if (idx<0 || idx>=_files.length){
    		return null;
    	}else{
    		//ArrayList<String> horFs=new ArrayList<String>();
    		ArrayList<String> fs= new ArrayList<String>();
    		for (int i=0;i<IMAGE_COUNT && idx+i<_files.length;++i){
    			String fn=_files[idx+i];
//    			if (Utility.isBitmapHorizontal(fn) && horFs.size()<2){
//    				horFs.add(fn);
//    			}else{
        			fs.add(fn);
//    			}
    		}
    		
//    		if (horFs.size()>0){
//    			fs.add(2, horFs.get(0));
//    			if (horFs.size()>1){
//    				fs.add(4, horFs.get(1));
//    			}
//    		}
    		String[] rfs=new String[fs.size()];
    		fs.toArray(rfs);
    		return rfs;
    	}
    }

	private int getSizePixel(int idx){
		if (idx==2 || idx==4){
			return (Utility.getScreenWidth()/3-6)*2;
		}else{
			return Utility.getScreenWidth()/3-6;
		}
	}
 
	private final int IMAGE_COUNT=7;
	
	private ImageView getImageView(View v,int idx){
		switch (idx){
			case 0:
				return (ImageView) v.findViewById(R.id.exhibitionImage1);
			case 1:
				return (ImageView) v.findViewById(R.id.exhibitionImage2);
			case 2:
				return (ImageView) v.findViewById(R.id.exhibitionImage3);
			case 3:
				return (ImageView) v.findViewById(R.id.exhibitionImage4);
			case 4:
				return (ImageView) v.findViewById(R.id.exhibitionImage5);
			case 5:
				return (ImageView) v.findViewById(R.id.exhibitionImage6);
			case 6:
				return (ImageView) v.findViewById(R.id.exhibitionImage7);
			default:
				return null;
		}
		
	}
    private void setImageView(final View v,final int idx,String file){
    	SerialImageLoadTask task=new SerialImageLoadTask(getSizePixel(idx)){
			@Override
			public void onBitmapLoaded(Bitmap bmp) {
				if (bmp!=null){
					getImageView(v,idx).setImageBitmap(bmp);
				}
			}
    	};
    	task.execute(file);
	}

    
    private void clearImageViewItem(View v,int idx){ 
		int sz=getSizePixel(idx);
		Bitmap thumb=BitmapFactory.decodeResource(this.getResources(),R.drawable.empty);
		thumb=Utility.extractThumbnail(thumb, sz, sz);
		getImageView(v,idx).setImageBitmap(thumb);
	}
 
    private void clearImageView(View v){
    	for (int i=0; i<IMAGE_COUNT ; ++i){
    		clearImageViewItem(v,i);
    		getImageView(v,i).setOnClickListener(this);
    		getImageView(v,i).setOnTouchListener(this);
    	}
    }
    
    private boolean setViewData(int screenIdx) {
    	
    	View v=getNextView();
    	clearImageView(v);
    	
    	String[] files=getSectionFiles(screenIdx);
    	if (files==null){
    		return false;
    	}
    	
    	for(int i=0;i<files.length;++i){
    		setImageView(v,i,files[i]);
    	}

    	
    	TextView bottomView=(TextView) v.findViewById(R.id.exhibition_bottom);
    	bottomView.setText(String.format("%d/%d",currentPageIndex()+1,totalPageCount()));
    	TextView titleView=(TextView)v.findViewById(R.id.exhibition_title);
    	titleView.setText(_title);
    	
    	return true;
    }

    private int getIndexFromResId(int rId){
		switch (rId)
		{
			case R.id.exhibitionImage1:
				return 0;	
			case R.id.exhibitionImage2:
				return 1;	
			case R.id.exhibitionImage3:
				return 2;	
			case R.id.exhibitionImage4:
				return 3;	
			case R.id.exhibitionImage5:
				return 4;	
			case R.id.exhibitionImage6:
				return 5;	
			case R.id.exhibitionImage7:
				return 6;
			default:
				return -1;
		}
    }
    
	public void onClick(View v) {
		int id=getIndexFromResId(v.getId());
		if (id==-1)
			return;
			
		int idx=_currentScreenIdx*IMAGE_COUNT+id;
		if (idx<_files.length){
			onViewClicked(v,id,_files[idx]);
		}
	} 
	
	abstract public void onViewClicked(View v,int idx,String url);

	private boolean _touched=false;
	private float _downX=0;
	//private long _downTimeMillis=System.currentTimeMillis();
	
	private final int MIN_OFFEST=10;
//	private final int MIN_DBL_CLICK_INTERVAL=300;
//	private final int MIN_DBL_CLICK_RECT=20;
	
	public boolean onTouch(View v, MotionEvent event) {
		
		float x=event.getX();
		switch(event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			_touched=true;
			_downX=x;
			//_downTimeMillis=  System.currentTimeMillis()  ;
			break;
		case MotionEvent.ACTION_UP:
			if (_touched){
				_touched=false;
				float offset=x-_downX;
				if (offset==0 )
					break;
			
				//int duration=(int)(System.currentTimeMillis()-_downTimeMillis);
				//double unitDurcation=Math.abs(duration/offset)/2;
				if (offset<-MIN_OFFEST){
					this.showNextScreen();
					return true;
				}else if (offset>MIN_OFFEST){
					this.showPreviousScreen();	
					return true;
				}
			}

			
			break;
		case MotionEvent.ACTION_MOVE:
			//View imgView=_viewSwitcher.getCurrentView();
			//if (this._adapterMode){
			//	imgView.scrollTo((int) (_downX-x), 0);
			//}else{
			//	imgView.scrollBy((int) (_downX-x), 0);
			//	_downX=x;
			//}
			break;
		}
		
		return false;
	}
	
	
}
