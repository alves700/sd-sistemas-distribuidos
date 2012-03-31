package Testes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Timer {
	public static void main(String []args) throws IOException{

		
	  try
      {
		  String[] command =  new String[3];
          command[0] = "cmd";
          command[1] = "/C";
          command[2] = "time";//path of the compiler

          Process p = Runtime.getRuntime().exec(command);
          BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
          String s = stdInput.readLine();
          s = (String) s.subSequence(12, s.length());
          System.out.println(s);
          String [] c = s.split(":");
          
          for(int i = 0 ; i < c.length-1;i++){
        	  System.out.println(c[i]);
          }
          
          long mills = Long.parseLong(c[0])*60*60*1000;
          mills +=  Long.parseLong(c[1])*60*1000;
          //c2 = c[2].split(",");
          //mills += 
          
	          

	          
      }
    catch(Exception e){  
                System.out.println("I am In catch");
             }
		  
    }
}
