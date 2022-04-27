package testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class ParseDataForGraph {
	
	public static ArrayList<Integer> response_;
	public static ArrayList<Integer> wait_;
	public static int avgResponse_;
	public static int avgWait_;

	/**
	 * Handles all parsing of RESULTS.txt data, puts in a easily readable form for python graphing.
	 * Only works if RequestGenerator has already outputted to RESULTS.txt.
	 * @param args - n/a
	 */
	public static void main(String[] args) {
		File obj = new File("C:\\Users\\Charlie\\Desktop\\RESULTS.txt");
		parseData(obj);
		
		try {
			PrintWriter out = new PrintWriter("C:\\Users\\Charlie\\Desktop\\response.txt");
			for (int i : response_) {
				out.println(i);
				out.flush();
			}
			out.close();
			out = new PrintWriter("C:\\Users\\Charlie\\Desktop\\wait.txt");
			for (int i : wait_) {
				out.println(i);
				out.flush();
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void parseData(File obj) {
		try {
			//Getting run results from the RESULTS.txt file they are saved to.
			Scanner in = new Scanner(obj);
			
			ArrayList<String> holdie = new ArrayList<>();
			
			int indOfWaitStart = 0;
			
			int count = 0;
			
			int avgResponse = 0;
			int avgWait = 0;
			boolean responseNext = false;
			boolean waitNext = false;
			
			//Saving the results to an arraylist and getting the relevant indices.
			while (in.hasNext()) {
				String temp = in.next();
				if (responseNext) {
					avgResponse = Integer.valueOf(temp);
					responseNext = false;
				}
				else if (waitNext) {
					avgWait = Integer.valueOf(temp);
					waitNext = false;
				}
				else {
					if (temp.length() > 1) {
						if (temp.substring(0, temp.length()-2).contains("[")) {
							if (count != 0) {
								indOfWaitStart = count+1;
								holdie.add(temp.substring(1, temp.length()-1));
								count++;
							}
							else {
								holdie.add(temp.substring(1, temp.length()-1));
							}
						}
						else if (temp.substring(0, temp.length()-2).contains("]")) {
							holdie.add(temp.substring(0, temp.length()-2));
							count++;
						}
						else {
							if (temp.equals("AvgResponse:") || temp.equals("AvgWait:")) {
								if (temp.equals("AvgResponse:")) {
									responseNext = true;
								}
								else if (temp.equals("AvgWait:")){
									waitNext = true;
								}
							}
							else {
								holdie.add(temp.substring(0, temp.length()-1));
								count++;
							}
							
						}
					}
				}
				
			}
				
			
			//Casting data to integers and splitting into two lists.
			ArrayList<Integer> response = new ArrayList<>();
			ArrayList<Integer> wait = new ArrayList<>();
			
			for (int i = 0; i < indOfWaitStart; i++) {
				response.add(Integer.valueOf(holdie.get(i)));
			}
			for (int i = indOfWaitStart; i < holdie.size()-4; i++) {
				wait.add(Integer.valueOf(holdie.get(i)));
			}
			
			response_ = response;
			wait_ = wait;
			avgResponse_ = avgResponse;
			avgWait_ = avgWait;
			
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
