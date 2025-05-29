package com.umainebiomechanicslab.biomechanicslabapp;

import android.app.Activity;
import android.widget.Button;

public class StartPageUI extends UserInterface {

    public StartPageUI(Activity activity, int pageID, UserInterface experimenterMenu, UserInterface participantMenu){

        super(activity, pageID);

        //Declare UI Objects
        Button forEperimenersButton = activity.findViewById(R.id.start_page_ForExperimentors);
        Button forParticipantsButton = activity.findViewById(R.id.start_page_ForParticipants);

        //Set Experimenter Menu Button Click Listener
        forEperimenersButton.setOnClickListener(view -> experimenterMenu.showPage());

        //Set Participant Menu Button Click Listener
        forParticipantsButton.setOnClickListener(view -> participantMenu.showPage());

    }

}
