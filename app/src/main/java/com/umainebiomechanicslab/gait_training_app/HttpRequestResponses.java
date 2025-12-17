package com.umainebiomechanicslab.gait_training_app;

/*
 * the requestResponses interface is a callback interface, where the actual actions that these functions
 * perform are coded at the time the sendIPRequest() method is called. This means the responses to each of
 * these things can be determined by the user. */
public interface HttpRequestResponses {

    void onRequestSent();

    void onSuccessfulRequest();

    void onFailedRequest();

}
