package com.android.adapter;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.utils.ALog;
import com.android.utils.ViewHolder;
import com.android.PEApplication;
import com.koi.chat.R;
import com.android.model.CountryAreaCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 地区适配器
 */
public class CountryAreaCodeAdapter extends BaseAdapter implements SectionIndexer {
    private LayoutInflater mInflater;
    private SparseIntArray positionOfSection;
    private SparseIntArray sectionOfPosition;
    private List<CountryAreaCode> datas;

    public CountryAreaCodeAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void setup(List<CountryAreaCode> datas) {
        this.datas = datas;
        this.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return datas == null ? null : datas.get(position);
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_country_area_code_cell, null);
        }
        LinearLayout countryLayout = ViewHolder.get(convertView, R.id.country_layout);
        TextView headerTxt = ViewHolder.get(convertView, R.id.country_header_txt);
        TextView nameTxt = ViewHolder.get(convertView, R.id.country_name_txt);

        final CountryAreaCode countryAreaCode = (CountryAreaCode) getItem(position);
        nameTxt.setText(String.format("%s %s", PEApplication.isEn() ? countryAreaCode.countryNameEn : countryAreaCode.countryNameCn, countryAreaCode.areaCode));
        if (position == 0 || (countryAreaCode.header != null && !countryAreaCode.header.equals(((CountryAreaCode) getItem(position - 1)).header))) {
            if ("".equals(countryAreaCode.header)) {
                headerTxt.setVisibility(View.GONE);
            } else {
                headerTxt.setVisibility(View.VISIBLE);
                headerTxt.setText(countryAreaCode.header);
            }
        } else {
            headerTxt.setVisibility(View.GONE);
        }
        countryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPicked(countryAreaCode);
                }
            }
        });
        return convertView;
    }

    public int getPositionForSection(int section) {
        return positionOfSection.get(section);
    }

    public int getSectionForPosition(int position) {
        return sectionOfPosition.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
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
            CountryAreaCode country = (CountryAreaCode) getItem(i);
            String letter = country.header;
            ALog.i("country adapter getsection getHeader:" + letter + " name:" + country.countryNameEn + "->" + country.countryNameCn);
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

    private CountryAreaCodeListener mListener;

    public void setCountryAreaCodeListener(CountryAreaCodeListener mListener) {
        this.mListener = mListener;
    }

    public interface CountryAreaCodeListener {
        void onPicked(CountryAreaCode countryAreaCode);
    }
}
