package org.dyndns.warenix.hkg;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;

public class BottomButtonWebView extends WebView {

	private Button mLoadMore;
	private OnBottomReachedListener mOnBottomReachedListener;

	public BottomButtonWebView(Context context) {
		this(context, null);

	}

	public BottomButtonWebView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public BottomButtonWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		int height = (int) Math
				.floor(this.getContentHeight() * this.getScale());
		int webViewHeight = this.getMeasuredHeight();
		if (this.getScrollY() + webViewHeight >= height) {
			Log.i("THE END", "reached");
			if (mOnBottomReachedListener != null) {
				mOnBottomReachedListener.OnBottomReached();
			}
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

	public void setOnBottomReachedListener(OnBottomReachedListener l) {
		mOnBottomReachedListener = l;
	}

	public interface OnBottomReachedListener {
		public void OnBottomReached();
	}
}
