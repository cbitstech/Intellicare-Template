package edu.northwestern.cbits.intellicare.mantra.activities;

import java.util.Date;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.mantra.NotificationAlarm;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusImageCursor;
import edu.northwestern.cbits.intellicare.mantra.MantraBoard;
import edu.northwestern.cbits.intellicare.mantra.MantraBoardGridFragment;
import edu.northwestern.cbits.intellicare.mantra.MantraBoardManager;
import edu.northwestern.cbits.intellicare.mantra.MantraImage;
import edu.northwestern.cbits.intellicare.mantra.PictureUtils;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

/**
 * Home/Main activity. The entry-point from a user's perspective.
 * @author mohrlab
 *
 */
public class IndexActivity extends ConsentedActivity {
	
	private static final String CN = "IndexActivity";
	
	private final IndexActivity self = this;
	
	private boolean displayedMantraAttachToast = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_fragments_home_activity);
		Log.d(CN+".onCreate", "entered");
		
		final IndexActivity me = this;
		
		FocusBoardCursor mantraItemCursor = MantraBoardManager.get(self).queryFocusBoards();
		
		if (mantraItemCursor.getCount() == 0)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_create_board);
			builder.setMessage(R.string.message_create_board);
			
			builder.setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					me.openNewFocusBoardActivity();
				}
			});
			
			builder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which) 
				{

				}
			});
			
			builder.create().show();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		CrashManager.register(this, "fcd8e6aab20e0e4ce94c0f86da7deb96", new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
				    return true;
			}
		});

		Log.d(CN+".onResume", "entered");
		
		// create, bind, and fill the main view for this activity
		attachGridView(self);

		// schedule the notifications, if not already done
		// src: http://stackoverflow.com/questions/4459058/alarm-manager-example
		Log.d(CN+".onResume","setting an alarm");
		NotificationAlarm na = new NotificationAlarm();
		na.SetAlarm(this);
		
		// DBG/TEST remove...
