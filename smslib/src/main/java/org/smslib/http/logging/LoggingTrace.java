package org.smslib.http.logging;

import java.nio.channels.SocketChannel;

import org.simpleframework.transport.TransportEvent;
import org.simpleframework.transport.connect.ConnectionEvent;
import org.simpleframework.transport.trace.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingTrace implements Trace {

	private Logger logger;
	
	public LoggingTrace(Logger rootlogger, SocketChannel channel) {
		logger = rootlogger;
	}

	@Override
	public void trace(Object event) {
		this.trace(event, null);

	}

	@Override
	public void trace(Object event, Object value) {
		Logger actualLogger = this.logger;
		StringBuilder actualValue = new StringBuilder();
		if (event instanceof ConnectionEvent) {
			ConnectionEvent connectionEvent = (ConnectionEvent) event;
		
			actualLogger = LoggerFactory.getLogger(logger.getName() + ".connection");
			actualValue.append(connectionEvent);
		} else if (event instanceof TransportEvent) {
			TransportEvent transportEvent = (TransportEvent) event;

			actualLogger = LoggerFactory.getLogger(logger.getName() + ".transport");
			actualValue.append(transportEvent);
		}

		if (value != null) {
			actualValue.append(" ").append(value);
		}

		if (value instanceof Throwable) {
			actualLogger.warn(actualValue.toString(), (Throwable)value);
		} else {
			actualLogger.info("{}", actualValue);
		}
	}

}
