package com.mobilize.geosafeSms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Morfo on 18/01/2018.
 */

public class IncomingSms extends BroadcastReceiver {
    private static String TAG = IncomingSms.class.getSimpleName();
    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();
    private static String AUTH_KEY = "Authorization";
    private static String AUTH_TOKEN = "875bb744ecb673f06b5d8383f6b5e095";
    AsyncHttpClient client;

    public void onReceive(Context context, Intent intent) {
        client = new AsyncHttpClient();
        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();
                    Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);
                    // Show Alert
                    /*int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,"senderNum: "+ senderNum + ", message: " + message, duration);
                    toast.show();*/

                    String Lat = findLat(message);
                    String Lng = findLng(message);
                    Log.d("MainActivity", "LAT IN SMS:" + findLat(message));
                    Log.d("MainActivity", "LNG IN SMS:" + findLng(message));
                    RequestParams params = new RequestParams();
                    params.put("lat", Lat);
                    params.put("lng", Lng);
                    params.put("numero", senderNum);
                    //TODO  tiene que mandar el teléfono también

                    String URL = "http://mobilize.mx/geosafe/api/v1/addLocation";
                    client.addHeader(AUTH_KEY, AUTH_TOKEN);
                    client.post(URL, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            if(BuildConfig.DEBUG)
                                Log.d(TAG, "onSuccess: " + response.toString());

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            if(BuildConfig.DEBUG)
                                Log.e(TAG, "onFailure : " + statusCode + " responseString " + responseString);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            //Log.d(TAG, "errorResponse " + errorResponse.toString());
                            if(BuildConfig.DEBUG) {
                                Log.d(TAG, "STATUS CODE " + statusCode);
                                Log.d(TAG, "throwable.getCause() " + throwable.getCause());
                            }
                            if (throwable.getCause() instanceof IOException) {
                                System.out.println("Connection timeout!");
                                return;
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            if(BuildConfig.DEBUG) {
                                Log.d(TAG, "errorResponse " + errorResponse.toString());
                                Log.d(TAG, "STATUS CODE " + statusCode);
                                Log.d(TAG, "throwable.getCause() " + throwable.getCause());
                            }
                            if (throwable.getCause() instanceof IOException) {
                                System.out.println("Connection timeout!");
                                return;
                            }
                        }

                    });



                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);

        }
    }

    /*
    Extraen latitud y longitud del SMS
     */
    public String findLat(String mensaje) {
        Pattern pattern = Pattern.compile("lat:(.*?) long:");
        Matcher matcher = pattern.matcher(mensaje);
        while (matcher.find()) {
            System.out.println(matcher.group(1));
            return matcher.group(1);
        }
        return null;
    }
    public String findLng(String mensaje) {
        Pattern pattern = Pattern.compile("long:(.*?) speed:");
        Matcher matcher = pattern.matcher(mensaje);
        while (matcher.find()) {
            System.out.println(matcher.group(1));
            return matcher.group(1);
        }
        return null;
    }
}
