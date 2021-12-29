package org.apache.zookeeper.inspector.manager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import lombok.Data;
import lombok.ToString;

@Data
public class ZookeeperProperties {
	
	private static final String PROP_CONNECTION_STRING = "hosts";
	private static final String PROP_SESSION_TIMEOUT = "timeout";
	private static final String PROP_ENCRYPTION_MANAGER = "encryptionManager";
	private static final String PROP_AUTH_SCHEME = "authScheme";
	private static final String PROP_AUTH_DATA = "authData";
	private static final String PROP_CLIENT_SECURE = "clientSecure";
	private static final String PROP_KEYSTORE_LOCATION = "keystoreLocation";
	private static final String PROP_KEYSTORE_PASSWORD = "keystorePassword";
	private static final String PROP_TRUSTSTORE_LOCATION = "truststoreLocation";
	private static final String PROP_TRUSTSTORE_PASSWORD = "truststorePassword";
	
    private String connectionString = "localhost:2181";
    private int sessionTimeoutMs = 10_000;
//    private int connectionTimeoutMs = 15_000;
//    private int baseSleepTimeMs = 1_000;
    private int maxRetries = 10;
//    private int requestTimeoutMs = 500;
    private String authScheme;
    private String authData;
    private String encryptionManager;
    private boolean clientSecure = false;
    private String keystoreLocation;
    @ToString.Exclude
    private String keystorePassword;
    private String truststoreLocation;
    @ToString.Exclude
    private String truststorePassword;
    
	public void load(Reader reader) throws IOException {
    	Properties props = new Properties();
    	props.load(reader);
    	props.forEach((key, value) -> {
    		if (value == null) {
    			return;
    		}
    		String valueStr = String.valueOf(value);
    		switch(String.valueOf(key)) {
    			case PROP_CONNECTION_STRING:
    				connectionString = valueStr;
    				break;
    			case PROP_SESSION_TIMEOUT:
    				sessionTimeoutMs = Integer.valueOf(valueStr);
    				break;
    			case PROP_ENCRYPTION_MANAGER:
    				encryptionManager = valueStr;
    				break;
    			case PROP_AUTH_SCHEME:
    				authScheme = valueStr;
    				break;
    			case PROP_AUTH_DATA:
    				authData = valueStr;
    				break;
    			case PROP_CLIENT_SECURE:
    				clientSecure = Boolean.valueOf(valueStr);
    				break;
    			case PROP_KEYSTORE_LOCATION:
    				keystoreLocation = valueStr;
    				break;
    			case PROP_KEYSTORE_PASSWORD:
    				keystorePassword = valueStr;
    				break;
    			case PROP_TRUSTSTORE_LOCATION:
    				truststoreLocation = valueStr;
    				break;
    			case PROP_TRUSTSTORE_PASSWORD:
    				truststorePassword = valueStr;
    				break;
    		}
    	});
	}

	public void store(Writer writer, String comment) throws IOException {
    	Properties props = new Properties();
    	props.setProperty(PROP_CONNECTION_STRING, connectionString);
    	props.setProperty(PROP_SESSION_TIMEOUT, String.valueOf(sessionTimeoutMs));
    	props.setProperty(PROP_ENCRYPTION_MANAGER, encryptionManager);
    	props.setProperty(PROP_CLIENT_SECURE, String.valueOf(clientSecure));
    	if (authScheme != null && !"".equals(authScheme)) {
    		props.setProperty(PROP_AUTH_SCHEME, authScheme);
    	}
    	if (authData != null && !"".equals(authData)) {
    		props.setProperty(PROP_AUTH_DATA, authData);
    	}
    	if (keystoreLocation != null && !"".equals(keystoreLocation)) {
    		props.setProperty(PROP_KEYSTORE_LOCATION, keystoreLocation);
    	}
    	if (keystorePassword != null && !"".equals(keystorePassword)) {
    		props.setProperty(PROP_KEYSTORE_PASSWORD, keystorePassword);
    	}
    	if (truststoreLocation != null && !"".equals(truststoreLocation)) {
    		props.setProperty(PROP_TRUSTSTORE_LOCATION, truststoreLocation);
    	}
    	if (truststorePassword != null && !"".equals(truststorePassword)) {    	
    		props.setProperty(PROP_TRUSTSTORE_PASSWORD, truststorePassword);
    	}
    	props.store(writer, comment);
	}
}
