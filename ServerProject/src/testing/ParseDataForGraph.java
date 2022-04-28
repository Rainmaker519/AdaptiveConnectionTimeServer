package testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class ParseDataForGraph {
	
	public static ArrayList<Integer> free_response_;
	public static ArrayList<Integer> paid_response_;

	/**
	 * Handles all parsing of RESULTS.txt data, puts in a easily readable form for python graphing.
	 * Only works if RequestGenerator has already outputted to RESULTS.txt.
	 * @param args - n/a
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		File obj = new File("C:\\Users\\Charlie\\Desktop\\RESULTS.txt");
		//parseData(obj);
		parse(obj);
		/*
		try {
			PrintWriter out = new PrintWriter("C:\\Users\\Charlie\\Desktop\\free_response.txt");
			for (int i : free_response_) {
				out.println(i);
				out.flush();
			}
			out.close();
			out = new PrintWriter("C:\\Users\\Charlie\\Desktop\\paid_response.txt");
			for (int i : paid_response_) {
				out.println(i);
				out.flush();
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public static void parseData(File obj) {
		try {
			//Getting run results from the RESULTS.txt file they are saved to.
			Scanner in = new Scanner(obj);
			
			ArrayList<String> holdie = new ArrayList<>();
			
			int indOfWaitStart = 0;
			
			int count = 0;
			
			boolean responseNext = false;
			boolean waitNext = false;
			
			//Saving the results to an arraylist and getting the relevant indices.
			while (in.hasNext()) {
				String temp = in.next();
				if (responseNext) {
					responseNext = false;
				}
				else if (waitNext) {
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
			ArrayList<Integer> free_response = new ArrayList<>();
			ArrayList<Integer> paid_response = new ArrayList<>();
			
			for (int i = 0; i < indOfWaitStart; i++) {
				free_response.add(Integer.valueOf(holdie.get(i)));
			}
			for (int i = indOfWaitStart; i < holdie.size(); i++) {
				paid_response.add(Integer.valueOf(holdie.get(i)));
			}
			
			free_response_ = free_response;
			paid_response_ = paid_response;
			
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void parse(File obj) throws FileNotFoundException {
		Scanner in = new Scanner(obj);
		String rTimes = in.nextLine();
		String sTimes = in.nextLine();
		String classes = in.nextLine();
	}

}
