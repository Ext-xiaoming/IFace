package dc.iface.object;

public class Course extends Object {
    private String teacherId;
    private String courseName;
    private String courseId;

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseId() {
        return courseId;
    }

}

