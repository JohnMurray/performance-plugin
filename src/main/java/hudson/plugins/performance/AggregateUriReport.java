package hudson.plugins.performance;

import hudson.util.ChartUtil;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.jfree.data.xy.XYDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class AggregateUriReport extends UriReport implements Serializable {

  private static final long serialVersionUID = -8703150813285290763L;
  
  private Date date;
  private int requestCount;
  private int errorCount;
  private long averageResponseTime;
  private double totalRequestSizeInKb;
  private long maxResponseTime;
  private long minResponseTime;
  private String httpCodes = null;

  private long _90Line;
  private boolean has90Line = false;
  
  private long median;
  private boolean hasMedian = false;

  public AggregateUriReport(PerformanceReport performanceReport, String staplerUri, String uri) {
    super(performanceReport, staplerUri, uri);
  }
  
  
  // Abstract method implementations
  
  /**
   * Return the error-count
   */
  public int countErrors() {
    return this.errorCount;
  }
  
  /**
   * Return the average response time in milliseconds
   */
  public long getAverage() {
    return this.averageResponseTime;
  }

  /**
   * Return the total request size in KB
   */
  public double getTotalTrafficInKb() {
    return this.totalRequestSizeInKb;
  }
  
  /**
   * Get the 90 line. Since this is an aggregated/summarized report, we don't
   * really have a 90-line. To keep consistent with prior passing tests for
   * the JMeterSummarizerParser, we'll return the max response time or the 90
   * line if provided explicitly. 
   * 
   * Note: We could attempt to guess the 90 line with something like:
   *       (long) ((maxResponseTime - averageResponseTime) * 0.9) + averageResponseTime
   */
  public long get90Line() {
    if (has90Line) {
      return this._90Line;
    } else {
      return maxResponseTime;
    }
  }
  
  /**
   * Return a comma delimited list of all status-codes returned. Since we're dealing
   * with aggregate data, either return nothing or return a preset list.
   * 
   * @return
   */
  public String getHttpCode() {
    if (httpCodes != null) {
      return httpCodes;
    }
    return "";
  }
  
  /**
   * Since we don't have access to individual requests, return the average.
   * However, if the median is provided by the creator of the report, then
   * that can be returned instead.
   */
  public long getMedian() {
    if (hasMedian) { return median; }
    return this.averageResponseTime;
  }
  
  /**
   * Return the max response time
   */
  public long getMax() {
    return this.maxResponseTime;
  }
  
  /**
   * Return the min response time
   */
  public long getMin() {
    return this.minResponseTime;
  }

  /**
   * Return the number of requests made
   */
  public int size() {
    return this.requestCount;
  }
  
  /**
   * Generate a trend graph. Since we have no samples, we can't really generate a
   * trend graph. So instead we basically set it to a "nothing" graph.
   */
  public void doSummarizerTrendGraph(StaplerRequest request,
      StaplerResponse response) throws IOException {

    ArrayList<XYDataset> dataset = new ArrayList<XYDataset>();

    ChartUtil.generateGraph(request, response,
        PerformanceProjectAction.createSummarizerTrend(dataset, uri), 400, 200);

  }

  /**
   * Given a DataSetBuilder, populate the builder with averages
   * information.
   * 
   * @param dataSet  DataSetBuilde<K,V>
   *          K (String)                URI of report/sample
   *          V (NumberOnlyBuildLabel)  Label provided as second parameter
   * @param label    Label to use while building data-set
   */
  public void buildAverageDataSet(DataSetBuilder<String, NumberOnlyBuildLabel> dataSet,
      NumberOnlyBuildLabel label) {
    buildAverageDataSetWithUriFilter(dataSet, label, null);
  }

  public void buildAverageDataSetWithUriFilter(DataSetBuilder<String, NumberOnlyBuildLabel> dataSet,
      NumberOnlyBuildLabel label, String filter) {
    if (filter == null || uri.equals(filter)) {
      for (int i = 0; i < size() - countErrors(); i++) {
        dataSet.add(getAverage(), getUri(), label);
      }
      for (int i = 0; i < countErrors(); i++) {
        dataSet.add(0, getUri(), label);
      }
    }
  }
  
  
  
  // getters & setters

  
  /**
   * Set the total number (count) of requests made, both successful and
   * failed requests.
   * 
   * @param count Number of requests made
   */
  public void setRequestCount(int count) {
    this.requestCount = count;
  }
  
  /**
   * Set number of non-2xx or non-3xx status codes sampled
   * 
   * @param count
   */
  public void setErrorCount(int count) {
    this.errorCount = count;
  }
  
  /**
   * Set the average response time (across all requests sampled/
   * considered).
   * 
   * @param avg  Average response time (in milliseconds)
   */
  public void setAverageResponseTime(long avg) {
    this.averageResponseTime = avg;
  }
  
  /**
   * Set the total request size (in KB)
   * @param size in KB
   */
  public void setTotalRequestSizeInKb(double size) {
    this.totalRequestSizeInKb = size;
  }
  
  /**
   * Set the max response time
   * 
   * @param time Response time in milliseconds
   */
  public void setMaxResponseTime(long time) {
    this.maxResponseTime = time;
  }
  
  /**
   * Set the min response time
   * 
   * @param time Response time in milliseconds
   */
  public void setMinResponseTime(long time) {
    this.minResponseTime = time;
  }
  
  /**
   * If the 90-line is known, then set it.
   * 
   * @param line the 90-line
   */
  public void set90Line(long line) {
    this.has90Line = true;
    this._90Line = line;
  }
  
  /**
   * Set a comma-delimited list of HTTP codes that represents a *unique* set
   * of codes generated by the test-run.
   * 
   * @param codes
   */
  public void setHttpCodes(String codes) {
    this.httpCodes = codes;
  }
  
  /**
   * If the median response time is known, then set it.
   * @param median  response time (in milliseconds)
   */
  public void setMedian(long median) {
    this.hasMedian = true;
    this.median = median;
  }
  
  public void setDate(Date d) { this.date = d; }
  public Date getDate() { return this.date; }

}
