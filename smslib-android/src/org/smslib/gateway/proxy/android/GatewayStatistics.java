package org.smslib.gateway.proxy.android;

import java.io.Serializable;

public class GatewayStatistics implements Serializable {

	@Override
	public String toString() {
		return "GatewayStatistics [startTime=" + startTime + ", totalSent="
				+ totalSent + ", totalReceived=" + totalReceived
				+ ", totalFailures=" + totalFailures + ", totalFailed="
				+ totalFailed + "]";
	}

	private static final long serialVersionUID = 3141817014772256255L;
	private long startTime;
	private int totalSent;
	private int totalReceived;
	private int totalFailures;
	private int totalFailed;

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setTotalSent(int totalSent) {
		this.totalSent = totalSent;
	}

	public void setTotalReceived(int totalReceived) {
		this.totalReceived = totalReceived;
	}

	public void setTotalFailures(int totalFailures) {
		this.totalFailures = totalFailures;
	}

	public void setTotalFailed(int totalFailed) {
		this.totalFailed = totalFailed;
	}

	public int getTotalSent() {
		return totalSent;
	}

	public int getTotalReceived() {
		return totalReceived;
	}

	public int getTotalFailures() {
		return totalFailures;
	}

	public int getTotalFailed() {
		return totalFailed;
	}
}
