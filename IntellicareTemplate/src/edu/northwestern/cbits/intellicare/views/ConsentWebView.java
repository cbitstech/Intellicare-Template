package edu.northwestern.cbits.intellicare.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

// Cribbed from http://stackoverflow.com/a/16257865/193812

public class ConsentWebView extends WebView 
{
    public ConsentWebView(Context context) 
    {
        this(context, null);
    }

    public ConsentWebView(Context context, AttributeSet attrs) 
    {
        this(context, attrs, 0);
    }

    public ConsentWebView(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
    }

    public OnBottomReachedListener mOnBottomReachedListener = null;
    private int mMinDistance = 0;

    /**
     * Set the listener which will be called when the WebView is scrolled to within some
     * margin of the bottom.
     * @param bottomReachedListener
     * @param allowedDifference
     */
    public void setOnBottomReachedListener(OnBottomReachedListener bottomReachedListener, int allowedDifference ) 
    {
        mOnBottomReachedListener = bottomReachedListener;
        mMinDistance = allowedDifference;
    }

    /**
     * Implement this interface if you want to be notified when the WebView has scrolled to the bottom.
     */

    public interface OnBottomReachedListener 
    {
        void onBottomReached(View v);
    }

    @Override
    protected void onScrollChanged(int left, int top, int oldLeft, int oldTop) 
    {
        if (mOnBottomReachedListener != null) 
        {
            if ((getContentHeight() - (top + getHeight())) <= mMinDistance)
                mOnBottomReachedListener.onBottomReached(this);
        }

        super.onScrollChanged(left, top, oldLeft, oldTop);
    }
}
