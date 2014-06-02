package edu.northwestern.cbits.intellicare.relax;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;

/**
 * Created by Gwen on 3/24/14.
 */
public class DeepBreathingActivity extends ActionBarActivity {

    // modal dialogue with instructions
    public void showInstructions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.dot_instructions);
        builder.setTitle(R.string.dot_instructions_title);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.dot_breathing_tool);

        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);

        WebView dotView = (WebView) this.findViewById(R.id.webView);

        dotView.getSettings().setJavaScriptEnabled(true);

        dotView.loadDataWithBaseURL("file:///android_asset/www/", "dot_tool.html", "text/html", "utf-8", null);
    }

}
