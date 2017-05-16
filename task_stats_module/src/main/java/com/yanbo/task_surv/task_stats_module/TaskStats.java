package com.yanbo.task_surv.task_stats_module;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.yanbo.common.driver.MongoDbClient;

public class TaskStats {
	
	class TaskStatus
	{
		public String id="";
		public String description;
		public int original_status;//0失败，1成功，2正在运行
		public int computed_status;//0失败，1成功，2正在运行正常, 3完成但严重超时，4未完成且严重超时
		public String last_stage;
		public double duration = 0.0; //分钟
		public String start_time_str="";
		public String end_time_str="";
		public long start_time = 0l;
		public long stop_time = 0l;
				
		public TaskStatus(String id, String message, int status, int computed_status, String last_stage, double duration, 
				String start_time_str, String end_time_str, long start_time, long stop_time )
		{
			this.id = id;
			this.description = message;
			this.original_status = status;		
			this.last_stage = last_stage;
			this.duration = duration;	
			this.start_time_str = start_time_str;
			this.end_time_str = end_time_str;	
			this.start_time = start_time;
			this.stop_time = stop_time;	
			this.computed_status = computed_status;
		}
	}
	
	private static Logger logger = LoggerFactory.getLogger(TaskStats.class);
	private String _db_name;
	private String _step_status_col= "task_stage_status";
	private String _alert_col = "alert_col";
	private final  String first_stage_name = "first";
	private final  String final_stage_name = "final";
	private final  long time_threshold_in_minute = 60;//任务流超时时间，单位分钟
	private String toList = "";
	private String ccList = "";
	private String _db_uri;
	private String _mail_uri = "";
	public TaskStats(String db_server_uri, String db_name, String col_name, String mail_uri, String toList, String ccList)
	{
		_db_uri = db_server_uri;
		_db_name = db_name;
		_step_status_col= col_name;
		this.toList = toList;
		this.ccList = ccList;
		this._mail_uri = mail_uri;
	}
	
	public void setMail(String mail_uri, String toList, String ccList)
	{
		this.toList = toList;
		this.ccList = ccList;
		this._mail_uri = mail_uri;
	}
	
	public TaskStats(String db_server_uri)
	{
		_db_uri = db_server_uri;
		_db_name = "task_tracking_db";
		_step_status_col= "task_stage_status";
	}
	
	public int getVersion()
	{
		return Constant.version;
	}
	/**
	 * 有3种统计模式：
	 * 1.全量统计，在较长时间段里，统计在这个时间段里产生的任务的所有当前状况。
	 * 2.变化统计，高频次运行，在一个较短时间段内，扫描状态发生变化（update_time在统计区间内）的任务状况。
	 * 3.通知或报警，高频次运行，统计时间窗口内运行超时/成功/失败的任务，发邮件通知，并且记录在数据库里，不再重复发邮件。
	 * @param start_time 统计时间窗口起点
	 * @param end_time  统计时间窗口终点
	 * @param mode 统计模式
	 */
	public void StatsTasksWithAlert(long start_time, long  end_time, int mode)
	{		
		try {
			
			String text_body = StatsTasks(start_time, end_time, mode);			
			String title = getTitle(start_time, end_time, mode);

			logger.info(text_body);
			if(1 != mode &&  text_body.isEmpty())
			{
				//非全量统计时，没有需要报告的信息，不发送邮件
				logger.info("非全量统计时，没有需要报告的信息，不发送邮件");
				return;
			}
			sendMail(title,text_body );
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		}		
	}

