/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.zxingdemo.zxing.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.test.zxingdemo.R;
import com.test.zxingdemo.zxing.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;

    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int laserColor;
    private final int resultPointColor;
    private int scannerAlpha;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;
    private int ScreenRate;//四个角边对应的长度
    private static final int CORNER_WIDTH = 10;//拐角对应矩形宽度
    private final float density;
    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;
    /**
     * 中间滑动线的最底端位置
     */
    private int slideBottom;

    /**
     * 扫描框中的中间线的与扫描框左右的间隙
     */
    private static final int MIDDLE_LINE_PADDING = 5;
    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 5;
    /**
     * 扫描框中的中间线的宽度
     */
    /**
     * 字体大小
     */
    private static final int TEXT_SIZE = 16;

    /**
     * 字体距离扫描框下面的距离
     */
    private static final int TEXT_PADDING_TOP = 30;


    private static final int MIDDLE_LINE_WIDTH = 6;
    private boolean isFirst;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        //将像素转换成dp
        ScreenRate = (int) (20 * density);
        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        laserColor = resources.getColor(R.color.viewfinder_laser);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        int width = getWidth();
        Rect frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        //初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top;
            slideBottom = frame.bottom;
        }
        //画出四个角
        paint.setColor(getResources().getColor(R.color.colorGreen));
        //左上角
        canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate, frame.top + CORNER_WIDTH, paint);
        canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top + ScreenRate, paint);

        //右上角
        canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right, frame.top + CORNER_WIDTH, paint);
        canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top + ScreenRate, paint);

        //左下角
        canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left + ScreenRate, frame.bottom, paint);
        canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left + CORNER_WIDTH, frame.bottom, paint);

        //右下角
        canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate, frame.right, frame.bottom, paint);
        canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH, frame.right, frame.bottom, paint);

        //绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
        slideTop += SPEEN_DISTANCE;
        if (slideTop >= frame.bottom) {
            slideTop = frame.top;
        }
        canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH / 2, frame.right - MIDDLE_LINE_PADDING, slideTop + MIDDLE_LINE_WIDTH / 2, paint);

        //画扫描框下面的字

        paint.setColor(Color.WHITE);
        paint.setTextSize(TEXT_SIZE * density);
        paint.setAlpha(0x40);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        String text = getResources().getString(R.string.scan_text);
        float textWidth = paint.measureText(text);//测量出文字的宽度，获取屏幕的宽度，(屏幕宽度-文字宽度) / 2 开始绘制文字

        canvas.drawText(text, (width - textWidth) / 2, frame.bottom + (float) TEXT_PADDING_TOP * density, paint);

        postInvalidateDelayed(ANIMATION_DELAY,
                frame.left - POINT_SIZE,
                frame.top - POINT_SIZE,
                frame.right + POINT_SIZE,
                frame.bottom + POINT_SIZE);

    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

}
