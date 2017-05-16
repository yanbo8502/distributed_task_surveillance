package com.yanbo.common.driver;

import java.io.Reader;

public class MysqlClient {
	private static Reader reader;
	private static class LazyHolder{
		private static final MysqlClient client = new MysqlClient(); 
	}
	private MysqlClient(){
	}
	
	public static MysqlClient getClient() {
		return LazyHolder.client;
	}

}
