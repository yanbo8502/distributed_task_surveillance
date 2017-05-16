package com.yanbo.common.driver;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class EsClient {
	private Client client;
	private static class LazyHolder{
		private static final EsClient esclient = new EsClient(); 
	}
	private EsClient(){
		SAXReader reader = new SAXReader();  
		try {
			Document document = reader.read(new File("conf/esconf.xml")); 
			Element root = document.getRootElement(); 
			Settings settings = ImmutableSettings.settingsBuilder().put(root.element("cluster").elementText("key"), root.element("cluster").elementText("value")).build();
			client = new TransportClient(settings);
			List<Element> Transport=root.element("transport").elements();
			for(Element e:Transport){
				((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(e.elementText("ip"), Integer.valueOf((e.elementText("port")))));
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static EsClient getEsClient() {
		return LazyHolder.esclient;
	}

	public Client getClient() {
		return client;
	}
}
