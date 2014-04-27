package org.smslib.gateway.proxy;

public class ProxyEndpointMessage {

	private String originatingAddress;
	private String messageBody;
	private long timestampMillis;
	private int status;

	public ProxyEndpointMessage() {
	}
	
	public String getOriginatingAddress() {
		return originatingAddress;
	}

	public void setOriginatingAddress(String originatingAddress) {
		this.originatingAddress = originatingAddress;
	}

	public long getTimestampMillis() {
		return timestampMillis;
	}

	public void setTimestampMillis(long timestampMillis) {
		this.timestampMillis = timestampMillis;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	@Override
	public String toString() {
		return "SmsMessage [originatingAddress=" + originatingAddress
				+ ", messageBody=" + messageBody + ", timestampMillis="
				+ timestampMillis + ", status=" + status + "]";
	}
	
}
