package domain;

import java.util.Scanner;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;

import java.util.Iterator;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileReader;
import java.io.FileWriter;

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
    
    public static void historyCommand() {
    	BufferedReader reader = null;
    	try {
    		reader = new BufferedReader(new FileReader("appData/history.txt"));
    	    String line = reader.readLine();
    	    if(line == null)
    	    	System.out.println("error: empty file");
    	    while (line != null) {
    	    	System.out.println(line);
    	        line = reader.readLine();
    	    }
    	    reader.close();
    	}
    	
    	catch(FileNotFoundException e){
    		e.getMessage();
    	}
    	
    	catch(IOException e){
    		e.getMessage();
    	}
    }

    
    public static void checkServicesAvailability() {
    	JSONObject infoJson = null;
    	String status = null;
    	BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("appData/history.txt", true));

    	for(Platform p: platforms) {
    		if(!p.isAccessable(10000))
    			System.out.println("The service " + p.getName() + "status is down");
    		else {
    			String info = p.getStatus();
    			JSONParser parser = new JSONParser();
    			try {
    				infoJson = (JSONObject) parser.parse(info);
    			}
    			catch(ParseException e) {
    				e.getMessage();
    			}
    			JSONObject pageInfo = (JSONObject) infoJson.get("page");
    			String update = (String) pageInfo.get("updated_at");
    			JSONObject pageStatus = (JSONObject) infoJson.get("status");
    			String description = (String) pageStatus.get("description");
    			if(description.equals("All Systems Operational"))
    				status = "up";
    			else
    				status = "down";
    			System.out.println("[" + p.getId() + "] " + update + " - " + status);
    			writer.write("[" + p.getId() + "] " + update + " - " + status);
    			writer.newLine();
    			}
    		}
    		writer.close();
    	
		} catch (IOException e1) {
			e1.getMessage();
		}
    }
    
    public static void restoreCommand(String file) {
    	String[] fileProperties = file.split("/");
    	String fileName = fileProperties[fileProperties.length-1];
    	
    	File fileInfo = new File(file);
        String mimeType = URLConnection.guessContentTypeFromName(fileInfo.getName());
    	try {
    		if(!mimeType.contains("text")) {
    			System.out.println("invalid file format");
    			System.exit(1);
    	}
    	Path source = Paths.get(file);
    	Path target = Paths.get("appData/" + fileName);

    		Files.copy(source, target);
    	} catch(IOException e) {
    		e.getMessage();
    	}
    }
    
    public static void getcommandOption(String command) {
    	String[] parts = null;
    	if(command.equals("bot poll"))
    		App.checkServicesAvailability();
    	
    	else if(command.equals("bot history"))
    		App.historyCommand();
    	
    	else if(command.equals("bot services"))
    		App.showServicesCommand();
    	
    	else if(command.equals("bot help"))
    		App.helpCommand();
    	
    	else if(command.equals("bot fetch"))
    		System.out.println("fetch");
    	
    	else if(command.contains("bot restore ")) {
    		parts = command.split("bot restore ");
    		String file = parts[1];
    		App.restoreCommand(file);
    	}
    	
    	else if(command.contains("bot backup ")) {
    		parts = command.split("bot backup ");
    		String file = parts[1];
    	}
    	
    	else
    		System.out.println("invalid command");
    }
    
	public static void main(String[] args){
        App.readJsonFileWithProperties();
        Scanner scanner = new Scanner(System.in);
        while(true){
            String command = scanner.nextLine();
            App.getcommandOption(command);
        }
    }
}