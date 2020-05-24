package dc.iface.BaseActivity;

public  final class StatisClass {

    private static boolean Cancel_automatic_login = false;

    public static boolean isCancel_automatic_login() {
        return Cancel_automatic_login;
    }

    public static void setCancel_automatic_login(boolean data) {
        Cancel_automatic_login=data;
    }



}
