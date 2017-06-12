package com.zhttty.mylibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import java.util.Arrays;
import java.util.List;

/**
 * author     zhangHeng
 * date :     2017/3/13 18:50.
 * describe:
 */

public class XRecyclerView<T> extends RecyclerView {
    private boolean test = false;
    //自定义LayoutManager的Type
    private final int LINEARMANAGER_TYPE_OTHER = -1;
    //垂直ListView
    private final int LINEARMANAGER_TYPE_LINEAR_VERTICAL = 0;
    //水平ListView
    private final int LINEARMANAGER_TYPE_LINEAR_HORIZONTAL = 1;
    //垂直GridView
    private final int LINEARMANAGER_TYPE_GRID_VERTICAL = 2;
    //水平GridView
    private final int LINEARMANAGER_TYPE_GRID_HORIZONTAL = 3;
    //垂直瀑布流
    private final int LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL = 4;
    //水平瀑布流
    private final int LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL = 5;
    //下拉刷新状态码
    private final int PUSH_TYPE_REFRESH = 0;
    //上拉加载状态码
    private final int PUSH_TYPE_LOAD = 1;
    //滑动阻力
    private final static float OFFSET_RADIO = 1.8f;
    //记录 下拉上拉的状态码
    private int mPullState = PUSH_TYPE_REFRESH;
    //记录设置的LayoutManager
    private int mLayoutManagerType;
    //RecyclerView的布局类型
    private LayoutManager mLayoutManager;
    //回弹计算器
    private Scroller mScroller;
    //容器型为ReccyClerView添加头部和尾部的Adapter
    private HeaderAndFooterWrapper mHeaderAndFooterWrapper;
    //下拉刷新的头部View
    public XHeaderAndFooterState mHeaderView;
    //上拉加载的尾部View
    public XHeaderAndFooterState mFooterView;
    //是否激活下拉
    private boolean mEnablePull = true;
    //是否激活下拉刷新
    private boolean mEnablePullRefresh = true;
    //是否激活上拉加载
    private boolean mEnablePushLoad = true;
    //是否在刷新中
    private boolean mPullRefreshing;
    //是否在加载中
    private boolean mPushLoading;
    //是否开启下拉弹性
    private boolean mPullDownRebound = false;
    //是否开启上拉弹性
    private boolean mPushUpRebound = false;
    //产生了下拉的状态
    private boolean mPullDownState;
    //产生了上拉的状态
    private boolean mPushUpState;
    //是否自动刷新
    private boolean mAutoRefresh;
    //手指上一次点击位置的Y坐标
    private float mLastY;
    //手指上一次点击位置的X坐标
    private float mLastX;
    //头部View的高度
    private int mHeaderHeight;
    //头部View的宽度
    private int mHeaderWidth;
    //尾部View的高度
    private int mFooterHeight;
    //尾部View的宽度
    private int mFooterWidth;
    //实现好的OnItemClickListener
    private OnItemClickListener<T> mOnItemClickListener;
    //上拉下拉的回调监听
    private IXRecyclerViewListener mRecyclerViewListener;
    //上拉下拉的零界点处理
    private CallPullDownAndPushUp mCallPullDownAndPushUp = new RecyclerViewCommonCallPullDownAndPushUp();
    //分页实体,内部自动维护
    private PageStatusBean mPageStatusBean;
    //列表的总长度
    private int mTotal = -1;
    //数据源
    private List<T> mList;

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                if (layoutParams != null)
                    getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                if (layoutParams != null)
                    getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                break;
        }
        super.onMeasure(widthSpec, heightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public XRecyclerView(Context context) {
        super(context);
        init();
    }

    public XRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext(), new DecelerateInterpolator());
        mHeaderAndFooterWrapper = new HeaderAndFooterWrapper();
    }

    public void setEnablePull(boolean enablePull) {
        mEnablePull = enablePull;
    }

    public boolean isEnablePullRefresh() {
        return mEnablePullRefresh;
    }

    public void setEnablePullRefresh(boolean enablePullRefresh) {
        mEnablePullRefresh = enablePullRefresh;
        if (enablePullRefresh) {
            mPullDownRebound = false;
        } else {
            mPullDownRebound = true;
            if (mHeaderView != null)
                mHeaderView.setVisibility(View.INVISIBLE);
        }

    }

    public boolean isEnablePushLoad() {
        return mEnablePushLoad;
    }

    public void setEnablePushLoad(boolean enablePushLoad) {
        mEnablePushLoad = enablePushLoad;
        if (enablePushLoad)
            mPushUpRebound = false;
        else
            mPushUpRebound = true;
    }


    public HeaderAndFooterWrapper getHeaderAndFooterWrapper() {
        return mHeaderAndFooterWrapper;
    }

    public void setHeaderAndFooterWrapper(HeaderAndFooterWrapper headerAndFooterWrapper) {
        mHeaderAndFooterWrapper = headerAndFooterWrapper;
    }

    public final void notifyDataSetChanged() {
        mHeaderAndFooterWrapper.notifyDataSetChanged();
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        mLayoutManager = layout;
        if (layout instanceof GridLayoutManager) {
            mLayoutManagerType = ((GridLayoutManager) layout).getOrientation() == OrientationHelper.VERTICAL ? LINEARMANAGER_TYPE_GRID_VERTICAL : LINEARMANAGER_TYPE_GRID_HORIZONTAL;
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) mLayoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (mHeaderAndFooterWrapper.hasHeaderView(position)) {
                        return gridLayoutManager.getSpanCount();
                    } else if (mHeaderAndFooterWrapper.hasFooterView(position)) {
                        return gridLayoutManager.getSpanCount();
                    }
                    return 1;
                }
            });

        } else if (layout instanceof LinearLayoutManager) {
            mLayoutManagerType = ((LinearLayoutManager) layout).getOrientation() == OrientationHelper.VERTICAL ? LINEARMANAGER_TYPE_LINEAR_VERTICAL : LINEARMANAGER_TYPE_LINEAR_HORIZONTAL;
        } else if (layout instanceof StaggeredGridLayoutManager) {
            mLayoutManagerType = ((StaggeredGridLayoutManager) layout).getOrientation() == OrientationHelper.VERTICAL ? LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL : LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL;
        } else {
            mLayoutManagerType = LINEARMANAGER_TYPE_OTHER;
        }
        int widthMeasureSpec = 0;
        int heightMeasureSpec = 0;
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                mHeaderView = new XRecyclerViewLinearVerticalHeader(getContext());
                mHeaderAndFooterWrapper.addRefreshHeaderView(mHeaderView);
                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.EXACTLY);
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST);
                mHeaderView.measure(widthMeasureSpec, heightMeasureSpec);
                mHeaderHeight = mHeaderView.getMeasuredHeight();
                mHeaderView.getLayoutParams().height = 0;
                if (!mEnablePullRefresh)
                    mHeaderView.setVisibility(View.INVISIBLE);

                mFooterView = new XRecyclerViewLinearVerticalFooter(getContext());
                mHeaderAndFooterWrapper.addRefreshFooterView(mFooterView);
                mFooterView.measure(widthMeasureSpec, heightMeasureSpec);
                mFooterHeight = mFooterView.getMeasuredHeight();
                mFooterView.getLayoutParams().height = mFooterHeight;
                mFooterView.setLayoutParams(new MarginLayoutParams(mFooterView.getLayoutParams()));
                ((MarginLayoutParams) mFooterView.getLayoutParams()).bottomMargin = -mFooterHeight;
                mFooterView.setVisibility(View.GONE);
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:

                mHeaderView = new XRecyclerViewLinearVerticalHeader(getContext());
                mHeaderAndFooterWrapper.addRefreshHeaderView(mHeaderView);
                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST);
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.EXACTLY);
                mHeaderView.measure(widthMeasureSpec, heightMeasureSpec);
                mHeaderWidth = mHeaderView.getMeasuredWidth();
                mHeaderView.getLayoutParams().width = 0;
                if (!mEnablePullRefresh)
                    mHeaderView.setVisibility(View.INVISIBLE);

                mFooterView = new XRecyclerViewLinearVerticalFooter(getContext());
                mHeaderAndFooterWrapper.addRefreshFooterView(mFooterView);
                mFooterView.measure(widthMeasureSpec, heightMeasureSpec);
                mFooterWidth = mFooterView.getMeasuredWidth();
                mFooterView.getLayoutParams().width = mFooterWidth;
                mFooterView.setLayoutParams(new MarginLayoutParams(mFooterView.getLayoutParams()));
                ((MarginLayoutParams) mFooterView.getLayoutParams()).rightMargin = -mFooterWidth;
                mFooterView.setVisibility(View.GONE);
                break;
        }


    }

    @Override
    public void addItemDecoration(ItemDecoration decor) {
        if (decor instanceof DividerItemDecoration) {
            super.addItemDecoration(new ListDividerItemDecoration(0xFFE5E5E5, 1));
        } else {
            super.addItemDecoration(decor);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mHeaderAndFooterWrapper.setAdapter(adapter);
        super.setAdapter(mHeaderAndFooterWrapper);
    }

    public void setPageStatusBean(PageStatusBean pageStatusBean) {
        mPageStatusBean = pageStatusBean;
    }

    public void setTotal(int total) {
        this.mTotal = total;
    }

    public void setList(List<T> list) {
        if (list == null) {
            throw new NullPointerException("list is null");
        }
        mList = list;
    }

    private void autoRefresh(boolean b) {
        //预防多次调用
        if (b && (mAutoRefresh || mPullRefreshing))
            return;
        //已经获取到数据,不再重复获取
        if (b && !mList.isEmpty())
            return;
        scrollToPosition(0);
        mAutoRefresh = true;
        mPullDownState = true;
        mHeaderView.setState(XHeaderAndFooterState.STATE_REFRESHING);
        int startY = 0;
        int dy = 0;
        int dx = 0;
        int startX = 0;
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                startY = 0;
                dy = mHeaderHeight;
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                startX = 0;
                dy = mHeaderWidth;
                break;
        }
        mScroller.startScroll(startX, startY, dx, dy, 500);
        invalidate();

    }

    public void autoRefresh() {
        autoRefresh(true);
    }

    public void autoRefreshAlways() {
        autoRefresh(false);
    }

    public void addListAndStop(List<T> list) {
        if (list != null) {
            if (mPullState == PUSH_TYPE_REFRESH) {
                mList.clear();
                if (list.size() < 5) {
                    setEnablePushLoad(false);
                } else {
                    if (mTotal != -1) {
                        if (mTotal > list.size()) {
                            setEnablePushLoad(true);
                            mFooterView.setVisibility(View.VISIBLE);
                        } else {
                            setEnablePushLoad(false);
                            mFooterView.setVisibility(View.GONE);
                        }
                    }
                }
            }
            mList.addAll(list);
            mHeaderAndFooterWrapper.notifyDataSetChanged();
            if (mPageStatusBean != null)
                mPageStatusBean.setpId(mList.size());
        } else {
            if (mPullState == PUSH_TYPE_REFRESH) {
                mFooterView.setVisibility(View.GONE);
                mList.clear();
                mHeaderAndFooterWrapper.notifyDataSetChanged();
            }
        }
        stop();
    }

    /**
     * 获取RecyclerView的滑行距离
     *
     * @return
     */
    public int getScollYDistance() {
        int position = getFirstVisiblePosition();
        View firstVisiableChildView = mLayoutManager.findViewByPosition(position == 0 ? mHeaderAndFooterWrapper.getHeaderCount() : position);
        if (firstVisiableChildView == null)
            return -1;
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
                int itemHeight = firstVisiableChildView.getHeight();
                return (position <= 1 ? 0 : position - mHeaderAndFooterWrapper.getHeaderCount()) * itemHeight - firstVisiableChildView.getTop();
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
                break;
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
                itemHeight = firstVisiableChildView.getHeight();
                return (position <= 1 ? 0 : position - mHeaderAndFooterWrapper.getHeaderCount()) * itemHeight - firstVisiableChildView.getTop();
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
                break;
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                break;
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                break;
        }
        return -1;
    }

    public int addHeaderView(View header) {
        return mHeaderAndFooterWrapper.addHeaderView(header);
    }

    public int addFooterView(View footer) {
        return mHeaderAndFooterWrapper.addFooterView(footer);

    }

    public void removeHeader(View header) {
        mHeaderAndFooterWrapper.removeHeaderView(header);
    }

    public void removeHeader(int index) {
        mHeaderAndFooterWrapper.removeHeaderView(index);
    }

    public void removeFooter(View footer) {
        mHeaderAndFooterWrapper.removeFooterView(footer);
    }

    public void removeFooter(int index) {
        mHeaderAndFooterWrapper.removeFooterView(index);
    }

    private void setCallPullDownAndPushUp(CallPullDownAndPushUp callPullDownAndPushUp) {
        mCallPullDownAndPushUp = callPullDownAndPushUp;
    }

