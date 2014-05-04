
package org.smslib.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.trace.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.http.handlers.StatusHandler;
import org.smslib.http.logging.LoggingFactoryAgent;

public class HttpServer implements Container
{
	private static final char[] KEYSTORE_PASSWORDd  = "entrypass".toCharArray();

	static Logger logger = LoggerFactory.getLogger(HttpServer.class);

	static HashMap<String, IHttpRequestHandler> httpRequestHandlers = new HashMap<>();

	static HashMap<String, List<String>> httpRequestACLs = new HashMap<>();

	static Executor executor;

	Container container;

	Connection connection;

	Server server;

	public HttpServer()
	{
		HttpServer.httpRequestHandlers.put("/status", new StatusHandler());
	}

	public void start(InetSocketAddress bindAddress, int poolSize, boolean useSsl) throws Exception
	{
		executor = Executors.newFixedThreadPool(poolSize);
		container = new HttpServer();
		server = new ContainerServer(container);
		connection = new SocketConnection(server, getAgent());
		SocketAddress address = bindAddress;
		connection.connect(address, getSSLContext(useSsl, bindAddress));
	}
	
	private SSLContext getSSLContext(boolean activateSsl, InetSocketAddress bindAddress) throws InvalidKeyException, UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, SecurityException, SignatureException, CertificateException, KeyStoreException, IOException
	{
		if (activateSsl)
		{
			String hostName = bindAddress.getHostName();
			logger.debug("Resolved {} to {} for certificate generation", bindAddress, hostName);
			return getSSLContext(hostName);
		}
		else
		{
			return null;
		}
	}

	private Agent getAgent() {
		if (logger.isDebugEnabled())
		{
			return new LoggingFactoryAgent(logger);
		}
		else
		{
			return null;
		}
	}

	private SSLContext getSSLContext(String domainName) throws NoSuchAlgorithmException, InvalidKeyException, SecurityException, SignatureException, CertificateException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException
	{
		KeyStore ks = generateCertificate(domainName);

	    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
	            .getDefaultAlgorithm());
	    kmf.init(ks, KEYSTORE_PASSWORDd);
		
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), null, null);

		return sslContext;
	}
	
	private KeyStore generateCertificate(String domainName) throws IOException, InvalidKeyException, SecurityException, SignatureException, NoSuchAlgorithmException, CertificateException, KeyStoreException
	{
		Security.addProvider(new BouncyCastleProvider());
		
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");  
		keyPairGenerator.initialize(1024);  
		KeyPair KPair = keyPairGenerator.generateKeyPair();
		
		X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
		int sr = new SecureRandom().nextInt();
		if (sr < 0) {
			sr = sr * -1;
		}
		v3CertGen.setSerialNumber(BigInteger.valueOf(sr));  
        v3CertGen.setIssuerDN(new X509Principal("CN=" + domainName + ", OU=None, O=None L=None, C=None"));  
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis()));  
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365)));  
        v3CertGen.setSubjectDN(new X509Principal("CN=" + domainName + ", OU=None, O=None L=None, C=None")); 
        
        v3CertGen.setPublicKey(KPair.getPublic());  
        v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");   
        
        X509Certificate PKCertificate = v3CertGen.generateX509Certificate(KPair.getPrivate());
        
        File f = File.createTempFile("SMSLib-", ".cert");
        FileOutputStream fos = new FileOutputStream(f);  
        fos.write(PKCertificate.getEncoded());  
        fos.close();  

		KeyStore privateKS = KeyStore.getInstance("JKS");  
		privateKS.load(null);
		
		privateKS.setKeyEntry(domainName + ".alias", KPair.getPrivate(),  
				KEYSTORE_PASSWORDd,  
                new java.security.cert.Certificate[]{PKCertificate});  
        
		return privateKS;
	}

	public void stop() throws IOException
	{
		if (connection != null) connection.close();
	}

	@Override
	public void handle(Request request, Response response)
	{
		try
		{
			HttpTask task = new HttpTask(request, response, this);
			executor.execute(task);
		}
		catch (Exception e)
		{
			logger.error("Error in HTTP dispatch!", e);
		}
	}

	public void registerHttpRequestHandler(String path, IHttpRequestHandler handler)
	{
		HttpServer.httpRequestHandlers.put(path, handler);
	}

	public void registerHttpRequestACL(String path, String cidr)
	{
		List<String> acl = HttpServer.httpRequestACLs.get(path);
		if (acl == null)
		{
			acl = new ArrayList<>();
			acl.add(cidr);
			HttpServer.httpRequestACLs.put(path, acl);
		}
		else acl.add(cidr);
	}

	HashMap<String, IHttpRequestHandler> getHttpRequestHandlers()
	{
		return HttpServer.httpRequestHandlers;
	}

	HashMap<String, List<String>> getHttpRequestACLs()
	{
		return HttpServer.httpRequestACLs;
	}
}
