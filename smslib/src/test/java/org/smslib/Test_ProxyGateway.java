package org.smslib;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.events.InboundMessageEvent;
import org.smslib.gateway.proxy.ProxyGateway;

public class Test_ProxyGateway extends TestCase {

	static Logger logger = LoggerFactory.getLogger(Test_SerialModem.class);

	public static String RECIPIENT = "";

	private CountDownLatch inboundMessageLatch = new CountDownLatch(1);

	public class InboundMessageCallback implements IInboundMessageCallback
	{

		@Override
		public boolean process(InboundMessageEvent event)
		{
			logger.info("[InboundMessageCallback] " + event.getMessage().toShortString());
			logger.info(event.getMessage().toString());
			inboundMessageLatch .countDown();
			return true;
		}
	}
	
	public void test() throws Exception
	{
		Service.getInstance().setInboundMessageCallback(new InboundMessageCallback());
		Service.getInstance().start();
		
		String ip = InetAddress.getLoopbackAddress().getHostAddress();
		logger.info("Debugging with {}", ip);
		ProxyGateway gateway = new ProxyGateway("proxy", "192.168.152.135", 9999, "ab");
		Service.getInstance().registerGateway(gateway);

		// Sleep to wait
		assertTrue("No message received",
				inboundMessageLatch.await(2000, TimeUnit.SECONDS));
		
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
