/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package URLChecker;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.EditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author Mike
 */
public class URLCheckTask implements Runnable {

	private URLCheckManager manager;
	private URLStat stat;

	public URLCheckTask(URLCheckManager manager, URLStat stat) {
		this.manager = manager;
		this.stat = stat;
	}

	@Override
	public void run() {
		try {
			URL url = new URL(stat.getURL());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10000);
			try {
				stat.setResponseCode(conn.getResponseCode());
			} catch (UnknownHostException e) {
				stat.flagException("UnknownHostException");
				System.out.println("broken, unknown host: " + stat.getURL());
				return;
			} catch (ConnectException e) {
				stat.flagException("Connect Exception");
				System.out.println("could not connect: " + stat.getURL());
				return;
			} catch (SocketTimeoutException e) {
				stat.flagException("SocketTimeout");
				System.out.println("socket timeout: " + stat.getURL());
				return;
			}
			int responseCode = conn.getResponseCode();
			if (responseCode == 404 || responseCode == 403
					|| responseCode == 400 || responseCode == 401) {
				stat.setWorking(false);
				System.out.println("broken, " + responseCode + ": "
						+ stat.getURL());
				return;
			} else if (responseCode != 200 && responseCode != 302
					&& responseCode != 300) {
				stat.setWorking(false);
				System.out.println("strange, " + responseCode + ": "
						+ stat.getURL());
				return;
			}
			//System.out.println("site: " + stat.getURL() + "; content type: "
			//		+ conn.getContentType());
			if (!conn.getContentType().toLowerCase().contains("html")) {
				//System.out.println("Not HTML: " + stat.getURL());
				return;
			}
			if (!manager.isWithinBaseURL(stat)) {
				//System.out.println("not within bounds: " + stat.getURL());
				return;
			}
			Reader rd = new InputStreamReader(conn.getInputStream());

			EditorKit kit = new HTMLEditorKit();
			HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
			doc.putProperty("IgnoreCharsetDirective", new Boolean(true));
			kit.read(rd, doc, 0);

			HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);
			while (it.isValid()) {
				SimpleAttributeSet s = (SimpleAttributeSet) it.getAttributes();

				String link = (String) s.getAttribute(HTML.Attribute.HREF);
				if (link != null && link.charAt(0) != '#'
						&& !link.startsWith("javascript")
						&& !link.startsWith("mailto")) {
					if (link.charAt(0) == '/') {
						link = manager.formatURL(link);
					}
					manager.addURL(link, stat);
				}
				it.next();
			}
			stat.setWorking(true);
		} catch (ChangedCharSetException e) {
			System.out.println("charset: " + e.getCharSetSpec());
			e.printStackTrace();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
