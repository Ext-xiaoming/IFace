package dc.iface.Server;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 自定义自动下拉刷新SwipeRefreshLayout
 * Created by 邹峰立 on 2017/5/10.
 */
public class AutoSwipeRefreshLayout extends SwipeRefreshLayout {
    public AutoSwipeRefreshLayout(Context context) {
        super(context);
    }

    public AutoSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 自动刷新
     */
    public void autoRefresh() {
        try {
            Field mCircleView = SwipeRefreshLayout.class.getDeclaredField("mCircleView");
            mCircleView.setAccessible(true);
            View progress = (View) mCircleView.get(this);
            progress.setVisibility(VISIBLE);

            Method setRefreshing = SwipeRefreshLayout.class.getDeclaredMethod("setRefreshing", boolean.class, boolean.class);
            setRefreshing.setAccessible(true);
            setRefreshing.invoke(this, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}