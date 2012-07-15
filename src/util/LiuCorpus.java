package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.stream.FileImageInputStream;

public class LiuCorpus {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		read("./Creative Labs Nomad Jukebox Zen Xtra 40GB.tgr");
	}
	
	public static void read(String filename) {
		try {
			Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(filename)));
			ArrayList<String> content = new ArrayList<String>();
			boolean contentStart = false;
			boolean tagStart = false;
			Pattern isOp = Pattern.compile(".*Opinion.*");
			Pattern id = Pattern.compile("id=newid(\\d+)");
			Pattern ln = Pattern.compile("ln=newid(\\d+)");
			Pattern location = Pattern.compile("\\[(\\d+)\\.(\\d+), (\\d+)\\.(\\d+)\\]");
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.startsWith("<contents>")) {
					contentStart = true;
				} else if (line.startsWith("</contents>")) {
					contentStart = false;
				} else 	if (line.startsWith("<tags>")) {
					tagStart = true;
				} else if (line.startsWith("</tags>")) {
					tagStart = false;
				}
				
				
				if (contentStart) {
					content.add(line);
				}
				
				if (tagStart) {
					Matcher m = location.matcher(line);
					if (m.find()) {
						int line1 = Integer.parseInt(m.group(1));
						int line2 = Integer.parseInt(m.group(3));
						int start = Integer.parseInt(m.group(2));
						int end = Integer.parseInt(m.group(4));
						
						System.out.println(content.get(line1));
						System.out.println(content.get(line1).substring(start, end));
					}
				}
			}
			
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
