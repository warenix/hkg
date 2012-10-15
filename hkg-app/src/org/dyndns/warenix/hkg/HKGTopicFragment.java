package org.dyndns.warenix.hkg;

import java.util.ArrayList;

import org.dyndns.warenix.hkg.HKGController.HKGListener;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.TextView;

public class HKGTopicFragment extends ListFragment implements HKGListener {
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
				// adapter = new ArrayAdapter<HKGThread>(getActivity(),
				// android.R.layout.simple_list_item_2, threadList) {
				//
				// public HKGThread getItem(int position) {
				// return threadList.get(position);
				// }
				//
				// @Override
				// public View getView(int position, View convertView,
				// ViewGroup parent) {
				// TwoLineListItem row;
				// if (convertView == null) {
				// LayoutInflater inflater = (LayoutInflater) getActivity()
				// .getSystemService(
				// Context.LAYOUT_INFLATER_SERVICE);
				// row = (TwoLineListItem) inflater.inflate(
				// android.R.layout.simple_list_item_2, null);
				// } else {
				// row = (TwoLineListItem) convertView;
				// }
				// HKGThread topic = threadList.get(position);
				// row.getText1().setText(
				// String.format("%s -- %d",
				// Html.fromHtml(topic.mTitle),
				// topic.mRepliesCount));
				// if (topic.mRating < 0) {
				// row.getText1().setTextColor(Color.GRAY);
				// } else {
				// row.getText1().setTextColor(Color.BLACK);
				// }
				// row.getText2().setText(
				// String.format("%s \u2764 %d\t\t[%s]",
				// topic.mUser, topic.mRating,
				// topic.mThreadId));
				//
				// return row;
				// }
				adapter = new ArrayAdapter<HKGThread>(getActivity(),
						android.R.layout.simple_list_item_2, threadList) {

					class ViewHolder {
						TextView line1;
						TextView line2;
						TextView left;
					}

					public HKGThread getItem(int position) {
						return threadList.get(position);
					}

					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						View row;
						if (convertView == null) {
							LayoutInflater inflater = (LayoutInflater) getActivity()
									.getSystemService(
											Context.LAYOUT_INFLATER_SERVICE);
							row = inflater
									.inflate(R.layout.thread_header, null);
							ViewHolder viewHolder = new ViewHolder();
							viewHolder.left = (TextView) row
									.findViewById(R.id.left);
							viewHolder.line1 = (TextView) row
									.findViewById(R.id.line1);
							viewHolder.line2 = (TextView) row
									.findViewById(R.id.line2);
							row.setTag(viewHolder);
						} else {
							row = convertView;
						}
						ViewHolder viewHolder = (ViewHolder) row.getTag();

						HKGThread topic = threadList.get(position);
						viewHolder.line1.setText(String.format("%s",
								Html.fromHtml(topic.mTitle)));
						if (topic.mRating < 0) {
							viewHolder.line1.setTextColor(Color.GRAY);
						} else {
							viewHolder.line1.setTextColor(Color.LTGRAY);
						}
						viewHolder.line2.setText(String.format(
								"%s \u2764 %d\t\t[%s]", topic.mUser,
								topic.mRating, topic.mThreadId));
						viewHolder.left.setText(String.format("%d",
								topic.mRepliesCount));

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

	public HKGTopicFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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

	public void clear() {
		if (adapter != null) {
			adapter.clear();
			adapter.notifyDataSetChanged();
		}
	}
}
