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

    //Setup Steps
    public static final int setupWelcome = 1;
    public static final int setupStep2 = 2;
    public static final int setupStep3 = 3;
    public static final int setupStep4 = 4;
    public static final int setupStep5 = 5;
    public static final int setupStep6 = 6;
    public static final int setupStep7 = 7;
    public static final int setupStep8 = 8;
    public static final int setupStep9 = 9;
    public static final int setupConclusion = 10;

    // DB-RELATED KEYS
    public static final String dbName = "daily_feats_database";
    public static final String checklistsTableName = "checklists";
    public static final String featResponsesTableName = "feat_responses";
    public static final String dateTakenKey = "date_taken_on";
    public static final String dateTimeTakenKey = "datetime_taken_at";
    public static final String featOfStrengthKey = "feat_of_strength";
    public static final String checklistIdKey = "checklist_id";
    public static final String featLevelKey = "level";
    public static final String featNameKey = "name";
    public static final String featCompletedKey = "completed";
    public static final String featDetailsKey = "details";

    public static final SimpleDateFormat iso8601Format     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat isoDateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat isoTimeOnlyFormat = new SimpleDateFormat("HH:mm:ss");

    public static final String gotOutOfBedKey = "out_of_bed";
    public static final String groomedMyselfKey = "groomed_myself";
    public static final String eatWellKey = "satisfactory_eating";
    public static final String wentOutKey = "went_outside";

    public static final String interactedKey = "interacted_with_others";
    public static final String livingSpaceKey = "living_space_care";
    public static final String restedKey = "felt_rested";

    public static final String accomplishedKey = "accomplished_thing";
    public static final String enjoyedKey = "enjoyed_activity";
    public static final String selfCareKey = "self_care";
    public static final String gratefulKey = "grateful";

    public static final String badHabitKey = "bad_habit_overcome";
    public static final String goodHabitKey = "good_habit_practiced";
    public static final String facedChallengeKey = "faced_challenge";
    public static final String avoidedChallengeKey = "avoided_challenge";

    public static final String DEPRESSION_LEVEL = "depression_level";
	public static final String REMINDER_HOUR = "preferred_hour";
	public static final String REMINDER_MINUTE = "preferred_minutes";
	public static final int DEFAULT_HOUR = 18;
	public static final int DEFAULT_MINUTE = 0;
	protected static final String SUPPORTERS = "supporters";

    // The AllFeats object that represents the collection of all possible
    // Items we can ask the user about.
    public static ArrayList<Feat> AllFeats = new ArrayList<Feat>();

    // shareFeatText maps a feat name, which serves as a unique identifier for a
    // type of feat to the appropriate string to display when sharing responses
    // with others.
    public static HashMap<String, String> shareFeatText = new HashMap<String, String>();

    static {
        AllFeats.add(0, (new Feat(1, gotOutOfBedKey,    "I got out of bed.")) );
        AllFeats.add(1, (new Feat(1, groomedMyselfKey,  "I groomed myself.")));
        AllFeats.add(2, (new Feat(1, eatWellKey,        "I am satisfied with how I ate.")));
        AllFeats.add(3, (new Feat(1, wentOutKey,        "I went outside.")));

        AllFeats.add(4, (new Feat(2, interactedKey,     "I interacted with people.")));
        AllFeats.add(5, (new Feat(2, livingSpaceKey,    "I took care of my living space.")));
        AllFeats.add(6, (new Feat(2, restedKey,         "I felt rested.")));

        AllFeats.add(7,  (new Feat(3, accomplishedKey,  "I accomplished something today.")));
        AllFeats.add(8,  (new Feat(3, enjoyedKey,       "I enjoyed an activity today.")));
        AllFeats.add(9, (new Feat(3,  selfCareKey,      "I took care of myself.")));
        AllFeats.add(10, (new Feat(3, gratefulKey,      "I was grateful for something.")));

        AllFeats.add(11, (new Feat(4, badHabitKey,          "I overcame a bad habit")));
        AllFeats.add(12, (new Feat(4, goodHabitKey,         "I strengthened a good habit")));
        AllFeats.add(13, (new Feat(4, facedChallengeKey,    "I faced something I wanted to avoid")));
        AllFeats.add(14, (new Feat(4, avoidedChallengeKey,  "I avoided something I wish I had faced")));

        shareFeatText.put(gotOutOfBedKey,       "Got out of bed.");
        shareFeatText.put(groomedMyselfKey,     "Prepared for the day.");
        shareFeatText.put(eatWellKey,           "Ate well.");
        shareFeatText.put(wentOutKey,           "Went outside.");

        shareFeatText.put(interactedKey,        "Interacted with other people.");
        shareFeatText.put(livingSpaceKey,       "Took care of where they live.");
        shareFeatText.put(restedKey,            "Felt Well Rested.");

        shareFeatText.put(accomplishedKey,      "Accomplished something.");
        shareFeatText.put(enjoyedKey,           "Enjoyed an activity.");
        shareFeatText.put(selfCareKey,          "Took care of themselves.");
        shareFeatText.put(gratefulKey,          "Was grateful.");

        shareFeatText.put(badHabitKey,          "Overcame a bad habit of theirs.");
        shareFeatText.put(goodHabitKey,         "Re-enforced a good habit.");
        shareFeatText.put(facedChallengeKey,    "Faced a challenge");
        shareFeatText.put(avoidedChallengeKey,  "Would feel better if they had faced a challenge they chose to avoid.");

    }

}