package edu.northwestern.cbits.intellicare;

import java.lang.reflect.Field;
import java.util.ArrayList;

import edu.northwestern.cbits.ic_template.R;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DrawablesActivity extends ActionBarActivity 
{
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_drawables);
        this.getSupportActionBar().setTitle(R.string.title_drawables_activity);
    }
	
	protected void onResume()
	{
		super.onResume();
		
    	ListView list = (ListView) this.findViewById(R.id.list_view);

    	final DrawablesActivity me = this;
    	
    	final ArrayList<Field> drawables = new ArrayList<Field>();
    	
    	Field[] drawableFields = R.drawable.class.getFields();

    	for (Field f : drawableFields)
    	{
    		if (f.getName().startsWith("ic_action_"))
    			drawables.add(f);
    	}
    	
    	list.setAdapter(new ArrayAdapter<Field>(this, R.layout.layout_drawable_row, drawables)
		{
    		public View getView (int position, View convertView, ViewGroup parent)
    		{
    			if (convertView == null)
    			{
    				LayoutInflater inflater = LayoutInflater.from(me);
    				convertView = inflater.inflate(R.layout.layout_drawable_row, parent, false);
    			}
    			
    			Field f = drawables.get(position);
    			
    			TextView drawableView = (TextView) convertView.findViewById(R.id.label_field);
    			drawableView.setText("R.drawable." + f.getName());
    			drawableView.setCompoundDrawablePadding(10);
    			
    			try 
    			{
					drawableView.setCompoundDrawablesWithIntrinsicBounds(((Integer) f.get(null)).intValue(), 0, 0, 0);
				} 
    			catch (IllegalArgumentException e) 
    			{
					drawableView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
					e.printStackTrace();
				} 
    			catch (IllegalAccessException e) 
    			{
					drawableView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
					e.printStackTrace();
				}

    			return convertView;
    		}
    	});
	}
}
