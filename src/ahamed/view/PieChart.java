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

import ahamed.view.utils.Dynamics;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

/**
 * <p>
 * Creates a {@link PieChart} with provided value for a given activity. Uses
 * Dynamics to Animate the View.<br>
 * Developed based on the library created by <b>Ken Yang</b><br>
 * </p>
 * 
 * @author Riyaz Ahamed M <br>
 *         Send Feedbacks to dev.ahamed(at)outlook.com
 */
public class PieChart extends View {

	public interface OnSelectedLisenter {
		public abstract void onSelected(int iSelectedIndex);
	}

	private Runnable animator = new Runnable() {
		@Override
		public void run() {
			boolean needNewFrame = false;
			long now = AnimationUtils.currentAnimationTimeMillis();
			for (Dynamics dynamics : dataPoints) {
				dynamics.update(now);
				if (!dynamics.isAtRest()) {
					needNewFrame = true;
				}
			}
			if (needNewFrame) {
				postDelayed(this, 20);
			}
			invalidate();
		}
	};

	private OnSelectedLisenter onSelectedListener = null;

	private static final String TAG = PieChart.class.getName();
	private static final int DEGREE_360 = 360;
	private static String[] PIE_COLORS = { "#0099CC", "#FF8800", "#669900",
			"#9933CC", "#CC0000", "#BF1A0B", "#590202", "#BBBF34", "#038C17",
			"#2E707B", "#5CC9CB", "#CAF1E7" };
	private static int iColorListSize = 0;

	private Paint paintPieFill;
	private Paint paintPieBorder;
	private Paint paintPieText;
	private Paint paintLegendText;
	
	private int bgColor;

	private int iDisplayWidth, iDisplayHeight;
	private int iSelectedIndex = -1;
	private int iCenterWidth = 0;
	private int iShift = 0;
	private int iMargin = 0;
	private int iDataSize = 0;

	private RectF r = null;
	private RectF innerRectF = null;
	private RectF legendRectF = null;

	private Rect textBounds;

	private float mPrimaryTextSize;
	private float mSecondaryTextSize;

	private float fDensity = 0.0f;
	private float fStartAngle = 0.0f;
	private float fEndAngle = 0.0f;

	private int totalValue;

