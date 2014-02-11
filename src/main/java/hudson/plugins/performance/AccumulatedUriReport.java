package hudson.plugins.performance;

import hudson.util.ChartUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class AccumulatedUriReport extends UriReport implements Serializable {

  private static final long serialVersionUID = -4679656174382939061L;

  /**
   * Individual HTTP invocations to this URI and how they went.
   */
  protected final List<HttpSample> httpSampleList = new ArrayList<HttpSample>();

  public AccumulatedUriReport(PerformanceReport performanceReport, String staplerUri, String uri) {
    super(performanceReport, staplerUri, uri);
  }
  
  
  
  
  // instance methods
  
  /**
   * Add an HttpSample to the URI report. This will be used to calculate
   * the overall metrics for a given URI.
   * 
   * @param httpSample
   */
  public void addHttpSample(HttpSample httpSample) {
    httpSampleList.add(httpSample);
  }
  
  
  
  // abstract method implementations
  
  /**
   * Return the error-count over all samples
   */
  public int countErrors() {
    int nbError = 0;
    for (HttpSample currentSample : httpSampleList) {
      if (!currentSample.isSuccessful()) {
        nbError++;
      }
    }
    return nbError;
  }
  
  /**
   * Get the average response time over all samples
   */
  public long getAverage() {
    long average = 0;
    for (HttpSample currentSample : httpSampleList) {
      average += currentSample.getDuration();
    }
    return average / size();
  }
  
  /**
   * Returns the total amount of data sent in requests
   */
  public double getTotalTrafficInKb(){ 
	  double traffic = 0 ; 
	  for (HttpSample currentSample : httpSampleList) {
		  traffic += currentSample.getSizeInKb();
	    }
	    return roundTwoDecimals(traffic);
  }


  public long get90Line() {
    long result = 0;
    Collections.sort(httpSampleList);
    if (httpSampleList.size() > 0) {
      result = httpSampleList.get((int) (httpSampleList.size() * .9)).getDuration();
    }
    return result;
  }

  /**
   * Return a comma delimited list of all status-codes returned
   * @return
   */
  public String getHttpCode() {
    String result = "";
    
    for (HttpSample currentSample : httpSampleList) {
      if ( !result.matches( ".*"+currentSample.getHttpCode()+".*" ) ) {
          result += ( result.length() > 1 ) ? ","+currentSample.getHttpCode() : currentSample.getHttpCode();
      }
    }
    
    return result;
  }

  /**
   * Return the median resposne time over all samples
   */
  public long getMedian() {
    long result = 0;
    Collections.sort(httpSampleList);
    if (httpSampleList.size() > 0) {
      result = httpSampleList.get((int) (httpSampleList.size() * .5)).getDuration();
    }
    return result;
  }
  
  /**
   * Get the max response time out of all samples
   */
  public long getMax() {
    long max = Long.MIN_VALUE;
    for (HttpSample currentSample : httpSampleList) {
      max = Math.max(max, currentSample.getDuration());
    }
    return max;
  }
  
  /**
   * Get the min response time out of all samples
   */
  public long getMin() {
    long min = Long.MAX_VALUE;
    for (HttpSample currentSample : httpSampleList) {
      min = Math.min(min, currentSample.getDuration());
    }
    return min;
  }

  /**
   * Return the number of requests made
   */
  public int size() {
    return httpSampleList.size();
  }
  
  /**
   * Generate a trend graph for all samples
   */
  public void doSummarizerTrendGraph(StaplerRequest request,
      StaplerResponse response) throws IOException {

    ArrayList<XYDataset> dataset = new ArrayList<XYDataset>();
    TimeSeriesCollection resp = new TimeSeriesCollection();
    TimeSeries responseTime = new TimeSeries("Response Time", FixedMillisecond.class);

    for (int i = 0; i <= this.httpSampleList.size() - 1; i++) {
      RegularTimePeriod current = new FixedMillisecond(this.httpSampleList.get( i).getDate());
      responseTime.addOrUpdate(current, this.httpSampleList.get(i).getDuration());
    }

    resp.addSeries(responseTime);
    dataset.add(resp);

    ChartUtil.generateGraph(request, response,
        PerformanceProjectAction.createSummarizerTrend(dataset, uri), 400, 200);

  }
  
  
  
  
  // getters and setters

  public List<HttpSample> getHttpSampleList() {
    return httpSampleList;
  }
  

}
