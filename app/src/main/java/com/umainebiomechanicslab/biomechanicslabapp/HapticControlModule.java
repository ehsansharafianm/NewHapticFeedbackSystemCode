package com.umainebiomechanicslab.biomechanicslabapp;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class HapticControlModule {

    int[] IPAddressBlocks = new int[4];

    public HapticControlModule(){

        //Set default IP Address Blocks to [0,0,0,0]
        Arrays.fill(IPAddressBlocks, 0);

    }

    public void sendHapticFeedback(String vibrationType, HttpRequestResponses responses){
        AsyncHttpRequestTask task = new AsyncHttpRequestTask(this.getIPAddress(), vibrationType, responses);
        Thread thread = new Thread(task);
        thread.start();
    }

    public void sendRepeatedHapticFeedback(HapticControlModule oppositeSideModule, String vibrationType, int cadence, HttpRequestResponses responses) {

        // Capture the start time using nanoTime for better precision
        long startTime = System.nanoTime();
        int intervalMillis = 60000 / cadence; // Compute time interval between requests
        final String TAG = "RepeatedHttpRequestTask";

        // Wrap the original response handler
        HttpRequestResponses firstRequestResponses = new HttpRequestResponses() {
            @Override
            public void onRequestSent() {
                responses.onRequestSent();
            }

            @Override
            public void onSuccessfulRequest() {
                responses.onSuccessfulRequest();

                // Calculate elapsed time and remaining delay using nanoTime
                long elapsedTime = System.nanoTime() - startTime;
                long elapsedMillis = elapsedTime / 1000000; // Convert nanoseconds to milliseconds
                long sleepTime = intervalMillis - elapsedMillis;

                if (sleepTime > 0) {
                    // If sleepTime is positive, wait the remaining time before sending the second request
                    new Thread(() -> {
                        try {
                            Thread.sleep(sleepTime);
                            oppositeSideModule.sendHapticFeedback(vibrationType, responses);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                } else {
                    // If sleepTime is negative, reset both devices and restart the vibration cycle
                    Log.e(TAG, "Warning: Negative sleep time detected (" + sleepTime + " ms). Resetting vibrations and retrying.");
                    stopRepeatedHapticFeedback(); // Stop vibrations on this device
                    oppositeSideModule.stopRepeatedHapticFeedback(); // Stop vibrations on the opposite device

                    // After reset, recursively restart the process from the beginning
                    new Thread(() -> {
                        try {
                            Thread.sleep(500); // Wait 500ms to align timing properly
                            sendRepeatedHapticFeedback(oppositeSideModule, vibrationType, cadence, responses); // Restart the process
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }
            }

            @Override
            public void onFailedRequest() {
                responses.onFailedRequest();
            }
        };

        // Send the first request and pass the wrapped response handler
        sendHapticFeedback(vibrationType, firstRequestResponses);
    }

    public void stopRepeatedHapticFeedback() {

        sendHapticFeedback("cycleStop", new HttpRequestResponses() {
            @Override
            public void onRequestSent() {}

            @Override
            public void onSuccessfulRequest() {}

            @Override
            public void onFailedRequest() {}
        });

    }

    public static int[] getIPBlocksFromString(String IPAddress) {
        String[] blocks = IPAddress.split("\\.");

        if (blocks.length != 4) {
            throw new IllegalArgumentException("Invalid IP address format.");
        }

        int[] ipBlocks = new int[4];
        for (int i = 0; i < 4; i++) {
            ipBlocks[i] = Integer.parseInt(blocks[i]);
        }

        return ipBlocks;
    }

    public void setIPBlock(int blockNumber, int IPBlock){
        IPAddressBlocks[blockNumber-1] = IPBlock;
    }

    public String getIPAddress(){
        return IPAddressBlocks[0] + "." + IPAddressBlocks[1] + "." +
                IPAddressBlocks[2] + "." + IPAddressBlocks[3];
    }

    private static class AsyncHttpRequestTask implements Runnable {
        private final String url;
        private final HttpRequestResponses responses;

        public AsyncHttpRequestTask(String ipAddress, String vibrationType, HttpRequestResponses responses) {
            url = "http://" + ipAddress + "/" + vibrationType;
            this.responses = responses;
        }

        @Override
        public void run() {
            try {
                responses.onRequestSent();
                URL url = new URL(this.url);

                Log.d("AsyncHttpRequestTask", "URL: " + url.toString());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                /*
                 * IMPORTANT:
                 * To make this HTTP Call work, you must ensure that android:usesCleartextTraffic="true" is included
                 * in the <Application> section of the AndroidManifest.xml file
                 * */

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //Request was successful
                    responses.onSuccessfulRequest();
                }
                else{
                    //Request was unsuccessful
                    responses.onFailedRequest();
                }
                connection.disconnect();
            } catch (IOException e) {
                //Request was unsuccessful
                responses.onFailedRequest();
                //e.printStackTrace(); // Add this line to print the stack trace
            }
        }
    }
}
