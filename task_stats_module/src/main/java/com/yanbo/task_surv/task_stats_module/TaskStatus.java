package com.yanbo.task_surv.task_stats_module;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.yanbo.common.driver.MongoDbClient;

public class TaskStatus {
	
	private String db_name = "task_tracking_db";
	private String collection_name = "task_stage_status";
	private String _db_uri;
	public TaskStatus(String db_server_uri)
	{
		_db_uri = db_server_uri;
	}
	/**
	 * 
	 * @param db_name  记录任务流各阶段状态的库名
	 * @param collection_name  记录任务流各阶段状态的表名
	 * @param db_server_uri 数据库服务器的地址
	 */
	public TaskStatus(String db_name, String collection_name, String db_server_uri)
	{
		this.db_name = db_name;
		this.collection_name = collection_name;
		_db_uri = db_server_uri;
	}
	/**
	 * 
	 * @param db_name  记录任务流各阶段状态的库名
	 * @param collection_name  记录任务流各阶段状态的表名
	 */
	public void setDataSource(String db_name, String collection_name)
	{
		this.db_name = db_name;
		this.collection_name = collection_name;
	}
	/***
	 * 根据输入参数，在数据库的相关库/表里插入/更新一条记录，在记录里会根据status参数，自动判断并记录该任务阶段的开始或者结束时间
	 * @param trackingId  用于把统一任务流不同阶段匹配起来的一个任务流ID
	 * @param stage 任务流所处的阶段
	 * @param status  阶段的状态, 失败：0， 成功：1，正在运行：2
	 * @param description 对任务情况的描述，如果输入“”（空字符串），则不会更新数据库的相关字段
	 * @return 更新成功返回true，失败返回false
	 */
	public boolean setTaskStatus(String trackingId, String stage, int status, String description){
		boolean success = false;
		try {
			MongoClient client = MongoDbClient.getInstance(_db_uri).getClient();
			MongoCollection<Document> collection = client.getDatabase(db_name).getCollection(collection_name);

			Document index = new Document();
			index.append("id", 1);	
			collection.createIndex((Bson)index, new IndexOptions().unique(false));
			//先看同一个stage有没有之前的记录
			Document filter = new Document();
			filter.append("id", trackingId);
			filter.append("stage", stage);
			
			Document status_doc = TaskStats.findOne(collection, filter);
		
			if(null == status_doc)
			{
				status_doc = new Document();
				status_doc.append("id", trackingId);
				status_doc.append("status", status).append("status", status).append("status", status);
			}
			status_doc.put("status", status);
			
			if(description!=null && !description.isEmpty())
			{
				status_doc.put("description", description);
			}		
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			Date current_date = calendar.getTime();
			String date_str = sdf.format(current_date);
			status_doc.put("update_time_str", date_str);
			status_doc.put("update_time", current_date.getTime());
			
			if((1 == status || 0 == status) && !status_doc.containsKey("stop_time") )
			{
				status_doc.put("stop_time_str", date_str);
				status_doc.put("stop_time", current_date.getTime());
			}
			else if(2 == status&& !status_doc.containsKey("start_time"))
			{
				status_doc.put("start_time_str", date_str);
				status_doc.put("start_time", current_date.getTime());
			}
			
			collection.updateMany(filter,new Document("$set", status_doc), new UpdateOptions().upsert(true));
			success = true;
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
		
	}
	
}
