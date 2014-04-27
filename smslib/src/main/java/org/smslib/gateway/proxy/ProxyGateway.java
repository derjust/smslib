package org.smslib.gateway.proxy;

import java.net.URI;
import java.net.URISyntaxException;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import org.smslib.core.Coverage;
import org.smslib.core.CreditBalance;
import org.smslib.gateway.AbstractGateway;
import org.smslib.message.DeliveryReportMessage.DeliveryStatus;
import org.smslib.message.InboundMessage;
import org.smslib.message.OutboundMessage;

public class ProxyGateway extends AbstractGateway {
	public static final String SCHEMA = "http";
	public static final String CONTEXT = "/proxygateway";
	
	static final Logger logger = LoggerFactory.getLogger(ProxyGateway.class);
	
	final URI uri;
	final ResourceConfig config;
	@SuppressWarnings("restriction")
	com.sun.net.httpserver.HttpServer server;
	
	public ProxyGateway(String gatewayId, String host, int port, String token) {
		super(2, gatewayId, "Proxy Gateway");
		// Just inbound messages - this is also the default
		//setCapabilities(new Capabilities());

		try
		{
			uri = new URI(SCHEMA, "", host, port, CONTEXT, "", "");
			//Provide feedback to the user
			logger.info("Started proxy gateway at {}//{}:{}", 
					SCHEMA,
					uri.getHost(),
					uri.getPort()
					);
		}
		catch (URISyntaxException e)
		{
			throw new IllegalArgumentException(e.getMessage(), e);
		} 
		
		//Register endpoints
		config = new ResourceConfig(MessagesEndpoint.class, VersionEndpoint.class);
		//Register binding to this proxy gateway from the endpoints
		config.register(new AbstractBinder()
		{
            @Override
            protected void configure()
            {
                bindFactory(new ProxyGatewayFactory(ProxyGateway.this)).to(ProxyGateway.class);
            }
        });
		config.register(MoxyJsonFeature.class);
	    config.register(JsonMoxyConfigurationContextResolver.class);
	     
	    if (token != null && token.length() > 0)
	    {
	    	logger.info("Authorization token '{}' activated", token);
	    	config.register(new SecurityFilter(token));
	    }
		
	}

	public ProxyGateway(String gatewayId, String... parms)
	{
		this(gatewayId, parms[0], Integer.parseInt(parms[1]), parms.length >= 2 ? parms[2]: null);
	}
	
	@Override
	protected void _start() throws Exception {
		//Create and start the server
		server = JdkHttpServerFactory.createHttpServer(uri, config);
	}

	@SuppressWarnings("restriction")
	@Override
	protected void _stop() throws Exception {
		server.stop(0);
	}

	@Override
	protected boolean _send(OutboundMessage message) {
		return false;
	}

	@Override
	protected boolean _delete(InboundMessage message) {
		return false;
	}

	@Override
	protected DeliveryStatus _queryDeliveryStatus(String operatorMessageId) {
		return DeliveryStatus.Unknown;
	}

	@Override
	protected CreditBalance _queryCreditBalance() throws Exception {
		CreditBalance cb = new CreditBalance();
		cb.setCredits(Double.NEGATIVE_INFINITY);
		return cb;
	}

	@Override
	protected Coverage _queryCoverage(Coverage coverage) throws Exception {
		coverage.setCoverage(false);
		return coverage;
	}
	
}
