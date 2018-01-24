import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
public class CourseSchedule {
	public static void main(String[] args) throws Exception{
		Scanner console = new Scanner(System.in);
		
        /* Each array in the list represents a course offering, which is uniquely identified by its item #
         * array[0] is the item #
         * array[1] is the course code
         * array[2] is the title
         * array[3] is the instructor
         * array[4] is the days
         */
		ArrayList<String[]> list = new ArrayList<String[]>();
       
        System.out.println("Enter the quarter: ");
        String quarter = console.nextLine();
        System.out.println("Enter the year: ");
        String year = console.nextLine();
        System.out.println("Enter the initial for the program: ");
        String initial = console.nextLine();
        		
        
        String text = getText("https://www.bellevuecollege.edu/classes/" + quarter + year + "?letter=" + initial);       

       
        //assume user enters valid program name
        System.out.println("Programs: ");   
        Pattern programPattern = Pattern.compile("(subject-name\">\\s*<a href=\"/classes/" + quarter + year + ".*>(.*)</a>\\s\\((.*),\\s(.*)amp;\\)\\n)|(subject-name\">\\s*<a href=\"/classes/" + quarter + year + ".*>(.*)</a>\\s\\((.*)\\)\\n)");
        

        
        Matcher programMatcher = programPattern.matcher(text);
        while(programMatcher.find()) {
        	
        	// 2 program keys
        	if(programMatcher.group(5) == null) {
        		System.out.println(programMatcher.group(2) + " ("+ programMatcher.group(3) + ", " + programMatcher.group(4) + ")");       		 
        	}
        	
        	// just 1 program key, like CS
        	else {       		
        		System.out.println(programMatcher.group(6) + " ("+ programMatcher.group(7) + ")");   
        	}  
        }       
        System.out.println();
        
        System.out.println("Enter the program's name: ");
        String programName = console.nextLine();
        System.out.println("Enter the course ID: ");
        String courseID = console.nextLine();
        String programCode = ""; //used for URLs. The program code for Computer Science is CS
        
        System.out.println();  
        
        // this pattern gets the program code for the user-specified program
        Pattern codePattern = Pattern.compile("(subject-name\">\\s*<a href=\"/classes/" + quarter + year + ".*>(.*)</a>\\s\\((.*),\\s(.*)amp;\\)\\n)|(subject-name\">\\s*<a href=\"/classes/" + quarter + year + ".*>(.*)</a>\\s\\((.*)\\)\\n)");      
        Matcher codeMatcher = codePattern.matcher(text);
        while(codeMatcher.find()) {           	
        	   
        	//2 program keys, like (ASTR, ASTR&)
        	if(codeMatcher.group(5) == null) {	 
        		if(codeMatcher.group(2).equals(programName)) {
        			programCode = codeMatcher.group(3);
        		}
        	}
        	
        	// just 1 program key, like CS
        	else { 
        		if(codeMatcher.group(6).equals(programName)) {
        			programCode = codeMatcher.group(7);      			
        		}
        	}          
        }         
      
        String courseInfo; // for URL of user-specified program
        courseInfo = getText("https://www.bellevuecollege.edu/classes/Spring2018/" + programCode);        
        System.out.println(programName + " courses in " + quarter + " " + year);

        
        String shortenedCourseInfo = ""; // will hold all text relevant to chosen course
        
        // this Pattern gets all the text relevant to the chosen course
        Pattern itemPattern = Pattern.compile("<h2\\sclass=\"classHeading[\\s\\S]*?\\s"+ courseID + "\\sdetails([\\s\\S]*?)<h2\\sclass=\"classHeading"  );                   
        Matcher itemMatcher = itemPattern.matcher(courseInfo);
        while(itemMatcher.find()) {
        	shortenedCourseInfo += itemMatcher.group(1) + "\n";       	
        }
       

        Pattern itemNumPattern = Pattern.compile("[\\s\\S]*?\\s</span>([0-9]+)</span>");        
        Matcher numberMatcher = itemNumPattern.matcher(shortenedCourseInfo);        
        while(numberMatcher.find()) {
        	System.out.println(numberMatcher.group(1));
        	String[] temp = new String[5];
        	temp[0] = numberMatcher.group(1);
        	temp[1] = courseID; // adding the course ID here for convenience        	        	
        	list.add(temp);
        }
     
        
        // for finding the course title
        Pattern titlePattern = Pattern.compile("<span class=\"courseID\">" + courseID + "</span>\\s<span\\sclass=\"courseTitle\">(.*)</span");
        Matcher titleMatcher = titlePattern.matcher(courseInfo);
        while(titleMatcher.find()) {
        	System.out.println(titleMatcher.group(1));
        	for(int i = 0; i < list.size(); i++) {      		
        		list.get(i)[2] = titleMatcher.group(1);
        	}
        }
        
        // for finding course instructor
        int instructorCount = 0; // for keeping track of where, in the ArrayList, to add this instructor's name
        Pattern instructorPattern = Pattern.compile("(Instructors:<[\\s\\S]*?<li>(.*)?</li>)|(Instructors:<[\\s\\S]*?<li>[\\s\\S]*?<a href=\"[\\s\\S]*?\">(.*)?</a>)");      
        Matcher instructorMatcher = instructorPattern.matcher(shortenedCourseInfo);
        while(instructorMatcher.find()) {       	
        	String temp;
        	
        	if(instructorMatcher.group(2) == null) {      		
        		temp = instructorMatcher.group(4);
        	}
        	
        	else{
        		temp = instructorMatcher.group(2);
        	}
        	list.get(instructorCount)[3] = temp;
        	instructorCount++;
        }
        
      
        // for finding course days
        int daysCount = 0;
        Pattern daysPattern = Pattern.compile("(<ul\\sclass=\"meets\"[\\s\\S]*?class=\"days\">[\\s\\S]*?=\"(.*?)\")|(<ul\\sclass=\"meets\"[\\s\\S]*?class=\"days\\sonline\")");
        Matcher daysMatcher = daysPattern.matcher(shortenedCourseInfo);
        while(daysMatcher.find()) {
        	String temp;
        	if(daysMatcher.group(2) == null) {
        		temp = "Online";
        	}
        	else{
        		temp = daysMatcher.group(2);
        	}
        	list.get(daysCount)[4] = temp;
        	daysCount++;
        }
        
        for(int i = 0; i<list.size(); i++){
        	System.out.println("====================");
        	System.out.println("Code: " + list.get(i)[0]);
        	System.out.println("Item#: " + list.get(i)[1]);
        	System.out.println("Title: " + list.get(i)[2]);
        	System.out.println("Instructor: " + list.get(i)[3]);
        	System.out.println("Days: " + list.get(i)[4]);
        }
        		
        console.close();
	}
	
	public static String getText(String url) throws Exception {     
		URL bc = new URL(url);  
        BufferedReader in = new BufferedReader( 
        new InputStreamReader(bc.openStream()));  
        
        String inputLine = "";   
        String text = "";
        
        while ((inputLine = in.readLine()) != null) {
        	text += inputLine + "\n";
        }
              
        in.close();
		return text;
		
	}

	
}
