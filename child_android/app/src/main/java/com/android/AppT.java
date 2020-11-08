package com.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.utils.ActivityTaskManager;
import com.android.utils.NetWorkInfo;
import com.android.utils.SPUtils;
import com.android.utils.ToastUtil;
import com.android.widgets.DialogProgress;
import com.koi.chat.R;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;



public class AppT extends FragmentActivity implements OnClickListener {
	public static String DEFAULT_TASK_TEXT = "请稍等...";
	public static String NET_WORK_UNABLE = "网络不可用，请检查您的网络！";
	public static String DEFAULT_HTTP_ERROR = "访问失败，请稍后重试!";
	protected Activity INSTANCE;
	public ActivityTaskManager mTaskManager;
	public void onNaviLeftClick(View v) {
		goBack();
	}

	protected void initNaviHeadView() {

	}

	public void onNaviRightClick(View v) {

	}

	public void onNaviRightSecondClick(View v) {
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		INSTANCE = this;
		mTaskManager = ActivityTaskManager.getInstance();
		mTaskManager.putActivity(this.getClass().getSimpleName(), INSTANCE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTaskManager.removeActivity(this.getClass().getSimpleName());
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
	}

	@Override
	public void onClick(View v) {
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public final boolean isNetOk() {
		return NetWorkInfo.isNetworkAvailable(getApplicationContext());
	}

	/** sp */
	public boolean getSp(String key, boolean defaultValue) {
		return SPUtils.getSp(key, defaultValue);
	}

	public String getSp(String key, String defaultValue) {
		return SPUtils.getSp(key, defaultValue);
	}

	public int getSp(String key, int defaultValue) {
		return SPUtils.getSp(key, defaultValue);
	}

	public long getLongSp(String key, long defaultValue) {
		return SPUtils.getSp(key, defaultValue);
	}

	public void setSp(String key, int value) {
		SPUtils.setSp(key, value);
	}

	public void setSp(String key, long value) {
		SPUtils.setSp(key, value);
	}

	public void setSp(String key, boolean value) {
		SPUtils.setSp(key, value);
	}

	public void setSp(String key, String value) {
		SPUtils.setSp(key, value);
	}

	public void removeSp(String key) {
		SPUtils.removeSp(key);
	}

	public void goBack() {
		this.finish();
		hideKb();
		this.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
	}

	public void exitProgrames() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void openTransition() {
		this.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}

	public void openAlpha() {
		this.overridePendingTransition(R.anim.alpha_in, 0);
	}

	public void alphaOpen(Class<? extends Activity> b, boolean finish) {
		Intent intent = new Intent(this, b);
		startActivity(intent);
		if (finish) {
			this.finish();
		}
		openAlpha();
	}

	public void open(Class<? extends Activity> b, boolean finishSelf) {
		Intent intent = new Intent();
		intent.setClass(this, b);
		this.startActivity(intent);
		if (finishSelf) {
			this.finish();
		}
		openTransition();
	}

	public void open(Class<? extends Activity> b) {
		open(b, false);
	}

	public void open(Class<? extends Activity> b, int requestCode) {
		Intent intent = new Intent();
		intent.setClass(this, b);
		this.startActivityForResult(intent, requestCode);
		openTransition();
	}

	public void open(Class<? extends Activity> b, int requestCode, String intentKey, Object intentValue) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(intentKey, intentValue);
		this.open(b, requestCode, params);
	}

	public void open(Class<? extends Activity> b, int requestCode, Map<String, Object> params) {
		this.startActivityForResult(openWithMap(b, params), requestCode);
		openTransition();
	}

	private Intent openWithMap(Class<? extends Activity> b, Map<String, Object> params) {
		Intent intent = new Intent();
		intent.setClass(this, b);
		for (Map.Entry<String, Object> each : params.entrySet()) {
			if (each.getValue() == null) {
				continue;
			}
			if (each.getValue() instanceof Boolean) {
				intent.putExtra(each.getKey(), (Boolean) each.getValue());
			} else if (each.getValue() instanceof Integer) {
				intent.putExtra(each.getKey(), (Integer) each.getValue());
			} else if (each.getValue() instanceof Long) {
				intent.putExtra(each.getKey(), (Long) each.getValue());
			} else if (each.getValue() instanceof String) {
				intent.putExtra(each.getKey(), String.valueOf(each.getValue()));
			} else if (each.getValue() instanceof Short) {
				intent.putExtra(each.getKey(), (Short) each.getValue());
			} else if (each.getValue() instanceof Serializable) {
				intent.putExtra(each.getKey(), (Serializable) each.getKey());
			} else if (each.getValue() instanceof Bundle) {
				intent.putExtras((Bundle) each.getValue());
			} else {
				intent.putExtra(each.getKey(), String.valueOf(each.getValue()));
			}
		}
		return intent;
	}

	public void open(Class<? extends Activity> b, Map<String, Object> params) {
		this.startActivity(openWithMap(b, params));
		openTransition();
	}

	public void open(Class<? extends Activity> b, String intentKey,
					 Object intentValue) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(intentKey, intentValue);
		this.open(b, params);
	}

	public void open(Class<? extends Activity> b, String intentKey,
					 Object intentValue, boolean finished) {
		this.open(b, intentKey, intentValue);
		if (finished) {
			this.finish();
		}
	}

