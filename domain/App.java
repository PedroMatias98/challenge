package domain;

import java.util.Scanner;

public class App {

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
    public static void main(String[] args){
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
                   System.out.println("4");
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