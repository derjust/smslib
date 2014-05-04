package org.smslib.gateway.proxy;

import org.glassfish.hk2.api.Factory;

/**
 * Factory to inject the very same instance of the ProxyGatway to all endpoints injected via @Inject
 * @author derjust
 */
public class ProxyGatewayFactory implements Factory<ProxyGateway> {

	private final ProxyGateway proxyGateway;
	
	public ProxyGatewayFactory(ProxyGateway proxyGateway) {
		this.proxyGateway = proxyGateway;
	}
	
	@Override
	public void dispose(ProxyGateway notused) {
	}

	@Override
	public ProxyGateway provide() {
		return proxyGateway;
	}

}
