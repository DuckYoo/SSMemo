package app.ssm.duck.duckapp.File;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IconfiedTextView extends LinearLayout {

	private TextView mText;
	private ImageView mIcon;
	
	public IconfiedTextView(Context context, IconfiedText iconfiedText) {
		super(context);
		// TODO Auto-generated constructor stub
		this.setOrientation(HORIZONTAL);
		
		mIcon = new ImageView(context);
		mIcon.setImageDrawable(iconfiedText.getIcon());
		
		mIcon.setPadding(0, 2, 5, 0);
		
		addView(mIcon, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	
		mText = new TextView(context);
		mText.setText(iconfiedText.getText());
		mText.setTextSize(20);
		addView(mText, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
	public void setText(String text) {
		mText.setText(text);
	}
	
	public void setIcon(Drawable drawable) {
		mIcon.setImageDrawable(drawable);
	}

}