	public PieChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.PieChart_Layout);
		bgColor = a.getColor(R.styleable.PieChart_Layout_bg_color,
				Color.WHITE);
		a.recycle();
		
		this.setBackgroundColor(bgColor);

		iColorListSize = PIE_COLORS.length;

		mPrimaryTextSize = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 18, getResources()
						.getDisplayMetrics());

		mSecondaryTextSize = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 14, getResources()
						.getDisplayMetrics());

		fnGetDisplayMetrics(context);
		iShift = (int) fnGetRealPxFromDp(10);
		iMargin = (int) fnGetRealPxFromDp(40);

		textBounds = new Rect();

		// used for paint circle
		paintPieFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintPieFill.setStyle(Paint.Style.FILL);

		// used for paint border
		paintPieBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintPieBorder.setStyle(Paint.Style.STROKE);
		paintPieBorder.setStrokeWidth(fnGetRealPxFromDp(3));
		paintPieBorder.setColor(Color.WHITE);

		paintPieText = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintPieText.setTextAlign(Align.CENTER);

		paintLegendText = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintLegendText.setTextAlign(Align.CENTER);

		paintPieText.setColor(Color.DKGRAY);

		Log.i(TAG, "PieChart init");
	}

	// set listener
	public void setOnSelectedListener(OnSelectedLisenter listener) {
		this.onSelectedListener = listener;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		fStartAngle = 0.0f;
		for (int i = 0; i < iDataSize; i++) {

			// check whether the data size larger than color list size
			if (i >= iColorListSize) {
				paintPieFill.setColor(Color.parseColor(PIE_COLORS[i
						% iColorListSize]));
			} else {
				paintPieFill.setColor(Color.parseColor(PIE_COLORS[i]));
			}

			fEndAngle = dataPoints[i].getPosition() / totalValue * DEGREE_360;

			if (iSelectedIndex == i) {
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				float fAngle = fStartAngle + fEndAngle / 2;
				double dxRadius = Math.toRadians((fAngle + DEGREE_360)
						% DEGREE_360);
				float fY = (float) Math.sin(dxRadius);
				float fX = (float) Math.cos(dxRadius);
				canvas.translate(fX * iShift, fY * iShift);
			}

			canvas.drawArc(r, fStartAngle, fEndAngle, true, paintPieFill);

			if (iSelectedIndex == i) {
				canvas.drawArc(r, fStartAngle, fEndAngle, true, paintPieBorder);
				canvas.restore();
			}
			fStartAngle = fStartAngle + fEndAngle;
			drawLegend(canvas, i);
		}
		paintPieFill.setColor(bgColor);
		paintPieText.setTextSize(innerRectF.width() / 7F);
		String centerText = totalValue + " Projects";
		paintPieText.getTextBounds(centerText, 0, centerText.length(),
				textBounds);
		canvas.drawArc(innerRectF, 0F, 360F, true, paintPieFill);
		canvas.drawText(totalValue + " Projects", innerRectF.centerX(),
				innerRectF.centerY() + textBounds.height() / 2, paintPieText);
	}

	private void drawLegend(Canvas canvas, int i) {

		float legendWidth = legendRectF.width() / mLegendNames.length;
		float legendEndPoint = legendWidth * (i + 1) + legendRectF.left;
		float legendStartPoint = legendEndPoint - legendWidth;

		float bottomOffset = legendRectF.bottom;
		float legendPadding = legendRectF.height() / 10;

		paintLegendText.setColor(paintPieFill.getColor());
		paintLegendText.setTextSize(mSecondaryTextSize);
		canvas.drawText(mLegendNames[i], legendStartPoint + legendWidth / 2,
				bottomOffset, paintLegendText);

		paintLegendText.getTextBounds("gjyALl", 0, 5, textBounds);
		bottomOffset = bottomOffset - textBounds.height() - legendPadding;

		canvas.drawRect(legendStartPoint, bottomOffset - legendPadding / 2,
				legendEndPoint, bottomOffset, paintPieFill);
		bottomOffset = bottomOffset - legendPadding * 1.5F;

		paintLegendText.setColor(paintPieFill.getColor());
		paintLegendText.setTextSize(mPrimaryTextSize);
		canvas.drawText(Integer.toString(mDataValues[i]), legendStartPoint
				+ legendWidth / 2, bottomOffset, paintLegendText);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// get screen size
		iDisplayWidth = MeasureSpec.getSize(widthMeasureSpec);
		iDisplayHeight = MeasureSpec.getSize(heightMeasureSpec);

		if (iDisplayWidth > iDisplayHeight) {
			iDisplayWidth = iDisplayHeight;
		}

		// determine the rectangle size

		iCenterWidth = iDisplayWidth / 2;
		// TODO
		int legendWidth = iCenterWidth / 2;
		int iR = iCenterWidth - iMargin;
		int oR = iR / 2;
		float lR = iCenterWidth - iMargin / 2;
		if (r == null) {
			r = new RectF(iCenterWidth - iR, // top
					iCenterWidth - iR, // left
					iCenterWidth + iR, // rights
					iCenterWidth + iR); // bottom
		}
		if (innerRectF == null) {
			innerRectF = new RectF(iCenterWidth - oR, // top
					iCenterWidth - oR, // left
					iCenterWidth + oR, // rights
					iCenterWidth + oR); // bottom
		}
		if (legendRectF == null) {
			legendRectF = new RectF(iCenterWidth - lR, // top
					iCenterWidth + iR, // left
					iCenterWidth + lR, // rights
					iCenterWidth + iR + legendWidth); // bottom
		}
		setMeasuredDimension(iDisplayWidth, iDisplayWidth + legendWidth);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (r.contains(event.getX(), event.getY())) {
			// get degree of the touch point
			double dx = Math.atan2(event.getY() - iCenterWidth, event.getX()
					- iCenterWidth);
			float fDegree = (float) (dx / (2 * Math.PI) * DEGREE_360);
			fDegree = (fDegree + DEGREE_360) % DEGREE_360;

			float tempTotalValue = 0f;
			// check which pie was selected
			for (int i = 0; i < iDataSize; i++) {
				tempTotalValue += dataPoints[i].getPosition() / totalValue
						* DEGREE_360;
				if (tempTotalValue > fDegree) {
					iSelectedIndex = i;
					break;
				}
			}
			if (onSelectedListener != null) {
				onSelectedListener.onSelected(iSelectedIndex);
			}
			invalidate();
		} else if (legendRectF.contains(event.getX(), event.getY())) {
			for (int i = iDataSize; i > 0; i--) {
				System.out.println("SYS " + event.getX() + " "
						+ (legendRectF.width() / iDataSize) * (i - 1));
				if (event.getX() > (legendRectF.width() / iDataSize) * (i - 1)) {
					iSelectedIndex = i - 1;
					break;
				}
			}
			if (onSelectedListener != null) {
				onSelectedListener.onSelected(iSelectedIndex);
			}
			invalidate();
		}
		return super.onTouchEvent(event);
	}

	private void fnGetDisplayMetrics(Context cxt) {
		final DisplayMetrics dm = cxt.getResources().getDisplayMetrics();
		fDensity = dm.density;
	}

	private float fnGetRealPxFromDp(float fDp) {
		return (fDensity != 1.0f) ? fDensity * fDp : fDp;
	}

	private Dynamics[] dataPoints;
	private String[] mLegendNames;
	private int[] mDataValues;

	public void setData(int[] dataValues, String[] legendNames) {
		totalValue = 0;
		mDataValues = dataValues;
		iDataSize = dataValues.length;
		iSelectedIndex = -1;
		mLegendNames = legendNames;
		long now = AnimationUtils.currentAnimationTimeMillis();
		if (dataPoints == null || dataPoints.length != dataValues.length) {
			dataPoints = new Dynamics[dataValues.length];

			for (int i = 0; i < dataValues.length; i++) {
				totalValue += dataValues[i];
				dataPoints[i] = new Dynamics(80f, 0.8f);
				dataPoints[i].setPosition(0, now);
				dataPoints[i].setTargetPosition(dataValues[i], now);
			}
			removeCallbacks(animator);
			post(animator);
		} else {
			for (int i = 0; i < dataValues.length; i++) {
				totalValue += dataValues[i];
				dataPoints[i].setTargetPosition(dataValues[i], now);
			}
			removeCallbacks(animator);
			post(animator);
		}
	}
}