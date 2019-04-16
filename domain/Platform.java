package domain;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;

public abstract class Platform {
    private String _id;
    private String _name;
    private String _statusUrl;
    private String _statusApiUrl;

    public Platform(String id, String name, String statusUrl, String statusApiUrl){
        setId(id);
        setName(name);
        setStatusUrl(statusUrl);
        setStatusApiUrl(statusApiUrl);
    }


    public boolean isAccessable(String url, int timeout) {
        //url = url.replaceFirst("https", "http");
    
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return false;
            }
        } catch (IOException exception) {
            return false;
        }
        return true;
    }

    public String getId(){
        return _id;
    }

    public String getName(){
        return _name;
    }

    public String getStatusUrl(){
        return _statusUrl;
    }

    public String getStatusApiUrl(){
        return _statusApiUrl;
    }

    public void setId(String id){
        _id = id;
    }

    public void setName(String name){
        _name = name;
    }

    public void setStatusUrl(String statusUrl){
        _statusUrl = statusUrl;
    }

    public void setStatusApiUrl(String statusApiUrl){
        _statusApiUrl = statusApiUrl;
    }

}
