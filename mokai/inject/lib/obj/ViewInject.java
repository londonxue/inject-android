package mokai.inject.lib.obj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * UI控件注入
 * @author mokai
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface ViewInject {
	public int id();
	/**
	 * 单击事件
	 * @return
	 */
	public String click() default "";
	/**
	 * 长按事件
	 * @return
	 */
	public String longClick() default "";
	/**
	 * listview item点击事件
	 * @return
	 */
	public String itemClick() default "";
	/**
	 * listview item长按事件
	 * @return
	 */
	public String itemLongClick() default "";
}
