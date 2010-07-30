/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package URLChecker;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Mike
 */
public class URLStat {

	private enum Status {

		NotChecked, Broken, Working, EncounteredException
	};
	private String URL;
	private Status stat;
	private HashMap<String, URLStat> fromURLs, toURLs;
	private String exception;
	private int responseCode;

	public URLStat(String url) {
		this(url, null);
	}

	public URLStat(String url, URLStat fromURL) {
		URL = url;
		stat = Status.NotChecked;
		responseCode = -1;
		fromURLs = new HashMap<String, URLStat>();
		if (fromURL != null) {
			fromURLs.put(fromURL.URL, fromURL);
		}
		toURLs = new HashMap<String, URLStat>();
	}

	public String getURL() {
		return URL;
	}

	public boolean isChecked() {
		return stat != Status.NotChecked;
	}

	public boolean doesNotComeFrom(ArrayList<String> urls) {
		for (String avoid : urls) {
			for (String url : fromURLs.keySet()) {
				if (!url.toLowerCase().startsWith(avoid.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<URLStat> getFromListNotFrom(ArrayList<String> urls) {
		ArrayList<URLStat> stats = new ArrayList<URLStat>();
		for (URLStat stat : fromURLs.values()) {
			stats.add(stat);
		}
		for (String avoid : urls) {
			for (int i = 0; i < stats.size(); i++) {
				if (stats.get(i).getURL().toLowerCase().
						startsWith(avoid.toLowerCase())) {
					stats.remove(i--);
				}
			}
		}
		return stats;
	}

	public boolean containsFrom(String URL) {
		return fromURLs.containsKey(URL);
	}

	public void addFrom(URLStat URL) {
		if (!fromURLs.containsKey(URL)) {
			fromURLs.put(URL.URL, URL);
		}
	}

	public void flagException(String ex) {
		exception = ex;
		stat = Status.EncounteredException;
	}

	public String getResult() {
		if (stat == Status.EncounteredException) {
			return exception;
		}
		return "" + responseCode;
	}

	public void addTo(URLStat URL) {
		if (!toURLs.containsKey(URL)) {
			toURLs.put(URL.URL, URL);
		}
	}

	public void setWorking(boolean isWorking) {
		if (isWorking) {
			stat = Status.Working;
		} else {
			stat = Status.Broken;
		}
	}

	public void setResponseCode(int code) {
		responseCode = code;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public boolean isWorking() {
		return stat == Status.Working;
	}

	@Override
	public int hashCode() {
		return URL.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final URLStat other = (URLStat) obj;
		if ((this.URL == null) ? (other.URL != null)
				: !this.URL.equals(other.URL)) {
			return false;
		}
		return true;
	}
}
