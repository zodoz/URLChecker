/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package URLChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Mike
 */
public class URLCheckManager extends Thread {

	private HashMap<String, URLStat> URLs;
	private ArrayList<URLCheckTask> checks;
	private ThreadPoolExecutor checklist;
	private String baseURL;

	public URLCheckManager(String entryURL) {

		baseURL = entryURL;
		URLs = new HashMap<String, URLStat>();
		URLStat entry = new URLStat(entryURL);
		URLs.put(entryURL, entry);
		checks = new ArrayList<URLCheckTask>();
		checklist = new ThreadPoolExecutor(3, 10, 10000L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
		checklist.execute(new URLCheckTask(this, entry));
	}

	@Override
	public void run() {
		try {
			System.out.println("starting to run manager");
			BlockingQueue q = checklist.getQueue();
			System.out.println("got queue");
			while (!q.isEmpty() || !isDone()) {
				if (!q.isEmpty()) {
					System.out.println("manager: q is not empty: " + q.size());
				} else {
					System.out.println("manager: not finished checking everything");
				}
				System.out.println("\tthreads running: "
						+ checklist.getActiveCount() + ".  Total: "
						+ URLs.size() + ".  done: "
						+ (URLs.size() - q.size()));
				Thread.sleep(Math.max(Math.min(20 * q.size(), 10000), 400));
				//q = checklist.getQueue();
			}
			System.out.println("shutting down thread pool");
			checklist.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//now sort and display
		System.out.println("\n\nDone, Reporting:");
		ArrayList<String> avoid = new ArrayList<String>();
		avoid.add("http://www.me.ttu.edu/ME/ClassWebsites");
		int totalBroken = 0;
		for (String url : URLs.keySet()) {
			URLStat stat = URLs.get(url);
			if (!stat.isWorking() && stat.doesNotComeFrom(avoid)) {
				totalBroken++;
				System.out.println(stat.getResult() + ": " + stat.getURL());
				ArrayList<URLStat> stats = stat.getFromListNotFrom(avoid);
				for (URLStat fromStat : stats) {
					System.out.println("\t" + fromStat.getURL());
				}
			}
		}
		System.out.println("Total Broken: " + totalBroken);
	}

	private boolean isDone() {
		for (String URL : URLs.keySet()) {
			if (!URLs.get(URL).isChecked()) {
				System.out.println("not finished checking: " + URL);
				return false;
			}
		}
		return true;
	}

	public boolean isWithinBaseURL(URLStat stat) {
		String url = stat.getURL();
		url = url.substring(0, Math.min(url.length(), baseURL.length()));
		return url.equals(baseURL);
	}

	public void addURL(String URL, URLStat fromURL) {
		URLStat to;
		if (URLs.containsKey(URL)) {
			to = URLs.get(URL);
			to.addFrom(fromURL);
			fromURL.addTo(to);
		} else {
			to = new URLStat(URL, fromURL);
			fromURL.addTo(to);
			URLs.put(URL, to);
			//try {
				checklist.execute(new URLCheckTask(this, to));
			/*} catch(Exception e) {
				System.out.println("cause: "+e.getCause());
				e.printStackTrace();
				throw e;
			}*/
		}
	}

	public URLStat getStat(String URL) {
		if (URLs.containsKey(URL)) {
			return URLs.get(URL);
		}
		return null;
	}

	public String formatURL(String url) {
		if (url.charAt(0) == '/') {
			url = baseURL + url;
		}
		return url;
	}
}
