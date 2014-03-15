package edu.northwestern.cbits.intellicare.ruminants;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;
import java.sql.Timestamp;
import java.util.ArrayList;


/**
 * Created by Gwen on 3/13/14.
 */
public class RuminationLogActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rumination_log);
    }

    public static class Entry {
        public String description;
        public int timestamp;

         public Entry(String description, int timestamp) {
         this.description = description;
         this.timestamp = timestamp;
        }
    }

    protected void onResume() {
        super.onResume();


        final RuminationLogActivity me = this;

        ArrayList<Entry> entries = new ArrayList<Entry>();

        final ListView concernList = (ListView) this.findViewById(R.id.concern_entries);
        final ListView triggerList = (ListView) this.findViewById(R.id.trigger_entries);

        Cursor c = me.getContentResolver().query(RuminantsContentProvider.PROFILE_URI, null, null, null, RuminantsContentProvider.PROFILE_ID);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(me, R.layout.row_concern, c, new String[0], new int[0]) {
            public void bindView(View view, Context context, Cursor cursor) {

                TextView rumDesc = (TextView) view.findViewById(R.id.concern_description);
                rumDesc.setText("Concern: " + cursor.getString(cursor.getColumnIndex(RuminantsContentProvider.PROFILE_RUMINATION_CONCERNS)));

                TextView timestamp = (TextView) view.findViewById(R.id.concern_date);
                Timestamp stamp = new Timestamp(cursor.getLong(cursor.getColumnIndex(RuminantsContentProvider.PROFILE_TIMESTAMP)));
                Date date = new Date(stamp.getTime());
                timestamp.setText(date.toString());

            }
        };

        concernList.setAdapter(adapter);

        Cursor k = me.getContentResolver().query(RuminantsContentProvider.WIZARD_ONE_URI, null, null, null, RuminantsContentProvider.WIZARD_ONE_ID);

        SimpleCursorAdapter wizard_adapter = new SimpleCursorAdapter(me, R.layout.row_trigger, k, new String[0], new int[0]) {

            public void bindView(View view, Context context, Cursor cursor) {

                TextView triggerDesc = (TextView) view.findViewById(R.id.trigger_description);
                triggerDesc.setText("Activity: " + cursor.getString(cursor.getColumnIndex(RuminantsContentProvider.WIZARD_ONE_TRIGGER)));

                TextView timestamp = (TextView) view.findViewById(R.id.trigger_date);

                Timestamp stamp = new Timestamp(cursor.getLong(cursor.getColumnIndex(RuminantsContentProvider.WIZARD_ONE_TIMESTAMP)));
                Date triggerDate = new Date(stamp.getTime());
                timestamp.setText(triggerDate.toString());

                }
            };
        triggerList.setAdapter(wizard_adapter);
        };

    }

