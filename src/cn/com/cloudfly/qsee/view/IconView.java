package cn.com.cloudfly.qsee.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import cn.com.cloudfly.qsee.model.FileItem;

public abstract class IconView extends AsyncImageView {

	public IconView(Context context,FileItem file) {
		super(context, file);
	}

	public void setText(String text){
		_text=text;
	}
	public String getText(){
		return _text;
	}
	
	protected float calcTextWidth(String txt,Paint p){
		float[] widths = new float[txt.length()];
		int count = p.getTextWidths(txt, 0, txt.length(), widths);
		float w=0;
		for (int i=0;i<count;++i){
			w+=widths[i];
		}
		return w;
	}
	
	@Override 
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint p=new Paint();
		p.setColor(Color.WHITE);
        p.setAntiAlias(true);

        float w=calcTextWidth(_text,p);
		if (w>this.getWidth()){
			float suffixWidth=calcTextWidth("...",p);
			int maxLength=calcMaxTextLength(_text,this.getWidth()-suffixWidth,p);
			canvas.drawText(_text.substring(0,maxLength)+"...", 0,this.getHeight()-4, p);
		}else{
			canvas.drawText(_text, (this.getWidth()-w)/2,this.getHeight()-4, p);
		}
	}

	private String _text;
	private int calcMaxTextLength(String txt,float width,Paint p){
		float[] measuredWidth=new float[txt.length()];;
		return p.breakText(txt, true, width, measuredWidth);
	}
}
