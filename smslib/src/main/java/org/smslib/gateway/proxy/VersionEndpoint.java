package org.smslib.gateway.proxy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/version")
public class VersionEndpoint {
	static final Logger logger = LoggerFactory.getLogger(VersionEndpoint.class);

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersion() {
		Package pck = VersionEndpoint.class.getPackage();
		String version = pck.getImplementationVersion();
		logger.info("Return version: {}", version);
		return version;
	}
}
