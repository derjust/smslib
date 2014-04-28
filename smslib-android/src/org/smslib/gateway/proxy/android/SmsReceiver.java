package org.smslib.gateway.proxy.android;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) 
    {
        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();
        List<SmsMessage> msgs = new ArrayList<SmsMessage>();
        if (bundle != null)
        {
            //---retrieve the SMS message received---
           Object[] pdus = (Object[]) bundle.get("pdus");
            for (int i=0; i<pdus.length; i++)
            {
                SmsMessage msg = SmsMessage.createFromPdu((byte[])pdus[i]);                
                msgs.add(msg);

                String logMessage = "Message '" + msg.getMessageBody() + "' from: " + msg.getOriginatingAddress(); 
                Log.i(SmsReceiver.class.getCanonicalName(), logMessage);
                //---display the new SMS message---
                Toast.makeText(context, logMessage, Toast.LENGTH_SHORT).show();
            }
            
            sendMessages(context, msgs);
       }                         
    }

	private void sendMessages(Context context, List<SmsMessage> msgs) {
		for(SmsMessage msg : msgs) {
			
		    Intent mServiceIntent = new Intent(context, ProxyBridge.class);
		    mServiceIntent.putExtra(MessageProxyBridge.EXTRA_S_MESSAGEBODY, msg.getMessageBody());
		    mServiceIntent.putExtra(MessageProxyBridge.EXTRA_S_ORIGINATINGADDRESS, msg.getOriginatingAddress());
		    mServiceIntent.putExtra(MessageProxyBridge.EXTRA_I_STATUS, msg.getStatus());
		    mServiceIntent.putExtra(MessageProxyBridge.EXTRA_L_TIMESTAMPMILLIS, msg.getTimestampMillis());
		    
		    context.startService(mServiceIntent);
		}
	}

}