package com.yanbo.task_surv.task_stats;

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
    	stats.StatsTasksWithAlert(1489667828931l, 1489668300601l, 1);
        assertTrue( true );
    }
}
