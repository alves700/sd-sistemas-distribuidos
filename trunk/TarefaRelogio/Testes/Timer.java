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
		  long tempo = System.currentTimeMillis();
		  String[] command =  new String[3];
          command[0] = "cmd";
          command[1] = "/C";
          command[2] = "time 13:10:12,13";//path of the compiler
          
          
          Process p = Runtime.getRuntime().exec(command);
          System.out.println(System.currentTimeMillis()- tempo);
          //BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
          //String s = stdInput.readLine();
          //s = (String) s.subSequence(12, s.length());
          
          
          
          /*
          System.out.println(s);
          String [] c = s.split(":");
          
     
          long mills = Long.parseLong(c[0])*60*60*1000; //Horas convertidos em millis
          mills +=  Long.parseLong(c[1])*60*1000; //Minutos convertidos em millis
          String [] c2= c[2].split(","); 
          mills += Long.parseLong(c2[0])*1000; //Segundos convertidos em millis
          mills += Long.parseLong(c2[1])*10; // millis
          
          System.out.println(c[0]);
          System.out.println(c[1]);
          System.out.println(c2[0]);
          System.out.println(c2[1]);
          System.out.println(mills);
          
          long elapsedTime = mills;  
          String format = String.format("%%0%dd", 2);
          String milliseconds = String.format(format, (elapsedTime % 1000)/10);
          System.out.println(milliseconds);
          elapsedTime = elapsedTime/ 1000;  
          String seconds = String.format(format, elapsedTime % 60);  
          String minutes = String.format(format, (elapsedTime % 3600) / 60);  
          String hours = String.format(format, elapsedTime / 3600);  
          String time =  hours + ":" + minutes + ":" + seconds+","+milliseconds;
          System.out.println(time);
		*/
      }
    catch(Exception e){  
                System.out.println("I am In catch");
             }
		  
    }
}
