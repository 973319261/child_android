package com.preschool.edu.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.utils.ALog;
import com.android.utils.DensityUtil;
import com.android.utils.ViewHolder;
import com.bumptech.glide.Glide;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.core.AppService;
import com.preschool.edu.core.GlideRoundTransform;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by jac_cheng on 2018/1/11.
 */
public class ContactAdapter extends BaseAdapter implements SectionIndexer, View.OnClickListener {

    private LayoutInflater mInflater;
    private BaseActivity baseActivity;
    private SparseIntArray positionOfSection;
    private SparseIntArray sectionOfPosition;
    private List<JSONObject> datas;
    private ArrayList<JSONObject> mOriginalValues;
    private final Object lock = new Object();
    private Filter mFilter;

    public ContactAdapter(Context context) {
        this.baseActivity = (BaseActivity) context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setup(List<JSONObject> datas) {
        this.datas = datas;
        this.notifyDataSetChanged();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return positionOfSection.get(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        return sectionOfPosition.get(position);
    }

    @Override
    public Object getItem(int position) {
        return position == 0 ? new JSONObject() : datas.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return datas == null ? 1 : datas.size() + 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.adapter_contact_header, null);
                ViewHolder.get(convertView, R.id.contact_new_friend_tv).setOnClickListener(this);
                ViewHolder.get(convertView, R.id.contact_chat_tv).setOnClickListener(this);
                final EditText searchEt = (EditText) convertView.findViewById(R.id.contact_search_et);
                searchEt.setFocusable(true);
                searchEt.setFocusableInTouchMode(true);
                searchEt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String s1 = s == null ? null : (s + "").toLowerCase();
                        getFilter().filter(s1);
                        if (onContactListener != null) {
                            onContactListener.onSearchEditViewChange(s.length() <= 0);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        } else {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.adapter_contact, null);
            }
            LinearLayout contactLayout = ViewHolder.get(convertView, R.id.contact_layout);
            TextView headerTxt = ViewHolder.get(convertView, R.id.contact_header_txt);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.contact_avatar_img);
            TextView nameTxt = ViewHolder.get(convertView, R.id.contact_name_tv);

            final JSONObject each = (JSONObject) getItem(position);
            nameTxt.setText(AppService.showUserName(each));
            String header = each.optString("firstLetter");
            Glide.with(baseActivity).load(each.optString("headPortraitsUrl")).transform(new GlideRoundTransform(baseActivity, DensityUtil.dip2px(baseActivity, 5))).placeholder(R.mipmap.default_user_avatar).into(avatarImg);
            if (position == 0 || header != null && !header.equals(((JSONObject) getItem(position - 1)).optString("firstLetter"))) {
                if ("".equals(header)) {
                    headerTxt.setVisibility(View.GONE);
                } else {
                    headerTxt.setVisibility(View.VISIBLE);
                    headerTxt.setText(header);
                }
            } else {
                headerTxt.setVisibility(View.GONE);
            }
            contactLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onContactListener != null) {
                        onContactListener.onContactItemClick(each);
                    }
                }
            });
        }
        return convertView;
    }

    @Override
    public Object[] getSections() {
        positionOfSection = new SparseIntArray();
        sectionOfPosition = new SparseIntArray();
        int count = getCount();
        List<String> list = new ArrayList<>();
        list.add("#");
        positionOfSection.put(0, 0);
        sectionOfPosition.put(0, 0);
        for (int i = 1; i < count; i++) {
            JSONObject contact = (JSONObject) getItem(i);
            String letter = contact.optString("firstLetter");
            ALog.i("contact adapter getsection getHeader:" + letter + " name:" + AppService.showUserName(contact));
            int section = list.size() - 1;
            if (list.get(section) != null && !list.get(section).equals(letter)) {
                list.add(letter);
                section++;
                positionOfSection.put(section, i);
            }
            sectionOfPosition.put(i, section);
        }
        return list.toArray(new String[list.size()]);
    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    @Override
    public void onClick(View v) {
        if (onContactListener != null) {
            if (v.getId() == R.id.contact_chat_tv) {
                onContactListener.onHeaderButtonClick(HeaderBtnType.GroupChat);
            } else if (v.getId() == R.id.contact_new_friend_tv) {
                onContactListener.onHeaderButtonClick(HeaderBtnType.NewBuddy);
            }
        }
    }

    private class ArrayFilter extends Filter {
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            datas = (List<JSONObject>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (mOriginalValues == null) {
                synchronized (lock) {
                    mOriginalValues = new ArrayList<>(datas);
                }
            }
            if (constraint != null && constraint.toString().length() > 0) {
                final String prefixString = constraint.toString().toLowerCase();
                ArrayList<JSONObject> values = mOriginalValues;
                int count = values.size();
                ArrayList<JSONObject> newValues = new ArrayList(count);
                for (int i = 0; i < count; i++) {
                    JSONObject item = values.get(i);
                    if (item.optString("name").contains(prefixString)
                            || item.optString("login").contains(prefixString)
                            || item.optString("pinyin").contains(prefixString)) {
                        newValues.add(item);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            } else {
                synchronized (lock) {
                    ArrayList<JSONObject> list = new ArrayList(mOriginalValues);
                    results.values = list;
                    results.count = list.size();
                }
            }
            return results;
        }
    }

    public OnContactListener onContactListener;

    public void setOnContactClickListener(OnContactListener onContactListener) {
        this.onContactListener = onContactListener;
    }

    public interface OnContactListener {

        void onSearchEditViewChange(boolean isEmpty);

        void onHeaderButtonClick(HeaderBtnType btnType);

        void onContactItemClick(JSONObject contact);
    }

    public enum HeaderBtnType {
        NewBuddy, GroupChat
    }

}
