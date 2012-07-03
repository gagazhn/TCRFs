/**
 * 
 */
package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;

/**
 * @author gagazhn
 *
 */
public class RawToData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
//			Scanner scanner = new Scanner(new FileInputStream("./trainG.data.raw"));
//			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream("./trainG.data"), "UTF-8");
			Scanner scanner = new Scanner(new FileInputStream("./testG.data.raw"));
			Scanner preScanner = new Scanner(new FileInputStream("./testG.data.raw"));
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream("./testG.data"), "UTF-8");
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				System.out.println(line);
				if (!line.equals("")) {
					String[] lines = line.split(" --- ");
					
					String[] elements = lines[2].split(" ");
					
					StringBuilder sb = new StringBuilder();
					
					sb.append(lines[0] + " --- " + lines[1] + " --- ");
					
					sb.append("W:" + elements[0] + " ");
					sb.append("#W:" + elements[0] + " ");
					sb.append("WW:" + "[" + elements[0] + "|" + elements[3] + "] ");
					sb.append("POS:" + elements[1] + " ");
					sb.append("#POS:" + elements[1] + " ");
					sb.append("PP:" + "[" + elements[1] + "|" + elements[4] + "] ");
					sb.append("#" + elements[2] + " ");
					sb.append(elements[5] + " ");
					sb.append(elements[6]);
					System.out.println(sb.toString());
					ow.write(sb.toString() + "\r\n");
				} else {
					System.out.println();
					ow.write("\r\n");
				}
			}
			
			scanner.close();
			ow.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
