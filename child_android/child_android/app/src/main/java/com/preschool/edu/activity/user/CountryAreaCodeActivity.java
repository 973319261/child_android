package com.preschool.edu.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.android.widgets.SideBar;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.adapter.CountryAreaCodeAdapter;
import com.preschool.edu.core.AppService;
import com.preschool.edu.model.CountryAreaCode;
import com.preschool.edu.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

/**
 * Created by jac_cheng on 2017/12/27.
 */
@ContentView(R.layout.activity_country_area_code_pick)
public class CountryAreaCodeActivity extends BaseActivity implements CountryAreaCodeAdapter.CountryAreaCodeListener {
    @ViewInject(R.id.listview)
    private ListView listView;
    @ViewInject(R.id.sideBar)
    private SideBar sideBar;
    private CountryAreaCodeAdapter countryAreaCodeAdapter;

    @Override
    protected void initNaviHeadView() {
        super.initNaviHeadView();
        addTextViewByIdAndStr(R.id.navi_title_txt, "请选择国家和地区");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initNaviHeadView();

        countryAreaCodeAdapter = new CountryAreaCodeAdapter(this);
        countryAreaCodeAdapter.setCountryAreaCodeListener(this);
        listView.setAdapter(countryAreaCodeAdapter);
        sideBar.setTypeEnum(SideBar.TypeEnum.AreaCode);
        sideBar.setListView(listView);
        showDialog();
        x.task().run(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new MessageEvent(-111, AppService.getCountryAreaCodeDatas()));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void initDatas(MessageEvent messageEvent) {
        if (messageEvent.getMsgType() == -111) {
            closeDialog();
            countryAreaCodeAdapter.setup((List<CountryAreaCode>) messageEvent.getPayload());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPicked(CountryAreaCode countryAreaCode) {
        Intent intent = new Intent();
        intent.putExtra("areaCode", countryAreaCode.areaCode);
        setResult(RESULT_OK, intent);
        goBack();
    }
}
