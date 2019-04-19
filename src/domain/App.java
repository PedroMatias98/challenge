package domain;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import exceptions.EmptyFileException;
import exceptions.FileAlreadyExistsException;
import exceptions.InvalidFileFormatException;

import java.io.File;

import java.util.Iterator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLConnection;
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
      System.out.println("\tfetch - Retrieves the status from of all configured services within an interval");
      System.out.println("\tservices - Lists all known services");
      System.out.println("\tbackup - backups the current internal state to a file");
      System.out.println("\trestore - Imports the internal state from another run or app");
      System.out.println("\thistory - Outputs all the data from the local storage");
      //System.out.println("\tstatus - Summarizes data and displays it in a table-like fashion");
      System.out.println("\thelp - This screen");
    }

    @SuppressWarnings("unchecked")
    public static void readJsonFileWithProperties() throws FileNotFoundException,ParseException,IOException{
      JSONParser parser = new JSONParser();
      Object obj = null;
      try{
        obj = parser.parse(new FileReader("storedData/storedData.json"));
      } catch(FileNotFoundException e){
    	  throw new FileNotFoundException("The file doesnt exist");
      }
        catch(ParseException e){
          throw new ParseException(e.getPosition(),e.getErrorType(),e.getUnexpectedObject());
      }
        catch(IOException e){
          throw new IOException("There was an io error");
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
    		StringBuilder info = new StringBuilder(); 
    		info.append("Service name:").append(p.getName()).append("\n");
    		info.append("Service Endpoint:").append(p.getStatusApiUrl()).append("\n");
    		System.out.println(info);
    	}
    }
    
    public static void historyCommand() throws EmptyFileException,FileNotFoundException,IOException{
    	BufferedReader reader = null;
    	try {
    		reader = new BufferedReader(new FileReader("appData/history.txt"));
    	    String line = reader.readLine();
    	    if(line == null) {
    	    	reader.close();
    	    	throw new EmptyFileException("The file is empty");
    	    }
    	    while (line != null) {
    	    	System.out.println(line);
    	        line = reader.readLine();
    	    }
    	    reader.close();
    	}
    	
    	catch(FileNotFoundException e){
    		throw new FileNotFoundException("The file doesnt exist");
    	}
    	
    	catch(IOException e){
    		throw new IOException("There was an io error");
    	}
    }

    
    public static void checkServicesAvailability() throws ParseException,IOException{
    	JSONObject infoJson = null;
    	String status = null;
    	BufferedWriter writer = null;
    	StringBuilder availability = new StringBuilder();
		try {
			writer = new BufferedWriter(new FileWriter("appData/history.txt", true));

    	for(Platform p: platforms) {
    		if(!p.isAccessable(5000)) {
    			availability.append("The service ").append(p.getName()).append("status is down");
    		}
    		else {
    			String info = p.getStatus();
    			JSONParser parser = new JSONParser();
    			infoJson = (JSONObject) parser.parse(info);
    			
    			JSONObject pageInfo = (JSONObject) infoJson.get("page");
    			String update = (String) pageInfo.get("updated_at");
    			JSONObject pageStatus = (JSONObject) infoJson.get("status");
    			String description = (String) pageStatus.get("description");
    			if(description.equals("All Systems Operational"))
    				status = "up";
    			else
    				status = "down";
    			availability.append("[").append(p.getId()).append("] ").append(update).append(" - ").append(status);
    			System.out.println(availability);
    			writer.write(availability.toString());
    			writer.newLine();
    			availability = new StringBuilder();
    			}
    		}
    		writer.close();
    	
		}
		
		catch(MalformedURLException e) {
			writer.close();
			throw new MalformedURLException("The url is malformed");
		}
		
		catch(ProtocolException e) {
			writer.close();
			throw new ProtocolException("There was an error with the protocol");
		}
		
		catch(ParseException e) {
			writer.close();
			throw new ParseException(e.getPosition(),e.getErrorType(),e.getUnexpectedObject());
		}
		
		catch (IOException e) {
			writer.close();
			throw new IOException("There was an io error");
		}
    }
    
    public static void restoreCommand(String file) throws InvalidFileFormatException,IOException,FileAlreadyExistsException{
    	String[] fileProperties = file.split("/");
    	String fileName = fileProperties[fileProperties.length-1];
    	
    	File fileInfo = new File(file);
    	File destFile = new File("appData/" + fileName);
        String mimeType = URLConnection.guessContentTypeFromName(fileInfo.getName());
    	try {
    		if(!mimeType.contains("text") && !mimeType.contains("xml"))
    			throw new InvalidFileFormatException("The file has an invalid format");
    		if(destFile.exists())
    			throw new FileAlreadyExistsException("The file already exists");
    	Path source = Paths.get(file);
    	Path target = Paths.get("appData/" + fileName);

    	Files.copy(source, target);
    	} catch(IOException e) {
    		throw new IOException("There was an io error");
    	}
    }
    
    public static void backupCommand(String file) throws EmptyFileException,FileNotFoundException,IOException{
    	try {
    		String directory = "";
    		String[] parts = file.split("/");
    		for(int i=0;i<parts.length -1;i++)
    			directory += parts[i];
    		Path path = Paths.get(directory);
    		Files.createDirectories(path);
    		BufferedReader reader = new BufferedReader(new FileReader("storedData/storedData.json"));
        	PrintWriter writer = new PrintWriter(new FileWriter(file));
    	    String line = reader.readLine();
    	    if(line == null) {
    	    	reader.close();
        	    writer.close();
    	    	throw new EmptyFileException("The file is empty");
    	    }
    	    while (line != null) {
    	    	writer.println(line);
    	        line = reader.readLine();
    	    }
    	    reader.close();
    	    writer.close();
    	}
    	
    	catch(FileNotFoundException e){
    		throw new FileNotFoundException("The file doesnt exist");
    	}
    	
    	catch(IOException e){
    		throw new IOException("There was an io error");
    	}
    }
    
    
    public static void fetchCommand(int interval) throws ParseException,IOException{
    	Timer timer = new Timer();
    	timer.schedule( 
    	        new TimerTask() {
    	            @Override
    	            public void run() {
    	            	try {
    	                App.checkServicesAvailability();
    	            	}
    	            	
    	            	catch(ParseException e) {
    	            		System.out.println("There was an error parsing the file");
    	            		System.exit(1);
    	            	}
    	            	
    	            	catch(IOException e) {
    	            		System.out.println("There was an io error");
    	            		System.exit(1);
    	            	}
    	            }
    	        }, 
    	        0,interval * 1000 
    	);
    }
    
    public static void getcommandOption(String command) throws EmptyFileException,InvalidFileFormatException,IOException,ParseException,FileAlreadyExistsException{
    	String[] parts = null;
    	if(command.equals("bot poll"))
    		App.checkServicesAvailability();
    	
    	else if(command.equals("bot history")) {
    		App.historyCommand();
    	}
    	
    	else if(command.equals("bot services"))
    		App.showServicesCommand();
    	
    	else if(command.equals("bot help"))
    		App.helpCommand();
    	
    	else if(command.equals("bot fetch"))
    		App.fetchCommand(5);
    	
    	else if(command.contains("bot fetch --refresh=")) {
    		parts = command.split("bot fetch --refresh=");
    		String interval = parts[1];
    		App.fetchCommand(Integer.valueOf(interval));
    	}
    	
    	else if(command.contains("bot restore ")) {
    		parts = command.split("bot restore ");
    		String file = parts[1];
    		App.restoreCommand(file);
    	}
    	
    	else if(command.contains("bot backup ")) {
    		parts = command.split("bot backup ");
    		String file = parts[1];
    		App.backupCommand(file);
    	}
    	
    	else
    		System.out.println("invalid command");
    }
    
	public static void main(String[] args) throws EmptyFileException,InvalidFileFormatException,IOException,ParseException,FileAlreadyExistsException{
        App.readJsonFileWithProperties();
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        while(!command.equals("exit")){
            App.getcommandOption(command);
            command = scanner.nextLine();
        }
        scanner.close();
    }
}