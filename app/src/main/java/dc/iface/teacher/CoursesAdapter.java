package dc.iface.teacher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;



import java.util.List;

import dc.iface.R;
import dc.iface.object.CourseListItem;

public class CoursesAdapter extends ArrayAdapter<CourseListItem> {
    private List<CourseListItem> CoursesData;
    private Context mContext;
    private int resourceId;

    public CoursesAdapter(Context context, int resourceId, List<CourseListItem> data){
        super(context,resourceId,data);
        this.mContext = context;
        this.CoursesData = data;
        this.resourceId = resourceId;
    }

    @Override
    public View getView(int position , View convertView , ViewGroup parent) {
        CourseListItem coursePoint = getItem(position);
        View view = LayoutInflater.from( getContext() ). inflate(resourceId , parent , false );

        TextView className = view.findViewById( R.id. className );
        TextView classCode = view.findViewById(R.id.classCode );
        TextView teacherNameItem = view.findViewById(R.id.teacherNameItem);

        className.setText( "课程名:" +coursePoint.getCourseName());
        classCode.setText( "加课码:" + coursePoint.getCourseId());
        teacherNameItem.setText( "老师名:" +coursePoint.getTeacherName() );
        return view;
    }
}