	/**
	 * 
	 * 有3种统计模式：
	 * 1.全量统计，在较长时间段里，统计在这个时间段里产生的任务的所有当前状况。
	 * 2.变化统计，高频次运行，在一个较短时间段内，扫描状态发生变化（update_time在统计区间内）的任务状况。
	 * 3.通知或报警，高频次运行，统计时间窗口内运行超时/成功/失败的任务，发邮件通知，并且记录在数据库里，不再重复发邮件。
	 * @param start_time 统计时间窗口起点
	 * @param end_time  统计时间窗口终点
	 * @param mode 统计模式
	 * @return String
	  * 可以通过邮件发送的带格式的内容正文
	 */
	public String StatsTasks(long start_time, long end_time, int mode) {
		String text_body = "";
		
		List<TaskStatus> matching_stasks = GetTaskStatus(start_time,
				end_time, mode);
								
		for(TaskStatus taskStatus:matching_stasks)
		{
			text_body +=  "<br/>" + getStatusMessage(taskStatus) + "\n";
		}
		if(matching_stasks.size() == 0)
		{
			text_body = "无重要变化";
			if(3 == mode || 2 == mode)
			{
				//模式3是高频报警模式, 模式2也是较高频的更新统计模式，无变化的邮件自然就不用发了
				return "";
			}
		}
		
		String title = getTitle(start_time, end_time, mode);
		text_body = title + "\n" + "<br/>" + text_body + "\n";
		
		return text_body;
	}

	/**
	 * 
	 * 有3种统计模式：
	 * 1.全量统计，在较长时间段里，统计在这个时间段里产生的任务的所有当前状况。
	 * 2.变化统计，高频次运行，在一个较短时间段内，扫描状态发生变化（update_time在统计区间内）的任务状况。
	 * 3.通知或报警，高频次运行，统计时间窗口内运行超时/成功/失败的任务，发邮件通知，并且记录在数据库里，不再重复发邮件。
	 * @param start_time 统计时间窗口起点
	 * @param end_time  统计时间窗口终点
	 * @param mode 统计模式
	 * @return
	 */
	public List<TaskStatus> GetTaskStatus(long start_time, long end_time, int mode) {
			List<TaskStatus> matching_stasks = new ArrayList<TaskStatus>();
		
			MongoClient client = null;
			try {
				client = MongoDbClient.getInstance(_db_uri).getClient();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
			}
			if(client == null)
			{
				return matching_stasks;
			}
			MongoDatabase database = client.getDatabase(_db_name);
			MongoCollection<Document> step_status_collection = database.getCollection(_step_status_col);
			MongoCollection<Document> alert_collection = database.getCollection(_alert_col);
						
			String surveilance_time_type = "start_time";
			if(2 == mode)
			{
				surveilance_time_type = "update_time";
			}
			
			Document filter = new Document();
			Document start_time_range = new Document();
			start_time_range.append("$gte",start_time).append("$lte",end_time);						
			filter.put(surveilance_time_type, start_time_range);
			
			/* another kind of range value filter for a< x < b
			BasicDBList values = new BasicDBList();
			values.add(new BasicDBObject(surveilance_time_type,new BasicDBObject("$gte",start_time)));
			values.add(new BasicDBObject(surveilance_time_type,new BasicDBObject("$lte",end_time)));
			filter.put("$and", values);
			*/
			
			if(mode!=2)
			{
				filter.put("stage", first_stage_name);
			}
			FindIterable<Document> find = step_status_collection.find(filter);						
								
			for(Document doc : find)
			{
				if(!doc.getString("stage").equals(this.first_stage_name))
				{
					//如果捕捉到的doc并非在第一阶段，则需要找到对应的第一阶段doc
					
					Document filter_first = new Document();
					filter_first.put("id", doc.getString("id"));	
					filter_first.put("stage", this.first_stage_name);
					Document doc_first =  findOne(step_status_collection, filter_first);
					if(null == doc_first)
					{
						String error_doc_format = "任务%s 描述%s 阶段%s 未找到对应的初始阶段信息，忽略分析，请检查";
						logger.error(String.format(error_doc_format, doc.getString("id"), doc.getString("description"), doc.getString("stage")));
						continue;
					}
					doc = doc_first;//下面使用fist的doc来用
				}
				//务必保证输入的doc是first阶段的
				TaskStatus taskStatus = computeTraskProcedureCurrentStatusInfo(
						step_status_collection, doc);
				
				if(mode == 3)//报警模式
				{				
					if(2 == taskStatus.original_status && 2 == taskStatus.computed_status )
					{
						//忽略正常未完成的任务
						continue;
					}
					
					Document filter_alert = new Document();
					filter_alert.put("id", doc.getString("id"));				
					Document doc_alert =  findOne(alert_collection, filter_alert);
					if(null == doc_alert)//说明未发送过这条任务的情况
					{
						doc_alert = getAlertRecord(taskStatus);
						alert_collection.insertOne(doc_alert);
					}
					else//已经发送过，忽略掉
					{
						continue;
					}										
				}
				matching_stasks.add(taskStatus);				
			}		
		
		return matching_stasks;
	}

