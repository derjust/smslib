package org.smslib.gateway.proxy.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Intent;

public class StatisticsProxyBridge extends ProxyBridge<GatewayStatistics> {

	public StatisticsProxyBridge() {
		super("org.smslib.gateway.proxy.android.StatisticsProxyBridge");
	}

	@Override
	protected GatewayStatistics parseResponse(HttpResponse response) throws UnsupportedEncodingException, IllegalStateException, JSONException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		StringBuilder builder = new StringBuilder();
		for (String line = null; (line = reader.readLine()) != null;) {
		    builder.append(line).append("\n");
		}
		JSONTokener tokener = new JSONTokener(builder.toString());
		JSONObject finalResult = new JSONObject(tokener);
		
		GatewayStatistics retValue = new GatewayStatistics();
		
		retValue.setStartTime(finalResult.getLong("startTime"));
		retValue.setTotalSent(finalResult.getInt("totalSent"));
		retValue.setTotalReceived(finalResult.getInt("totalReceived"));
		retValue.setTotalFailures(finalResult.getInt("totalFailures"));
		retValue.setTotalFailed(finalResult.getInt("totalFailed"));
		return retValue;
	}

	@Override
	protected HttpUriRequest prepareMessage(URI uri, Intent workIntent)
			throws JSONException, UnsupportedEncodingException {
		HttpGet httppost = new HttpGet(uri);
		httppost.setHeader("Accept", "application/json");

		return httppost;
	}

}
