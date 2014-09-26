inject-android
==============

by ~ mokai
扣扣:865425695

android注入，视图注入，资源注入，页面值自动绑定等 

1、为Activity自动绑定Layout文件 不用再手动调用setContentView()  一般封装在BaseActivity里，实现layout自动绑定的功能 

	绑定规则：
			 * 只限于Activity自动绑定
			 * Activity命名格式:XXXActivity,否则绑定失败
			 * Layout文件位于当前项目，且命名格式:activity_XXXX,否则绑定失败
		
	ObjectIOC.layoutInject(this, R.layout.class);




2、注解注入。主要用于对象属性与view、资源、系统服务的注入

	ObjectIOC.inject(this);



3、值绑定
	有没有这样一种情景 ～ 在一个信息填写页面或者信息展示页面、需要多次finViewById、多次getText、多次setText。想到这是不是整个人都不好了！

	NOW!神器来啦！

	你只需在信息填写页面中为需要拿值的view中指定一个tag，就可以拿到整个页面的填写信息进行后台提交了
	只需在展示页面、使View的id与数据的key保持，便可以自动注入值

