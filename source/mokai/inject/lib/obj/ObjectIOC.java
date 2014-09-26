package mokai.inject.lib.obj;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.UiModeManager;
import android.app.admin.DevicePolicyManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;

/**
 * 对象的注入
 * 
 * 1、activity自动注入layout文件
 * 2、Activity对象属性注入
 * 3、自定义对象属性的注入
 * @author mokai
 *
 */
@SuppressLint("NewApi")
public class ObjectIOC {
	public static final String TAG = "IOC";

	private static final String ACTIVITY_POSTFIX = "Activity";
	private static final String LAYOUT_PREFIX = "activity_";
	private static final HashMap<String, Integer> layoutIds = new HashMap<String, Integer>();

	/**
	 * 为Activity自动绑定Layout文件 不用再手动调用setContentView()  一般封装在BaseActivity里，实现layout自动绑定的功能 
	 * 
	 * 绑定规则：
	 * 	只限于Activity自动绑定
	 * 	Activity命名格式:XXXActivity,否则绑定失败
	 * 	Layout文件位于当前项目，且命名格式:activity_XXXX(XXXX小写),否则绑定失败
	 * 
	 * 调用方式:ObjectIOC.layoutInject(this, R.layout.class);
	 * 
	 * @param activity 要自动绑定的activity
	 * @param rLayoutClsss 你当前项目所在的R.layout.class,切记是当前项目
	 *
	 */
	public static void layoutInject(Activity activity,Class rLayoutClsss) {
		if(activity==null || rLayoutClsss==null) return;
		String activityName = activity.getClass().getSimpleName();
		int lastIndex = activityName.lastIndexOf(ACTIVITY_POSTFIX);
		if (lastIndex == activityName.length() - ACTIVITY_POSTFIX.length()) {
			activityName = activityName.substring(0,
					activityName.lastIndexOf(ACTIVITY_POSTFIX)).toLowerCase();
		}
		try {
			Integer layoutId = layoutIds.get(activityName);
			if (layoutId == null) {
				layoutId = rLayoutClsss.getField(LAYOUT_PREFIX + activityName)
						.getInt(null);
				layoutIds.put(activityName, layoutId);
			}
			activity.setContentView(activity.getLayoutInflater().inflate(
					layoutId, null));
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "自动注入layout失败:" + e.getMessage());
		}
	}

	

	/**
	 * Activity注解注入。主要用于对象属性与view、资源、系统服务的注入
	 * 
	 * @param mActivity
	 */
	@SuppressLint("NewApi")
	public static void inject(Activity mActivity) {
		Class clazz = mActivity.getClass();
		List<Field> fields = new ArrayList();
		do {
			Field[] fieldsArray = clazz.getDeclaredFields();
			if (fieldsArray != null && fieldsArray.length > 0) {
				fields.addAll(Arrays.asList(fieldsArray));
			}
		} while ((clazz = clazz.getSuperclass()) != null
				&& clazz != Activity.class);

		if (fields != null && fields.size() > 0) {
			Resources res = mActivity.getResources();
			LayoutInflater inflater = LayoutInflater.from(mActivity);
			String[] strArray = new String[] {};
			int[] intArray = new int[] {};
			Integer[] intgerArray = new Integer[] {};
			for (Field field : fields) {
				try {
					field.setAccessible(true);
					Class typeClazz = field.getType();
					if (field.get(mActivity) != null)
						continue;
					// 页面注入
					ViewInject viewInject = field
							.getAnnotation(ViewInject.class);
					if (viewInject != null) {
						int viewId = viewInject.id();
						field.set(mActivity, mActivity.findViewById(viewId));

						setListener(field, viewInject.click(), Method.Click,
								mActivity);
						setListener(field, viewInject.longClick(),
								Method.LongClick, mActivity);
						setListener(field, viewInject.itemClick(),
								Method.ItemClick, mActivity);
						setListener(field, viewInject.itemLongClick(),
								Method.itemLongClick, mActivity);
						continue;
					}

					// 资源文件注入
					ResourceInject resourceInject = field
							.getAnnotation(ResourceInject.class);
					if (resourceInject != null) {
						int id = resourceInject.value();
						if (typeClazz == String.class) {// string
							field.set(mActivity, res.getString(id));
						} else if (typeClazz == strArray.getClass()) {// string-array
							field.set(mActivity, res.getStringArray(id));
						} else if (typeClazz == intArray.getClass()
								|| typeClazz == intgerArray.getClass()) {// int-array
							field.set(mActivity, res.getIntArray(id));
						} else if (typeClazz == int.class
								|| typeClazz == Integer.class) {// dimens
							if (resourceInject.type() == ResourceType.COLOR) {
								field.set(mActivity, res.getColor(id));
							} else if (resourceInject.type() == ResourceType.INTEGER) {
								field.set(mActivity, res.getInteger(id));
							}
						} else if (typeClazz == float.class
								|| typeClazz == Float.class) {// dimens
							field.set(mActivity, res.getDimension(id));
						} else if (typeClazz == Boolean.class
								|| typeClazz == boolean.class) {
							field.set(mActivity, res.getBoolean(id));
						} else if (typeClazz == Drawable.class) {// drawable
							field.set(mActivity, res.getDrawable(id));
						} else if (typeClazz == Animation.class) {// animation
							field.set(mActivity,
									AnimationUtils.loadAnimation(mActivity, id));
						} else if (typeClazz == View.class) {// layout view
							field.set(mActivity, inflater.inflate(id, null));
						} else if (typeClazz == ColorStateList.class) {// colorstatelist
							field.set(mActivity, res.getColorStateList(id));
						}
						continue;
					}
					// 系统服务注入
					Inject inject = field.getAnnotation(Inject.class);
					if (inject != null) {
						if (typeClazz == AccessibilityManager.class) {
							field.set(
									mActivity,
									mActivity
											.getSystemService(Context.ACCESSIBILITY_SERVICE));
						} else if (typeClazz == AccountManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.ACCOUNT_SERVICE));
						} else if (typeClazz == ActivityManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.ACTIVITY_SERVICE));
						} else if (typeClazz == AlarmManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.ALARM_SERVICE));
						} else if (typeClazz == ClipboardManager.class) {
							field.set(
									mActivity,
									mActivity
											.getSystemService(Context.CLIPBOARD_SERVICE));
						} else if (typeClazz == ConnectivityManager.class) {
							field.set(
									mActivity,
									mActivity
											.getSystemService(Context.CONNECTIVITY_SERVICE));
						} else if (typeClazz == AudioManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.AUDIO_SERVICE));
						} else if (typeClazz == DevicePolicyManager.class) {
							field.set(
									mActivity,
									mActivity
											.getSystemService(Context.DEVICE_POLICY_SERVICE));
						} else if (typeClazz == DownloadManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.DOWNLOAD_SERVICE));
						} else if (typeClazz == WindowManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.WINDOW_SERVICE));
						} else if (typeClazz == LayoutInflater.class) {
							field.set(
									mActivity,
									mActivity
											.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
						} else if (typeClazz == PowerManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.POWER_SERVICE));
						} else if (typeClazz == NotificationManager.class) {
							field.set(
									mActivity,
									mActivity
											.getSystemService(Context.NOTIFICATION_SERVICE));
						} else if (typeClazz == KeyguardManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.KEYGUARD_SERVICE));
						} else if (typeClazz == LocationManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.LOCATION_SERVICE));
						} else if (typeClazz == SearchManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.SEARCH_SERVICE));
						} else if (typeClazz == Vibrator.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.VIBRATOR_SERVICE));
						} else if (typeClazz == ConnectivityManager.class) {
							field.set(
									mActivity,
									mActivity
											.getSystemService(Context.CONNECTIVITY_SERVICE));
						} else if (typeClazz == WifiManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.WIFI_SERVICE));
						} else if (typeClazz == InputMethodManager.class) {
							field.set(
									mActivity,
									mActivity
											.getSystemService(Context.INPUT_METHOD_SERVICE));
						} else if (typeClazz == UiModeManager.class) {
							field.set(mActivity, mActivity
									.getSystemService(Context.UI_MODE_SERVICE));
						}
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "自动注入field失败:" + e.getMessage());
				}
			}
		}
	}

	/**
	 * 自定义对象属性的注入
	 * 
	 * @param obj
	 * @param targetParentView
	 */
	public static void objectInject(Object obj, View targetParentView) {
		Class clazz = obj.getClass();
		List<Field> fields = new ArrayList();
		do {
			Field[] fieldsArray = clazz.getDeclaredFields();
			if (fieldsArray != null && fieldsArray.length > 0) {
				fields.addAll(Arrays.asList(fieldsArray));
			}
		} while ((clazz = clazz.getSuperclass()) != null
				&& clazz != Object.class);

		if (fields != null && fields.size() > 0) {
			for (Field field : fields) {
				try {
					field.setAccessible(true);
					Class typeClazz = field.getType();
					if (field.get(obj) != null)
						continue;
					// 页面注入
					ViewInject viewInject = field
							.getAnnotation(ViewInject.class);
					if (viewInject != null) {
						int viewId = viewInject.id();
						field.set(obj, targetParentView.findViewById(viewId));
						setListener(field, viewInject.click(), Method.Click,
								obj);
						setListener(field, viewInject.longClick(),
								Method.LongClick, obj);
						setListener(field, viewInject.itemClick(),
								Method.ItemClick, obj);
						setListener(field, viewInject.itemLongClick(),
								Method.itemLongClick, obj);
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
					
					Log.e(TAG, "自动注入field失败:" + e.getMessage());
				}
			}
		}
	}
	
	
	
	private static void setListener(Field field, String methodName,
			Method method, Object targetHandler) throws Exception {
		if (methodName == null || methodName.trim().length() == 0)
			return;

		Object obj = field.get(targetHandler);

		switch (method) {
		case Click:
			if (obj instanceof View) {
				((View) obj)
						.setOnClickListener(new EventListener(targetHandler)
								.click(methodName));
			}
			break;
		case ItemClick:
			if (obj instanceof AbsListView) {
				((AbsListView) obj).setOnItemClickListener(new EventListener(
						targetHandler).itemClick(methodName));
			}
			break;
		case LongClick:
			if (obj instanceof View) {
				((View) obj).setOnLongClickListener(new EventListener(
						targetHandler).longClick(methodName));
			}
			break;
		case itemLongClick:
			if (obj instanceof AbsListView) {
				((AbsListView) obj)
						.setOnItemLongClickListener(new EventListener(
								targetHandler).itemLongClick(methodName));
			}
			break;
		default:
			break;
		}
	}

	public enum Method {
		Click, LongClick, ItemClick, itemLongClick
	}
}
