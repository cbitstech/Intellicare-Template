package edu.northwestern.cbits.intellicare.mantra.activities;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusImageCursor;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardGridFragment;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardManager;
import edu.northwestern.cbits.intellicare.mantra.FocusImage;
import edu.northwestern.cbits.intellicare.mantra.PictureUtils;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.Util;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

public class NoFragmentsHomeActivity extends ActionBarActivity {
	
	private static final String CN = "NoFragmentsHomeActivity";
	
	private final NoFragmentsHomeActivity self = this;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_fragments_home_activity);
		Log.d(CN+".onCreate", "entered");
//		findAndApplyFragment();
		
		
	}
	
	@Override
	protected void onResume() {
//		super.onCreate(savedInstanceState);
		super.onResume();
		setContentView(R.layout.no_fragments_home_activity);
		Log.d(CN+".onResume", "entered");
//		findAndApplyFragment();
		
		
		final GridView gv = (GridView) this.findViewById(R.id.gridview);
		FocusBoardCursor mantraItemCursor = FocusBoardManager.get(this).queryFocusBoards();
		Util.logCursor(mantraItemCursor);
		
		@SuppressWarnings("deprecation")
		CursorAdapter adapter = new CursorAdapter(this, mantraItemCursor) {

			@Override
			public void bindView(View mantraItemView, Context homeActivity, Cursor focusBoardCursor) {
				// set the image
				final int imageId = focusBoardCursor.getInt(focusBoardCursor.getColumnIndex("_id")); 
				Log.d(CN+".CursorAdapter.bindView", "imageId = " + imageId);
				final FocusImageCursor imageCursor = FocusBoardManager.get(homeActivity).queryFocusImages(imageId);
				Util.logCursor(imageCursor);
				// if the mantra item has an image, then display the first one
				if(imageCursor.getCount() > 0) {
					imageCursor.moveToFirst();
					FocusImage image = imageCursor.getFocusImage();
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
				Intent intent = new Intent(self, SoloFocusBoardActivity.class);
				intent.putExtra(NewFocusBoardActivity.FOCUS_BOARD_ID, id);
				
				Uri uri = getIntent().getData();
				if(uri != null) {
					Log.d(CN+".onItemClick", "uri.toString() = " + uri.toString());
					intent.setData(uri);
				}
				
				startActivity(intent);
			}
		});
		
		// DELETE action.
		gv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
//				Toast.makeText(self, "TODO: delete this mantra board", Toast.LENGTH_SHORT).show();
				
				// options
				final String[] optionItems = new String[] { "Edit", "Delete" };
				
				// create dialog for list of options
				AlertDialog.Builder dlg = new Builder(self);
				dlg.setTitle("Modify Mantra");
				dlg.setAdapter(
						new ArrayAdapter<String>(getApplicationContext(), R.layout.longpress_list_row, R.id.title, optionItems), 
						new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(self, "You chose " + optionItems[which] + "; which = " + which, Toast.LENGTH_SHORT).show();
						switch(which) {
							case 0:
								Log.d(CN+".onResume.setOnItemLongClickListener.onItemLongClick.setAdapter.onClick", "You chose " + optionItems[which]);
								break;
							case 1:
								Log.d(CN+".onResume.setOnItemLongClickListener.onItemLongClick.setAdapter.onClick", "You chose " + optionItems[which]);
								break;
						}
					}
					
					class ViewHolder {
						TextView title;
					}
					
					private View getView(int position, View convertView, ViewGroup parent) {
						Log.d(CN+".onResume.setOnItemLongClickListener.onItemLongClick.setAdapter.onClick", "entered");
						final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						ViewHolder holder = new ViewHolder();
						if(convertView == null) {
							convertView = inflater.inflate(R.layout.longpress_list_row, null);
							holder.title = (TextView) convertView.findViewById(R.id.title);
							convertView.setTag(holder);
						}
						else {
							holder = (ViewHolder) convertView.getTag();
						}
						holder.title.setText(optionItems[position]);
						return convertView;
					}
				});
				AlertDialog alert = dlg.create();
				alert.show();
				
				return false;
			}
		});
		

		// if this activity was opened by a response to the image gallery,
		// then inform the user they need to tap on a mantra with which they wish to associate an image.
		if(getIntent().getData() != null) {
			Toast.makeText(this, "Now tap on a mantra to attach your selected image to it!", Toast.LENGTH_LONG).show();
		}

	}

//	/**
//	 * 
//	 */
//	private void findAndApplyFragment() {
//		FragmentManager fm = getSupportFragmentManager();
//		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
//		
//		if (fragment == null) {
//			Log.d(CN+".findAndApplyFragment", "fragment != null");
//			fragment = new FocusBoardGridFragment();
//			fm.beginTransaction()
//				.add(R.id.fragmentContainer, fragment)
//				.commit();
//		}
//		else
//			Log.d(CN+".findAndApplyFragment", "fragment != null");
//	}
	
//	protected Fragment createFragment() {
//		return new FocusBoardGridFragment();
//	}
	
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
	        default:
	            return super.onOptionsItemSelected(item);
	    }
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
}