//		NotificationAlarm.dialogOnNewPhotos(this, NotificationAlarm.getCameraImagesSinceDate(this, new Date(System.currentTimeMillis() - 300 * 1000)));

		// if this activity was opened by a response to the image gallery,
		// then inform the user they need to tap on a mantra with which they wish to associate an image.
		if(!displayedMantraAttachToast && getIntent().getData() != null) {
			Toast.makeText(this, "Now tap on a mantra to attach your selected image to it!", Toast.LENGTH_LONG).show();
			displayedMantraAttachToast = true;
		}
	}

	/**
	 * Creates, binds to data, and fills the main view for this activity.
	 */
	private static void attachGridView(final Activity self) {
		self.setContentView(R.layout.no_fragments_home_activity);
		final GridView gv = (GridView) self.findViewById(R.id.gridview);

		FocusBoardCursor mantraItemCursor = MantraBoardManager.get(self).queryFocusBoards();
//		Util.logCursor(mantraItemCursor);
		
		@SuppressWarnings("deprecation")
		CursorAdapter adapter = new CursorAdapter(self, mantraItemCursor) {

			@Override
			public void bindView(View mantraItemView, Context homeActivity, Cursor focusBoardCursor) {
				// set the image
				final int imageId = focusBoardCursor.getInt(focusBoardCursor.getColumnIndex("_id")); 
				Log.d(CN+".CursorAdapter.bindView", "imageId = " + imageId);
				final FocusImageCursor imageCursor = MantraBoardManager.get(homeActivity).queryFocusImages(imageId);
//				Util.logCursor(imageCursor);
				// if the mantra item has an image, then display the first one
				if(imageCursor.getCount() > 0) {
					imageCursor.moveToFirst();
					MantraImage image = imageCursor.getFocusImage();
					Log.d(CN+".CursorAdapter.bindView", "image == null = " + (image == null));
					ImageView iv = (ImageView) mantraItemView.findViewById(R.id.imageThumb);
					Log.d(CN+".CursorAdapter.bindView", "image.getPath() = " + image.getPath());
					Drawable d = PictureUtils.getScaledDrawable(self, image.getPath());
					iv.setImageDrawable(d);
				}
				
				// set the mantra
				TextView tv = (TextView) mantraItemView.findViewById(R.id.imageCaption);
				String mantraItemText = focusBoardCursor.getString(focusBoardCursor.getColumnIndex("mantra"));
				tv.setText(mantraItemText);
			}

			@Override
			public View newView(Context homeActivity, Cursor arg1, ViewGroup arg2) {
				LayoutInflater inflater = (LayoutInflater) homeActivity.getSystemService(homeActivity.LAYOUT_INFLATER_SERVICE);
				return inflater.inflate(R.layout.cell_image_item, arg2, false);
			}
			
		};
		gv.setAdapter(adapter);
		
		// OPEN action.
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = new Intent(self, SingleMantraBoardActivity.class);
				intent.putExtra(NewFocusBoardActivity.FOCUS_BOARD_ID, id);
				
				Uri uri = self.getIntent().getData();
				if(uri != null) {
					Log.d(CN+".onItemClick", "uri.toString() = " + uri.toString());
					intent.setData(uri);
					self.setIntent(new Intent());	// wipe the URI-passing intent that called this so we don't tell the user to attach the image to a Mantra board later
				}
				
				self.startActivity(intent);
			}
		});
		
		// EDIT OR DELETE action.
		gv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, final long id) {
				// options
				final String[] optionItems = new String[] { "Edit", "Delete" };
				
				// create dialog for list of options
				AlertDialog.Builder dlg = new Builder(self);
				dlg.setTitle("Modify Mantra");
				dlg.setItems(optionItems, new OnClickListener() {
					
					// on user clicking the Edit or Delete option...
					@Override
					public void onClick(DialogInterface dialog, int which) {
//						Toast.makeText(self, "You chose " + optionItems[which] + "; which = " + which, Toast.LENGTH_SHORT).show();
						
						// which option from the dialog menu did the user select?
						switch(which) {
							case 0:
								Log.d(CN+".onItemLongClick....onClick", "You chose " + optionItems[which]);
								
								((IndexActivity) self).editSelectedMantraCaption(self, id);
								break;

							case 1:
								Log.d(CN+".onItemLongClick....onClick", "You chose " + optionItems[which]);
								
								AlertDialog.Builder dlg1 = new AlertDialog.Builder(self);
								dlg1.setTitle("Confirm deletion");
								dlg1.setMessage("Are you sure you want to delete this mantra?");
								dlg1.setPositiveButton("Yes", new OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										int rowsDeleted = MantraBoardManager.get(self).deleteFocusBoard(id);
										((IndexActivity) self).attachGridView(self);
										Log.d(CN+".onItemLongClick....onClick", "deleted row = " + id + "; deleted row count = " + rowsDeleted);
									}
								});
								dlg1.setNegativeButton("No", new OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Log.d(CN+".onItemLongClick....onClick", "not deleting " + id);
									}
								});
								dlg1.show();
								break;
						}
					}
				});
				dlg.create().show();
				
				return true;
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.home_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_new_focus_board:
	            openNewFocusBoardActivity();
	            return true;
	        
	        case R.id.action_settings:
	        	Intent settingsIntent = new Intent(this, SettingsActivity.class);
				this.startActivity(settingsIntent);
	            return true;
			case R.id.action_feedback:
				this.sendFeedback(this.getString(R.string.app_name));

				return true;
			case R.id.action_faq:
				this.showFaq(this.getString(R.string.app_name));
					
	            return true;
	    }

	    return super.onOptionsItemSelected(item);
	}
	
	private void openNewFocusBoardActivity() {
		Intent intent = new Intent(this, NewFocusBoardActivity.class);
		Intent intentFromSharedUrlActivity = getIntent();
		if(intentFromSharedUrlActivity != null) {
			Uri uriFromImageBrowser = intentFromSharedUrlActivity.getData();
			if(uriFromImageBrowser != null) {
				// get the URL returned by the image browser
				Log.d(CN+".openNewFocusBoardActivity", "uriFromImageBrowser = " + uriFromImageBrowser.toString());
				intent.setData(intentFromSharedUrlActivity.getData());
			}
		}
		startActivity(intent);
	}
	
	
	/**
	 * @param id
	 */
	public static void editSelectedMantraCaption(final Activity self, final long id) {
		Log.d(CN+".editSelectedMantraCaption", "entered");
		// get the current caption
		// v2: via database
		final View v = self.getLayoutInflater().inflate(R.layout.edit_text_field, null);
		MantraBoard fb = MantraBoardManager.get(self).getFocusBoard(id);
		((EditText) v.findViewById(R.id.text_dialog)).setText(fb.getMantra());

		AlertDialog.Builder editTextDlg = new AlertDialog.Builder(self);
		editTextDlg.setMessage("Edit the text");
		editTextDlg.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// update the selected mantra's text
//				Toast.makeText(self, "Mantra text should change.", Toast.LENGTH_SHORT).show();
				String newMantra = ((EditText) v.findViewById(R.id.text_dialog)).getText().toString();
				MantraBoard fb = MantraBoardManager.get(self).getFocusBoard(id);
				fb.setMantra(newMantra);
				long updateRet = MantraBoardManager.get(self).setFocusBoard(fb);
				Log.d(CN+".onItemLongClick....onClick", "updateRet = " + updateRet);
				attachGridView(self);
			}
		});

		editTextDlg.setView(v);
		AlertDialog dlg = editTextDlg.create();
		Log.d(CN+".editSelectedMantraCaption", "showing dialog");
		dlg.show();
	}
}
