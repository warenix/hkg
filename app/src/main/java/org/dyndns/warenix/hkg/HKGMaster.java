package org.dyndns.warenix.hkg;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.net.Uri;

public class HKGMaster {

	/**
	 * extract thread id from given url
	 * 
	 * @param urlString
	 *            example:
	 *            http://m.hkgolden.com/view.aspx?message=<threadId>&type=BW
	 * @return null if the message id is not found.
	 */
	public static String extraceThreadIdFromURL(String urlString) {
		Uri uri;
		try {
			uri = Uri.parse(URLDecoder.decode(urlString, "UTF-8"));
			return uri.getQueryParameter("message");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * extract thread id from given url
	 * 
	 * @param urlString
	 *            example:
	 *            http://m.hkgolden.com/view.aspx?message=<threadId>&type=BW
	 * @return null if the message id is not found.
	 */
	public static int extracePageNoFromURL(String urlString) {
		Uri uri;
		try {
			uri = Uri.parse(URLDecoder.decode(urlString, "UTF-8"));
			final String pageNo = uri.getQueryParameter("page");
			if (pageNo != null) {
				return Integer.parseInt(pageNo);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return 1;
	}
}
