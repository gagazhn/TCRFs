/**
 * 
 */
package tools;

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
//			Scanner scanner = new Scanner(new FileInputStream("./data/HIT.raw"));
			Scanner scanner = new Scanner(new FileInputStream("./data/COAE.digital.raw"));
//			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream("./data/HIT.data"), "UTF-8");
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream("./data/COAE.digital.data"), "UTF-8");
			StringBuilder instance = new StringBuilder();
			boolean isOp = false;
			String[] preLines = null;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
//				System.out.println(line);
				if (!line.equals("")) {
					String[] lines = line.split(" --- ");
					
					if (lines[0].equals("KEY:ATTR") || lines[0].equals("KEY:OP")) {
						isOp = true;
					}
					
					String[] elements = lines[2].split(" ");
					
					StringBuilder sb = new StringBuilder();
					
					sb.append(lines[0] + " --- " + lines[1] + " --- ");
					
					////////////
					
//					/*
					sb.append("W:" + elements[0] + " ");
					sb.append("#W:" + elements[0] + " ");
					sb.append("WW:" + "[" + elements[1] + "] ");
					sb.append("POS:" + elements[2] + " ");
					sb.append("#POS:" + elements[2] + " ");
					sb.append("POSPOS:" + elements[3] + " ");
					sb.append(elements[7] + " ");
					sb.append(elements[8]);
//					*/
					
					///////////
					
/*					
					sb.append("W:" + elements[0] + " ");
					sb.append("#W:" + elements[0] + " ");
					// #DW
					sb.append("#" + elements[5] + " ");
					sb.append("WW:" + "[" + elements[1] + "] ");
					sb.append("#DWW:" + "[" + elements[0] + "|" + elements[5] + "] ");
					sb.append("POS:" + elements[2] + " ");
					sb.append("#POS:" + elements[2] + " ");
					sb.append("POSPOS:" + elements[3] + " ");
					sb.append("#POSPOS:" + elements[3] + " ");
					sb.append("DPOSPOS:" + "[" + elements[2] + "|" + elements[6] + "] ");
					sb.append("#DPOSPOS:" + "[" + elements[2] + "|" + elements[6] + "] ");
					// #dep
					sb.append("#" + elements[4] + " ");
					sb.append(elements[7] + " ");
					sb.append(elements[8]);
*/
					///////////
					
//					System.out.println(sb.toString());
					instance.append(sb.toString() + "\r\n");
//					ow.write(sb.toString() + "\r\n");
					
					preLines = lines;
				} else {
//					System.out.println();
					if (isOp) {
						ow.write(instance.toString());
						ow.write("\r\n");
						isOp = false;
						instance = new StringBuilder();
					} else {
						instance = new StringBuilder();
					}
					
					preLines = null;
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
