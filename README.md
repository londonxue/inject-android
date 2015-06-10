inject-android
==============

by ~ mokai
扣扣:865425695

android注入，视图注入，资源注入，页面值自动绑定等 

#####1、为Activity自动绑定Layout文件 

不用再手动调用setContentView();一般封装在BaseActivity里，实现layout自动绑定的功能 

	绑定规则：
			 * 只限于Activity自动绑定
			 * Activity命名格式:XXXActivity,否则绑定失败
			 * Layout文件位于当前项目，且命名格式:activity_XXXX,否则绑定失败
		
	ObjectIOC.layoutInject(this, R.layout.class);


#####2、注解注入。
主要用于对象属性与view、资源、系统服务的注入


1. 成员变量与注解绑定设置

		/**
		 * View的注入，传入view的id，以及要监听的事件【传入方法名 public void xxx(XXXX view】
		 */
		@ViewInject(id = R.id.inject,click="click")
		private TextView tv;
		@ViewInject(id = R.id.edit_layout)
		private ViewGroup edit_layout;
		
		/**
		 * 资源文件注入
		 */
		@ResourceInject(R.string.app_name)//字符
		private String appName;
	
	
2. 绑定
	
		ObjectIOC.inject(this);
	
>为了View类型的属性能正常绑定，必须在setContentView后调用


#####3、值绑定、获取 
有没有这样一种情景 ～ 在一个信息填写页面或者信息展示页面、需要多次finViewById、多次getText、多次setText。想到这是不是整个人都不好了！

	NOW!神器来啦！

	你只需在信息填写页面中为需要拿值的view中指定一个tag，就可以拿到整个页面的填写信息进行后台提交了
	只需在展示页面、使View的id与数据的key保持，便可以自动注入值
		
![](/Volumes/Macintosh HD 2/git/inject-android/demo_screen.gif)

比如一个用户信息编辑页面

user_edit.xml

<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical" >

    <LinearLayout
        android:id="@+id/edit_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical" >

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="10dip" >

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="姓名:" />

            <EditText
                android:id="@+id/name"
                android:layout_width="0dip"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:tag="name" />
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="10dip" >

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="年龄:" />

            <EditText
                android:id="@+id/age"
                android:layout_width="0dip"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:tag="age" />
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="10dip" >

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="性别:" />

            <Spinner
                android:id="@+id/sex"
                android:layout_width="0dip"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:entries="@array/sex_arr"
                android:spinnerMode="dropdown"
                android:tag="sex" />
        </LinearLayout>
    </LinearLayout>
</LinearLayout>

我们从后台获取到数据先进行展示
		
		Map userData = new HashMap();
		userData.put("name","mokai");
		userData.put("age",20);
		userData.put("sex",0);
		
		new ViewIOC(R.id.class).inValues(findViewById(R.id.edit_layout), userData);
		
当用户点击保存，一键获取相应的值
		
		Map<String, String> userData = ViewIOC.getValues(findViewById(R.id.edit_layout));
		