package dc.iface.student;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.object.CourseListItem;
import dc.iface.teacher.CoursesAdapter;


public class StuMainFragmentClass extends Fragment {
    private static StuMainFragmentClass mf;
    private static String TAG = "StuMainFragmentClass";
    private List<CourseListItem> listItemCourses = new ArrayList<>();//ListItem课程集
    private String studentId;
    private  CoursesAdapter coursesAdapter;
    //单例模式
    public static StuMainFragmentClass getMainFragment(String studentid){
        return  new StuMainFragmentClass(studentid);
    }

    public StuMainFragmentClass(String studentId){
        this.studentId= studentId;
        System.out.printf("初始studentId= " +studentId );
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG,"进入onCreateView :");
        final View view = inflater.inflate( R.layout.main_fragment_class ,container, false);//fragment_message为底部栏的界面
        final ListView lvListView = view.findViewById(R.id.course_list);
        System.out.printf("234324studentId= " +studentId );
        Log.i(TAG,"123."+"进入:");
        //从 student_course表中查找并以列表的形式显示出来

        //教师名称 + 课程名称 + 加课码
        //从course中找到 教师名称 + 课程名称 ，从student_course中找到 加课码
        //*************************************************************************************************************


        new Thread( new Runnable() {
            @Override
            public void run() {
                System.out.printf("studentId= " +studentId );
                DBUtils dbUtils= new DBUtils();
                String sql = "select  student_course.course_id,course_name,teacher_id  from  " +
                        "student_course, course  where student_course.course_id=course.course_id and  student_id ="+studentId;
                System.out.printf( sql );
                ResultSet resultSet = dbUtils.excuteSQL( sql );
                try{

                    while(resultSet.next()){
                        CourseListItem item = new CourseListItem();
                        item.setCourseId( resultSet.getString("course_id") );
                        item.setCourseName(resultSet.getString("course_name"));
                        item.setTeacherName(QueryTeacherName(resultSet.getString("teacher_id")));
                        listItemCourses.add(item);
                    }

                    coursesAdapter = new CoursesAdapter(getActivity() ,
                            R.layout.course_item , listItemCourses);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println( "list.size="+listItemCourses.size()+"--00000000000000\n");
                            for (int i = 0; i < listItemCourses.size(); i++) {
                                CourseListItem s = (CourseListItem)listItemCourses.get(i);
                                System.out.println(i+"输出："+s.getCourseId()+"  "+s.getCourseName()+"  "+s.getTeacherName()+"\n");
                            }
                            lvListView.setAdapter(coursesAdapter);
                        }
                    });

                    resultSet.getStatement().getConnection().close();
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.printf( e.getMessage() );
                }
            }
        } ).start();



        /////////////////////////////////////////////////////////////////////////////////////////////

        //*************************************************************************************************************

        lvListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                //点击课程后进入考勤界面
                Activity activity = getActivity();
                Intent intent = new Intent( activity , KaoqinActivity.class);//考勤界面
                intent.putExtra("courseId", listItemCourses.get(position).getCourseId());
                intent.putExtra("studentId", studentId );
                startActivity(intent);
            }
        });

        return view;
    }

    public String QueryTeacherName(String teacher_id){

            String TeacherName="";

            DBUtils dbUtils= new DBUtils();
            String sql = "select teacher_name from teacher where teacher_id ="+teacher_id;
            System.out.printf( sql );
            ResultSet resultSet = dbUtils.excuteSQL( sql );

            try{

                if( resultSet.next()) {
                    TeacherName=resultSet.getString( "teacher_name" );
                    resultSet.getStatement().getConnection().close();
                }
                else{
                    //TeacherName=0;
                }
            }catch (Exception e){
                e.printStackTrace();
                System.out.printf( e.getMessage() );
            }
            return TeacherName;
    }








}
