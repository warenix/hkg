package org.dyndns.warenix.hkg;

public class HKGSearchResult extends HKGThread {

	public String mUrl;
	public String mContent;
	public String mResultCount;
	public int mCurrentPageIndex;

	public HKGSearchResult(String url, String title, String content,
			String resultCount, int currentPageIndex) {
		super(HKGMaster.extraceThreadIdFromURL(url), null, -1, title, 0, 1);

		mUrl = url;
		mContent = content;
		mResultCount = resultCount;
		mCurrentPageIndex = currentPageIndex;
	}

}
