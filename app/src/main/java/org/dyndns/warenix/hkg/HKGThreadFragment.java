package org.dyndns.warenix.hkg;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dyndns.warenix.abs.activity.SwitchPageAdapter;
import org.dyndns.warenix.hkg.BottomButtonWebView.OnBottomReachedListener;
import org.dyndns.warenix.hkg.HKGController.HKGListener;
import org.dyndns.warenix.hkg.HKGThread.HKGPage;
import org.dyndns.warenix.hkg.HKGThread.HKGReply;
import org.dyndns.warenix.hkg.parser.HKGParser;
import org.dyndns.warenix.hkg.provider.HKGMetaData;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class HKGThreadFragment extends SherlockFragment implements HKGListener {
	BottomButtonWebView mWebView;

	static final String TAG = "HKGThreadFragment";

	SwitchPageAdapter mAdapter;

	HKGController mController;

	static final String mLoadingHtml = "Loading... Please wait";
	static final String mEmptyHtml = "Cannot open thread.";

	/**
	 * Currently displayed thread
	 */
	HKGThread mThread;
	private boolean mLoaded = false;
	Handler mUIHandler = new Handler() {

		public void handleMessage(Message msg) {
			mLoaded = true;
			mThread = (HKGThread) msg.obj;

			// HKGPage page = mThread.mPageMap.get(mThread.mSelectedPage);
			HKGPage page = mThread.getPage(mThread.mSelectedPage);
			if (page == null) {
				setWebViewContent(getString(R.string.thread_loading_error));
			} else {
				setWebViewContent(formatHKGPageToHTML(page));
			}

			mLoadNextPage.setVisibility(View.GONE);
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

	private View mLoadNextPage;

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

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_webview, container,
				false);
		if (this.isAdded()) {
			mLoadNextPage = view.findViewById(R.id.load_next_page);
			mLoadNextPage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mLoadNextPage.setVisibility(View.GONE);
					Log.d(TAG, "current page:" + mThread.mSelectedPage);
					((MainActivity) getActivity()).showNextThreadPage();
				}
			});
			mWebView = (BottomButtonWebView) view.findViewById(R.id.webView1);
			mWebView.setOnBottomReachedListener(new OnBottomReachedListener() {

				@Override
				public void OnBottomReached() {
					Log.d(TAG, "selected page?" + mThread.mSelectedPage);
					if (mThread.mSelectedPage < mThread.mPageCount) {
						mLoadNextPage.setVisibility(View.VISIBLE);
					}
				}
			});

			WebSettings settings = mWebView.getSettings();

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				settings.setLayoutAlgorithm(LayoutAlgorithm.TEXT_AUTOSIZING);
			}
			settings.setDefaultTextEncodingName("utf-8");
			settings.setBuiltInZoomControls(true);
			settings.setUseWideViewPort(true);
			settings.setLoadWithOverviewMode(true);

			mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
			mWebView.setBackgroundColor(Color.BLACK);
			setWebViewContent(getString(R.string.thread_loading_start));
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
		/*
		 * s.append(
		 * "<script src=\"http://use.edgefonts.net/syncopate.js\"></script>");
		 */
		// s.append("<head><meta name=\"viewport\" content=\"width=device-width\" /></head>");
		s.append("<style type=\"text/css\">");
		s.append(mCss);
		s.append("</style>");
		s.append("<div id=\"hkgcontent\">");
		s.append(content);
		s.append("</div>");
		// // add load next page button
		// s.append("<div><button type=\"button\" id=\"loadNextPage\" style=\"display: block; width: 100%; height:15%\" onclick=\"loadNextPage();\">下一頁</button></div>");
		s.append("</html>");
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
		MenuItem bookmark = menu.add(getString(R.string.menu_bookmark));
		bookmark.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		bookmark.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Toast.makeText(
						getActivity(),
						String.format(getString(R.string.bookmark_saved)
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
		MenuItem share = menu.add(getString(R.string.menu_share));
		share.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		share.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
				i.putExtra(Intent.EXTRA_TEXT, String.format(
						"%sview.aspx?message=%s&page=%s",
						Config.getRandomDomain(), mThread.mThreadId,
						mThread.mSelectedPage));
				startActivity(Intent.createChooser(i, "Share URL"));
				return true;

			}
		});

		MenuItem gallery = menu.add(getString(R.string.menu_gallery));
		gallery.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		gallery.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				HKGPage page = mThread.getPage(mThread.mSelectedPage);
				ArrayList<String> imageList = extractImageFromPage(page);
				if (imageList != null && imageList.size() > 0) {
					Intent i = new Intent(getActivity(),
							ImageDetailActivity.class);

					i.putExtra(ImageDetailActivity.EXTRA_IMAGE, imageList);
					startActivity(i);
				} else {
					Toast.makeText(getActivity(),
							getString(R.string.toast_gallery_image_list_empty),
							Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});

		MenuItem clipboard = menu.add(getString(R.string.menu_clipboard));
		clipboard.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		clipboard.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				HKGPage page = mThread.getPage(mThread.mSelectedPage);
				if (page.getReplyList().size() > 0) {
					// String content = mThread.mTitle
					// + "\n\n"
					// + removeTags(page.getReplyList().get(0).mContent
					// .replace("<br />", "\n"));
					String content = Html.fromHtml(
							mThread.mTitle + "\n\n"
									+ page.getReplyList().get(0).mContent)
							.toString();

					Intent sendIntent = new Intent();
					sendIntent.setAction(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_SUBJECT, "");
					sendIntent.putExtra(Intent.EXTRA_TEXT, content);
					sendIntent.setType("text/plain");

					Context context = getActivity();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						// This will open the "Complete action with" dialog if
						// the
						// user doesn't have a default app set.
						context.startActivity(sendIntent);
					} else {
						context.startActivity(Intent.createChooser(sendIntent,
								"Share Via"));
					}
				} else {
					Toast.makeText(getActivity(),
							getString(R.string.toast_clipboard_post_empty),
							Toast.LENGTH_SHORT).show();
				}

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

	public static ArrayList<String> extractImageFromPage(HKGPage page) {
		if (page == null || page.getReplyList() == null) {
			return null;
		}
		String imgRegex = "src\\s*=\\s*['\"]([^'\"]+)['\"]";
		Pattern p = Pattern.compile(imgRegex);
		Matcher m = null;
		ArrayList<String> imageList = new ArrayList<String>();
		for (HKGReply reply : page.getReplyList()) {
			Matcher srcMatcher = HKGParser.mImgPattern.matcher(reply.mContent);
			String src = null;
			while (srcMatcher.find()) {
				src = srcMatcher.group(1);
				if (!(src.contains("hkgolden.com/faces/") || imageList
						.contains(src))) {
					imageList.add(src);
				}
			}

			srcMatcher = HKGParser.mHrefPattern.matcher(reply.mContent);
			String smallSrc = null;
			while (srcMatcher.find()) {
				src = srcMatcher.group(1);
				Log.d(TAG, String.format("warenix found %s", src));
				smallSrc = src.toLowerCase();
				if ((smallSrc.endsWith("jpg") || smallSrc.endsWith("png") || smallSrc
						.endsWith("gif"))
						&& !(src.contains("hkgolden.com/faces/") || imageList
								.contains(src))) {
					imageList.add(src);
				}
			}
		}
		Log.d(TAG, "found " + imageList.size());
		return imageList;
	}

	public String removeTags(String in) {
		int index = 0;
		int index2 = 0;
		while (index != -1) {
			index = in.indexOf("<");
			index2 = in.indexOf(">", index);
			if (index != -1 && index2 != -1) {
				in = in.substring(0, index).concat(
						in.substring(index2 + 1, in.length()));
			}
		}
		return in;
	}
}
