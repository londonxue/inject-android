package mokai.inject.lib.ui;

import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
/**
 * 值注入进行过滤
 * @author mokai
 *
 */
public abstract class InjectFilter {
	public String textViewFileter(int viewId, String value, TextView view) {
		return value;
	}

	public String editTextFileter(int viewId, String value, EditText view) {
		return value;
	}

	public String spinnerFileter(int viewId, String value, Spinner view) {
		return value;
	}

	public void otherViewFileter(View view) {
	}
}