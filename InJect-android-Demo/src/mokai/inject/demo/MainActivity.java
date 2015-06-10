package mokai.inject.demo;

import java.util.Map;

import mokai.inject.lib.obj.Inject;
import mokai.inject.lib.obj.ObjectIOC;
import mokai.inject.lib.obj.ResourceInject;
import mokai.inject.lib.obj.ResourceType;
import mokai.inject.lib.obj.ViewInject;
import mokai.inject.lib.ui.InjectFilter;
import mokai.inject.lib.ui.ViewIOC;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {
	/**
	 * View的注入，传入view的id，以及要监听的事件【传入方法名 public void xxx(XXXX view】
	 */
	@ViewInject(id = R.id.inject, click = "click")
	private Button btn;

	@ViewInject(id = R.id.edit_layout)
	private ViewGroup edit_layout;

	@ViewInject(id = R.id.detail_layout)
	private ViewGroup detail_layout;

	/**
	 * 资源文件注入
	 *
	 * 
	 */
	@ResourceInject(R.string.app_name)
	// 字符
	private String appName;

	@ResourceInject(R.drawable.ic_launcher)
	// 图片等
	private Drawable icon;

	@ResourceInject(R.array.str_arr)
	// string数组
	private String[] stringArray;

	@ResourceInject(R.array.int_arr)
	// int数组
	private int[] intArray;

	// Animation
	private Animation anima;

	@ResourceInject(R.layout.activity_main)
	// layout
	private View view;

	private ColorStateList colorStateList;// color state list

	// 需要注意的是,在定义dimens为整型(int)时，我们这里必须通过type为进行区别,浮点型就不需要
	@ResourceInject(value = R.dimen.app_width, type = ResourceType.COLOR)
	// dimens
	private int appWidth;
	@ResourceInject(R.dimen.app_width)
	private float appWidthF;
	@ResourceInject(value = R.color.app_color, type = ResourceType.COLOR)
	// color
	private int appColor;

	/**
	 * 系统服务的注入
	 */
	@Inject
	private LayoutInflater inflater;
	@Inject
	private ActivityManager aMgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/***
		 * 为Activity自动绑定Layout文件 不用再手动调用setContentView()
		 * 一般封装在BaseActivity里，实现layout自动绑定的功能
		 * 
		 * 绑定规则： 只限于Activity自动绑定 Activity命名格式:XXXActivity,否则绑定失败
		 * Layout文件位于当前项目，且命名格式:activity_XXXX,否则绑定失败
		 * 
		 * 调用方式:ObjectIOC.layoutInject(this, R.layout.class);
		 */
		ObjectIOC.layoutInject(this, R.layout.class);

		/**
		 * 注解注入。主要用于对象属性与view、资源、服务的注入 见上面的成员属性
		 * 
		 */
		ObjectIOC.inject(this);
		
		
	}

	/**
	 * 此处的click为inject注解中自动绑定事件
	 * 
	 * @param view
	 */
	public void click(Button view) {
		/**
		 * 有没有这样一种情景 ～
		 * 在一个信息填写页面或者信息展示页面、需要多次finViewById、多次getText、多次setText。想到这是不是整个人都不好了！
		 * 
		 * NOW!神器来啦！
		 * 
		 * 你只需在信息填写页面中为需要拿值的view中指定一个tag，就可以拿到整个页面的填写信息进行后台提交了
		 * 只需在展示页面、使View的id与数据的key保持，便可以自动注入值
		 * 
		 * 
		 */
		//一行代码搞定，不指定前缀
		//new ViewIOC(R.id.class).inValues(detail_layout,ViewIOC.getValues(edit_layout));
		
		
		// 1、先获取填写layout中带tag的view的值
		Map<String, String> data = ViewIOC.getValues(edit_layout);

		// 2、为了测试，这里就直接注入到详情layout
		// format 为格式占位符、为了把R.id.xxx模块化。当然你也可以不指定。
		// ViewFilter为注入的控件“类型”前进行过滤
		new ViewIOC(R.id.class).setFormat("user_%s")
				.setViewFilter(new InjectFilter() {
					public String textViewFileter(int viewId, String value,
							TextView view) {
						return value;
					}

					public String editTextFileter(int viewId, String value,
							EditText view) {
						return value;
					}

					public String spinnerFileter(int viewId, String value,
							Spinner view) {
						return value;
					}

					public void otherViewFileter(View view) {
					}
				}).inValues(detail_layout, data);

		// 3、是不是保存后得重置填写layout
		ViewIOC.resetValues(edit_layout);

	}

}
