package hudson.plugins.performance;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.*;
import java.text.DecimalFormat;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * A report about a particular tested URI.
 * 
 * This object belongs under {@link PerformanceReport}.
 */
public abstract class UriReport extends AbstractReport implements  Serializable, ModelObject,
    Comparable<UriReport> {

  private static final long serialVersionUID = 6377220939528230222L;

  public final static String END_PERFORMANCE_PARAMETER = ".endperformanceparameter";

  /**
   * The parent object to which this object belongs.
   */
  protected final PerformanceReport performanceReport;

  /**
   * Escaped {@link #uri} that doesn't contain any letters that cannot be used
   * as a token in URL.
   */
  private String staplerUri;
  
  protected UriReport lastBuildUriReport;

  protected String uri;

  UriReport(PerformanceReport performanceReport, String staplerUri, String uri) {
    this.performanceReport = performanceReport;
    this.staplerUri = staplerUri;
    this.uri = uri;
  }

  /**
   * Compare UriReports based on the URI. This ensures that we can avoid
   * creating multiple reports for the same URI.
   */
  public int compareTo(UriReport uriReport) {
    if (uriReport == this) {
      return 0;
    }
    return uriReport.getUri().compareTo(this.getUri());
  }
  
  
  
  
  // abstract methods

  public abstract int countErrors();
  public abstract long getAverage();
  public abstract long get90Line();
  /**
   * Return a comma delimited list of all status-codes returned
   * @return
   */
  public abstract String getHttpCode();
  public abstract long getMedian();
  public abstract long getMax();
  public abstract long getMin();
  public abstract double getTotalTrafficInKb();
  public abstract int size();
  /**
   * Given a DataSetBuilder, populate the builder with averages
   * information.
   * 
   * @param dataSet  DataSetBuilde<K,V>
   *          K (String)                URI of report/sample
   *          V (NumberOnlyBuildLabel)  Label provided as second parameter
   * @param label    Label to use while building data-set
   */
  public abstract void buildAverageDataSet(DataSetBuilder<String, NumberOnlyBuildLabel> dataSet,
      NumberOnlyBuildLabel label);
  public abstract void buildAverageDataSetWithUriFilter(DataSetBuilder<String, NumberOnlyBuildLabel> dataSet,
      NumberOnlyBuildLabel label, String filter);
  
  
  
  // concrete methods

  public double errorPercent() {
    return ((double) countErrors()) / size() * 100;
  }

  public AbstractBuild<?, ?> getBuild() {
    return performanceReport.getBuild();
  }

  public String getDisplayName() {
    return getUri();
  }

  public String getShortUri() {
    if ( uri.length() > 130 ) {
        return uri.substring( 0, 129 );
    }
    return uri;
  }
  
  public double getAverageSizeInKb() {
    return roundTwoDecimals(getTotalTrafficInKb() / (double)size());
  }

  public String encodeUriReport() throws UnsupportedEncodingException {
    StringBuilder sb = new StringBuilder(120);
    sb.append(performanceReport.getReportFileName()).append(
        GraphConfigurationDetail.SEPARATOR).append(getStaplerUri()).append(
        END_PERFORMANCE_PARAMETER);
    return URLEncoder.encode(sb.toString(), "UTF-8");
  }

  /**
   * Simple utility function to round long decimals to have at most a
   * precision of two.
   * 
   * @param d  Double value to round
   */
  protected double roundTwoDecimals(double d) {
    DecimalFormat twoDForm = new DecimalFormat("#.##");
    return Double.valueOf(twoDForm.format(d));
  }
  
  
  
  // diff methods (possibly should be abstracted)
  public long getAverageDiff() {
      if ( lastBuildUriReport == null ) {
          return 0;
      }
      return getAverage() - lastBuildUriReport.getAverage();
  }
  
  public long getMedianDiff() {
      if ( lastBuildUriReport == null ) {
          return 0;
      }
      return getMedian() - lastBuildUriReport.getMedian();
  }
  
  public double getErrorPercentDiff() {
      if ( lastBuildUriReport == null ) {
          return 0;
      }
      return errorPercent() - lastBuildUriReport.errorPercent();
  }
  
  public String getLastBuildHttpCodeIfChanged() {
      if ( lastBuildUriReport == null ) {
          return "";
      }
      
      if ( lastBuildUriReport.getHttpCode().equals(getHttpCode()) ) {
          return "";
      }
      
      return lastBuildUriReport.getHttpCode();
  }
  
  public int getSizeDiff() {
      if ( lastBuildUriReport == null ) {
          return 0;
      }
      return size() - lastBuildUriReport.size();
  }

  
  

  // getters and setters


  public PerformanceReport getPerformanceReport() {
    return performanceReport;
  }
  public String getStaplerUri() {
    return staplerUri;
  }
  
  public boolean isFailed() {
    return countErrors() != 0;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
    this.staplerUri = uri.replace("http:", "").replace("/", "_");
  }
  
  public String getStapleUri() {
    return staplerUri;
  }

  public void addLastBuildUriReport( UriReport lastBuildUriReport ) {
      this.lastBuildUriReport = lastBuildUriReport;
  }
  


//  public long getSummarizerMax() {
//    long max =  Long.MIN_VALUE;
//    for (HttpSample currentSample : httpSampleList) {
//        max = Math.max(max, currentSample.getSummarizerMax());
//    }
//    return max;
//  }
//
//  public long getSummarizerMin() {
//    long min = Long.MAX_VALUE;
//    for (HttpSample currentSample : httpSampleList) {
//        min = Math.min(min, currentSample.getSummarizerMin());
//    }
//    return min;
//  }
//
//  public long getSummarizerSize() {
//    long size=0;
//    for (HttpSample currentSample : httpSampleList) {
//        size+=currentSample.getSummarizerSamples();
//    }
//    return size;
//  }
//
//  public String getSummarizerErrors() {
//    float nbError = 0;
//    for (HttpSample currentSample : httpSampleList) {
//        nbError+=currentSample.getSummarizerErrors();
//    }
//    return new DecimalFormat("#.##").format(nbError/getSummarizerSize()*100).replace(",", ".");     
//  }


  public abstract void doSummarizerTrendGraph(StaplerRequest request,
      StaplerResponse response) throws IOException;

}
