package org.dyndns.warenix.hkg.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.dyndns.warenix.hkg.Config;

public abstract class HKGParser {
	public void parse(String urlString) throws IOException {
		System.out.println(urlString);
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			if (!feed(inputLine)) {
				break;
			}
		in.close();
	}

	/**
	 * 
	 * @param inputLine
	 * @return true to continue feeding, false to stop further feeding.
	 */

	public abstract boolean feed(String inputLine);

	public static class PageRequest {
		static final String READ_THERAD_PATH = "view.aspx";
		static final String LIST_THERAD_PATH = "topics.aspx";

		/**
		 * these forums are not cached statically on hkgolden server, need to
		 * use aspx method to list topics
		 */
		static String[] cachedForums = { "MB", "JT", "SY", "ED", "PT", "BB",
				"TR", "CO", "TO", "MU", "DC", "ST", "WK", "TS", "RA", "HW",
				"IN", "SW", "AP", "CA", "FN",
				// the cache doesn't update
				"WS", "BW", "EP", "AC", "VI", "AN", "LV", "SP", "MP", "HW",
				"GM", "ET" };

		static Map<String, Boolean> sCachedForumMap = new HashMap<String, Boolean>();

		public static String getReadThreadUrl(String threadId, int pageNo) {
			final String domain = Config.getRandomDomain();
			String url = domain + READ_THERAD_PATH + "?";
			// fill get parameters
			url += String.format("&message=%s", threadId);
			url += String.format("&page=%d", pageNo);
			return url;
		}

		public static String getListUrl(String type, int pageNo) {
			final String domain = Config.getRandomDomain();
			String url = null;
			if (pageNo == 1) {
				if (isCached(type, true)) {
					url = domain + String.format("topics_%s.htm", type);
				} else {
					url = domain + String.format("topics.aspx?type=%s", type);
				}
			} else {
				if (isCached(type, true) && pageNo <= 3) {
					url = domain
							+ String.format("topics_%s_%d.htm", type, pageNo);
				} else {
					url = domain
							+ String.format("topics.aspx?type=%s&page=%d",
									type, pageNo);
				}
			}
			return url;
		}

		public static boolean isCached(String type, boolean forceNocache) {
			if (forceNocache) {
				return false;
			}
			// check cached result first
			if (sCachedForumMap.containsKey(type)) {
				return sCachedForumMap.get(type);
			}
			boolean b = true;
			String t = type.toUpperCase();
			for (String s : cachedForums) {
				if (s.equals(t)) {
					b = false;
					break;
				}
			}

			sCachedForumMap.put(type, b);
			return b;
		}
	}

	public static final Pattern mReplyUserPattern = Pattern
			.compile("<span class=\"(ViewNameMale|ViewNameFemale)\">(.*?)</span>");
	public static final Pattern mPostDatePattern = Pattern
			.compile("<div class=\"ViewDate\">(.*?)</div>");
	public static final Pattern mImgPattern = Pattern
			.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
	public static final Pattern mRealImgPattern = Pattern
			.compile("\\[(img|IMG)\\](.*?)\\[/(img|IMG)\\]");
	public static final Pattern mSrcPattern = Pattern
			.compile("src\\s*=\\s*['\"]([^'\"]+)['\"]");

	public static final Pattern mTitlePattern = Pattern
			.compile("<title>(.*?)</title>");
	public static final Pattern mThreadUserPattern = Pattern
			.compile("(.*?)\\(評分: (-?[0-9]+)");
	public static final Pattern mHrefPattern = Pattern
			.compile("<a[^>]+href\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
	public static final Pattern mThreadIdPattern = Pattern
			.compile("message=([0-9]+)");

}
