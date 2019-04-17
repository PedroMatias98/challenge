package domain;

import java.util.Scanner;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.Iterator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class App {
    private static List<Platform> platforms = new ArrayList<>();

    public static void helpCommand(){
      System.out.println("\nCommand:");
      System.out.println("\tpoll - Retrieves the status from of all configured services");
      System.out.println("\tfetch - Retrieves the status from of all configured services");
      System.out.println("\tservices - Lists all known services");
      System.out.println("\tbackup - backups the current internal state to a file");
      System.out.println("\trestore - Imports the internal state from another run or app");
      System.out.println("\thistory - Outputs all the data from the local storage");
      System.out.println("\tstatus - Summarizes data and displays it in a table-like fashion");
      System.out.println("\thelp - This screen");
    }

    @SuppressWarnings("unchecked")
    public static void readJsonFileWithProperties(){
      JSONParser parser = new JSONParser();
      Object obj = null;
      try{
        obj = parser.parse(new FileReader("storedData/storedData.json"));
      } catch(FileNotFoundException e){
        System.out.println("inexistent file");
      }
        catch(ParseException e){
          System.out.println("parse error");
      }
        catch(IOException e){
          System.out.println("io error");
      }
        JSONObject jsonObject = (JSONObject) obj;

        JSONArray platformList = (JSONArray) jsonObject.get("services");

		  Iterator<JSONObject> iterator = platformList.iterator();
        while(iterator.hasNext()){
          jsonObject = iterator.next();
          String id = (String) jsonObject.get("id");
          String name = (String) jsonObject.get("name");
          String statusUrl = (String) jsonObject.get("statusUrl");
          String statusApiUrl = (String) jsonObject.get("statusApiUrl");
          App.platforms.add(buildPlatform(id,name,statusUrl,statusApiUrl));
        }
    }

    public static Platform buildPlatform(String id, String name, String statusUrl, String statusApiUrl){
      Platform p = null;
      if(id.equals("github"))
        p = new Github(id,name,statusUrl,statusApiUrl);
      if(id.equals("bitbucket"))
        p = new Bitbucket(id,name,statusUrl,statusApiUrl);
      return p;
    }
    
    public static void showServicesCommand() {
    	for(Platform p: App.platforms) {
    		System.out.println("Service name:" + p.getName());
    		System.out.println("Service Endpoint:" + p.getStatusApiUrl() + "\n");
    	}
    }
    
	public static void main(String[] args){
        App.readJsonFileWithProperties();
        for(Platform p: platforms){
          System.out.println(p.getId());
          System.out.println(p.getName());
          System.out.println(p.getStatusUrl());
          System.out.println(p.getStatusApiUrl());
        }
        Scanner scanner = new Scanner(System.in);
        while(true){
            String command = scanner.nextLine();
            switch(command) {
                case "bot poll":
                  System.out.println("0");
                  break;
                case "bot fetch":
                   System.out.println("1");
                  break;
                case "bot history":
                   System.out.println("2");
                  break;
                case "bot backup":
                   System.out.println("3");
                  break;
                case "bot services":
                   App.showServicesCommand();
                  break;
                case "bot help":
                   App.helpCommand();
                  break;
                default:
                  System.out.println("invalid command");
              }
        }
    }
}