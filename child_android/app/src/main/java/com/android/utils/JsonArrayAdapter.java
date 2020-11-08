package com.android.utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class JsonArrayAdapter extends BaseAdapter {

    protected LayoutInflater mInflater;
    protected JSONArray data;
    protected boolean haveMore;
    protected Activity context;
    protected int span;

    public JsonArrayAdapter(Activity context) {
        this(context, null, false);
    }

    public JsonArrayAdapter(Activity context, JSONArray jSONArray) {
        this(context, jSONArray, false);
    }

    public JsonArrayAdapter(Activity context, JSONArray jSONArray, int span) {
        this(context, jSONArray, false, span);
    }

    public JsonArrayAdapter(Activity context, JSONArray jSONArray,
                            boolean haveMore) {
        this(context, jSONArray, false, 0);
    }

    public JsonArrayAdapter(Activity context, JSONArray jSONArray,
                            boolean haveMore, int span) {
        this.mInflater = LayoutInflater.from(context);
        this.data = jSONArray;
        this.haveMore = haveMore;
        this.context = context;
        this.span = span;
    }

    public boolean isFetchMorePosition(int position) {
        if (haveMore) {
            return span + data.length() == position;
        } else {
            return false;
        }
    }

    public int getCount() {
        if (null == data) {
            return span;
        } else {
            int length = data.length();
            if (haveMore) {
                return length + 1 + span;
            } else {
                return length + span;
            }
        }
    }

    public Object getItem(int position) {
        if (position < span) {
            return null;
        }
        if (haveMore) {
            if (position == data.length()) {
                return null;
            } else {
                return data.opt(position - span);
            }
        } else {
            return data.opt(position - span);
        }
    }

    public void fillNewData(JSONArray jsonArray, boolean haveMore) {
        this.data = jsonArray;
        this.haveMore = haveMore;
        notifyDataSetChanged();
    }

    public void fillNewData(JSONArray jsonArray) {
        fillNewData(jsonArray, false);
    }

    public void addDatas(JSONArray jsonArray) {
        addDatas(jsonArray, false);
    }

    public void addDatas(JSONArray jsonArray, boolean haveMore) {
        if (this.data == null) {
            this.data = jsonArray;
        } else {
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    this.data.put(jsonArray.optJSONObject(i));
                }
            }
        }
        this.haveMore = haveMore;
        notifyDataSetChanged();
    }

    public void addFirstData(JSONObject data) {
        if (this.data == null) {
            this.data = new JSONArray();
            this.data.put(data);
        } else {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(data);
            for (int i = 0; i < this.data.length(); i++) {
                jsonArray.put(this.data.optJSONObject(i));
            }
            this.data = jsonArray;
        }
        notifyDataSetChanged();
    }

    public long getItemId(int arg0) {
        return arg0 - span;
    }

    public JSONArray getDatas() {
        return data;
    }

    public View getFetchMoreView() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.CENTER_HORIZONTAL);
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LayoutParams(320, 80));
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setTextSize(16);
        textView.setText("获取更多...");
        textView.setPadding(0, 0, 10, 0);
        layout.addView(textView);
        return layout;
    }

    public OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(JSONObject data);
    }
}
