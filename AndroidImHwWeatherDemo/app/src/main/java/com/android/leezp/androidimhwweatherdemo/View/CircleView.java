package com.android.leezp.androidimhwweatherdemo.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.android.leezp.androidimhwweatherdemo.R;

/**
 * Created by Leezp on 2017/4/14 0014.
 *
 * function:    draw a circle weather view
 */

public class CircleView extends View {
    //线条的画笔
    private Paint lineBrush;
    //文字的画笔
    private TextPaint textBrush;
    //最低的温度角度与最高温度的角度
    private float lowAngle,highAngle;
    //这个圆的半径
    private float r;
    //这个圆的圆心坐标x、y
    private float centerX,centerY;
    //根据温度设置渐变的颜色
    private Shader mShader, mWhiteShader;
    //最低温度与最高温度(default:25-35)
    private int lowTem = 25,highTem = 35;
    //实时温度(default:30)
    private int nowTem = 30;
    //天气种类的图标
    private Bitmap weaBitmap;


    public CircleView(Context context) {
        this(context,null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * function:    初始化控件
     */
    private void init(Context context) {
        lineBrush = new Paint();
        lineBrush.setStrokeWidth(3);
        lineBrush.setAntiAlias(true);

        textBrush = new TextPaint();
        textBrush.setColor(Color.WHITE);
        textBrush.setStrokeWidth(4);
        textBrush.setAntiAlias(true);

        weaBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_snow);

        setAngle(lowTem, highTem);
    }

    /**
     *function:     1.最低温度和最高温度计算出开始的角度与结束的角度
     *              2.需更新最新的最低温度与最高温度调用
     *              3.默认的最低温度与最高温度的夹角是60°
     * @param lowTem    最低温度
     * @param highTem   最高温度
     */
    public void setAngle(int lowTem, int highTem) {
        if ((lowTem>=0&&lowTem<50) && (highTem>=0&&highTem<=60)) {
            //当最低温度大于等于0时
            this.lowAngle = lowTem*2;

            if ((highTem >= 50)||(lowAngle+60 > 150)) {
                this.highAngle = 50*3;
            } else {
                this.highAngle = lowAngle+60;
            }
        } else if ((lowTem<0&&lowTem>-50) && (highTem<=0&&highTem>-50)) {
            //当最低温度小于0,最大温度小于或者等于0时
            if (highTem == 0) {
                //最高温度等于0时
                this.highAngle = 0;
                this.lowAngle = 360-60;
            } else {
                this.highAngle = 360-Math.abs(highTem)*2;
                if (highAngle-60<205) {
                    this.highAngle = 205;
                } else {
                    this.lowAngle = highAngle - 60;
                }
            }
        } else if ((lowTem<0&&lowTem>=-50) && (highTem>0&&highTem<=50)) {
            //当最低温度小于0，最高温度大于0时
            this.highAngle = highTem*2;
            this.lowAngle = 360-(60-highAngle);
        }
        invalidate();
    }

    /**
     * 设置实时温度
     * @param nowTem    实时温度
     */
    public void setNowTem(int nowTem) {
        this.nowTem = nowTem;
        invalidate();
    }

    /**
     * 设置天气种类的图片
     *
     */
    public void setWeaBitmap(Bitmap bitmap) {
        weaBitmap = bitmap;
        invalidate();
    }

    /**
     * 设置最低温度与最高温度
     */
    public void setLowHighTem(int lowTem, int highTem) {
        this.lowTem = lowTem;
        this.highTem = highTem;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        for (double angle = 0;angle <= 360d; angle += 3.0d) {
            float xStart = calculateX(r,angle);
            float xStop = calculateX(r*0.9f,angle);

            float yStart = calculateY(r,angle);
            float yStop = calculateY(r*0.9f,angle);

            //绘制起始角度和终止角度的着色线条
            //根据起始角度和终止角度所在位置，大致可以分为三种情况
            if (((lowAngle>=0&&lowAngle<180) && (highAngle>=0&&highAngle<180)) || ((lowAngle>180&&lowAngle<360) && (highAngle>180&&highAngle<360))) {
                //当起始角度和终止角度在右边半圆或者都在左边半圆时
                if (angle <= highAngle && angle >= lowAngle) {
                    lineBrush.setShader(mShader);
                } else {
                    lineBrush.setShader(mWhiteShader);
                }
            } else if ((lowAngle>180&&lowAngle<=360) && (highAngle>=0&&highAngle<180)) {
                //当起始角度在左边半圆，终止角度在右边半圆时
                if ((angle>=0&&angle<=highAngle) || (angle>=lowAngle&&angle<=360)) {
                    lineBrush.setShader(mShader);
                } else {
                    lineBrush.setShader(mWhiteShader);
                }
            }

            //刻度盘有两种长度和宽度的线条
            if (angle == 207 || angle==153) {
                //绘制边界位置的两条线条
                float xStartL = calculateX(r*1.05f,angle);
                float xStopL = calculateX(r*0.9f,angle);

                float yStartL = calculateY(r*1.05f,angle);
                float yStopL = calculateY(r*0.9f,angle);
                //边界比较长的线
                canvas.drawLine(xStartL,yStartL,xStopL,yStopL,lineBrush);
            } else if(!(angle < 207 && angle > 153)){
                //绘制其他位置的线条
                lineBrush.setStrokeWidth(3);
                //中间区域的短线
                canvas.drawLine(xStart, yStart, xStop, yStop, lineBrush);
            }
        }

        //绘制最低温度、最高温度、中心的实时温度
        drawLowTem(canvas);
        drawHighTem(canvas);
        drawNowTemAndBitMap(canvas);

        canvas.restore();
    }

    /**
     * 根据半径和角度计算x坐标
     */
    private float calculateX(float r, double angle) {
        angle = angle*((2*Math.PI)/360);
        double x = r*Math.sin(angle);

        double xFinal = centerX+x;
        return (float) xFinal;
    }

    /**
     * 根据半径和角度计算y坐标
     */
    private float calculateY(float r, double angle) {
        angle = angle*((2*Math.PI)/360);
        double y = r*Math.cos(angle);

        double yFinal = centerY-y;
        return (float) yFinal;
    }

    /**
     * 画中心位置实时温度以及天气种类的图片
     * @param canvas
     */
    private void drawNowTemAndBitMap(Canvas canvas) {
        textBrush.setTextSize(r*0.6f);
        textBrush.setAntiAlias(true);
        textBrush.setTextAlign(Paint.Align.CENTER);
        float textY = centerY-(textBrush.descent()+textBrush.ascent())/2;
        canvas.drawText(nowTem+"°", centerX, textY, textBrush);
        canvas.drawBitmap(weaBitmap,centerX-0.15f*r,centerY+0.8f*r,null);
    }

    /**
     * 画起始温度
     */
    private void drawLowTem(Canvas canvas) {
        textBrush.setTextSize(r*0.1f);
        canvas.drawText(lowTem+"°",calculateX(r*1.1f,lowAngle),calculateY(r*1.1f,lowAngle),textBrush);
    }

    /**
     * 画截止温度
     */
    private void drawHighTem(Canvas canvas) {
        textBrush.setTextSize(r*0.1f);
        canvas.drawText(highTem+"°",calculateX(r*1.1f,highAngle),calculateY(r*1.1f,highAngle),textBrush);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        centerX = viewWidth/2f;
        centerY = viewHeight/2f;
        r = viewWidth*0.3f;

        //设置渐变色
        mShader = new SweepGradient(centerX,centerY,new int[]{
                Color.parseColor("#FB8B13"),
                Color.parseColor("#FB1414"),
                Color.parseColor("#1488FB"),
                Color.parseColor("#13FBE0"),
                Color.parseColor("#8BFB13"),
                Color.parseColor("#FB8B13")},null);

        mWhiteShader = new SweepGradient(centerX,centerY,new int[]{
                Color.WHITE,
                Color.WHITE},null);

        lineBrush.setShader(mShader);

        invalidate();
    }
}
