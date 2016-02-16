package com.jiubai.taskmoment.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

@SuppressWarnings("FieldCanBeLocal")
public class SlidingLayout extends FrameLayout {

    /*可根据手势滑动的View*/
    private SlidingView mSlidingView ;
    /*需要绑定的Activity*/
    private Activity mActivity ;
    /*滑动View 的滑动监听*/
    private SlidingView.OnPageChangeListener mPageChangeListener ;
    /*这个是当Activity 滑动到可以结束的时候用到的常量*/
    public static final int POSITION_FINISH = 1 ;
    /*页面的View*/
    private View mContextView  ;
    /*用于定制自己的动画效果的接口*/
    private OnAnimListener mAnimListener ;

    private SlidingLayout(Context context) {
        this(context, null);
    }

    public SlidingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        /*初始化可滑动的View*/
        mSlidingView = new SlidingView(context) ;
        /*吧可滑动的View 添加到当前Layout 中*/
        addView(mSlidingView);
        /*设置滑动监听*/
        mPageChangeListener = new SlidingOnPageChangeListener() ;
        mSlidingView.setOnPageChangeListener(mPageChangeListener);
        mActivity = (Activity) context;
        /*绑定Activity 到可滑动的View 上面*/
        bindActivity(mActivity) ;
    }

    /**
     * 侧滑View 和Activity 绑定
     */
    private void bindActivity(Activity activity){
        /*获取Activity 的最顶级ViewGroup*/
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        /*获取Activity 显示内容区域的ViewGroup，包行ActionBar*/
        ViewGroup child = (ViewGroup) decorView.getChildAt(0);
        decorView.removeView(child);
        setContentView(child) ;
        decorView.addView(this);
    }

    private void setContentView(View view){
        mContextView = view ;
        mSlidingView.setContent(view);

    }
    private class SlidingOnPageChangeListener implements SlidingView.OnPageChangeListener{
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            /*到达了结束Activity 的条件*/
            if (position == POSITION_FINISH){
                /*结束当前 */
                mActivity.finish();
            }
            if (mAnimListener != null){
                mAnimListener.onAnimationSet(mContextView,positionOffset,positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {

        }
    }

    public void setEnable(boolean isEnable){
        mSlidingView.setEnable(isEnable);
    }

    public void setOnPageChangeListener(SlidingView.OnPageChangeListener listener){
        this.mPageChangeListener = listener;
        mSlidingView.setOnPageChangeListener(mPageChangeListener);
    }

    /**
     * 设置自己定义的动画
     */
    @SuppressWarnings("unused")
    public void setOnAnimListener(OnAnimListener listener){
        mAnimListener = listener ;
        mSlidingView.setShouldDraw(false);
    }

    /**
     * 定制动画的接口
     */
    public interface OnAnimListener {
        /**
         * 重写这个方法来定制动画
         */
        void onAnimationSet(View view, float offSet, int offSetPix) ;
    }

    /**
     * 默认的动画效果
     */
    @SuppressWarnings("unused")
    public static class SimpleAnimImpl implements OnAnimListener{
        private final int MAX_ANGLE = 25 ;
        @Override
        public void onAnimationSet(View view, float offSet,int offSetPix) {
            view.setPivotX(view.getWidth()/2.0F);
            view.setPivotY(view.getHeight());
            View.ROTATION.set(view,MAX_ANGLE*offSet);
        }
    }
}