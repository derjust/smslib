package org.smslib.gateway.proxy;

import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.core.Statistics;
import org.smslib.message.InboundMessage;

@Path("/messages")
public class MessagesEndpoint {
	static final Logger logger = LoggerFactory.getLogger(MessagesEndpoint.class);

	@Inject
	ProxyGateway proxyGateway;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ProxyEndpointStatistics showStatistics() {
		Statistics stats = proxyGateway.getStatistics();
		ProxyEndpointStatistics retValue = new ProxyEndpointStatistics();
		retValue.setStartTime(stats.getStartTime().getTime());
		retValue.setTotalFailed(stats.getTotalFailed());
		retValue.setTotalFailures(stats.getTotalFailures());
		retValue.setTotalReceived(stats.getTotalReceived());
		retValue.setTotalSent(stats.getTotalSent());
		return retValue;
	}
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
	public void  postMessage(ProxyEndpointMessage msg) {
		logger.info("Receieved message {}", msg);
		proxyGateway.processMessage(mapMessage(msg));
	}

	private InboundMessage mapMessage(ProxyEndpointMessage msg) {
		InboundMessage retValue = new InboundMessage(msg.getOriginatingAddress(), msg.getMessageBody(), new Date(msg.getTimestampMillis()), null, 0);
		logger.debug("Mapped {} to {}", msg, retValue);
		return retValue;
	}
}
