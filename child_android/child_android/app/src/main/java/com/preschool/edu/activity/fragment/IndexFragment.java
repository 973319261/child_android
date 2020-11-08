package com.preschool.edu.activity.fragment;

import android.os.Bundle;
import android.view.View;

import com.preschool.edu.R;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;

/**
 * Created by jac_cheng on 2017/12/27.
 */
@ContentView(R.layout.fragment_index)
public class IndexFragment extends BaseFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initNaviBar(true, "首页", true);
        mActivity.setImageResByResId(mView, R.id.navi_left_img, R.mipmap.camera);
        mActivity.setImageResByResId(mView, R.id.navi_right_img, R.mipmap.search);
    }

    @Event(value = {R.id.navi_left_layout, R.id.navi_right_layout})
    private void onBtnsClick(View view) {
        switch (view.getId()) {
            case R.id.navi_left_layout:
                break;
            case R.id.navi_right_layout:
                break;
        }
    }
}
