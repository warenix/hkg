package org.dyndns.warenix.hkg.parser;

import org.dyndns.warenix.hkg.parser.HKGThreadParser.PageRequest;

public class TestHKGThreadParser {
	public static void main(String args[]) {
		HKGThreadParser parser = new HKGThreadParser();
		try {
			String threadId = "3976935";
			int pageNo = 2;
			parser.parse(PageRequest.getReadThreadUrl(threadId, pageNo));
			System.out.println(parser);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
