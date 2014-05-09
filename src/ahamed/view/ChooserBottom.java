/**
 * Copyright 2014 Riyaz Ahamed
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ahamed.view;

import ahamed.view.utils.ColorDynamics;
import ahamed.view.utils.Dynamics;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

public class ChooserBottom extends FrameLayout implements OnClickListener {

	public interface ChooserBottomListener {
		public void onItemChosen(int item);

	}

	private static final int INDICATOR_HEIGHT = 8;

	private Dynamics offset = new Dynamics(120f, 0.8f);
	private Paint paint = new Paint();
	private RectF indicationRect = new RectF();
	private ColorDynamics indicatorColor = new ColorDynamics();

	private Runnable animator = new Runnable() {

		@Override
		public void run() {
			boolean schedlueNewFrame = false;
			final long now = AnimationUtils.currentAnimationTimeMillis();
			offset.update(now);
			if (!offset.isAtRest()) {
				schedlueNewFrame = true;
			}

			indicatorColor.update(now);
			if (!indicatorColor.isAtRest()) {
				schedlueNewFrame = true;
			}

			if (schedlueNewFrame) {
				postDelayed(this, 15);
			}
			invalidate(0, (int) indicationRect.top, getWidth(),
					(int) indicationRect.bottom);
		}
	};

	private ChooserBottomListener listener;

	/**
	 * @author Riyaz Ahamed <br>
	 *         <p>
	 *         Creates a customised tabhost with proper touch listeners. Uses
	 *         Dynamics to Animate the View.<br>
	 *         Send Feedbacks to dev.ahamed(at)outlook.com
	 *         </p>
	 */
	public ChooserBottom(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint.setAntiAlias(true);
		setWillNotDraw(false); // we draw things
	}

	public void setChooserListener(ChooserBottomListener listener) {
		this.listener = listener;
	}

	public int getColor() {
		return indicatorColor.getColor();
	}

	@Override
	public void onClick(View view) {
		long now = AnimationUtils.currentAnimationTimeMillis();
		offset.setTargetPosition(view.getLeft(), now);
		invalidate();

		int color = getLayoutColor(view);
		indicatorColor.setTargetColor(color, now);

		if (listener != null) {
			int selected = indexOfChild(view);
			listener.onItemChosen(selected);
		}

		removeCallbacks(animator);
		post(animator);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new ChooserBottom.LayoutParams(getContext(), attrs);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);

		child.setOnClickListener(this);
		int color = getLayoutColor(child);
		child.setBackgroundDrawable(createBackgroundDrawable(color));

		if (getChildCount() == 1) {
			long now = AnimationUtils.currentAnimationTimeMillis();
			indicatorColor.setColor(color, now);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int childCount = getChildCount();

		int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()
				- getPaddingRight();
		int childWidth = width / childCount;
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth,
				MeasureSpec.EXACTLY);

		int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()
				- getPaddingBottom() - INDICATOR_HEIGHT;
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
				heightMode);

		int maxChildHeight = 0;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			int childMeasuredHeight = child.getMeasuredHeight();
			if (childMeasuredHeight > maxChildHeight) {
				maxChildHeight = childMeasuredHeight;
			}
		}

		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxChildHeight,
				MeasureSpec.EXACTLY);
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}

		setMeasuredDimension(width + getPaddingLeft() + getPaddingRight(),
				maxChildHeight + INDICATOR_HEIGHT + getPaddingBottom()
						+ getPaddingTop());

		// int indicationTop = getPaddingTop();
		// indicationRect.set(0, indicationTop, childWidth, indicationTop +
		// INDICATOR_HEIGHT);
		indicationRect.set(0, 0, childWidth, INDICATOR_HEIGHT);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int childCount = getChildCount();
		int width = getWidth() - getPaddingLeft() - getPaddingRight();
		int childWidth = width / childCount;
		int childTop = getPaddingTop();

		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);

			final int childMeasuredWidth = child.getMeasuredWidth();
			final int childMeasuredHeight = child.getMeasuredHeight();

			int childLeft = (int) (getPaddingLeft() + i * childWidth);
			child.layout(childLeft, INDICATOR_HEIGHT - childTop, childLeft
					+ childMeasuredWidth, INDICATOR_HEIGHT - childTop
					+ childMeasuredHeight);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawIndicator(canvas);
		drawDividers(canvas);
	}

	private Drawable createBackgroundDrawable(int color) {
		StateListDrawable backgroundDrawable = new StateListDrawable();
		backgroundDrawable.addState(PRESSED_ENABLED_STATE_SET,
				new ColorDrawable(color));
		return backgroundDrawable;
	}

	private int getLayoutColor(View view) {
		ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
		if (layoutParams instanceof ChooserBottom.LayoutParams) {
			ChooserBottom.LayoutParams params = (ChooserBottom.LayoutParams) layoutParams;
			return params.color;
		}
		return Color.WHITE;
	}

	private void drawIndicator(Canvas canvas) {
		indicationRect.offsetTo(offset.getPosition(), indicationRect.top);
		paint.setColor(indicatorColor.getColor());
		canvas.drawRect(indicationRect, paint);
	}

	private void drawDividers(Canvas canvas) {
		int height = getHeight() - getPaddingTop() - getPaddingBottom();
		int childCount = getChildCount();

		int top = (int) (height * 0.2f) + getPaddingTop();
		int bottom = (int) (height * 0.8f) + getPaddingTop();

		paint.setColor(0x40333333);
		paint.setAntiAlias(false);
		for (int i = 1; i < childCount; i++) {
			View prev = getChildAt(i - 1);
			View next = getChildAt(i);
			float x = (prev.getRight() + next.getLeft()) / 2;
			canvas.drawLine(x, top, x, bottom, paint);

		}
		paint.setAntiAlias(true);
	}
	
	public void setSelectedView(View view) {
		long now = AnimationUtils.currentAnimationTimeMillis();
		offset.setTargetPosition(view.getLeft(), now);
		invalidate();

		int color = getLayoutColor(view);
		indicatorColor.setTargetColor(color, now);

		removeCallbacks(animator);
		post(animator);
	}

	static class LayoutParams extends FrameLayout.LayoutParams {

		int color;

		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.Choser_Layout);
			color = a.getColor(R.styleable.Choser_Layout_layout_color,
					Color.WHITE);
			a.recycle();
		}
	}
}
