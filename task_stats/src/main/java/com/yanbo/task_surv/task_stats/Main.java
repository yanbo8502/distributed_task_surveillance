package com.yanbo.task_surv.task_stats;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class Main 
{
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) throws Exception {
		  // Create a Parser 
		  CommandLineParser parser = new DefaultParser();
		  Options options = new Options( );
		  options.addOption("h", "help", false, "Print this usage information");
		  options.addOption("d", "db", true, "optional, record database name, default is task_tracking_db" );
		  options.addOption("t", "table", true, "optional, record collection name, default is task_stage_status");
		  options.addOption("c", "cron", false, "optional,  if is arg exists, the program will run in periodical mode");
		  options.addOption("i", "interval", true, "optional, alert surveillance time window， unit is seconds, default is 30 minutes");
		  options.addOption("cc", "cc-list", true, "mail cc address, comma seperated, aaa@le.com,bbb@le.com");
		  options.addOption("to", "to-list", true, "mail to address, comma seperated,  aaa@le.com,bbb@le.com");
		  // Parse the program arguments
		  CommandLine commandLine = parser.parse( options, args );
		  // Set the appropriate variables based on supplied options
		  String db_name = "task_tracking_db";
		  String collection_name = "task_stage_status";
		  boolean is_cron = false;
		  long interval = 30*60;
		  String cc_list = "";
		  String to_list = "";
		 
		  if( commandLine.hasOption('h') ) {
		    System.out.println( "Help Message");
		    
		    for(Option option : options.getOptions())
		    {
		    	System.out.println(option.getOpt() + " " + "--" + option.getLongOpt() + " " + option.getDescription());
		    }
		    System.exit(0);
		  }
		  if( commandLine.hasOption('d') ) {
			  db_name = commandLine.getOptionValue('d');
		  }
		  if( commandLine.hasOption('t') ) {
			  collection_name = commandLine.getOptionValue('t');
		  }
		  if( commandLine.hasOption("c") ) {
			  is_cron = true;
		  }
		  if( commandLine.hasOption("i") ) {
			  String interval_str = commandLine.getOptionValue("i");
			  interval = Integer.parseInt(interval_str);
		  }
		  if( commandLine.hasOption("cc") ) {
			 cc_list = commandLine.getOptionValue("cc");
		  }
		  if( commandLine.hasOption("to") ) {
			 to_list = commandLine.getOptionValue("to");
		  }
		  else
		  {
			  System.out.println("missing arguments to-list");
			  System.exit(1);
		  }
		  
		  addDailyFullStatsTask(is_cron,db_name, collection_name, to_list, cc_list);
		  addDailyUpdateStatsTask(is_cron,db_name, collection_name, to_list, cc_list);
		  addIncremantlStatsAlertTask(is_cron,interval,db_name, collection_name, to_list, cc_list);
		}
	
		//线程池能按时间计划来执行任务，允许用户设定计划执行任务的时间，int类型的参数是设定    
	        //线程池中线程的最小数目。当任务较多时，线程池可能会自动创建更多的工作线程来执行任务    
	        //此处用Executors.newSingleThreadScheduledExecutor()更佳。  
	    public static ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);    
   
	       //添加新任务    
	    public static void addDailyFullStatsTask(boolean is_cron,String db_name, String col_name, String to, String cc){ 
	        
	    	Runnable task = new StatsTaskRunner(db_name, col_name, 24*60*60l, 1, "DailyFullStatsTask", to ,cc);    

	        if(is_cron)
	        {
	        	 scheduExec.scheduleWithFixedDelay(task, 0, 12, TimeUnit.HOURS);  
	        }
	        else
	        {
	        	task.run();
	        }
	    }  
	    
	      //添加新任务    
	    public static void addDailyUpdateStatsTask(boolean is_cron,String db_name, String col_name, String to, String cc){ 
	    	Date time = new Date();
	    	Runnable task = new StatsTaskRunner(db_name, col_name, 1*60*60l, 
	    			 2, "DailyUpdateStatsTask", to ,cc);    

	        if(is_cron)
	        {
	        	 scheduExec.scheduleWithFixedDelay(task, 0, 1, TimeUnit.HOURS);  
	        }
	        else
	        {
	        	task.run();
	        }
	    }  
	    
	    //添加新任务    
	    public static void addIncremantlStatsAlertTask(boolean is_cron, long interval_by_second,String db_name
	    		, String col_name, String to, String cc){ 
	    	Date time = new Date();
	    	Runnable task = new StatsTaskRunner(db_name, col_name, 24*60*60l, 
	    			3, "IncremantlStatsAlertTask", to ,cc);    

	        if(is_cron)
	        {
	        	 scheduExec.scheduleWithFixedDelay(task, 0, interval_by_second, TimeUnit.SECONDS);
	        }
	        else
	        {
	        	task.run();
	        }
	        	           
	    }
}
