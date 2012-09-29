package org.dyndns.warenix.hkg.parser;

import org.dyndns.warenix.hkg.HKGThread;
import org.dyndns.warenix.hkg.parser.HKGParser.PageRequest;

public class TestHKGThreadParser {
	public static void main(String args[]) {
		HKGThreadParser parser = new HKGThreadParser();
		try {
			String threadId = "3976935";
			int pageNo = 2;
			HKGThread thread = new HKGThread(threadId, null, 0, null, 0, 0);
			System.out.println("before:" + thread.mSelectedPage);

			parser.setHKGThread(thread);
			parser.parse(PageRequest.getReadThreadUrl(threadId, pageNo));
			System.out.println("after:" + thread.mSelectedPage);

			// System.out.println(parser);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
