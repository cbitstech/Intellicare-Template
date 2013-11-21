package edu.northwestern.cbits.intellicare.relax;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.MediaController;

public class PersistentMediaController extends MediaController 
{
    public PersistentMediaController(Context context)
    {
    	super(context);
    }
    
    public PersistentMediaController(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs);
    }   

    public PersistentMediaController(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void hide()
    {

    }

    public void superHide()
    {
    	super.hide();
    }

	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		{
			Context context = this.getContext();
			
			if (context instanceof Activity)
			{
				Activity activity = (Activity) context;
				
				activity.dispatchKeyEvent(event);
			}

			return false;
		}
		
		return super.dispatchKeyEvent(event);
	}
}
