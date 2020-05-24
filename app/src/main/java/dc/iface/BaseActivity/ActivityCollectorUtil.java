package dc.iface.BaseActivity;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Iterator;

public class ActivityCollectorUtil {

    public static ArrayList<Activity> activities = new ArrayList<Activity>();

    /**
     * onCreate()时添加
     * @param activity
     */
    public static void addActivity(Activity activity){
        //判断集合中是否已经添加，添加过的则不再添加
        if (!activities.contains(activity)){
            activities.add(activity);
        }
    }

    /**
     * onDestroy()时删除
     * @param activity
     */
    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }

    /**
     * 关闭所有Activity
     */
    public static void finishAllActivity(){
        for (Activity activity : activities){
            if (!activity.isFinishing()){
                activity.finish();
            }
        }
    }

    /**
     * 关闭指定类名的Activity
     */
    public static void finishActivity(Class<?> cls) {
        if (activities != null) {
            // 使用迭代器安全删除
            for (Iterator<Activity> it = activities.iterator(); it.hasNext(); ) {
                Activity activity = it.next();
                // 清理掉已经释放的activity
                if (activity == null) {
                    it.remove();
                    continue;
                }
                if (activity.getClass().equals(cls)) {
                    it.remove();
                    activity.finish();
                }
            }
        }
    }

}
