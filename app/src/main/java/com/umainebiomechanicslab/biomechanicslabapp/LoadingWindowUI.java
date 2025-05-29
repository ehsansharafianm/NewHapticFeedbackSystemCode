package com.umainebiomechanicslab.biomechanicslabapp;

import android.app.Activity;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;

public class LoadingWindowUI extends UserInterface {

    // Declare UI Text Views
    private final TextView instructionsTextView;
    private final TextView insideSpinnerTextView;

    // Declare the LoadingPageListener interface
    public interface LoadingPageListener {
        void onLoadingPageFinished();
        void onLoadingPageCancelled();
    }

    // Declare the loadingPageListener variable
    private LoadingPageListener loadingPageListener;

    // Declare UI Progress Bar (Loading Spinner)
    private final ProgressBar loadingSpinner;

    // Declare handler and progress variables
    private final Handler handler;
    private Runnable progressRunnable;
    private int progress = 0;
    private static final int PROGRESS_MAX = 100;
    private static final int PROGRESS_INCREMENT = 1;
    private static final long PROGRESS_UPDATE_INTERVAL = 10; // 0.01 seconds (10 milliseconds)
    private static final long PROGRESS_RESET_DELAY = 1000; // 1 second (1000 milliseconds)

    public LoadingWindowUI(Activity activity, int pageID) {

        super(activity, pageID);

        // Declare UI Text Views
        instructionsTextView = activity.findViewById(R.id.loading_page_LoadingInstructions);
        insideSpinnerTextView = activity.findViewById(R.id.loading_page_LoadingText);

        // Declare UI Progress Bar
        loadingSpinner = activity.findViewById(R.id.loading_page_ProgressSpinner);

        //Initialize handler and spinner
        loadingSpinner.setMax(PROGRESS_MAX);
        handler = new Handler(Looper.getMainLooper());

        //Link the cancel button to the LoadingPageListener
        Button cancelButton = activity.findViewById(R.id.loading_page_CancelButton);

        //Set the response to the cancel button
        cancelButton.setOnClickListener(v -> {

            //Call the onLoadingPageCancelled method if loadingPageListener is not null
            if (loadingPageListener != null) {
                loadingPageListener.onLoadingPageCancelled();
            }
        });
    }

    public void startLoadingPage(String instructionsText, LoadingPageListener loadingPageListener) {

        //Set the loadingPageListener to the provided listener code
        this.loadingPageListener = loadingPageListener;

        //Update the instructions text view
        updateTextViewText(instructionsTextView, instructionsText);

        //Update the inside spinner text view
        updateTextViewText(insideSpinnerTextView, "Loading...");

        //Start the loading animation
        startProgressAnimation();

    }

    private void startProgressAnimation() {

        //Reset current progress
        progress = 0;
        loadingSpinner.setProgress(progress);

        //Runnable to update progress
        progressRunnable = new Runnable() {
            @Override
            public void run() {

                //Increment progress by PROGRESS_INCREMENT
                progress += PROGRESS_INCREMENT;
                loadingSpinner.setProgress(progress);

                if (progress >= PROGRESS_MAX) {
                    //Reset to 0
                    resetProgress();
                } else {
                    // Schedule the next update
                    handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
                }
            }
        };

        // Start the progress update loop
        handler.post(progressRunnable);
    }

    private void resetProgress() {
        //Stop current loop
        handler.removeCallbacks(progressRunnable);

        //Reset to 0 after delay
        handler.postDelayed(() -> {
            progress = 0;
            loadingSpinner.setProgress(progress);
            handler.post(progressRunnable);
        }, PROGRESS_RESET_DELAY);
    }

    public void onLoadingComplete(){

        //Stop any progress animations
        if(handler != null && progressRunnable != null){
            handler.removeCallbacks(progressRunnable);
        }

        //Call the onLoadingPageFinished method if loadingPageListener is not null
        loadingPageListener.onLoadingPageFinished();
    }

    public void updateInnerSpinnerText(String text){
        updateTextViewText(insideSpinnerTextView, text);
    }
}