//兼容其他的一些View

    public void setScrollView(final ScrollView mScrollView) {
        setCallPullDownAndPushUp(new ScrollViewCallPullDownAndPushUp());
        setLayoutManager(new LinearLayoutManager(mScrollView.getContext()));
        setAdapter(new Adapter() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ViewHolder(mScrollView) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });
    }

    public void setWebView(final WebView mWebView) {
        setCallPullDownAndPushUp(new WebViewCallPullDownAndPushUp());
        setLayoutManager(new LinearLayoutManager(mWebView.getContext()));
        setAdapter(new Adapter() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ViewHolder(mWebView) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });

    }

    public void setView(final View mView) {
        setLayoutManager(new LinearLayoutManager(mView.getContext()));
        setAdapter(new Adapter() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ViewHolder(mView) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });

    }

    public void setView(View mView, CallPullDownAndPushUp mCallPullDownAndPushUp) {
        setCallPullDownAndPushUp(mCallPullDownAndPushUp);
        setView(mView);
    }

    public void stop() {
        if (mPullRefreshing) {
            mPullRefreshing = false;
            stopRefresh();
        } else if (mPushLoading) {
            mPushLoading = false;
            stopLoadMore();
        }

    }

    private void stopRefresh() {
        mHeaderView.setState(XHeaderAndFooterState.STATE_SUCCESS);
        mHeaderView.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetNormalHeaderHeight();
            }
        }, 500);
    }

    private void stopLoadMore() {
        mFooterView.setState(XHeaderAndFooterState.STATE_SUCCESS);
        mFooterView.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetNormalFooterHeight();
            }
        }, 200);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setIXRecyclerViewListener(IXRecyclerViewListener recyclerViewListener) {
        mRecyclerViewListener = recyclerViewListener;
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return super.startNestedScroll(axes);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        System.out.println("push:dispatchNestedPreScroll " + dx + " dy " + dy + "  " + Arrays.toString(consumed) + " " + Arrays.toString(offsetInWindow));
        if (mHeaderView.getLayoutParams().height > 0) {
            System.out.println("push:dispatchNestedPreScroll 拦截");
            return false;
        }
        System.out.println("push:dispatchNestedPreScroll 不拦截");
        return super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

//    @Override
//    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
//        System.out.println("push:dispatchNestedScroll "+dyConsumed+" dy "+dxUnconsumed+"  "+ Arrays.toString(offsetInWindow));
//        return false;
//    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (test)
            System.out.println("diapath:" + ((MarginLayoutParams) mFooterView.getLayoutParams()).bottomMargin);
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished())
                    mScroller.forceFinished(true);
                mLastY = e.getRawY();
                mLastX = e.getRawX();
                if (!mPullRefreshing) {
                    mHeaderView.setState(XHeaderAndFooterState.STATE_NORMAL);
                }
                if (!mPushLoading) {
                    mFooterView.setState(XHeaderAndFooterState.STATE_NORMAL);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mScroller.isFinished())
                    mScroller.forceFinished(true);
                float dy = e.getRawY() - mLastY;
                float dx = e.getRawX() - mLastX;
                mLastY = e.getRawY();
                mLastX = e.getRawX();
                if (mEnablePull && (mPullDownRebound || mEnablePullRefresh) && mCallPullDownAndPushUp.canPullDown(dy, dx)) {
                    if (test) System.out.println("push:下拉~~~~~~~~~");
                    scrollToPosition(0);
                    updateHeaderHeight(dy, dx);
                    mPullDownState = true;
                    mPushUpState = false;
                } else if ((mPushUpRebound || mEnablePushLoad) && mCallPullDownAndPushUp.callPushUp(dy, dx)) {
                    if (test) System.out.println("push:上拉~~~~~~~~~");
                    updateFooterHeight(dy, dx);
                    mPullDownState = false;
                    mPushUpState = true;
                }
                if (test) System.out.println("push:" + dy);
                break;
            default:
                if (test)
                    System.out.println("push: top:" + ((MarginLayoutParams) mHeaderView.getLayoutParams()).height + " mPullDownRebound:" + mPullDownRebound + " mPushUpRebound:" + mPushUpRebound);
                if (mPullDownState) {
                    callbackRefreshListener();
                    resetFooterHeight();
                }
                if (mPushUpState) {
                    callbackLoadMoreListener();
                    resetHeaderHeight();
                }
                break;


        }

        return super.dispatchTouchEvent(e);
    }


