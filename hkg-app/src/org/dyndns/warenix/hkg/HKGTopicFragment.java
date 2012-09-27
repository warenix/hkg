package org.dyndns.warenix.hkg;

import java.io.IOException;
import java.util.ArrayList;

import org.dyndns.warenix.hkg.parser.HKGListParser;
import org.dyndns.warenix.hkg.parser.HKGListParser.HKGList;
import org.dyndns.warenix.hkg.parser.HKGListParser.Topic;
import org.dyndns.warenix.hkg.parser.HKGThreadParser.PageRequest;

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

public class HKGTopicFragment extends ListFragment {
	HKGListParser parser;
	WebView mWebView;

	int mPageNo;
	String mType;

	TopicListener mListener;
	ArrayAdapter<Topic> adapter;
	Handler mHKGThreadHandler = new Handler() {
		public void handleMessage(Message msg) {
			HKGList hkgList = parser.getHKGList();
			final ArrayList<Topic> topicList = hkgList.getTopicList();

			if (getActivity() != null) {
				adapter = new ArrayAdapter<Topic>(getActivity(),
						android.R.layout.simple_list_item_2, topicList) {
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
						Topic topic = topicList.get(position);
						row.getText1().setText(
								String.format("%s -- %d  \u2764 %d",
										Html.fromHtml(topic.title),
										topic.repliesCount, topic.rating));
						row.getText2().setText(topic.user);

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
				parser = new HKGListParser();
				try {
					parser.parse(PageRequest.getListUrl(mType, mPageNo));
					mHKGThreadHandler.sendEmptyMessage(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("FragmentList", "Item clicked: " + id);
		if (mListener != null) {
			mListener.onTopicSelected((Topic) adapter.getItem(position));
		}
	}

	public void setTopicListener(TopicListener listener) {
		mListener = listener;
	}

	interface TopicListener {
		public void onTopicSelected(Topic topic);
	}
}
