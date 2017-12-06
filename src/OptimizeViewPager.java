package com.shinichi.stickynavlayout.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.lang.reflect.Field;

/**
 * Created by SherlockHolmes on 2017/12/6.09:59
 */
public class OptimizeViewPager extends ViewPager {

	private Context mContext;
	private float xDistance, yDistance, xLast, yLast;// x、y滑动距离及坐标点
	private boolean mIsBeingDragged = true;// 是否是横向滑动
	private static float MIN_ANGLE = 0.5f;// 最小触发横向滑动的 y/x 的比例
	private static int MIN_RANGE = 10;// 最小触发横向滑动的距离

	public OptimizeViewPager(Context context) {
		super(context);
		this.mContext = context;
		fixTouchSlop();
	}

	public OptimizeViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		fixTouchSlop();
	}

	/**
	 * 通过反射修改viewpager的触发切换的最小滑动距离
	 **/
	private void fixTouchSlop() {
		Field field;
		try {
			field = ViewPager.class.getDeclaredField("mMinimumVelocity");
			field.setAccessible(true);
			try {
				field.setInt(this, px2sp(mContext, MIN_RANGE));// 不是固定的，可以根据自己需求更改
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	private int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 根据滑动的角度进行事件分发，解决和recyclerview嵌套使用，滑动不流畅的问题
	 **/
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		getParent().requestDisallowInterceptTouchEvent(true);
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				xDistance = yDistance = 0f;
				xLast = ev.getX();
				yLast = ev.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				final float curX = ev.getX();
				final float curY = ev.getY();
				xDistance += Math.abs(curX - xLast);
				yDistance += Math.abs(curY - yLast);
				xLast = curX;
				yLast = curY;
				if (!mIsBeingDragged) {
					if (yDistance <= xDistance * MIN_ANGLE) {// 不是固定的，可以根据自己需求更改
						mIsBeingDragged = true;
						getParent().requestDisallowInterceptTouchEvent(true);
					} else {
						mIsBeingDragged = false;
						getParent().requestDisallowInterceptTouchEvent(false);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mIsBeingDragged = false;
				break;
		}
		return super.dispatchTouchEvent(ev);
	}
}