package org.dyndns.warenix.hkg.parser;

import java.util.ArrayList;

import org.dyndns.warenix.hkg.HKGThread;
import org.dyndns.warenix.hkg.parser.HKGParser.PageRequest;

public class TestHKGListParser {
	public static void main(String args[]) {
		// String s = " 蝦蝦大笑&nbsp;&nbsp;-&nbsp;(評分: 4)";
		// Pattern p = Pattern.compile("(.*?)\\(評分: ([0-9]+)");
		// Matcher m = p.matcher(s);
		// if (m.find()) {
		// System.out.println(m.group(2));
		// }

		HKGListParser parser = new HKGListParser();
		try {
			String type = "BW";
			int pageNo = 2;
			parser.parse(PageRequest.getListUrl(type, pageNo));
			// System.out.println(parser);
			ArrayList<HKGThread> threadList = parser.getHKGThreadList();

			for (HKGThread thread : threadList) {
				System.out.println(thread.mUser);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
