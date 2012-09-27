package org.dyndns.warenix.hkg;

import java.io.IOException;

import org.dyndns.warenix.hkg.parser.HKGThreadParser;
import org.dyndns.warenix.hkg.parser.HKGThreadParser.PageRequest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class HKGThreadFragment extends Fragment {
	HKGThreadParser parser;
	WebView mWebView;

	int mPageNo;
	String mThreadId;

	static final String mLoadingHtml = "Loading... Please wait";

	Handler mHKGThreadHandler = new Handler() {
		public void handleMessage(Message msg) {
			setWebViewContent(parser.toString());
		}
	};

	final String mCss = ".ViewQuote { padding: 0px 0px 10px 12px; border-bottom: 2px dashed rgb(208, 208, 208); color: rgb(0, 0, 160);}";

	public static HKGThreadFragment newInstance(String threadId, int pageNo) {
		HKGThreadFragment f = new HKGThreadFragment();
		f.mPageNo = pageNo;
		f.mThreadId = threadId;
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
				parser = new HKGThreadParser();
				try {
					parser.parse(PageRequest.getReadThreadUrl(mThreadId,
							mPageNo));
					mHKGThreadHandler.sendEmptyMessage(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
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

}
