package org.dyndns.warenix.hkg.parser;

import java.util.ArrayList;

import org.dyndns.warenix.hkg.HKGController;
import org.dyndns.warenix.hkg.HKGController.HKGListener;
import org.dyndns.warenix.hkg.HKGThread;
import org.dyndns.warenix.hkg.HKGThread.HKGPage;

public class TestHKGTController {
	public static void main(String args[]) {
		MyHKGListener listener = new MyHKGListener();
		HKGController controller = HKGController.getController();
		controller.setHKGListener(listener);
		// controller.readTopicByPage("BW", 1);
		HKGThread thread = new HKGThread("3997451", null, -1, null, -1, -1);
		printThread(thread);
		controller.readThreadByPage(thread, 1);
		controller.readThreadByPage(thread, 2);

	}

	static class MyHKGListener implements HKGListener {

		@Override
		public void onTopicLoaded(String type, int page,
				ArrayList<HKGThread> threadList) {
			System.out.println("Topic loaded");

			for (HKGThread thread : threadList) {
				System.out.println(thread.mThreadId + ":" + thread.mTitle);
			}
		}

		@Override
		public void onThreadLoaded(HKGThread thread) {
			System.out.println("Thread loaded");
			printThread(thread);
			// HKGPage page = thread.mPageMap.get(thread.mSelectedPage);
			// for (HKGReply reply : page.getReplyList()) {
			// System.out.println(reply);
			// }
		}
	}

	static void printThread(HKGThread thread) {
		System.out.println(String.format("selected:%d pageCount:%d",
				thread.mSelectedPage, thread.mPageCount));
	}
}