package domain;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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


    public boolean isAccessable(int timeout) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(_statusUrl).openConnection();
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
    
    
    public String getStatus() throws MalformedURLException,ProtocolException,IOException{
        StringBuilder result = new StringBuilder();
        try {
        URL url = new URL(_statusApiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
           result.append(line);
        }
        rd.close();
        }
        
        catch(MalformedURLException e) {
        	throw new MalformedURLException("The url is malformed");
        }
        
        catch(ProtocolException e) {
        	throw new ProtocolException("There was an error with the protocol");
        }
        
        catch(IOException e) {
        	throw new IOException("There was an io error");
        }
  
        
        return result.toString();
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
