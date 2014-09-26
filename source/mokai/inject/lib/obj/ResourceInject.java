package mokai.inject.lib.obj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 资源文件注入
 * 
 * @author mokai
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceInject {
	public int value();
	/**
	 * 为了区分R.color.xxx与R.dimen.xxx
	 * @return
	 */
	public int type() default 1;
}
