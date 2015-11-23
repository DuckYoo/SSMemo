package app.ssm.duck.duckapp.File;

import android.graphics.drawable.Drawable;

public class IconfiedText implements Comparable<IconfiedText>{
	private String mText = "";
	private Drawable mIcon;
	private boolean mSelectable = true;
	
	public IconfiedText(String text, Drawable drawable) {
		mIcon = drawable;
		mText = text;
	}
	
	public boolean isSelectable()
	{
		return mSelectable;
	}
	
	public void setSelectable(boolean selectable) {
		mSelectable = selectable;
	}
	
	public String getText()
	{
		return mText;
	}
	
	public void setText(String text) {
		mText = text;
	}
	
	public void setIcon(Drawable icon) {
		mIcon = icon = icon;
	}
	
	public Drawable getIcon()
	{
		return mIcon;
	}

	@Override
	public int compareTo(IconfiedText another) {
		// TODO Auto-generated method stub
		if(this.mText != null)
		{
			return this.mText.compareTo(another.getText());
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
}
