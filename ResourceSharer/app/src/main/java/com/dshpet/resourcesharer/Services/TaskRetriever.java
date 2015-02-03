package com.dshpet.resourcesharer.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class TaskRetriever extends Service {
    public TaskRetriever() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String res = getTasksFromServer();
        Log.d("TaskRetriever", res);
        return Service.START_NOT_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("TaskRetriever", "Binded successfully");
        return new Binder();
    }

    public String getTasksFromServer() {
        //getting my host url
        URL host = null;
        URL tasksLocation = null;
        try {
            host = new URL("https://resource-sharer-server.herokuapp.com");
            tasksLocation = new URL(host + "/tasks");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //hoping that everything is fine
        assert tasksLocation != null;
        HttpsURLConnection taskLocationConnection = null;
        try {
            taskLocationConnection = (HttpsURLConnection) tasksLocation.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final HttpURLConnection finalTaskLocationConnection = taskLocationConnection;
        final URL finalHost = host;
        final StringBuilder out = new StringBuilder();
        Thread bdRetriever = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //have connection with server
                    try {
                        assert finalTaskLocationConnection != null;
                        InputStream in = new BufferedInputStream(finalTaskLocationConnection.getInputStream());

                        //converting stream into data
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            out.append(line);
                        }
                        reader.close();
                        //now in my out the valid db connection address
                        Log.d("TaskRetriever", out.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    assert finalTaskLocationConnection != null;
                    finalTaskLocationConnection.disconnect();
                }
            }
        });
        bdRetriever.start();
        try {
            bdRetriever.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    public String computeTask(){
        return "Not yet implemented";
    }
}
