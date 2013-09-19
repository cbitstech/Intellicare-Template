package edu.northwestern.cbits.intellicare;

import java.util.HashMap;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public abstract class SequentialPageActivity extends ConsentedActivity 
{
	private static final String INITIAL_PAGE = "INITIAL_PAGE";

	public abstract int pagesSequence();
	public abstract int titlesSequence();
	
	private int _pageIndex = 0;
	private String[] _urls = null;
	private String[] _titles = null;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_sequential);

		this._pageIndex = this.getIntent().getIntExtra(SequentialPageActivity.INITIAL_PAGE, 0);

		this._urls = this.getResources().getStringArray(this.pagesSequence());
		this._titles = this.getResources().getStringArray(this.titlesSequence());
	}
	
	public void onResume()
	{
		super.onResume();
		
		this.showPage();
	}
	
	public void showPage()
	{
		String url = this._urls[this._pageIndex];
		String title = this._titles[this._pageIndex];
		
		this.getSupportActionBar().setTitle(title);
		
		final SequentialPageActivity me = this;

		WebView webView = (WebView) this.findViewById(R.id.web_view);
		webView.loadUrl(url);
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		payload.put("page_url", url);
		payload.put("timestamp", Long.valueOf(System.currentTimeMillis()));
		
		LogManager.getInstance(this).log("page_displayed", payload);
		
		Button back = (Button) this.findViewById(R.id.back_button);
		
		if (this._pageIndex == 0)
			back.setVisibility(View.INVISIBLE);
		else
			back.setVisibility(View.VISIBLE);
		
		back.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				me._pageIndex -= 1;
				me.showPage();
			}
		});
		
		Button next = (Button) this.findViewById(R.id.next_button);

		if (this._pageIndex == this._urls.length - 1)
			next.setText(R.string.button_close);
		else
			next.setText(R.string.button_next);

		next.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				if (me._pageIndex < me._urls.length - 1)
				{
					me._pageIndex += 1;
					me.showPage();
				}
				else
				{
					me.onSequenceComplete();
					me.finish();
				}
			}
		});
	}

	public void onSequenceComplete()
	{
		// Placeholder for sequences with no completion action.
	}
}
