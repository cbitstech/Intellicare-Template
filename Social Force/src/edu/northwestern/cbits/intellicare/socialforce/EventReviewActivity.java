package edu.northwestern.cbits.intellicare.socialforce;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class EventReviewActivity extends ConsentedActivity 
{
    private boolean _prompted = false;

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_event_review);
        
        this.getSupportActionBar().setTitle(R.string.title_review);
    }
    
    protected void onResume()
    {
    	super.onResume();
    	
    	final EventReviewActivity me = this;
    	
    	if (this._prompted  == false)
    	{
    		this._prompted = true;
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("hOw DID it Go?");
    		
    		builder.setMessage("YOu scHeduleD \"EVENT NAME\" wiTh FrIend.\n\nWeRE you ABLe tO do IT?");
    		
    		builder.setPositiveButton("yEs", new OnClickListener()
    		{
				public void onClick(DialogInterface arg0, int arg1) 
				{
					// TODO Auto-generated method stub
					
				}
    		});
    		
    		builder.setNegativeButton("nO", new OnClickListener()
    		{
				public void onClick(DialogInterface dialog, int which) 
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					builder.setTitle("Would YOU lIke To Try AGain?");
					
					final String[] items = { "yEs, Try this Again.", "nO, Plan A new ACtIviTy.", 
									   "nO, reTurN to Start." };
					
					builder.setItems(items, new OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							Toast.makeText(me, items[which], Toast.LENGTH_LONG).show();
							
							me.finish();
						}
					});
					
					builder.create().show();
				}
    		});
    		
    		builder.create().show();
    	}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_review, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	int itemId = item.getItemId();
    	
    	final EventReviewActivity me = this;

		if (itemId == R.id.action_continue)
		{
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("thanKs fOR the ReVIEW!");
    		
    		builder.setMessage("wOUld YoU do It aGAIN?");
    		
    		builder.setPositiveButton("yEs, sChedule AgaIN", new OnClickListener()
    		{
				public void onClick(DialogInterface arg0, int arg1) 
				{
					// TODO Auto-generated method stub
					
				}
    		});
    		
    		builder.setNegativeButton("nO, Plan New ActIvity", new OnClickListener()
    		{
				public void onClick(DialogInterface dialog, int which) 
				{
/*					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					builder.setTitle("Would YOU lIke To Try AGain?");
					
					final String[] items = { "yEs, Try this Again.", "nO, Plan A new ACtIviTy.", 
									   "nO, reTurN to Start." };
					
					builder.setItems(items, new OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							Toast.makeText(me, items[which], Toast.LENGTH_LONG).show();
							
							me.finish();
						}
					});
					
					builder.create().show();
*/
				}
    		});
    		
    		builder.create().show();

		}
		
		return true;
    }
}