//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent e) {
//        return true;
//    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            invalidate();
            if (test)
                System.out.println("push compute true:" + mScroller.getCurrY() + " mPullDownRebound:" + mPullDownRebound);
            if (mPullDownState) {
                switch (mLayoutManagerType) {
                    case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
                    case LINEARMANAGER_TYPE_GRID_VERTICAL:
                    case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                        mHeaderView.getLayoutParams().height = mScroller.getCurrY();
                        if (test) System.out.println("down " + mScroller.getCurrY());
                        if (mScroller.getCurrY() == mScroller.getFinalY()) {
                            if (test) System.out.println("停止 down " + mScroller.getCurrY());
                            if (mAutoRefresh) {
                                mAutoRefresh = false;
                                callbackRefreshListener();
                            }
                        }
                        break;
                    case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
                    case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
                    case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                        mHeaderView.getLayoutParams().width = mScroller.getCurrY();
                        if (test) System.out.println("down " + mScroller.getCurrY());
                        if (mScroller.getCurrY() == mScroller.getFinalY()) {
                            if (test) System.out.println("停止 down " + mScroller.getCurrY());
                            if (mAutoRefresh) {
                                mAutoRefresh = false;
                                callbackRefreshListener();
                            }
                        }
                        break;
                }
                mHeaderView.requestLayout();
            }
            if (mPushUpState) {
                switch (mLayoutManagerType) {
                    case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
                    case LINEARMANAGER_TYPE_GRID_VERTICAL:
                    case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                        ((MarginLayoutParams) mFooterView.getLayoutParams()).bottomMargin = mScroller.getCurrY();
                        if (test) System.out.println("getCurrY():" + mScroller.getCurrY());
                        if (mScroller.getCurrY() == mScroller.getFinalY()) {
                        }
                        break;
                    case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
                    case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
                    case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                        break;
                }
                mFooterView.requestLayout();
            }
        }
    }

    private void callbackRefreshListener() {
        boolean isRefreshing = false;
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                if (mEnablePullRefresh && mHeaderView.getLayoutParams().height >= mHeaderHeight) {
                    isRefreshing = true;
                }
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                if (mEnablePullRefresh && mHeaderView.getLayoutParams().width >= mHeaderHeight) {
                    isRefreshing = true;
                }
                break;
        }
        if (isRefreshing) {
            mPullRefreshing = true;
            if (mPageStatusBean != null)
                mPageStatusBean.setpId(0);
            if (mRecyclerViewListener != null)
                mRecyclerViewListener.onRefresh();
            mPullState = PUSH_TYPE_REFRESH;
            mHeaderView.setState(XHeaderAndFooterState.STATE_REFRESHING);
            resetHeaderHeightAnimation();
        } else {
            resetNormalHeaderHeight();
        }

    }

    private void callbackLoadMoreListener() {
        boolean isLoading = false;
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                if (mEnablePushLoad && ((MarginLayoutParams) mFooterView.getLayoutParams()).bottomMargin >= 0) {
                    isLoading = true;
                }
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                break;
        }
        if (isLoading) {
            mPushLoading = true;
            if (mRecyclerViewListener != null)
                mRecyclerViewListener.onLoadMore();
            mPullState = PUSH_TYPE_LOAD;
            mFooterView.setState(XHeaderAndFooterState.STATE_REFRESHING);
            resetFooterHeightAnimation();
        } else {
            resetNormalFooterHeight();
        }

    }


    private void updateHeaderHeight(float dy, float dx) {
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                mHeaderView.getLayoutParams().height += dy / OFFSET_RADIO;
                if (mHeaderView.getLayoutParams().height < 0)
                    mHeaderView.getLayoutParams().height = 0;
                if (mHeaderView.getLayoutParams().height >= mHeaderHeight) {
                    mHeaderView.setState(XHeaderAndFooterState.STATE_READY);
                }
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                mHeaderView.getLayoutParams().width += dx / OFFSET_RADIO;
                if (mHeaderView.getLayoutParams().width >= mHeaderWidth) {
                    mHeaderView.setState(XHeaderAndFooterState.STATE_READY);
                }
                break;
        }
        mHeaderView.requestLayout();
    }

    private void updateFooterHeight(float dy, float dx) {
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                ((MarginLayoutParams) mFooterView.getLayoutParams()).bottomMargin -= dy / OFFSET_RADIO;
                if (((MarginLayoutParams) mFooterView.getLayoutParams()).bottomMargin >= 0) {
                    mFooterView.setState(XHeaderAndFooterState.STATE_READY);
                }
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                break;
        }
    }

    private void resetNormalHeaderHeight() {
        int startY = 0;
        int dy = 0;
        int dx = 0;
        int startX = 0;
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                startY = mHeaderView.getLayoutParams().height;
                dy = -startY;
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                startY = mHeaderView.getLayoutParams().width;
                dy = -startY;
                break;
        }
        if (dx == 0 && dy == 0)
            return;
        mScroller.startScroll(startX, startY, dx, dy, 500);
        invalidate();

    }

    private void resetHeaderHeightAnimation() {
        int startY = 0;
        int dy = 0;
        int dx = 0;
        int startX = 0;
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                startY = mHeaderView.getLayoutParams().height;
                dy = mHeaderHeight - startY;
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                startY = mHeaderView.getLayoutParams().width;
                dy = mHeaderWidth - startY;
                break;
        }
        if (dx == 0 && dy == 0)
            return;
        mScroller.startScroll(startX, startY, dx, dy, 500);
        invalidate();

    }

    private void resetHeaderHeight() {
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                mHeaderView.getLayoutParams().height = 0;
                mHeaderView.requestLayout();
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                mHeaderView.getLayoutParams().width = 0;
                mHeaderView.requestLayout();
                break;
        }
    }

    private void resetNormalFooterHeight() {
        int startY = 0;
        int dy = 0;
        int dx = 0;
        int startX = 0;
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                startY = ((MarginLayoutParams) mFooterView.getLayoutParams()).bottomMargin;
                dy = -startY - mFooterHeight;
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                break;
        }
        if (dx == 0 && dy == 0)
            return;
        mScroller.startScroll(startX, startY, dx, dy, 500);
        invalidate();
    }

    private void resetFooterHeightAnimation() {
        int startY = 0;
        int dy = 0;
        int dx = 0;
        int startX = 0;
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                startY = ((MarginLayoutParams) mFooterView.getLayoutParams()).bottomMargin;
                dy = mFooterHeight - startY - mFooterHeight;
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                break;
        }
        if (dx == 0 && dy == 0)
            return;
        mScroller.startScroll(startX, startY, dx, dy, 500);
        invalidate();


    }

    private void resetFooterHeight() {
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                ((MarginLayoutParams) mFooterView.getLayoutParams()).bottomMargin = -mFooterHeight;
                mFooterView.requestLayout();
                break;
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                break;
        }
    }

    public int getFirstVisiblePosition() {
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
                return ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
                return ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) mLayoutManager;
                int[] into = staggeredGridLayoutManager.findFirstVisibleItemPositions(null);
                int firstVisiblePosition = into[0];
                for (int i = 1; i < into.length; i++) {
                    if (firstVisiblePosition < into[i]) {
                        firstVisiblePosition = into[i];
                    }
                }
                return firstVisiblePosition;
        }
        return 0;
    }

    public int getLastVisiblePosition() {
        switch (mLayoutManagerType) {
            case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
            case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
                return ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            case LINEARMANAGER_TYPE_GRID_VERTICAL:
            case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
                return ((GridLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
            case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) mLayoutManager;
                int[] into = staggeredGridLayoutManager.findLastVisibleItemPositions(null);
                int firstVisiblePosition = into[0];
                for (int i = 1; i < into.length; i++) {
                    if (firstVisiblePosition > into[i]) {
                        firstVisiblePosition = into[i];
                    }
                }
                return firstVisiblePosition;
        }
        return 0;
    }

    public interface OnItemClickListener<T> {
        void onItemClick(android.support.v7.widget.RecyclerView recyclerView, View view, T info,
            int realPosition);
    }

    public interface IXRecyclerViewListener {
        void onRefresh();

        void onLoadMore();
    }

    public interface CallPullDownAndPushUp {
        boolean canPullDown(float dy, float dx);

        boolean callPushUp(float dy, float dx);
    }

    private class RecyclerViewCommonCallPullDownAndPushUp implements CallPullDownAndPushUp {

        @Override
        public boolean canPullDown(float dy, float dx) {
            switch (mLayoutManagerType) {
                case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
                case LINEARMANAGER_TYPE_GRID_VERTICAL:
                case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                    if (test)
                        System.out.println("push callPull:" + (mHeaderView.getLayoutParams().height > 0) + "  " + (dy > 0 && mHeaderAndFooterWrapper.getRealCount() == 0
                        ) + "" + (dy > 0 && getFirstVisiblePosition() <= mHeaderAndFooterWrapper.getHeaderCount() && getChildAt(0).getTop() >= 0));
                    return (dy < 0 && mHeaderView.getLayoutParams().height > 0)
                            ||
                            (dy > 0 && mHeaderAndFooterWrapper.getRealCount() == 0)
                            ||
                            (dy > 0 && getFirstVisiblePosition() <= mHeaderAndFooterWrapper.getHeaderCount() && getChildAt(0).getTop() >= 0);
                case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
                case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
                case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
                    return dx > 0 && mHeaderAndFooterWrapper.getRealCount() == 0 || dx > 0 && getFirstVisiblePosition() <= mHeaderAndFooterWrapper.getHeaderCount() && getChildAt(0).getLeft() >= 0;
            }

            return false;

        }

        @Override
        public boolean callPushUp(float dy, float dx) {
            switch (mLayoutManagerType) {
                case LINEARMANAGER_TYPE_LINEAR_VERTICAL:
                case LINEARMANAGER_TYPE_GRID_VERTICAL:
                case LINEARMANAGER_TYPE_STAGGEREDGRID_VERTICAL:
                    if (test)
                        System.out.println("updateFooterHeight dy:" + dy + " last:" + getLastVisiblePosition() + " count:" + mHeaderAndFooterWrapper.getItemCount() + " realcount:" + mHeaderAndFooterWrapper.getRealCount() + " bottom:" + getChildAt(getChildCount() - 1).getBottom() + " height:" + getMeasuredHeight());
                    return dy < 0 && getLastVisiblePosition() >= mHeaderAndFooterWrapper.getRealCount() + mHeaderAndFooterWrapper.getHeaderCount() - 1 && getChildAt(getChildCount() - 1).getBottom() <= getMeasuredHeight() + mFooterHeight;
                case LINEARMANAGER_TYPE_LINEAR_HORIZONTAL:
                case LINEARMANAGER_TYPE_GRID_HORIZONTAL:
                case LINEARMANAGER_TYPE_STAGGEREDGRID_HORIZONTAL:
            }
            return false;

        }
    }

    private class ScrollViewCallPullDownAndPushUp implements CallPullDownAndPushUp {

        @Override
        public boolean canPullDown(float dy, float dx) {
            return false;
        }

        @Override
        public boolean callPushUp(float dy, float dx) {
            return false;
        }
    }

    private class WebViewCallPullDownAndPushUp implements CallPullDownAndPushUp {

        @Override
        public boolean canPullDown(float dy, float dx) {
            return false;
        }

        @Override
        public boolean callPushUp(float dy, float dx) {
            return false;
        }
    }

    public abstract class XHeaderAndFooterState extends FrameLayout {
        public static final int STATE_NORMAL = 0;
        public static final int STATE_READY = 1;
        public static final int STATE_REFRESHING = 2;
        public static final int STATE_SUCCESS = 3;

        public XHeaderAndFooterState(Context context) {
            super(context);
        }

        abstract void setState(int state);
    }

    public class XRecyclerViewLinearVerticalHeader extends XHeaderAndFooterState {

        private ImageView mImageView;
        private ProgressBar mProgressBar;
        private FitView mFitView;
        private TextView mTextView;
        private TextView mTvContent;
        private int mState;
        private Animation mRotateUpAnim;
        private Animation mRotateDownAnim;

        public XRecyclerViewLinearVerticalHeader(Context context) {
            super(context);
            View.inflate(context, R.layout.xrecyclerview_header, this);
            mImageView = (ImageView) findViewById(R.id.xrecyclerview_header_arrow);
            mProgressBar = (ProgressBar) findViewById(R.id.xrecyclerview_header_pro);
            mFitView = (FitView) findViewById(R.id.xrecyclerview_header_fit);
            mTextView = (TextView) findViewById(R.id.xrecyclerview_header_tv);
            mTvContent = (TextView) findViewById(R.id.xrecyclerview_header_content);
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            setBackgroundColor(0xFFF5F5F5);

            mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            mRotateUpAnim.setDuration(500);
            mRotateUpAnim.setFillAfter(true);
            mRotateDownAnim = new RotateAnimation(-180.0f, -360.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            mRotateDownAnim.setDuration(500);
            mRotateDownAnim.setFillAfter(true);
        }

        @Override
        public void setState(int state) {
            if (mState == state) {
                return;
            }
            if (test) System.out.println("state:" + state);
            if (state == STATE_REFRESHING) {
                mImageView.setVisibility(View.INVISIBLE);
                mImageView.clearAnimation();
                mProgressBar.setVisibility(View.VISIBLE);
                mFitView.setVisibility(INVISIBLE);
                mTvContent.setVisibility(View.INVISIBLE);
            } else if (state == STATE_SUCCESS) {
                mImageView.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mFitView.setVisibility(View.VISIBLE);
                mFitView.startAnimation(500);
                mTvContent.setVisibility(View.VISIBLE);
            } else {
                mImageView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                mFitView.setVisibility(INVISIBLE);
                mTvContent.setVisibility(View.INVISIBLE);
            }
            switch (state) {
                case STATE_NORMAL:
                    mTextView.setText("继续下拉请求新数据");
                    if (mState == STATE_READY) {
                        mImageView.startAnimation(mRotateDownAnim);
                    }
                    break;
                case STATE_READY:
                    mTextView.setText("松开刷新数据");
                    mImageView.startAnimation(mRotateUpAnim);
                    break;
                case STATE_REFRESHING:
                    mTextView.setText("正在加载...");
                    break;
                case STATE_SUCCESS:
                    mTextView.setText("刷新成功");
                    mTvContent.setText("本次刷新了" + mList.size() + "条新数据");
                    break;
            }
            mState = state;
        }

    }

    public class XRecyclerViewLinearVerticalFooter extends XHeaderAndFooterState {

        private TextView mTextView;
        private ProgressBar mProgressBar;
        private int mState;

        public XRecyclerViewLinearVerticalFooter(Context context) {
            super(context);
            View.inflate(context, R.layout.xrecyclerview_footer, this);
            mTextView = (TextView) findViewById(R.id.xrecyclerview_footer_tv);
            mProgressBar = (ProgressBar) findViewById(R.id.xrecyclerview_footer_pro);
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            setBackgroundColor(0xFFF5F5F5);
        }

        @Override
        public void setState(int state) {
            if (mState == state) {
                return;
            }
            if (test) System.out.println("state:" + state);
            if (state == STATE_REFRESHING) {
                mProgressBar.setVisibility(View.VISIBLE);
            } else if (state == STATE_SUCCESS) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                mProgressBar.setVisibility(View.GONE);
            }
            switch (state) {
                case STATE_NORMAL:
                    mTextView.setText("继续上拉加载更多数据");
                    break;
                case STATE_READY:
                    mTextView.setText("松开刷新数据");
                    break;
                case STATE_REFRESHING:
                    mTextView.setText("正在加载...");
                    break;
                case STATE_SUCCESS:
                    mTextView.setText("加载成功");
                    break;
            }
            mState = state;
        }

    }


    public class ListDividerItemDecoration extends RecyclerView.ItemDecoration {
        private final int DIRECTION_VERTICAL = LINEARMANAGER_TYPE_LINEAR_VERTICAL;
        private final int DIRECTION_HORIZONTAL = LINEARMANAGER_TYPE_LINEAR_HORIZONTAL;
        private Drawable divider;
        private int direction;

        public ListDividerItemDecoration(Drawable divider) {
            super();
            this.divider = divider;
        }

        public ListDividerItemDecoration(int color, int height) {
            super();
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(color);
            gd.setSize(0, height);
            this.divider = gd;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            if (divider == null)
                return;
            if (mLayoutManagerType == DIRECTION_VERTICAL) {
                drawVerticalDivider(c, parent);
            } else {
                drawHoriziontalDivider(c, parent);
            }
        }

        private void drawHoriziontalDivider(Canvas c, RecyclerView parent) {
            int left = 0;
            int top = 0;
            int right = 0;
            int bottom = 0;
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            for (int i = mHeaderAndFooterWrapper.getHeaderCount(); i < parent.getChildCount() - mHeaderAndFooterWrapper.getFooterCount(); i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                left = child.getRight() + layoutParams.rightMargin;
                right = left + divider.getIntrinsicWidth();
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }

        }


        private void drawVerticalDivider(Canvas c, RecyclerView parent) {
            int left = 0;
            int top = 0;
            int right = 0;
            int bottom = 0;
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (test)
//                    System.out.println("drawVerticalDivider:" + i + " child:" + child.getClass().getSimpleName());
                    if (mHeaderAndFooterWrapper.hasHeaderView(child)) {
//                    if (test) System.out.println("drawVerticalDivider:" + "头部过滤");
                        continue;
                    }
                if (mHeaderAndFooterWrapper.hasFooterView(child)) {
//                    if (test) System.out.println("drawVerticalDivider:" + "尾部过滤");
                    continue;
                }
                if (i - 1 >= 0 && mHeaderAndFooterWrapper.hasHeaderView(parent.getChildAt(i - 1))) {
//                    if (test) System.out.println("drawVerticalDivider:" + "第一个Item过滤");
                    continue;
                }
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                bottom = child.getTop() + layoutParams.topMargin;
                top = bottom - divider.getIntrinsicHeight();
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (test)
                System.out.println("divider:" + divider.getIntrinsicHeight() + "  " + divider.getIntrinsicWidth());
            int position = parent.getChildAdapterPosition(view);
            int p1 = parent.getChildLayoutPosition(view);
            if (test) System.out.println("position:" + position + " p1:" + p1);
            if (mHeaderAndFooterWrapper.hasHeaderView(position))
                return;
            else if (mHeaderAndFooterWrapper.hasFooterView(position))
                return;
            position -= mHeaderAndFooterWrapper.getHeaderCount();
            if (direction == DIRECTION_VERTICAL) {
                if (position == 0) {
                    outRect.set(0, 0, 0, 0);
                } else {
                    outRect.set(0, divider.getIntrinsicHeight(), 0, 0);
                }
            } else {
                if (position == 0) {
                    outRect.set(0, 0, 0, 0);
                } else {
                    outRect.set(divider.getIntrinsicWidth(), 0, 0, 0);
                }
            }
        }
    }


    private class HeaderAndFooterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Adapter mAdapter;
        private SparseArrayCompat<View> mArrayHeader;
        private SparseArrayCompat<View> mArrayFooter;
        private final int ITEM_VIEW_TYPE_HEADER = 10000;
        private final int ITEM_VIEW_TYPE_FOOTER = 20000;

        public HeaderAndFooterWrapper() {
            super();
            mArrayHeader = new SparseArrayCompat<View>();
            mArrayFooter = new SparseArrayCompat<View>();
        }

        public void setAdapter(RecyclerView.Adapter adapter) {
            mAdapter = adapter;
        }

        private boolean hasHeaderView(int position) {
            return position < getHeaderCount();
        }

        private boolean hasFooterView(int position) {
            return position >= getHeaderCount() + getRealCount();
        }

        private boolean hasHeaderView(View view) {
            return mArrayHeader.indexOfValue(view) != -1;
        }

        private boolean hasFooterView(View view) {
            return mArrayFooter.indexOfValue(view) != -1;
        }

        private int getRealCount() {
            return mAdapter.getItemCount();
        }

        private int getHeaderCount() {
            return mArrayHeader.size();
        }

        private int getFooterCount() {
            return mArrayFooter.size();
        }

        private void addRefreshHeaderView(View header) {
            addHeaderView(ITEM_VIEW_TYPE_HEADER, header);
        }

        private int addHeaderView(View header) {
            int index = this.mArrayHeader.size() + ITEM_VIEW_TYPE_HEADER;
            addHeaderView(index, header);
            return index;
        }

        private void addHeaderView(int key, View header) {
            this.mArrayHeader.put(key, header);
        }

        private void removeHeaderView(View header) {
            this.mArrayHeader.remove(this.mArrayHeader.indexOfValue(header));
        }

        private void removeHeaderView(int index) {
            this.mArrayHeader.remove(index);
        }

        private void addRefreshFooterView(View footer) {
            addFooterView(ITEM_VIEW_TYPE_FOOTER, footer);
        }

        private int addFooterView(View footer) {
            int index = this.mArrayFooter.size() + (int) (ITEM_VIEW_TYPE_FOOTER * 0.8f);
            addFooterView(index, footer);
            return index;
        }

        private void addFooterView(int key, View footer) {
            this.mArrayFooter.put(key, footer);
        }

        private void removeFooterView(View footer) {
            this.mArrayFooter.remove(this.mArrayFooter.indexOfValue(footer));
        }

        private void removeFooterView(int index) {
            this.mArrayFooter.remove(index);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (mArrayHeader.get(viewType) != null) {
                View header = mArrayHeader.get(viewType);
                if (header.getLayoutParams() == null) {
                    header.setLayoutParams(parent.getLayoutParams());
                }
                ViewHolder holder = new XViewHolder(header);
                return holder;
            } else if (mArrayFooter.get(viewType) != null) {
                View footer = mArrayFooter.get(viewType);
                if (footer.getLayoutParams() == null) {
                    footer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                ViewHolder holder = new XViewHolder(footer);
                return holder;
            }
            final ViewHolder holder = mAdapter.onCreateViewHolder(parent, viewType);
            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(XRecyclerView.this, v, mList.get(holder.getLayoutPosition() - getHeaderCount()), holder.getLayoutPosition() - getHeaderCount());
                    }
                });
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (hasHeaderView(position)) {
                return;
            } else if (hasFooterView(position)) {
                return;
            }
            mAdapter.onBindViewHolder(holder, position - getHeaderCount());
        }

        @Override
        public int getItemCount() {
            return getHeaderCount() + getRealCount() + getFooterCount();
        }

        @Override
        public int getItemViewType(int position) {
            if (hasHeaderView(position)) {
                return mArrayHeader.keyAt(position);
            } else if (hasFooterView(position)) {
                return mArrayFooter.keyAt(position - getHeaderCount() - getRealCount());
            }
            return mAdapter.getItemViewType(position - getHeaderCount());
        }

        private class XViewHolder extends ViewHolder {

            public XViewHolder(View itemView) {
                super(itemView);
            }
        }

    }
}
