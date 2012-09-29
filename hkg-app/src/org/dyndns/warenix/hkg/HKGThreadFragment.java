package org.dyndns.warenix.hkg;

import java.util.ArrayList;

import org.dyndns.warenix.hkg.HKGController.HKGListener;
import org.dyndns.warenix.hkg.HKGThread.HKGPage;
import org.dyndns.warenix.hkg.HKGThread.HKGReply;
import org.dyndns.warenix.hkg.parser.HKGThreadParser;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class HKGThreadFragment extends Fragment implements HKGListener {
	HKGThreadParser parser;
	WebView mWebView;

	int mPageNo;
	String mThreadId;

	HKGThread mThread;

	static final String mLoadingHtml = "Loading... Please wait";

	Handler mHKGThreadHandler = new Handler() {
		public void handleMessage(Message msg) {
			HKGThread thread = (HKGThread) msg.obj;
			HKGPage page = thread.mPageMap.get(thread.mSelectedPage);
			setWebViewContent(formatHKGPageToHTML(page));
		}
	};

	final String mCss = ".ViewQuote {border-left: 1px solid;"
			+ "margin: 0.1em 0;" + "padding: 0.1em 10px;"
			+ "line-height: 1.45;" + "position: relative;"
			+ "border-bottom: 2px solid;" + "border-left: 1px solid;" + "}";

	public static HKGThreadFragment newInstance(String threadId, int pageNo) {
		HKGThreadFragment f = new HKGThreadFragment();
		f.mPageNo = pageNo;
		f.mThreadId = threadId;
		f.mThread = new HKGThread(threadId, null, 0, null, 0, 0);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_webview, container,
				false);
		mWebView = (WebView) view.findViewById(R.id.webView1);
		mWebView.getSettings().setBuiltInZoomControls(true);
		setWebViewContent(mLoadingHtml);
		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		new Thread() {
			public void run() {
				HKGController controller = HKGController.getController();
				controller.setHKGListener(HKGThreadFragment.this);
				controller.readThreadByPage(mThread, mPageNo);
				// parser = new HKGThreadParser();
				// try {
				// parser.parse(PageRequest.getReadThreadUrl(mThreadId,
				// mPageNo));
				// mHKGThreadHandler.sendEmptyMessage(0);
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
			}
		}.start();
	}

	void setWebViewContent(String content) {
		String mimeType = "text/html; charset=utf-8";
		String encoding = null;

		StringBuffer s = new StringBuffer();
		s.append("<html><style type=\"text/css\">");
		s.append(mCss);
		s.append("</style>");
		s.append(content);
		s.append("</html>");
		mWebView.loadData(s.toString(), mimeType, encoding);
	}

	@Override
	public void onTopicLoaded(String type, int pageNo,
			ArrayList<HKGThread> threadList) {

	}

	@Override
	public void onThreadLoaded(HKGThread thread) {
		Message msg = new Message();
		msg.obj = thread;
		mHKGThreadHandler.sendMessage(msg);
	}

	String formatHKGPageToHTML(HKGPage page) {

		StringBuffer s = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		s.append("\nTitle:" + mThread.mTitle);
		s.append("\nPages Count:" + mThread.mSelectedPage + "/"
				+ mThread.mPageCount);
		s.append("\nReplies Count:" + page.getReplyList().size());
		int count = 0;
		for (HKGReply reply : page.getReplyList()) {
			s.append("\n<hr/>#" + count++);
			s.append(reply.toString());
		}
		return s.toString();
	}

}
