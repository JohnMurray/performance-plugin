package hudson.plugins.performance;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import org.kohsuke.stapler.DataBoundConstructor;
import org.xml.sax.SAXException;

import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Parses JMeter Summarized results
 * 
 * @author Agoley
 */
public class JmeterSummarizerParser extends PerformanceReportParser {

  public final String logDateFormat;

  @Extension
  public static class DescriptorImpl extends PerformanceReportParserDescriptor {
    @Override
    public String getDisplayName() {
      return "JmeterSummarizer";
    }
  }

  @DataBoundConstructor
  public JmeterSummarizerParser(String glob, String logDateFormat) {
    super(glob);

    if (logDateFormat == null || logDateFormat.length() == 0) {
      this.logDateFormat = getDefaultDatePattern();
    } else {
      this.logDateFormat = logDateFormat;
    }
  }

  @Override
  public String getDefaultGlobPattern() {
    return "**/*.log";
  }

  public String getDefaultDatePattern() {
    return "yyyy/mm/dd HH:mm:ss";
  }

  @Override
  public Collection<PerformanceReport> parse(AbstractBuild<?, ?> build,
      Collection<File> reports, TaskListener listener) {

    List<PerformanceReport> result = new ArrayList<PerformanceReport>();
    PrintStream logger = listener.getLogger();

    for (File f : reports) {
      Scanner s = null;
      try {
        final PerformanceReport r = new PerformanceReport();
        r.setReportFileName(f.getName());
        r.setReportFileName(f.getName());

        AggregateUriReport report = new AggregateUriReport(r, "", "");
        report.setMinResponseTime(Long.MAX_VALUE);
        long averageTimeCount = 0;
        
        List<Long> averages = new ArrayList<Long>();

        s = new Scanner(f);
        String key;
        String line;
        SimpleDateFormat dateFormat = new SimpleDateFormat(logDateFormat);

        logger.println("Performance: Parsing JMeterSummarizer report file " + f.getName());
        while (s.hasNextLine()) {
          line = s.nextLine().replaceAll("=", " ");
          if (line.contains("+")) {
            Scanner scanner = null;
            try {
                    scanner = new Scanner(line);
                    Pattern delimiter = scanner.delimiter();
                    scanner.useDelimiter("INFO"); // as jmeter logs INFO mode
                    String dateString = scanner.next();
                    report.setDate(dateFormat.parse(dateString));
                    scanner.findInLine("jmeter.reporters.Summariser:");
                    scanner.useDelimiter("\\+");
                    key = scanner.next().trim();
                    scanner.useDelimiter(delimiter);
                    scanner.next();

                    int reqCount = scanner.nextInt();
                    report.setRequestCount(reqCount + report.size());

                    scanner.findInLine("Avg:"); // set response time
                    long avgResTime = scanner.nextLong();
                    averages.add(avgResTime);
                    report.setAverageResponseTime(report.getAverage() + avgResTime);
                    averageTimeCount++;

                    scanner.findInLine("Min:"); // set MIN
                    long min = scanner.nextLong();
                    if (min < report.getMin()) { report.setMinResponseTime(min); }

                    scanner.findInLine("Max:"); // set MAX
                    long max = scanner.nextLong();
                    if (max > report.getMax()) { report.setMaxResponseTime(max); }

                    scanner.findInLine("Err:"); // set errors count
                    int errCount = scanner.nextInt();
                    report.setErrorCount(report.countErrors() + errCount);

                    // sample.setSummarizerErrors(
                    // Float.valueOf(scanner.next().replaceAll("[()%]","")));
                    report.setUri(key);
            } finally {
              if (scanner != null) scanner.close();
            }
          }
        }

        report.setAverageResponseTime((long)(report.getAverage() / (float)averageTimeCount));
        Collections.sort(averages);
        report.setMedian(averages.get((int) (averages.size() * 0.5)));

        r.addUriReport(report);
        result.add(r);
      } catch (FileNotFoundException e) {
        logger.println("File not found" + e.getMessage());
      } catch (ParseException e) {
        logger.println(e.getMessage());
      } finally {
        if (s != null) s.close();
      }
    }
    return result;
  }

}