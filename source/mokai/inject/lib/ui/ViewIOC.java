package mokai.inject.lib.ui;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * 页面注入类
 * 
 * 1、详情页面的值注入
 * 
 * 2、填写页面的值获取
 * 
 * 3、填写页面的值重置
 * 
 *
 * @author mokai
 * 
 */
public class ViewIOC {
	private static final String TAG="V";
	private static Field[] fields;
	private String format="%s";
	private InjectFilter viewFilter;
	
	public ViewIOC(Class rIdClazz){
		if(fields==null) fields = rIdClazz.getDeclaredFields();
		viewFilter = new InjectFilter(){};
	}
	
	/**
	 * 注入格式符
	 * 
	 * 把map中的相对应key通过格式后注入到相应的控件id上去，如 map [name:mokai,qq:865425695] ids
	 * [textview:id为field_user_name,edittext:id为field_user_qq] 
	 * 那么进来的format应该为field_user_%s
	 * 
	 */
	public ViewIOC setFormat(String format){
		this.format=format;
		return this;
	}
	
	
	/**
	 * 设置View过滤，重写的方法中如果返回null则代表已处理，程序将跳过设置Value
	 * @return 
	 */
	public  ViewIOC setViewFilter(InjectFilter filter){
		if(filter!=null) this.viewFilter=filter;
		return this;
	}
	
	/**
	 * 把map中的相对应key注入到相应的控件id上去
	 */
	public  void  inValues(View view, Map map) {
		if (map == null || view == null || map.size()==0) return;
		map = formatMap(map);
		if (view instanceof ViewGroup && view.getClass()!=Spinner.class) {
			ViewGroup layout = (ViewGroup) view;
			for (int i = 0; i < layout.getChildCount(); i++) {
				View childView = layout.getChildAt(i);
				inValues(childView, map);
			}
		} else {
			setValues(view, map);
		}
	}

	public  void inValues(View view, Bundle bundle) {
		if (view == null || bundle == null)
			return;
		Map retMap = new HashMap();
		if (bundle != null) {
			for (String key : bundle.keySet()) {
				retMap.put(key, bundle.get(key));
			}
		}
		inValues(view, retMap);
	}
	
	
	/**
	 * 获得View下所有子控件有tag值的内容,建议tag与请求参数相同
	 * @param view
	 * @return
	 */
	public static Map<String,String> getValues(View view){
		return getValues(view, null);
	}
	private static Map<String,String> getValues(View view,Map<String,String> datas){
		if(datas==null) datas = new HashMap();
		if (view instanceof ViewGroup && view.getClass()!=Spinner.class) {
			ViewGroup layout = (ViewGroup) view;
			for (int i = 0; i < layout.getChildCount(); i++) {
				View childView = layout.getChildAt(i);
				getValues(childView,datas);
			}
		}else if(view.getTag()!=null && view.getTag().getClass()==String.class){
			String key = view.getTag().toString();
			if(view.getClass()==EditText.class){
				datas.put(key, ((EditText)view).getText().toString());
			}else if(view.getClass()==Spinner.class){
				datas.put(key, ((Spinner)view).getSelectedItemPosition()+"");
			}else if(view.getClass()==TextView.class){
				datas.put(key, ((TextView)view).getText().toString());
			}
		}
		return datas;
	}
	
	/**
	 * 重置所有Tag的控件内容，EditText清空,选择类控件选择第一项
	 * @param view  
	 */
	public static void resetValues(View view){
		if(view==null) return;
		if (view instanceof ViewGroup && view.getClass()!=Spinner.class) {
			ViewGroup layout = (ViewGroup) view;
			for (int i = 0; i < layout.getChildCount(); i++) {
				View childView = layout.getChildAt(i);
				resetValues(childView);
			}
		}else if(view.getTag()!=null && view.getTag().getClass()==String.class){
			if(view.getClass()==EditText.class){
				((EditText)view).setText("");
			}else if(view.getClass()==Spinner.class){
				Spinner sp = (Spinner)view;
				if(sp.getCount()>0){
					((Spinner)view).setSelection(0);
				}
			}else if(view.getClass()==ImageView.class){
				ImageView iView = (ImageView)view;
				iView.setImageDrawable(null);
			}
		}
	}
	
	
	/**
	 * 自定义插件
	 * 1、TextView
	 * 	支持通过 tag 来自定义功能【配置项间以|分开】:
	 * 	max_size:XXX 最大长度，超过以...表示
	 * 	
	 * 
	 * 
	 */
	private  void setValues(View view, Map<String, Object> map) {
		if (view.getClass() == TextView.class) {//TextView
			if (view.getId() > 0) {
				String name = getNameById(view.getId());
				if (name != null && map.containsKey(name)) {//只有id名出现在map中的才处理
					String value = map.get(name)!=null?map.get(name).toString():"";
					TextView textView = ((TextView) view);
					value = viewFilter.textViewFileter(view.getId(), value, textView);
					//插件
					try {
						String tag = textView.getTag() == null ? "" : textView.getTag().toString();
						if(!tag.equals("") && tag.getClass()==String.class){
							String[] tags = tag.split("\\|");
							for (String tg : tags) {
								String pluinName = tg.split(":")[0];
								String pluinValue = tg.split(":")[1];
								if("max_size".equalsIgnoreCase(pluinName)){//最大长度
									this.overflow(value, Integer.valueOf(pluinValue));
								}
							}
						}
					} catch (NumberFormatException e) {
						Log.e(TAG, "V类TextView注入插件id为"+name+":"+e.getMessage());
					}
					if(value!=null){
						textView.setText(value);
					}
				}
			}
		}else if (view.getClass() == EditText.class) {//EditText
			if (view.getId() > 0) {
				String name = getNameById(view.getId());
				if (name != null && map.containsKey(name)) {
					String value = map.get(name)!=null?map.get(name).toString():"";
					EditText editView = ((EditText) view);
					value = viewFilter.editTextFileter(view.getId(), value, editView);
					if(value!=null){
						editView.setText(value);
					}
				}
			}
		}else if (view.getClass() == Spinner.class) {//Spinner  提供Options index索引
			if (view.getId() > 0) {
				String name = getNameById(view.getId());
				if (name != null && map.containsKey(name)) {
					String value = map.get(name)!=null?map.get(name).toString():"0";
					Spinner spinner = ((Spinner) view);
					value = viewFilter.spinnerFileter(view.getId(), value, spinner);
					if(value!=null){
						spinner.setSelection(Integer.valueOf(value));
					}
				}
			}
		}
	}

	private  String getNameById(int id) {
		try {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getInt(null) == id) {
					return field.getName();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 截取指定长度的字符数,用于列表项显示详细内容的长度控制
	 * @return
	 */
	private static String overflow(String str,int length){
		if(str==null || length<=0) return str;
		return str.length()>length?str.substring(0, length)+"...":str;
	}
	
	private Map formatMap(Map map) {
		//格式化key
		if(format!=null && !"".equals(format)){
			Map newMap = new HashMap();
			for (Entry<String,Object> entry : (Set<Entry<String,Object>>)map.entrySet()) {
				newMap.put(String.format(format, entry.getKey()), entry.getValue());
			}
			format = "";
			map = newMap;
		}
		return map;
	}
}
