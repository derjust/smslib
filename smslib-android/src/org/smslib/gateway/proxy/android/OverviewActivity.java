package org.smslib.gateway.proxy.android;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class OverviewActivity extends ActionBarActivity implements SendTestMessageResultReceiver.Receiver, StatisticsResultReceiver.Receiver {

	public SendTestMessageResultReceiver mReceiver;
	private StatisticsResultReceiver mStatisticsReceiver;

	private List<String> loglines = new ArrayList<>();
	
	public void sendTestMessage(View view) {
		Log.i(OverviewActivity.class.getName(), "Prepare test message");
		
	    Intent mServiceIntent = new Intent(this, MessageProxyBridge.class);
	    mServiceIntent.putExtra(MessageProxyBridge.EXTRA_S_MESSAGEBODY, "Test Message!");
	    mServiceIntent.putExtra(MessageProxyBridge.EXTRA_S_ORIGINATINGADDRESS, "Test");
	    mServiceIntent.putExtra(MessageProxyBridge.EXTRA_I_STATUS, 0);
	    mServiceIntent.putExtra(MessageProxyBridge.EXTRA_L_TIMESTAMPMILLIS, System.currentTimeMillis());

	    mServiceIntent.putExtra(ProxyBridge.EXTRA_O_RECEIVERTAG, mReceiver);

	    // Starts the IntentService
	    startService(mServiceIntent);
	}
	
	public void refreshStatistics() {
        Log.i(OverviewActivity.class.getName(), "Refresh Statistics");

	    Intent mServiceIntent = new Intent(this, StatisticsProxyBridge.class);

	    mServiceIntent.putExtra(ProxyBridge.EXTRA_O_RECEIVERTAG, mStatisticsReceiver);

	    // Starts the IntentService
	    startService(mServiceIntent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mReceiver.setReceiver(this);
		mStatisticsReceiver.setReceiver(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mReceiver.setReceiver(null);
		mStatisticsReceiver.setReceiver(null);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		final ListView listview = (ListView) findViewById(R.id.logList);
	    ArrayAdapter<String> x = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, loglines);
	    listview.setAdapter(x);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
	    mReceiver = new SendTestMessageResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		mStatisticsReceiver = new StatisticsResultReceiver(new Handler());
		mStatisticsReceiver.setReceiver(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.overview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			refreshStatistics();
			return true;
		} else if (id == R.id.action_settings) {
			Intent i = new Intent(this, SettingsActivity.class);
			Log.i("settings", "bla " + i);
		    startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_overview,
					container, false);
			return rootView;
		}
	}

	@Override
	public void onReceiveSendTestMessageResult(int resultCode, Bundle resultData) {
		if (resultCode == 0 ) {
			loglines.add("Testmessage send!");
		} else {
			Exception e = (Exception) resultData.getSerializable(ProxyBridge.EXTRA_O_RECEIVEREXCEPTION);
			Log.e(OverviewActivity.class.getName(), e.getMessage(), e);
			if (resultCode == -200) {
				loglines.add("Data error: " + e.getMessage());
			} else if (resultCode == -100) {
				loglines.add("Network error: " + e.getMessage());
			} else {
				loglines.add("Internal error: " + e.getMessage());
			}
		}

		ListView loglist = (ListView) findViewById(R.id.logList);
		((BaseAdapter)loglist.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onStatisticsResult(int resultCode, Bundle resultData) {
		if (resultCode == 0 ) {
			final GatewayStatistics stats = (GatewayStatistics) resultData.getSerializable(ProxyBridge.EXTRA_O_RECEIVERTAGRESULT);
			Log.i(OverviewActivity.class.getName(), "Statistics receieved: " + stats);

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					((TextView) findViewById(R.id.txtFailed)).setText(""+stats.getTotalFailed());
					((TextView) findViewById(R.id.txtFailures)).setText(""+stats.getTotalFailures());
					((TextView) findViewById(R.id.txtSent)).setText(""+stats.getTotalSent());
					((TextView) findViewById(R.id.txtReceived)).setText(""+stats.getTotalReceived());
					((TextView) findViewById(R.id.txtStarttime)).setText("" + new Date(stats.getStartTime()));

				}});

		} else {
			Exception e = (Exception) resultData.getSerializable(ProxyBridge.EXTRA_O_RECEIVEREXCEPTION);
			Log.e(OverviewActivity.class.getName(), e.getMessage(), e);
			loglines.add("Internal error: " + e.getMessage());
		}
		ListView loglist = (ListView) findViewById(R.id.logList);
		((BaseAdapter)loglist.getAdapter()).notifyDataSetChanged();	

	}

}
