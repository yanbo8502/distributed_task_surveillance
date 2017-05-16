package com.yanbo.common.driver;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;

public class MongoDbClient {
	private static Logger logger = LoggerFactory.getLogger(MongoDbClient.class.getName());
	
	private static  MongoDbClient st = null;
	private MongoClient client = null;
	private String mongo_uri;
    
    public MongoDbClient(String host_uri) throws UnknownHostException{
    	logger.info("start make mongodb connection");
    	mongo_uri = host_uri;
        init();
    }  
      
    public static MongoDbClient getInstance(String uri) throws UnknownHostException{
        if (null == st) {  
            synchronized (MongoDbClient.class) {  
                if (st == null) {  
                    st = new MongoDbClient(uri);  
                    return st;  
                }  
            }  
        }  
        return st;  
    }
    
    public void close()
    {
    	if(client!=null)
    	{
        	client.close();		
    	}

    }
    
    public MongoClient getClient()
    {
    	return client;
    }
    
	private void init() throws UnknownHostException {

			MongoClientURI uri = new MongoClientURI(mongo_uri);
	
			MongoClientOptions opt = MongoClientOptions.builder().build();
			client = new MongoClient(uri);

			MongoClientOptions opt1 = client.getMongoClientOptions();
			logger.info("mongo client with settings: connection pool size: " + opt1.getConnectionsPerHost());
	}
}
