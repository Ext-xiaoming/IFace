package dc.iface.teacher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dc.iface.R;
import dc.iface.object.CourseListItem;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.ViewHolder> {
    private List<CourseListItem> CoursesData;
    private Context mContext;
    private int resourceId;

    //定义点击事件的接口
    private OnitemClick onitemClick;   //定义点击事件接口
    private OnLongClick onLongClick;  //定义长按事件接口

    //定义设置点击事件监听的方法
    public void setOnItemClickListener (OnitemClick onitemClick) {
        this.onitemClick = onitemClick;
    }
    //定义设置长按事件监听的方法
    public void setOnLongClickListener (OnLongClick onLongClick) {
        this.onLongClick = onLongClick;
    }

    //定义一个点击事件的接口
    public interface OnitemClick {
        void onItemClick(int position);
    }
    //定义一个长按事件的接口
    public interface OnLongClick {
        void onLongClick(int position);
    }


    public CoursesAdapter(Context context, int resourceId, List<CourseListItem> data){

        this.mContext = context;
        this.CoursesData = data;
        this.resourceId = resourceId;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView className;
        TextView classCode;
        TextView teacherNameItem;
        View view;
        public ViewHolder(@NonNull View itemView) {
            super( itemView );
            view=itemView;
            className = itemView.findViewById( R.id. className );
            classCode = itemView.findViewById(R.id.classCode );
            teacherNameItem = itemView.findViewById(R.id.teacherNameItem);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  view = LayoutInflater.from(mContext).inflate(resourceId , parent , false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public int getPosition(View view){
        final ViewHolder holder = new ViewHolder( view );
        int position = holder.getAdapterPosition();
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        CourseListItem coursePoint = CoursesData.get(position);
        holder.className.setText( "课程名:" +coursePoint.getCourseName());
        holder.classCode.setText( "加课码:" + coursePoint.getCourseId());
        holder.teacherNameItem.setText( "老师名:" +coursePoint.getTeacherName() );
        if (onitemClick != null) {
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //在TextView的地方进行监听点击事件，并且实现接口
                    onitemClick.onItemClick(position);
                }
            });
        }

        if (onLongClick != null) {
            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //在TextView的地方进行长按事件的监听，并实现长按接口
                    onLongClick.onLongClick(position);
                    return true;
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return CoursesData.size();
    }

}
