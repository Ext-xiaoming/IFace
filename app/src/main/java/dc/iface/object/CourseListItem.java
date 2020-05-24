package dc.iface.object;

public class CourseListItem {
    private String teacherName;
    private String courseName;
    private String  courseId;

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseId() {
        return courseId;
    }
}
