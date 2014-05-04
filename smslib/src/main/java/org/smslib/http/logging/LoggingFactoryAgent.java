package org.smslib.http.logging;

import java.nio.channels.SocketChannel;

import org.simpleframework.transport.trace.Agent;
import org.simpleframework.transport.trace.Trace;
import org.slf4j.Logger;

public class LoggingFactoryAgent implements Agent {

	private Logger rootlogger;

	public LoggingFactoryAgent(Logger logger) {
		this.rootlogger = logger;
	}

	@Override
	public Trace attach(SocketChannel channel) {
		return new LoggingTrace(rootlogger, channel);
	}

	@Override
	public void stop() {
	}

}
