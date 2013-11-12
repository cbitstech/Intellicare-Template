package edu.northwestern.cbits.intellicare.relax;

import android.content.Context;
import android.widget.MediaController;

public class PersistentMediaController extends MediaController 
{
    public PersistentMediaController(Context context)
    {
      super(context);
    }

    public void hide()
    {
      // don't hide
    }
}
