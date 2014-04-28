package org.smslib.gateway.proxy.android;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class ProxyBridge<T extends Serializable> extends IntentService  {
	public static final String SCHEMA = "http";
	public static final String CONTEXT = "/proxygateway";

	public static final String EXTRA_O_RECEIVERTAG = ProxyBridge.class.getCanonicalName() + ".receiverTag";
	public static final String EXTRA_O_RECEIVERTAGRESULT = ProxyBridge.class.getCanonicalName() + ".receiverTagResult";
	public static final String EXTRA_O_RECEIVEREXCEPTION = ProxyBridge.class.getCanonicalName() + ".receiverException";

	
	public ProxyBridge(String id) {
		super(id);
	}
	
	@Override
	protected final void onHandleIntent(Intent workIntent) {
		ResultReceiver rec = workIntent.getParcelableExtra(EXTRA_O_RECEIVERTAG);

		Bundle b = new Bundle();
		try {
			Serializable result = doCommuniction(workIntent);
			Log.i(ProxyBridge.class.getName(), "Message send sucessfully");
			b.putSerializable(EXTRA_O_RECEIVERTAGRESULT, result);
			rec.send(0, b);
		} catch (IOException e) {
			Log.i(ProxyBridge.class.getName(), "IOException " + e.getMessage(), e);
			b.putSerializable(EXTRA_O_RECEIVEREXCEPTION, e);
			rec.send(-100, b);
		} catch (URISyntaxException | JSONException e) {
			Log.i(ProxyBridge.class.getName(), "DataException " + e.getMessage(), e);
			b.putSerializable(EXTRA_O_RECEIVEREXCEPTION, e);
			rec.send(-200, b);
		} catch (Exception e) {
			Log.i(ProxyBridge.class.getName(), "Exception: " + e.getMessage(), e);
			b.putSerializable(EXTRA_O_RECEIVEREXCEPTION, e);
			rec.send(-1, b);
		}
	}
	protected Serializable doCommuniction(Intent workIntent) throws URISyntaxException, JSONException, ClientProtocolException, IOException {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String host = sharedPref.getString("host", null);
		int port = Integer.parseInt(sharedPref.getString("port", "-1"));
		
		String authToken = sharedPref.getString("authToken", "");
		
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		URI uri = new URI(SCHEMA, "", host, port, CONTEXT + "/messages", "", "");
		
		Log.d(ProxyBridge.class.getName(), "Sending message to " + uri);
		
		HttpUriRequest request = prepareMessage(uri, workIntent);
		
	    if (authToken != null && authToken.length() > 0) {
			Log.d(ProxyBridge.class.getName(), "Sending message with authToken " + authToken);

			request.setHeader("X-SMSlib-Token", authToken);
	    }
		
	    HttpResponse response = httpclient.execute(request);
	    int statusCode = response.getStatusLine().getStatusCode();
	    Log.d(ProxyBridge.class.getName(), "Message send returned with status code: " + statusCode);

	    if (statusCode / 100 == 2) {
	    	Log.i(ProxyBridge.class.getName(), "Message send sucessfull to server: " + statusCode);	
	    	Serializable result  = parseResponse(response);
	    	return result;
	    } 
	    
    	Log.w(ProxyBridge.class.getName(), "Message send to server returned: " + statusCode);
    	throw new IOException("Server returned status " + statusCode);
	}

	
	protected abstract T parseResponse(HttpResponse response) throws UnsupportedEncodingException, IllegalStateException, JSONException, IOException;

	protected abstract HttpUriRequest prepareMessage(URI uri, Intent workIntent) throws JSONException, UnsupportedEncodingException;

}