	public String getIntentString(String key) {
		Bundle extras = this.getIntent().getExtras();
		return extras == null ? "" : extras.getString(key);
	}

	public int getIntentInt(String key) {
		Bundle extras = this.getIntent().getExtras();
		return extras == null ? 0 : extras.getInt(key);
	}

	public boolean getIntentBoolean(String key) {
		Bundle extras = this.getIntent().getExtras();
		return extras == null ? false : extras.getBoolean(key);
	}

	public long getIntentLong(String key) {
		Bundle extras = this.getIntent().getExtras();
		return extras == null ? 0 : extras.getLong(key);
	}

	public short getIntentShort(String key) {
		Bundle extras = this.getIntent().getExtras();
		return extras.getShort(key);
	}

	public void addClickListener(int... onClickId) {
		for (int each : onClickId) {
			findViewById(each).setOnClickListener(this);
		}
	}

	public void addClickListener(View view, int... onClickId) {
		for (int each : onClickId) {
			view.findViewById(each).setOnClickListener(this);
		}
	}

	public void addTextViewByIdAndStr(View view, int id, String showString) {
		TextView tv = (TextView) view.findViewById(id);
		tv.setText(showString);
	}

	public void addTextViewByStr(View view, String showString) {
		((TextView) view).setText(showString);
	}

	public void addTextViewByIdAndStr(int id, String showString) {
		TextView tv = (TextView) findViewById(id);
		tv.setText(showString);
	}

	public void addTextViewHintByIdAndStr(int id, String showString) {
		TextView tv = (TextView) findViewById(id);
		tv.setHint(showString);
	}

	public void addTextViewByIdAndStr(Map<Integer, String> map) {
		for (int id : map.keySet()) {
			addTextViewByIdAndStr(id, map.get(id));
		}
	}

	public void addTextViewByIdAndStr(View view, Map<Integer, String> map) {
		for (int id : map.keySet()) {
			addTextViewByIdAndStr(view, id, map.get(id));
		}
	}

	public void addTextByIdWithStringId(int viewId, int stringId) {
		addTextViewByIdAndStr(viewId, getResources().getString(stringId));
	}

	public String etTxt(EditText et) {
		return et.getText().toString();
	}

	public boolean etIsNull(EditText et) {
		return StringUtils.isBlank(etTxt(et));
	}

	public String tvTxt(TextView tv) {
		return tv.getText().toString();
	}

	public boolean tvIsNull(TextView tv) {
		return StringUtils.isBlank(tvTxt(tv));
	}

	public String tvTxt(int tvId) {
		TextView tv = (TextView) findViewById(tvId);
		return tv != null ? tv.getText().toString() : "";
	}

	public boolean tvIsNull(int tvId) {
		return StringUtils.isBlank(tvTxt(tvId));
	}

	public void showView(View view) {
		view.setVisibility(View.VISIBLE);
	}

	public void showViewById(int id) {
		findViewById(id).setVisibility(View.VISIBLE);
	}

	public void hideView(View view, boolean gone) {
		view.setVisibility(gone ? View.GONE : View.INVISIBLE);
	}

	public void hideViewId(int id, boolean gone) {
		findViewById(id).setVisibility(gone ? View.GONE : View.INVISIBLE);
	}

	public void setBackgroundByResId(int id, int resid) {
		findViewById(id).setBackgroundResource(resid);
	}

	public void setImageResByResId(int id, int resid) {
		ImageView imageView = (ImageView) findViewById(id);
		imageView.setImageResource(resid);
	}


	public void setImageResByResId(View view, int id, int resid) {
		ImageView imageView = (ImageView) view.findViewById(id);
		imageView.setImageResource(resid);
	}

	public void alert(String content) {
		new Builder(this).setMessage(content).setPositiveButton("确定", null)
				.show();
	}

	public void alert(String content, DialogInterface.OnClickListener listener) {
		new Builder(this).setMessage(content).setPositiveButton("确定", listener)
				.show();
	}

	public void alertWithCancel(String content,
								DialogInterface.OnClickListener listener) {
		new Builder(this).setMessage(content).setNegativeButton("确定", listener)
				.setPositiveButton("取消", null).show();
	}

	public void hideKb() {
		try {
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(this.getCurrentFocus()
							.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
		}
	}

	public void showKb() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public void toast(String msg) {
		ToastUtil.centerToast(this, msg);
	}

	public void call(String phone) {
		Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		startActivity(mIntent);
	}

	public void tel(final String phone) {
		Builder builder = new Builder(this);
		builder.setMessage(phone);
		builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("呼叫", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				call(phone);
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void exit() {
		mTaskManager.closeAllActivity();
		System.exit(0);
	}
	
	private DialogProgress proDialog;
	
	public void showDialog(){
		if (proDialog == null) {
			proDialog = new DialogProgress(INSTANCE, R.style.dialog, DEFAULT_TASK_TEXT);
		}
		if (!proDialog.isShowing()) {
			proDialog.show();
		}
	}
	
	public void showDialog(String showStr){
		if (proDialog == null) {
			proDialog = new DialogProgress(INSTANCE, R.style.dialog, showStr);
		}
		if (!proDialog.isShowing()) {
			proDialog.show();
		}
	}
	
	public void closeDialog() {
		if (proDialog != null && proDialog.isShowing()) {
			proDialog.dismiss();
		}
	}
	
	public View inflateView(int id){
		return getLayoutInflater().inflate(id, null);
	}


}
