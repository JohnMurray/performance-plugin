package hudson.plugins.performance;

import java.util.Date;
import java.io.Serializable;

/**
 * Information about a particular HTTP request and how that went.
 * 
 * This object belongs under {@link UriReport}.
 */
public class HttpSample implements Serializable, Comparable<HttpSample> {

  private static final long serialVersionUID = -1980997520755400556L;

	private long duration;

	private boolean successful;

	private boolean errorObtained;

	private Date date;

	private String uri;

	private String httpCode = "";

	private double sizeInKb;

	public long getDuration() {
		return duration;
	}

	public Date getDate() {
		return date;
	}

	public String getUri() {
		return uri;
	}

	public String getHttpCode() {
		return httpCode;
	}

	public boolean isFailed() {
		return !isSuccessful();
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public void setErrorObtained(boolean errorObtained) {
		this.errorObtained = errorObtained;
	}

	public boolean hasError() {
		return errorObtained;
	}

	public void setDate(Date time) {
		this.date = time;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setHttpCode(String httpCode) {
		this.httpCode = httpCode;
	}

	public int compareTo(HttpSample o) {
		return (int) (getDuration() - o.getDuration());
	}

	public double getSizeInKb() {
		return sizeInKb;
	}

	public void setSizeInKb(double d) {
		this.sizeInKb = d;
	}

	public boolean isErrorObtained() {
		return errorObtained;
	}
}
