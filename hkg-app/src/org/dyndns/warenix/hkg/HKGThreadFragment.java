package org.dyndns.warenix.hkg;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.dyndns.warenix.abs.activity.SwitchPageAdapter;
import org.dyndns.warenix.hkg.HKGController.HKGListener;
import org.dyndns.warenix.hkg.HKGThread.HKGPage;
import org.dyndns.warenix.hkg.HKGThread.HKGReply;
import org.dyndns.warenix.lab.hkg.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class HKGThreadFragment extends SherlockFragment implements HKGListener {
	WebView mWebView;

	static final String TAG = "HKGThreadFragment";

	SwitchPageAdapter mAdapter;

	HKGController mController;

	static final String mLoadingHtml = "Loading... Please wait";
	static final String mEmptyHtml = "Cannot open thread.";

	/**
	 * Currently displayed thread
	 */
	HKGThread mThread;

	Handler mUIHandler = new Handler() {
		public void handleMessage(Message msg) {
			mThread = (HKGThread) msg.obj;

			// HKGPage page = mThread.mPageMap.get(mThread.mSelectedPage);
			HKGPage page = mThread.getPage(mThread.mSelectedPage);
			if (page == null) {
				setWebViewContent(mEmptyHtml);
			} else {
				setWebViewContent(formatHKGPageToHTML(page));
			}
		}
	};

	final String mCss = ".ViewQuote {border-left: 1px solid;"
			+ "margin: 0.1em 0;" + "padding: 0.1em 10px;"
			+ "line-height: 1.45;" + "position: relative;"
			+ "border-bottom: 2px solid;" + "border-left: 1px solid;" + "}";

	public static HKGThreadFragment newInstance(String threadId, int pageNo) {
		HKGThreadFragment f = new HKGThreadFragment();
		f.mThread = new HKGThread(threadId, null, -1, null, -1, -1);
		return f;
	}

	public static HKGThreadFragment newInstance(HKGThread thread, int pageNo) {
		HKGThreadFragment f = new HKGThreadFragment();

		f.mThread = thread;
		return f;
	}

	public HKGThreadFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_webview, container,
				false);
		if (this.isAdded()) {
			mWebView = (WebView) view.findViewById(R.id.webView1);
			mWebView.getSettings().setDefaultTextEncodingName("utf-8");
			mWebView.getSettings().setBuiltInZoomControls(true);
			setWebViewContent(mLoadingHtml);
		}
		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	public void setWebViewContent(String content) {
		String mimeType = "text/html; charset=utf-8";
		String encoding = "utf-8";

		StringBuffer s = new StringBuffer();
		s.append("<html><style type=\"text/css\">");
		s.append(mCss);
		s.append("</style>");
		s.append(content);
		s.append("</html>");
		//mWebView.loadData(s.toString(), mimeType, encoding);
		loadAndCleanData(mWebView, s.toString());
	}

	@Override
	public void onTopicLoaded(String type, int pageNo,
			ArrayList<HKGThread> threadList) {

	}

	@Override
	public void onThreadLoaded(HKGThread thread) {
		Log.d(TAG, "onThreadLoaded(), update UI");
		Message msg = new Message();
		msg.obj = thread;
		mUIHandler.sendMessage(msg);
	}

	protected String formatHKGPageToHTML(HKGPage page) {

		StringBuffer s = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		s.append("<br />Title:" + mThread.mTitle);
		s.append("<br />Pages Count:" + mThread.mSelectedPage + "/"
				+ mThread.mPageCount);
		s.append("<br />Replies Count:" + page.getReplyList().size());
		int count = 25 * (page.mPageNo - 1);
		if (page.mPageNo > 1) {
			count++;
		}
		for (HKGReply reply : page.getReplyList()) {
			s.append("<br/><hr/>#" + count++ + "<br />");
			s.append(reply.toString());
		}
		return s.toString();
	}

	// void switchPage(final String threadId, final int pageNo) {
	//
	// new Thread() {
	// public void run() {
	// Log.d(TAG, "switchPage(), reading thread by page");
	//
	// // mThread = new HKGThread(threadId, null, -1, null, -1, -1);
	// mController.readThreadByPage(mThread, pageNo);
	// }
	// }.start();
	// }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		// MenuItem refresh = menu.add("Save");
		// refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
		// | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	/**
	 * prevent "web page not available" when 
	 * @param webView
	 * @param html
	 */
	public static void loadAndCleanData(WebView webView, String html) {
		try {
			webView.loadData(
					URLEncoder.encode(html, "utf-8").replaceAll("\\+", " "),
					"text/html", "utf-8");
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException(
					"utf-8 encoding failed when loading String into WebView");
		}
	}

}
