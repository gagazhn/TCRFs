/**
 * 
 */
package tools;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;

/**
 * @author gagazhn
 *
 */
public class ToTrainData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(new FileInputStream("./test.tagged"));
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream("./test.data"), "UTF-8");
			int index = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if (!line.equals("")) {
					String[] elems = line.split(" ");
					StringBuilder sb = new StringBuilder();
					
					sb.append(elems[elems.length - 1]);
					sb.append(" --- " + index + "@" + (index++ - 1) + " ---");
					for (int i = 0; i < elems.length - 2; i++) {
						sb.append(" " + elems[i]);
					}
					
					ow.write(sb.toString());
				} else {
					index = 0;
				}
				
				ow.write("\n");
			}
			
			scanner.close();
			ow.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