	private void sendMail(String title, String body)
	{
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		// 为了简单，无论如何都会创建一个httpclient
		connectionManager.setMaxTotal(10);
		CloseableHttpClient httpClient = HttpClients.custom()
						.setConnectionManager(connectionManager).build();
		
		String mail_para_format = "to=%s&copyto=%s&title=%s&html=%s";
        String to = this.toList;
        String copyto = this.ccList;
		String postData = String.format(mail_para_format, to,copyto,title,body);
		//创建http请求(post方式)  
        HttpPost httppost = new HttpPost(this._mail_uri);
        String strContainingChCode;
            
        try {
				strContainingChCode = new String(postData.getBytes("UTF-8"),"ISO-8859-1");
				StringEntity postStringData = new StringEntity(strContainingChCode);
				httppost.addHeader("Content-Type","text/html;charset=UTF-8");				
	            postStringData.setContentEncoding("UTF-8");
	            httppost.setEntity(postStringData);
	            CloseableHttpResponse response = httpClient.execute(httppost);
		} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
		}
            
	}
	private Document getAlertRecord(TaskStatus taskStatus) {
		Document alert_doc = new Document();
		alert_doc.put("id", taskStatus.id);
		alert_doc.put("message", taskStatus.description);
		alert_doc.put("last_stage", taskStatus.last_stage);
		alert_doc.put("duration", taskStatus.duration);
		alert_doc.put("status", taskStatus.original_status);
		return alert_doc;
	}

	private String getTitle(long start_time,  long end_time, int mode) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		Date current_date = calendar.getTime();
		String date_str = sdf.format(current_date);
		String time_window_start_str = sdf.format(start_time);
		String time_window_stop_str = sdf.format(end_time);
		String mode_str = "";
		switch(mode)
		{
			case 1:
				mode_str = "被创建的任务流状态全量统计";
				break;
			case 2:
				mode_str = "发生状态更新的任务流统计";
				break;
			case 3:
				mode_str = "有重要变化（成功，失败或者超时）的任务流统计";
				break;							
		}
		
		String title_format = "从%s到%s的时间范围内，%s, 统计时间%s";
		String title = String.format(title_format, time_window_start_str,time_window_stop_str, mode_str, date_str);
		return title;
	}

	private TaskStatus computeTraskProcedureCurrentStatusInfo(
			MongoCollection<Document> step_status_collection, Document doc1) {
		int computed_status = 0;
		String id = doc1.getString("id");
		long first_start_time =  doc1.getLong("start_time");
		String first_start_time_str =  doc1.getString("start_time_str");
		String description = doc1.getString("description");

		//查找是否有最后阶段的状态
		Document filter_final = new Document();
		filter_final.put("id", id);
		filter_final.put("stage", final_stage_name);
		Document doc_final =  findOne(step_status_collection, filter_final);

		//初始化一些状态默认值，下面可能会更新
		int  current_status = 0;
		String latest_update_time_str = "";
		long latest_update_time =  0l;
		String latest_state_str = "first_stage";				
		double duration = -1.0;

		String final_end_time_str = "";
		boolean final_stage_end = false;				
		if(doc_final!=null)
		{
			//System.out.println("final: " + doc_matching);
			latest_state_str = "final_stage";
			int final_status = doc_final.getInteger("status");
			current_status = final_status;
			latest_update_time_str = doc_final.getString("update_time_str");
			latest_update_time = doc_final.getLong("update_time");
					
			//已经有了结果，不论成功还是失败
			if(1 == final_status || 0 == final_status)
			{
				long final_end_time = doc_final.getLong("stop_time");
				latest_update_time = final_end_time;
				final_end_time_str = doc_final.getString("stop_time_str");
				duration = final_end_time - first_start_time;	
				latest_update_time_str = final_end_time_str;
				final_stage_end = true;
			}
		}
		//没有以最后阶段完结状态计算任务时长的情况
		if(!final_stage_end)
		{			
			//如果没到最后阶段，则需要搜索其他阶段(stage)确定项目到底在哪个阶段表里			
			Document filter_temp = new Document();
			filter_temp.put("id", id);
			FindIterable<Document> find_middle= step_status_collection.find(filter_temp);
			long last_stage_time = 0l;
			for(Document doc3: find_middle)
			{
				long temp_time = doc3.getLong("update_time");
				if(temp_time > last_stage_time)
				{
					last_stage_time = temp_time;
					latest_state_str = doc3.getString("stage");
					current_status = doc3.getInteger("status");
					latest_update_time_str = doc3.getString("update_time_str");
					latest_update_time = temp_time;
				}
			}						
		}
		//现在还没完的情况
		if(2 == current_status)
		{
			Date now = new Date();
			duration = now.getTime() - first_start_time;
		}
		else
		{
			duration = latest_update_time - first_start_time;
		}

		computed_status = current_status;
		if(duration > time_threshold_in_minute*60*1000)
		{
			computed_status = (2 == current_status )? 4 : 3;
		}
		
		return new TaskStatus(id, description, current_status, computed_status, latest_state_str, duration, 
				first_start_time_str, latest_update_time_str,first_start_time, latest_update_time );
	}
	
	private String getStatusMessage(TaskStatus taskStatus)
	{		
		String message = "";		
		String basic_format = "任务%s, 描述为%s， 于%s开始, 目前状态%s，所处阶段%s, %s, %s ";
		String extra_message = "";
		//初始化一些状态默认值，下面可能会更新
		String current_status_str = "正在运行中";
		String duration_str = "not computed";
		String duration_format = "耗时%f 分钟";				
		double duration_scaled = 0.0;
		if(2 != taskStatus.original_status)	
		{
			extra_message =  "全流程结束，结束时间: " +  taskStatus.end_time_str;			
		}
				
		if(taskStatus.duration > time_threshold_in_minute*60*1000)
		{
			String format =  "运行时间过长(阈值%d分钟)，请注意！ 最近一次状态变化时间:%s ";
			extra_message += String.format(format, this.time_threshold_in_minute,  taskStatus.end_time_str);
		}
		
		current_status_str = getStatueStr(taskStatus.original_status);
		BigDecimal   b   =   new   BigDecimal(taskStatus.duration/1000/60.0);  
		duration_scaled   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();  	
		duration_str = 		String.format(duration_format, duration_scaled);
		
	    message = String.format(basic_format, taskStatus.id, taskStatus.description, taskStatus.start_time_str, current_status_str, taskStatus.last_stage, duration_str, extra_message );

		return message;
	}
	
	private String getStatueStr(int status)
	{
		String message = "";
		switch(status)
		{
		case 0:
			message = "执行失败";
			break;
		case 1:
			message = "执行成功";
			break;
		case 2:
			message = "正在运行 ";
			break;
		
		}
		return message;
	}
	
	public static Document findOne(MongoCollection<Document> col, Document filter)
	{
		Document doc_matching = null;
		FindIterable<Document> find= col.find(filter).limit(1);
		for(Document doc: find)
		{
			doc_matching = doc;
			break;
		}
		return doc_matching;
	}


}
