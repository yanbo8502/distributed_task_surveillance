package com.le.bigdata.audience.task_record;


import com.yanbo.task_surv.task_stats_module.TaskStats;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    { 
    	TaskStats stats = new TaskStats( "mongodb://localhost:27017");
       String mail_uri = "todo";
    	stats.setMail(mail_uri, "yanbo@le.com", "yanbo@le.com");
    	stats.StatsTasksWithAlert(1490902951354l, 1490952951354l, 2);
        assertTrue( true );
    }
}
