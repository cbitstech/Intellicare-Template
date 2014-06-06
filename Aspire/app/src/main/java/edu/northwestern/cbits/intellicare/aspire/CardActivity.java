package edu.northwestern.cbits.intellicare.aspire;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.HashMap;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class CardActivity extends ConsentedActivity
{
    private int _count = 0;
    private Menu _menu = null;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_select_virtues);

        this.getSupportActionBar().setTitle(R.string.title_card);
    }

    protected void onResume() {
        super.onResume();

        Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
        this._count = c.getCount();

        final String[] from = {AspireContentProvider.CARD_NAME, AspireContentProvider.CARD_DESCRIPTION};
        final int[] to = {R.id.virtue_name, R.id.virtue_description};

        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_virtue, c, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
            public void bindView (View view, final Context context, Cursor cursor){
                super.bindView(view, context, cursor);

                final SimpleCursorAdapter meAdapter = this;

                CheckBox check = (CheckBox) view.findViewById(R.id.selectVirtue);

                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    }
                });

                check.setChecked(cursor.getInt(cursor.getColumnIndex(AspireContentProvider.CARD_ENABLED)) != 0);

                final int cardId = cursor.getInt(cursor.getColumnIndex(AspireContentProvider.ID));

                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                        ContentValues values = new ContentValues();
                        values.put(AspireContentProvider.CARD_ENABLED, b);

                        String where = AspireContentProvider.ID + "= ?";
                        String[] args = {"" + cardId};

                        context.getContentResolver().update(AspireContentProvider.ASPIRE_CARD_URI, values, where, args);

                        Log.e("Aspire", "card id equals" + cardId + "checked equals" + b);

                        Cursor c = context.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);

                        Cursor old = meAdapter.swapCursor(c);
                        old.close();
                    }
                });

            }
        };

        ListView list = (ListView) this.findViewById(R.id.virtueList);

        list.setAdapter(adapter);

    };

    protected void onPause()
    {
        super.onPause();

        HashMap<String, Object> payload = new HashMap<String, Object>();
        LogManager.getInstance(this).log("closed_cards", payload);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.menu_card, menu);

        this._menu = menu;

        return true;
    }

    public void useCard(final long id, final String title, final String description)
    {
        final CardActivity me = this;

        // change to if checkbox checked, add virtue
        if (id != -1)
        {
            String where = AspireContentProvider.PATH_CARD_ID + " = ?";
            String[] whereArgs = { "" + id };

            Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_PATH_URI, null, where, whereArgs, AspireContentProvider.ID);

            if (c.getCount() == 0)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(R.string.title_first_path);

                LayoutInflater inflater = LayoutInflater.from(me);
                View view = inflater.inflate(R.layout.view_add_path, null, false);

                builder.setView(view);

                final EditText pathField = (EditText) view.findViewById(R.id.field_new_path);

                final long cardId = id;

                builder.setPositiveButton(R.string.action_add_path, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ContentValues values = new ContentValues();
                        values.put(AspireContentProvider.PATH_CARD_ID, cardId);
                        values.put(AspireContentProvider.PATH_PATH, pathField.getText().toString().trim());

                        me.getContentResolver().insert(AspireContentProvider.ASPIRE_PATH_URI, values);

                        HashMap<String, Object> payload = new HashMap<String, Object>();
                        payload.put("path", pathField.getText().toString().trim());
                        LogManager.getInstance(me).log("added_path", payload);

                        me.finish();
                    }
                });

                builder.create().show();
            }
            else
                this.finish();

            c.close();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();

        final CardActivity me = this;

        switch (itemId)
        {
            case R.id.action_add_card:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.title_add_card);

                LayoutInflater inflater = LayoutInflater.from(this);
                View view = inflater.inflate(R.layout.view_add_card, null, false);

                builder.setView(view);

                final EditText name = (EditText) view.findViewById(R.id.field_card_name);
                final EditText description = (EditText) view.findViewById(R.id.field_card_description);

                builder.setPositiveButton(R.string.action_add_card, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        ContentValues values = new ContentValues();
                        values.put(AspireContentProvider.CARD_NAME, name.getText().toString());
                        values.put(AspireContentProvider.CARD_DESCRIPTION, description.getText().toString());

                        me.getContentResolver().insert(AspireContentProvider.ASPIRE_CARD_URI, values);

                        Cursor c = me.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);

                        if (c.moveToLast())
                            me.useCard(c.getLong(c.getColumnIndex(AspireContentProvider.ID)), values.getAsString(AspireContentProvider.CARD_NAME), values.getAsString(AspireContentProvider.CARD_DESCRIPTION));

                        c.close();
                    }
                });

                builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface arg0, int arg1)
                    {

                    }
                });

                builder.create().show();

                break;

            case R.id.action_close:

                Intent htmlIntent = new Intent(me, MainActivity.class);
                me.startActivity(htmlIntent);
        }

        return true;
    }
}