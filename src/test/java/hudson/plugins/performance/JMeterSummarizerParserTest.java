package hudson.plugins.performance;

import static org.junit.Assert.*;

import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

public class JMeterSummarizerParserTest {
  
  private JmeterSummarizerParser parser;
  private PrintStream logger;
  private TaskListener listener;

  @Before
  public void beforeTest() {
    // custom extension just to keep things sane in our resources
    parser = new JmeterSummarizerParser("**/*.jms", null);
    logger = System.out;
    listener = new StreamTaskListener((java.io.OutputStream) logger);
  }

  @Test
  /**
   * Just take a test file and ensure that things look correct based on the sample file being
   * tested against. 
   */
  public void testCanParse() {
    List<File> files = new ArrayList<File>(1);
    files.add(new File(getClass().getResource("/JMeterSummarizer.jms").getFile()));
    
    try {
      Collection<PerformanceReport> reports = parser.parse(null, files, listener);
      assertTrue(reports.size() > 0);
      for (PerformanceReport report : reports) {
        assertTrue(report.size() > 0);
        
        // some assertions based on the test-report
        assertEquals(report.get90Line(), 69);
        assertEquals(report.getAverage(), 18);
        assertEquals(report.getAverageSizeInKb(), 0f, 0f);  // untracked at the moment
        assertEquals(report.getMax(), 69);
        assertEquals(report.getMin(), 1);
        assertEquals(report.getMedian(), 1);
        
        for (UriReport uriReport : report.getUriReportMap().values()) {
          assertEquals(uriReport.errorPercent(), 0f, 0f);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.println(e.getMessage());
      fail(e.getMessage());
    }
  }

}
