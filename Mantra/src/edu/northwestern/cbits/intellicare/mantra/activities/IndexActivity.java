package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.File;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.MantraBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.MantraImageCursor;
import edu.northwestern.cbits.intellicare.mantra.EventLogging;
import edu.northwestern.cbits.intellicare.mantra.MantraBoard;
import edu.northwestern.cbits.intellicare.mantra.MantraBoardManager;
import edu.northwestern.cbits.intellicare.mantra.MantraImage;
import edu.northwestern.cbits.intellicare.mantra.NotificationAlarm;
import edu.northwestern.cbits.intellicare.mantra.OnboardingActivity;
import edu.northwestern.cbits.intellicare.mantra.PictureUtils;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.Util;
import edu.northwestern.cbits.intellicare.views.UriImageView;

/**
 * Home/Main activity. The entry-point from a user's perspective. Displays the first image for and handles the top-level behaviors for all the Mantra boards.
 * 
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
		setContentView(R.layout.index_activity);
		Log.d(CN + ".onCreate", "entered");

		MantraBoardCursor mantraItemCursor = MantraBoardManager.get(self)
				.queryMantraBoards();

		if (mantraItemCursor.getCount() == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(self.getString(R.string.title_create_board));
			builder.setMessage(self.getString(R.string.message_create_board));

			builder.setPositiveButton(self.getString(R.string.action_yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							EventLogging.log(self, "Creating a new Mantra board.", "onCreate.onClick", CN);
							IndexActivity.createNewMantra(self);
						}
					});

			builder.setNegativeButton(self.getString(R.string.action_no),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							EventLogging.log(self, "Not creating a new Mantra board.", "onCreate.onClick", CN);
						}
					});

			builder.create().show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		EventLogging.log(self, "entered...", "onResume", CN);
		
		CrashManager.register(this,
				self.getString(R.string.crash_manager_app_id),
				new CrashManagerListener() {
					public boolean shouldAutoUploadCrashes() {
						return true;
					}
				});

		Log.d(CN + ".onResume", "entered");

		// do onboarding, if it's the first time running this app
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean onboardingRunPreviously = p.getBoolean(
				OnboardingActivity.ONBOARDING_COMPLETED, false);
		if (!onboardingRunPreviously) {
			startOnboardingActivity();
		}

		// create, bind, and fill the main view for this activity
		attachGridView(self);

		// schedule the notifications, if not already done
		// src: http://stackoverflow.com/questions/4459058/alarm-manager-example
		Log.d(CN + ".onResume", "setting an alarm");
		NotificationAlarm na = new NotificationAlarm();
		na.SetAlarm(this);

		// if this activity was opened by a response to the image gallery,
		// then inform the user they need to tap on a mantra with which they
		// wish to associate an image.
		boolean isContentUri = false;
		if(getIntent() != null && getIntent().getData() != null) {
			isContentUri =getIntent().getData().toString().startsWith("content");
		}
		if (!displayedMantraAttachToast && isContentUri) {
			Log.d(CN+".onResume", "getIntent().getData() = " + getIntent().getData());
			Toast.makeText(this,
					self.getString(R.string.now_tap_on_a_mantra_to_attach_your_selected_image_to_it_),
					Toast.LENGTH_LONG).show();
			displayedMantraAttachToast = true;
		}
	}

	/**
	 * 
	 */
	public void startOnboardingActivity() {
		startActivity(new Intent(self, OnboardingActivity.class));
		EventLogging.log(this, "Started OnboardingActivity.", "startOnboardingActivity", CN);
	}

	/**
	 * Creates, binds to data, and fills the main view for this activity.
	 */
	private static void attachGridView(final Activity self) {
		self.setContentView(R.layout.index_activity);
		final GridView gv = (GridView) self.findViewById(R.id.gridview);

		MantraBoardCursor mantraItemCursor = MantraBoardManager.get(self)
				.queryMantraBoards();
//		Util.logCursor(mantraItemCursor);

		@SuppressWarnings("deprecation")
		CursorAdapter adapter = new CursorAdapter(self, mantraItemCursor) {

			@Override
			public void bindView(View mantraItemView, Context homeActivity,
					Cursor mantraBoardCursor) {
				// set the image
				final int imageId = mantraBoardCursor.getInt(mantraBoardCursor
						.getColumnIndex("_id"));
				Log.d(CN + ".CursorAdapter.bindView", "imageId = " + imageId);
				final MantraImageCursor imageCursor = MantraBoardManager.get(
						homeActivity).queryMantraImages(imageId);
				// if the mantra item has an image, then display the first one
				if (imageCursor.getCount() > 0) {
					imageCursor.moveToFirst();
					MantraImage image = imageCursor.getMantraImage();
					Log.d(CN + ".CursorAdapter.bindView", "image == null = "
							+ (image == null));
					UriImageView iv = (UriImageView) mantraItemView
							.findViewById(R.id.imageThumb);
					Log.d(CN + ".CursorAdapter.bindView", "image.getPath() = "
							+ image.getPath());

					Uri imageUri = Uri.fromFile(new File(image.getPath()));
					iv.setCachedImageUri(imageUri, -1, true);
				}

				// set the mantra
				TextView tv = (TextView) mantraItemView
						.findViewById(R.id.imageCaption);
				String mantraItemText = mantraBoardCursor
						.getString(mantraBoardCursor.getColumnIndex("mantra"));
				tv.setText(mantraItemText);
			}

			@Override
			public View newView(Context homeActivity, Cursor arg1,
					ViewGroup arg2) {
				LayoutInflater inflater = (LayoutInflater) homeActivity
						.getSystemService(homeActivity.LAYOUT_INFLATER_SERVICE);
				return inflater.inflate(R.layout.cell_image_item, arg2, false);
			}

		};
		gv.setAdapter(adapter);

		// OPEN action.
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(self,
						SingleMantraBoardActivity.class);
				intent.putExtra(SingleMantraBoardActivity.MANTRA_BOARD_ID, id);

				Uri uri = self.getIntent().getData();
				if (uri != null) {
					Log.d(CN + ".onItemClick",
							"uri.toString() = " + uri.toString());
					intent.setData(uri);
					self.setIntent(new Intent()); // wipe the URI-passing intent
													// that called this so we
													// don't tell the user to
													// attach the image to a
													// Mantra board later
				}

				self.startActivity(intent);
				EventLogging.log(self, "opening item (id = " + id + ").", "onItemClick", CN);
			}
		});

		// EDIT OR DELETE action.
		gv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent,
					final View view, int position, final long id) {
				// options
				final String[] optionItems = new String[] {
						self.getString(R.string.edit),
						self.getString(R.string.delete) };

				// create dialog for list of options
				AlertDialog.Builder dlg = new Builder(self);
				dlg.setTitle(self.getString(R.string.modify_mantra));
				dlg.setItems(optionItems, new OnClickListener() {

					// on user clicking the Edit or Delete option...
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// which option from the dialog menu did the user
						// select?
						switch (which) {
						case 0:
							Log.d(CN + ".onItemLongClick....onClick",
									"You chose " + optionItems[which]);

							((IndexActivity) self).editSelectedMantraCaption(self, id);
							EventLogging.log(self, "editing item (id = " + id + ").", "onItemLongClick.onClick", CN);
							break;

						case 1:
							Log.d(CN + ".onItemLongClick....onClick",
									"You chose " + optionItems[which]);

							AlertDialog.Builder dlg1 = new AlertDialog.Builder(self);
							dlg1.setTitle(self.getString(R.string.confirm_deletion));
							dlg1.setMessage(self.getString(R.string.are_you_sure_you_want_to_delete_this_mantra_));
							dlg1.setPositiveButton(self.getString(R.string.yes), new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											int rowsDeleted = MantraBoardManager
													.get(self)
													.deleteMantraBoard(id);
											((IndexActivity) self).attachGridView(self);
											String m = "deleted row = " + id + "; deleted row count = " + rowsDeleted;
											Log.d(CN + ".onItemLongClick.onClick", m);
											EventLogging.log(self, m, "onItemLongClick.onClick", CN);
										}
									});
							dlg1.setNegativeButton(R.string.no, new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									Log.d(CN + ".onItemLongClick....onClick",
											"not deleting " + id);
									EventLogging.log(self, "clicked No.", "onItemLongClick.onClick", CN);
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
		inflater.inflate(R.menu.index_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_new_focus_board:
				EventLogging.log(this, "Creating a new mantra.", "onOptionsItemSelected", CN);
				IndexActivity.createNewMantra(this);
				return true;
	
			case R.id.action_settings:
				EventLogging.log(this, "Opening settings page..", "onOptionsItemSelected", CN);
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				this.startActivity(settingsIntent);
				return true;
	
			case R.id.action_feedback:
				EventLogging.log(this, "Sending feedback..", "onOptionsItemSelected", CN);
				this.sendFeedback(this.getString(R.string.app_name));
				return true;
			
			case R.id.action_faq:
				EventLogging.log(this, "Opening the FAQ.", "onOptionsItemSelected", CN);
				this.showFaq(this.getString(R.string.app_name));
				return true;
	
			case R.id.action_help:
				startOnboardingActivity();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private static void createNewMantra(final Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		builder.setTitle(R.string.action_new_mantra_board);

		LayoutInflater inflater = LayoutInflater.from(activity);
		final AutoCompleteTextView addView = (AutoCompleteTextView) inflater
				.inflate(R.layout.view_add_mantra, null, false);

		String[] mantras = activity.getResources().getStringArray(
				R.array.sample_mantras);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_dropdown_item_1line, mantras);

		addView.setAdapter(adapter);

		builder.setView(addView);

		builder.setPositiveButton(R.string.action_new_mantra_board,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					EventLogging.log(activity, "Creating a new Mantra board.", "createNewMantra.onClick", CN);
					final Intent intent = new Intent(activity,
							SingleMantraBoardActivity.class);

					String mantra = addView.getText().toString().trim();

					MantraBoardManager boards = MantraBoardManager
							.get(activity);

					MantraBoard mantraBoard = boards
							.createMantraBoard(mantra);
					intent.putExtra(
							SingleMantraBoardActivity.MANTRA_BOARD_ID,
							mantraBoard.getId());

					// handle image-URI-passing intent from IndexActivity
					Intent intentFromIndexActivity = activity.getIntent();

					if (intentFromIndexActivity != null) {
						Uri uriFromImageBrowser = intentFromIndexActivity
								.getData();

						if (uriFromImageBrowser != null) {
							// get the URL returned by the image browser
							Log.d(CN + ".addSubmitListener", "uriFromImageBrowser = " + uriFromImageBrowser.toString());
							intent.setData(intentFromIndexActivity.getData());
							EventLogging.log(activity, "Received URI from image browser: " + uriFromImageBrowser.toString(), "createNewMantra.onClick", CN);
						}
					}

					activity.startActivity(intent);
				}
			});

		builder.setNegativeButton(R.string.action_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						EventLogging.log(activity, "Not creating a new Mantra board.", "createNewMantra.onClick", CN);
					}
				});

		builder.create().show();
	}

	/**
	 * @param id
	 */
	public static void editSelectedMantraCaption(final Activity self, final long id) {
		Log.d(CN + ".editSelectedMantraCaption", "entered");
		// get the current caption
		// v2: via database
		final View v = self.getLayoutInflater().inflate(
				R.layout.edit_text_field, null);
		MantraBoard fb = MantraBoardManager.get(self).getMantraBoard(id);
		((EditText) v.findViewById(R.id.text_dialog)).setText(fb.getMantra());

		AlertDialog.Builder editTextDlg = new AlertDialog.Builder(self);
		editTextDlg.setMessage(self.getString(R.string.edit_the_text));
		editTextDlg.setPositiveButton(self.getString(R.string.ok),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// update the selected mantra's text
						String newMantra = ((EditText) v
								.findViewById(R.id.text_dialog)).getText()
								.toString();
						MantraBoard fb = MantraBoardManager.get(self)
								.getMantraBoard(id);
						fb.setMantra(newMantra);
						long updateRet = MantraBoardManager.get(self)
								.setMantraBoard(fb);
						Log.d(CN + ".onItemLongClick....onClick",
								"updateRet = " + updateRet);
						attachGridView(self);
						EventLogging.log(self, "Edited a mantra caption (id=" + id + ").", "editSelectedMantraCaption.onClick", CN);
					}
				});

		editTextDlg.setView(v);
		AlertDialog dlg = editTextDlg.create();
		Log.d(CN + ".editSelectedMantraCaption", "showing dialog");
		dlg.show();
	}

	public static Uri activityUri(Context context) {
		return Uri.parse("intellicare://mantra/home");
	}
}
