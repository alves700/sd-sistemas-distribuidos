package Testes;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class Timer2 {
	public static void main(String [] args){
			long timeInMillis = System.currentTimeMillis();
		
		Calendar cal = Calendar.getInstance();
		
		cal.setTimeInMillis(timeInMillis);
		
		java.util.Date date = cal.getTime();
		
		System.out.println(date);
		
		Time time2 = Time.valueOf("12:03:12");
		System.out.println(time2.toString());
		
		
		
	}
}
