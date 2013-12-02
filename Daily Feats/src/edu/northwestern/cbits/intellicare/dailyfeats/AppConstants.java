package edu.northwestern.cbits.intellicare.dailyfeats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gabe on 9/19/13.
 */
public class AppConstants 
{
    // PREFERENCE KEYS
    public static final String currentStreakKey     = "current_streak";
    public static final String currentSetupKey      = "current_setup_step";

    public static final SimpleDateFormat iso8601Format     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat isoDateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat isoTimeOnlyFormat = new SimpleDateFormat("HH:mm:ss");

    
    public static final String DEPRESSION_LEVEL = "depression_level";
	public static final String REMINDER_HOUR = "preferred_hour";
	public static final String REMINDER_MINUTE = "preferred_minutes";
	public static final int DEFAULT_HOUR = 18;
	public static final int DEFAULT_MINUTE = 0;
	protected static final String SUPPORTERS = "supporters";

    // shareFeatText maps a feat name, which serves as a unique identifier for a
    // type of feat to the appropriate string to display when sharing responses
    // with others.
    public static HashMap<String, String> shareFeatText = new HashMap<String, String>();

}