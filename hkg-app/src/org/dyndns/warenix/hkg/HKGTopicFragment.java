package org.dyndns.warenix.hkg;

import java.util.ArrayList;

import org.dyndns.warenix.hkg.HKGController.HKGListener;
import org.dyndns.warenix.hkg.parser.HKGListParser;
import org.dyndns.warenix.hkg.parser.HKGListParser.HKGList;
import org.dyndns.warenix.lab.hkg.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TwoLineListItem;

public class HKGTopicFragment extends ListFragment implements HKGListener {
	HKGListParser parser;
	WebView mWebView;

	int mPageNo;
	String mType;

	HKGThreadListener mListener;
	ArrayAdapter<HKGThread> adapter;
	Handler mHKGThreadHandler = new Handler() {
		public void handleMessage(Message msg) {

			final ArrayList<HKGThread> threadList = (ArrayList<HKGThread>) msg.obj;

			// HKGList hkgList = parser.getHKGList();
			// final ArrayList<HKGThread> topicList =
			// hkgList.getHKGThreadList();

			if (getActivity() != null) {
				adapter = new ArrayAdapter<HKGThread>(getActivity(),
						android.R.layout.simple_list_item_2, threadList) {
					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						TwoLineListItem row;
						if (convertView == null) {
							LayoutInflater inflater = (LayoutInflater) getActivity()
									.getSystemService(
											Context.LAYOUT_INFLATER_SERVICE);
							row = (TwoLineListItem) inflater.inflate(
									android.R.layout.simple_list_item_2, null);
						} else {
							row = (TwoLineListItem) convertView;
						}
						HKGThread topic = threadList.get(position);
						row.getText1().setText(
								String.format("%s -- %d  \u2764 %d",
										Html.fromHtml(topic.mTitle),
										topic.mRepliesCount, topic.mRating));
						row.getText2().setText(topic.mUser);

						return row;
					}
				};
				setListAdapter(adapter);
			}
		}
	};

	public static HKGTopicFragment newInstance(String type, int pageNo) {
		HKGTopicFragment f = new HKGTopicFragment();
		f.mPageNo = pageNo;
		f.mType = type;
		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		new Thread() {
			public void run() {
				HKGController controller = HKGController.getController();
				controller.setHKGListener(HKGTopicFragment.this);
				controller.readTopicByPage(mType, mPageNo);
				// parser = new HKGListParser();
				// try {
				// parser.parse(PageRequest.getListUrl(mType, mPageNo));
				// mHKGThreadHandler.sendEmptyMessage(0);
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
			}
		}.start();

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("FragmentList", "Item clicked: " + id);
		if (mListener != null) {
			mListener
					.onHKGThreadSelected((HKGThread) adapter.getItem(position));
		}
	}

	public void setHKGThreadListener(HKGThreadListener listener) {
		mListener = listener;
	}

	/**
	 * UI listener
	 * 
	 * @author warenx
	 * 
	 */
	interface HKGThreadListener {
		public void onHKGThreadSelected(HKGThread topic);
	}

	@Override
	public void onTopicLoaded(String type, int pageNo,
			ArrayList<HKGThread> threadList) {
		Message msg = new Message();
		msg.obj = threadList;
		mHKGThreadHandler.sendMessage(msg);
	}

	@Override
	public void onThreadLoaded(HKGThread thread) {

	}
}
