package com.preschool.edu.activity.fragment;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;

import org.xutils.x;


/**
 * Created by jac_cheng on 2017/4/13.
 */

public class BaseFragment extends Fragment {

    protected BaseActivity mActivity;
    protected View mView;
    private boolean injected = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        injected = true;
        mView = x.view().inject(this, inflater, container);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!injected) {
            x.view().inject(this, this.getView());
        }
    }

    protected void initNaviBar(String title, boolean rightShow) {
        initNaviBar(true, title, rightShow);
    }

    protected void initNaviBar(boolean leftShow, String title, boolean rightShow) {
        LinearLayout leftLayout = (LinearLayout) mView.findViewById(R.id.navi_left_layout);
        leftLayout.setVisibility(View.INVISIBLE);
        if (leftShow) {
            leftLayout.setVisibility(View.VISIBLE);
        }
        LinearLayout rightLayout = (LinearLayout) mView.findViewById(R.id.navi_right_layout);
        rightLayout.setVisibility(View.INVISIBLE);
        if (rightShow) {
            rightLayout.setVisibility(View.VISIBLE);
        }
        TextView titleTxt = (TextView) mView.findViewById(R.id.navi_title_txt);
        titleTxt.setText(title);
    }
}
