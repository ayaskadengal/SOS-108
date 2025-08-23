package util;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class userAPI extends Application {
    private String userName;
    private String userId;
    private String name;

    private static userAPI instance;

    public static userAPI getInstance(){
        if(instance==null)
            instance=new userAPI();
        return instance;

    }

    public userAPI() {
    }



    public String getUserName() {
        return userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
