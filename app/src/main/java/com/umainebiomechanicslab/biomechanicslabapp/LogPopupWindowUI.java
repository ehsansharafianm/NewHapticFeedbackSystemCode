package com.umainebiomechanicslab.biomechanicslabapp;

import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class LogPopupWindowUI extends UserInterface{

    private final String TAG = "LogPopupWindowUI";

    //Declare logContents TextView
    private final TextView logContents;

    public LogPopupWindowUI(Activity activity, int pageID) {
        super(activity, pageID);

        //Instantiate logContents TextView and set it to Scrolling
        logContents = activity.findViewById(R.id.log_popup_window_LogContents);
        logContents.setMovementMethod(new ScrollingMovementMethod());
    }

    public void appendToLogPopUpWindow(String logEntry) {

        //Append the logEntry to the Trial Log Pop-Up Text Box
        try{
            activity.runOnUiThread(() -> logContents.append("\n" + logEntry));
        } catch (IndexOutOfBoundsException e){
            Log.e(TAG, "appendToLogPopUpWindow", e);
            errorMessagePopUp("Error Appending Log");
        }
    }

    public void showLogPopUpWindow(boolean showWindow){
        updateLayoutVisibility(this.relativeLayout, showWindow);
    }
}
