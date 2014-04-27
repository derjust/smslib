package org.smslib.gateway.proxy;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityFilter implements ContainerRequestFilter {
	private static final String SMSLIB_TOKEN = "X-SMSlib-Token";

	static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

	private final String token;

	public SecurityFilter(String token) {
		if (token == null || token.length() == 0)
		{
			throw new IllegalArgumentException("token might not be null or empty!");
		}
		this.token = token;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {

		String requestToken = requestContext.getHeaderString(SMSLIB_TOKEN);
		
		if (requestToken == null)
		{
			requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
		}
		else if (token.equals(requestToken))
		{
			logger.debug("Accepting incomming request by provided token");
		}
		else
		{
			requestContext.abortWith(Response.status(Status.FORBIDDEN).build());
		}

	}

}
