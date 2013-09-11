package org.dyndns.warenix.hkg.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

public abstract class HKGParser {
	public void parse(String urlString) throws IOException {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0");
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
		static final String DOMAIN = "http://m.hkgolden.com/";
		static final String READ_THERAD_PATH = "view.aspx";
		static final String LIST_THERAD_PATH = "topics.aspx";

		public static String getReadThreadUrl(String threadId, int pageNo) {
			String url = DOMAIN + READ_THERAD_PATH + "?";
			// fill get parameters
			url += String.format("&message=%s", threadId);
			url += String.format("&page=%d", pageNo);
			return url;
		}

		public static String getListUrl(String type, int pageNo) {
			String url = DOMAIN + LIST_THERAD_PATH + "?";
			// fill get parameters
			url += String.format("&type=%s", type);
			url += String.format("&page=%d", pageNo);
			return url;
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
