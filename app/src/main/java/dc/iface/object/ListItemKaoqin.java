package dc.iface.object;

public class ListItemKaoqin {
    private String time;
    private String qiandaoNumber;//签到的人数
    private String checkNumber;//第几次签到
    private String postId;


    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getQiandaoNumber() {
        return qiandaoNumber;
    }

    public void setQiandaoNumber(String qiandaoNumber) {
        this.qiandaoNumber = qiandaoNumber;
    }
}
