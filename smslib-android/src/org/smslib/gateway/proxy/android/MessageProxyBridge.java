package org.smslib.gateway.proxy.android;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

public class MessageProxyBridge extends ProxyBridge<Serializable>  {
	public static final String SCHEMA = "http";
	public static final String CONTEXT = "/proxygateway";
	
	public final static String EXTRA_S_ORIGINATINGADDRESS = MessageProxyBridge.class.getCanonicalName() + ".originatingAddress";
	public final static String EXTRA_S_MESSAGEBODY = MessageProxyBridge.class.getCanonicalName() + ".messageBody";
	public final static String EXTRA_L_TIMESTAMPMILLIS = MessageProxyBridge.class.getCanonicalName() + ".timestampMillis";
	public final static String EXTRA_I_STATUS = MessageProxyBridge.class.getCanonicalName() + ".status";
	public static final String EXTRA_O_RECEIVERTAG = MessageProxyBridge.class.getCanonicalName() + ".receiverTag";
	
	public static final String EXTRA_O_RECEIVEREXCEPTION = MessageProxyBridge.class.getCanonicalName() + ".receiverException";

	
	public MessageProxyBridge() {
		super("org.smslib.gateway.proxy.android.MessageProxyBridge");
	}

	@Override
	protected Serializable parseResponse(HttpResponse response) {
		return null;
	}

	@Override
	protected HttpUriRequest prepareMessage(URI uri, Intent workIntent) throws JSONException, UnsupportedEncodingException {
		String originatingAddress = workIntent.getStringExtra(EXTRA_S_ORIGINATINGADDRESS);
		String messageBody = workIntent.getStringExtra(EXTRA_S_MESSAGEBODY);
		long timestampMillis = workIntent.getLongExtra(EXTRA_L_TIMESTAMPMILLIS, 0);
		int status = workIntent.getIntExtra(EXTRA_I_STATUS, 0);
		
		HttpPost httppost = new HttpPost(uri);
		//Endpoint does not provide any content
		//httppost.setHeader("Accept", "application/json");
		httppost.setHeader("Content-Type", "application/json");
		
		JSONObject jMsg = new JSONObject();
	    jMsg.put("originatingAddress", originatingAddress);
	    jMsg.put("messageBody", messageBody);
	    jMsg.put("timestampMillis", String.valueOf(timestampMillis));
	    jMsg.put("status", String.valueOf(status));
	    // Execute HTTP Post Request
		Log.d(MessageProxyBridge.class.getName(), "Assembled JSON message: " + jMsg.toString());

	    httppost.setEntity(new StringEntity(jMsg.toString()));
	    
	    return httppost;
	}

}
