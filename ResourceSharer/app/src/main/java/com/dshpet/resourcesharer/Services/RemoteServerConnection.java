package com.dshpet.resourcesharer.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.os.Binder;
import android.net.http.AndroidHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import android.provider.Settings.Secure;

import static java.security.AccessController.getContext;

public class RemoteServerConnection extends Service {
    public RemoteServerConnection() {
    }

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    //private final IBinder mBinder = new LocalBinder();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //sendDataToDatabase(new JSONObject());
        try {
            getDatabaseURL();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            sendDataToDatabaseApache(new JSONObject());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("RemoteServerConnection", "Binded successfully");
        return new Binder();
    }

    public boolean sendDataToDatabaseLight(final JSONObject data) {

        Log.d("sendDataToDatabase", "in method");
        //getting my host url
        URL host = null;
        URL dbLoc = null;
        URL dbSend = null;
        try {
            host = new URL("https://resource-sharer-server.herokuapp.com");
            dbLoc = new URL(host + "/dbUrl");
            dbSend = new URL(host + "/recieveData");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //establishing connection on url
        HttpsURLConnection dbSendConnection = null;
        try {
            assert dbSend != null;
            dbSendConnection = (HttpsURLConnection) dbSend.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }



        //specifying post params
        try {
            dbSendConnection.setRequestMethod("POST");
            Log.d("sendDataToDatabase", "dbSendConnection established");
        } catch (ProtocolException e) {
            Log.d("sendDataToDatabase", "exception((");
            e.printStackTrace();
        }
        // Send post request
        dbSendConnection.setDoOutput(true);
        dbSendConnection.setDoInput(true);
        dbSendConnection.setRequestProperty("Content-Type", "application/json");
        dbSendConnection.setRequestProperty("Accept", "application/json");


        //what i need to receive data from server
        final DataOutputStream[] wr = {null};
        final HttpsURLConnection finalDbSendConnection = dbSendConnection;
        final int[] responseCode = {0};
        final URL finalDbSend = dbSend;
        final StringBuilder response = new StringBuilder();

        final Thread streamReadingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wr[0] = new DataOutputStream(finalDbSendConnection.getOutputStream());
                    wr[0].writeBytes(data.toString());
                    wr[0].flush();
                    wr[0].close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d("sendDataToDatabase", wr[0].toString());

                //getting response code from the method
                try {
                    responseCode[0] = finalDbSendConnection.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d("POST_Service:", "\nSending 'POST' request to URL : " + finalDbSend);
                Log.d("POST_Service:", "Post parameters : " + "Forgot what it was");
                Log.d("POST_Service:", "Response Code : " + responseCode[0]);

                BufferedReader in = null;
                try {
                    in = new BufferedReader(
                            new InputStreamReader(finalDbSendConnection.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String inputLine;


                try {
                    assert in != null;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //print result
                Log.d("POST_Service:", "Response = " + response.toString());
            }
        });

        try {
            streamReadingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Log.d("sendDataToDatabase", wr[0].toString());

        Log.d("POST_Service:", "\nSending 'POST' request to URL : " + finalDbSend);
        Log.d("POST_Service:", "Post parameters : " + "Forgot what it was");
        Log.d("POST_Service:", "Response Code : " + responseCode[0]);

        Log.d("POST_Service:", "Response = " + response);

        Log.d("POST_Service:", "Exiting");

        return true;
    }
    public boolean sendDataToDatabaseApache(JSONObject data) throws Exception {
        String LOG_TAG = "POST_APACHE_CLIENT";

        URL host = new URL("https://resource-sharer-server.herokuapp.com");
        URL dbSend = new URL(host + "/recieveData");

        final HttpClient client = new DefaultHttpClient();
        final HttpPost post = new HttpPost(dbSend.toString());

        // add header
        //post.setHeader("User-Agent", USER_AGENT);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("sn", "C02G8416DRJM"));
        urlParameters.add(new BasicNameValuePair("cn", ""));
        urlParameters.add(new BasicNameValuePair("locale", ""));
        urlParameters.add(new BasicNameValuePair("caller", ""));
        urlParameters.add(new BasicNameValuePair("num", "12345"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        final HttpResponse[] response = {null};
        final StringBuffer result = new StringBuffer();
        Thread sendingThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    response[0] = client.execute(post);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                assert response[0] != null;
                BufferedReader rd = null;
                try {
                    rd = new BufferedReader(new InputStreamReader(response[0].getEntity().getContent()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String line = "";
                assert rd != null;
                try {
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


        Log.d(LOG_TAG, "nSending 'POST' request to URL : " + dbSend);
        Log.d(LOG_TAG, "Post parameters : " + post.getEntity());
        //Log.d(LOG_TAG, "Response Code : " + response[0].getStatusLine().getStatusCode());

        Log.d(LOG_TAG,"converted responde:" + result.toString());

        return true;
    }
    public boolean sendDataToDatabaseURL(JSONObject data){
        //todo maybe implement another one
        return true;
    }

    public String getDatabaseURL() throws InterruptedException {
        //getting my host url
        URL host = null;
        URL dbLoc = null;
        try {
            host = new URL("https://resource-sharer-server.herokuapp.com");
            dbLoc = new URL(host + "/dbLoc");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //hoping that everything is fine
        assert dbLoc != null;
        HttpsURLConnection dbLocConnection = null;
        try {
            dbLocConnection = (HttpsURLConnection) dbLoc.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final HttpURLConnection finalDbLocConnection = dbLocConnection;
        final URL finalHost = host;
        final StringBuilder out = new StringBuilder();
        Thread bdRetriever = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //have connection with server
                    try {
                        assert finalDbLocConnection != null;
                        InputStream in = new BufferedInputStream(finalDbLocConnection.getInputStream());

                        //converting stream into data
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            out.append(line);
                        }
                        reader.close();
                        //now in my out the valid db connection address
                        Log.d("RemoteServerConnection", out.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    assert finalDbLocConnection != null;
                    finalDbLocConnection.disconnect();
                }
            }
        });
        bdRetriever.start();
        bdRetriever.join();
        return out.toString();
    }
}