package com.umainebiomechanicslab.biomechanicslabapp;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPListenerThread extends Thread {

    public interface onUDPReceivedListener{
        void onUDPReceived(int block1, int block2, int block3);
    }

    private final String TAG = "UDPListenerThread";

    private final LoadingWindowUI loadingWindowUI;
    private final ExperimenterMenuUI experimenterMenuUI;
    private final TestHapticCellsUI testHapticCellsUI;
    private final UserInterfaceWithIMU userInterfaceWithIMU;

    private DatagramSocket socket;

    // Declare the listener variable
    private final onUDPReceivedListener listener;

    //Declare the boolean for whether the loading is canceled
    private boolean loadingCanceled;

    // Constructor to accept TrialManager object
    public UDPListenerThread(ExperimenterMenuUI experimenterMenuUI, TestHapticCellsUI testHapticCellsUI, LoadingWindowUI loadingWindowUI, onUDPReceivedListener listener) {

        this.experimenterMenuUI = experimenterMenuUI;
        this.testHapticCellsUI = testHapticCellsUI;
        this.loadingWindowUI = loadingWindowUI;
        this.userInterfaceWithIMU = null;

        this.listener = listener;

        //Initialize the loadingCanceled variable to false
        loadingCanceled = false;

    }

    public UDPListenerThread(ExperimenterMenuUI experimenterMenuUI, UserInterfaceWithIMU userInterfaceWithIMU, LoadingWindowUI loadingWindowUI, onUDPReceivedListener listener) {

        this.experimenterMenuUI = experimenterMenuUI;
        this.testHapticCellsUI = null;
        this.loadingWindowUI = loadingWindowUI;
        this.userInterfaceWithIMU = userInterfaceWithIMU;

        this.listener = listener;

        //Initialize the loadingCanceled variable to false
        loadingCanceled = false;

    }

    public void run() {

        //Show the loading page
        loadingWindowUI.showPage();

        loadingWindowUI.startLoadingPage("Turn On Haptic Control Module (If Previously Turned On, Turn Off Then Back On)", new LoadingWindowUI.LoadingPageListener() {
            @Override
            public void onLoadingPageFinished() {
                if(userInterfaceWithIMU != null){
                    userInterfaceWithIMU.showPage();
                }
                else if (testHapticCellsUI != null) {
                    testHapticCellsUI.showPage();
                }
            }

            @Override
            public void onLoadingPageCancelled() {
                experimenterMenuUI.showPage();

                stopUDPThread();
            }
        });


        try {
            socket = new DatagramSocket(5001);
            //DatagramSocket socket = new DatagramSocket(5001); // Listen on the same port as ESP8266
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (!loadingCanceled) {

                Log.d("UDPListener", "Scanning for ESP8266 IP...");

                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                if (message.startsWith("ESP8266_IP=")) {
                    String deviceIP = message.split("=")[1];

                    // Log the received IP
                    Log.d("UDPListener", "Received ESP8266 IP: " + deviceIP);

                    // Split the IP address into its 4 blocks and convert to integers
                    String[] ipParts = deviceIP.split("\\."); // Split by '.' to get the 4 parts
                    if (ipParts.length == 4) {
                        try {
                            int block1 = Integer.parseInt(ipParts[0]);
                            int block2 = Integer.parseInt(ipParts[1]);
                            int block3 = Integer.parseInt(ipParts[2]);

                            // Call the listener method with the IP blocks
                            listener.onUDPReceived(block1, block2, block3);

                            // Log the blocks as integers
                            Log.d("UDPListener", "IP Blocks: " + block1 + ", " + block2 + ", " + block3 + ", " + 255);

                            // Save the IP address blocks as integers or use as needed
                            // Example: SharedPreferences could store these blocks if desired

                        } catch (NumberFormatException e) {
                            Log.e("UDPListener", "Error parsing IP blocks", e);
                        }
                    }

                    socket.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listening for ESP8266 IP", e);
        }
    }

    public void stopUDPThread(){
        loadingCanceled = true;
        socket.close();
    }
}
