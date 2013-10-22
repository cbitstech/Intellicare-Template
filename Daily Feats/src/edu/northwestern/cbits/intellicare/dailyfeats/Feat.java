package edu.northwestern.cbits.intellicare.dailyfeats;

/**
 * Created by Gabe on 9/16/13.
 */
public class Feat {
    private int featLevel= 0;
    private boolean completed = false;
    private String featName;
    private String featLabel;

    public Feat(int level, String name, String label) {
        featLevel = level;
        featName  = name;
        featLabel = label;
    }

    public int getFeatLevel() {
        return featLevel;
    }

    public String getFeatName() {
        return featName;
    }

    public String getFeatLabel() {
        return featLabel;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean setCompletedTo(boolean b) {
        completed = b;
        return completed;
    }

    public boolean toggleComplete() {
        completed = !completed;
        return completed;
    }

}