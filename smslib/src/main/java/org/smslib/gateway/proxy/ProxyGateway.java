package org.smslib.gateway.proxy;

import java.io.IOException;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.simple.SimpleContainer;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.smslib.Service;
import org.smslib.core.Coverage;
import org.smslib.core.CreditBalance;
import org.smslib.gateway.AbstractGateway;
import org.smslib.http.IHttpRequestHandler;
import org.smslib.message.DeliveryReportMessage.DeliveryStatus;
import org.smslib.message.InboundMessage;
import org.smslib.message.OutboundMessage;

public class ProxyGateway extends AbstractGateway {
	public static final String SCHEMA = "https";
	public static final String CONTEXT = "/proxygateway";
	
	static final Logger logger = LoggerFactory.getLogger(ProxyGateway.class);
	
	final ResourceConfig config;
	private IHttpRequestHandler handler;
	
	public ProxyGateway(String gatewayId, String token) {
		super(2, gatewayId, "Proxy Gateway");
		// Just inbound messages - this is also the default
		//setCapabilities(new Capabilities());

		//Redirect Jersey's logging - levels must still be specified via jul's logging.properties!
		// Optionally remove existing handlers attached to j.u.l root logger
		 SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)

		 // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
		 // the initialization phase of your application
		 SLF4JBridgeHandler.install();
		
		//Register endpoints
		config = new ResourceConfig(MessagesEndpoint.class, VersionEndpoint.class, StatusEndpoint.class);

		//Register binding to this proxy gateway for the endpoints
		config.register(new AbstractBinder()
		{
            @Override
            protected void configure()
            {
                bindFactory(new ProxyGatewayFactory(ProxyGateway.this)).to(ProxyGateway.class);
            }
        });
		
		//Register Moxy feature for Json
		config.register(MoxyJsonFeature.class);
	    config.register(JsonMoxyConfigurationContextResolver.class);
	     
	    //Enable security token only if present
	    if (token != null && token.length() > 0)
	    {
	    	logger.info("Authorization token '{}' activated", token);
	    	config.register(new SecurityFilter(token));
	    }
	}

	public ProxyGateway(String gatewayId, String... parms)
	{
		this(gatewayId, parms.length >= 1 ? parms[0] : null);
	}
	
	@Override
	protected void _start() throws Exception {
        
        handler = new IHttpRequestHandler() {
        	SimpleContainer container = ContainerFactory.createContainer(SimpleContainer.class, config);
			
			@Override
			public org.simpleframework.http.Status process(Request request, Response response) {
				container.handle(request, response);
				return null;
			}
		};

		Service.getInstance().registerHttpRequestHandler("/messages", handler);
	}

	@Override
	protected void _stop() throws IOException {
		//TODO
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
	protected CreditBalance _queryCreditBalance() {
		CreditBalance cb = new CreditBalance();
		cb.setCredits(Double.NEGATIVE_INFINITY);
		return cb;
	}

	@Override
	protected Coverage _queryCoverage(Coverage coverage) {
		coverage.setCoverage(false);
		return coverage;
	}
	
}
