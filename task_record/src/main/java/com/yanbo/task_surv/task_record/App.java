package com.yanbo.task_surv.task_record;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.yanbo.task_surv.task_stats_module.TaskStatus;

/**
 * Hello world!
 */
public class App 
{
	public static void main(String[] args) throws Exception {
		  // Create a Parser 
		  CommandLineParser parser = new DefaultParser();
		  Options options = new Options( );
		  options.addOption("h", "help", false, "Print this usage information");
		  options.addOption("d", "db", true, "optional, record database name, default is task_tracking_db" );
		  options.addOption("c", "collection", true, "optional, record collection name, default is task_stage_status");
		  options.addOption("s", "status", true, "task status, 0 is fail, 1 is success, 2 is running");
		  options.addOption("n", "id", true, "task id name");
		  options.addOption("i", "description", true, "optional, task status description");
		  options.addOption("m", "stage", true, "the stage of the task procedure");
		  // Parse the program arguments
		  CommandLine commandLine = parser.parse( options, args );
		  // Set the appropriate variables based on supplied options
		  String db_name = "task_tracking_db";
		  String collection_name = "task_stage_status";
		  String task_id ="null";
		  int status = 0;
		  String description = "";
		  String stage = "default";
		 
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
		  if( commandLine.hasOption('c') ) {
			  collection_name = commandLine.getOptionValue('c');
		  }
		  if( commandLine.hasOption("status") ) {
			  status = Integer.parseInt(commandLine.getOptionValue("status"));
		  }
		  else
		  {
			  System.out.println("missing args status...");
			  System.exit(1);
		  }
		  if( commandLine.hasOption("id") ) {
			  task_id = commandLine.getOptionValue("id");
		  }
		  else
		  {
			  System.out.println("missing args id...");
			  System.exit(1);
		  }
		  
		  if( commandLine.hasOption("stage") ) {
			  stage = commandLine.getOptionValue("stage");
		  }
		  else
		  {
			  System.out.println("missing args stage...");
			  System.exit(1);
		  }
		  if( commandLine.hasOption("description") ) {
			  description = commandLine.getOptionValue("description");
		  }
		  
		  TaskStatus task_status = new TaskStatus(db_name, collection_name,  Constant.mongo_uri);
		  if(!task_status.setTaskStatus(task_id, stage, status, description))
		  {
			  System.exit(1);
		  }
		}
}
