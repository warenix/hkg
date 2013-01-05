package org.dyndns.warenix.hkg;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.dyndns.warenix.abs.activity.SwitchPageAdapter;
import org.dyndns.warenix.hkg.HKGController.HKGListener;
import org.dyndns.warenix.hkg.HKGThread.HKGPage;
import org.dyndns.warenix.hkg.HKGThread.HKGReply;
import org.dyndns.warenix.hkg.provider.HKGMetaData;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

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

	final String mCssBlackColorTheme = " #hkgcontent{background:black; color:#EB8921} a {color:#0099CC}";
	String mCssColorTheme = mCssBlackColorTheme;
	final String mCssViewQuote = ".ViewQuote {border-left: 1px solid;"
			+ "margin: 0.1em 0;" + "padding: 0.1em 10px;"
			+ "line-height: 1.45;" + "position: relative;"
			+ "border-bottom: 2px solid;" + "border-left: 1px solid;"
			+ "font-family: syncopate, serif;" + "}";
	// + "img {max-width:100%;}";

	final String mCss = mCssViewQuote + mCssColorTheme;

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
		// setHasOptionsMenu(true);
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
			mWebView.getSettings().setUseWideViewPort(true);
			mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
			mWebView.setBackgroundColor(Color.BLACK);
			setWebViewContent(mLoadingHtml);
		}
		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	public void setWebViewContent(String content) {
		StringBuffer s = new StringBuffer();
		s.append("<html>");
		s.append("<script src=\"http://use.edgefonts.net/syncopate.js\"></script>");
		// s.append("<head><meta name=\"viewport\" content=\"width=device-width\" /></head>");
		s.append("<style type=\"text/css\">");
		s.append(mCss);
		s.append("</style>");
		s.append("<div id=\"hkgcontent\">");
		s.append(content);
		s.append("</html>");
		s.append("</div>");
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
		MenuItem bookmark = menu.add("Bookmark");
		bookmark.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		bookmark.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Toast.makeText(
						getActivity(),
						String.format("bookmark "
								+ HKGThreadFragment.this.mThread.mThreadId),
						Toast.LENGTH_SHORT).show();

				// save bookmark
				new Thread() {
					public void run() {
						HKGThread thread = HKGThreadFragment.this.mThread;

						ContentValues values = new ContentValues();
						values.put(HKGMetaData.BookmarkColumns.threadId,
								thread.mThreadId);
						values.put(HKGMetaData.BookmarkColumns.title,
								thread.mTitle);
						values.put(HKGMetaData.BookmarkColumns.user,
								thread.mUser);
						values.put(HKGMetaData.BookmarkColumns.rating,
								thread.mRating);
						values.put(HKGMetaData.BookmarkColumns.pageCount,
								thread.mPageCount);
						values.put(HKGMetaData.BookmarkColumns.repliesCount,
								thread.mRepliesCount);
						values.put(
								HKGMetaData.BookmarkColumns.last_page_no_seen,
								thread.mSelectedPage);

						Uri uri = HKGMetaData.getUriListBookmark();
						Uri insertUri = getActivity().getContentResolver()
								.insert(uri, values);
						Log.d(TAG, "inserted uri:" + insertUri);
					}
				}.start();
				return true;
			}
		});
		MenuItem share = menu.add("Share");
		share.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		share.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
				i.putExtra(Intent.EXTRA_TEXT, String.format(
						"http://m.hkgolden.com/view.aspx?message=%s&page=%s",
						mThread.mThreadId, mThread.mSelectedPage));
				startActivity(Intent.createChooser(i, "Share URL"));
				return true;

			}
		});
	}

	/**
	 * prevent "web page not available" when
	 * 
	 * @param webView
	 * @param html
	 */
	public static void loadAndCleanData(WebView webView, String html) {
		try {
			String mimeType = "text/html; charset=utf-8";
			String encoding = "utf-8";
			webView.loadData(
					URLEncoder.encode(html, "utf-8").replaceAll("\\+", " "),
					mimeType, encoding);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException(
					"utf-8 encoding failed when loading String into WebView");
		}
	}

}
