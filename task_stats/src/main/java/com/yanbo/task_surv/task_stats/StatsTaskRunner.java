package com.yanbo.task_surv.task_stats;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanbo.task_surv.task_stats_module.TaskStats;


public class StatsTaskRunner implements Runnable
{
	private static Logger logger = LoggerFactory.getLogger(StatsTaskRunner.class);
	private String _db_name= "";
	private String _step_status_col = "";
	private long stats_window_length;//单位秒
	private int mode = 1;
	private String label = "";
	private String toList;
	private String ccList;
	public StatsTaskRunner(String db_name, String col_name, long stats_window_length, int mode, String label, String toList, String ccList)
	{
		_db_name = db_name;
		_step_status_col= col_name;
		this.stats_window_length = stats_window_length;
		this.mode  = mode;
		this.label = label;
		this.toList = toList;
		this.ccList = ccList;
	}
	@Override
	public void run() {
		String format = "%s:%s:监控数据库%s,表%s, 时间窗口起点%d,终点%d, 模式%d";
		Date time = new Date();
		long stats_window_start = time.getTime() - stats_window_length*1000;
		long stats_window_end = time.getTime();
		String run_log = String.format(format, "StatsTaskRunner", label, _db_name,_step_status_col, 
				stats_window_start, stats_window_end, mode);
		System.out.println(run_log); 
		logger.info(run_log);
        TaskStats task_stats = new TaskStats(Constant.mongo_uri,  _db_name, _step_status_col,  Constant.mail_uri , toList, ccList); 
        task_stats.StatsTasksWithAlert(stats_window_start, stats_window_end, mode);				
	}
	
}