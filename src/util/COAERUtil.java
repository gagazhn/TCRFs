/**
 * 
 */
package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gagazhn
 *
 */
public class COAERUtil {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream("task3.clear.data"), "UTF-8"));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] elements = line.split("\t");
				if (elements.length < 3) {
					continue;
				}
				
				String sent = elements[1];
				for (int i = 2; i < elements.length - 1; i += 3) {
					if (i + 2 >= elements.length) {
						System.out.println(line);
					}
					String attr = elements[i];
					String op = elements[i + 1];
					String polary = elements[i + 2];
					
					System.out.println(polary);
				}
//				System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}
	
	public static void clear() {
		try {
			Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream("task3.data"), "GBK"));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				line = line.replaceAll("℃|±|×|●|^|▲|~|□|■|>|★|=|○|°|\\$|≥|\\+|☆|←|[\\^Ⅲ┌╮└┘ⅰ⌒⌒└╮└┐━━┌Ⅱ┘②⑥──╮╭┴㈩㈩`─→─┤├╮│││╰┬──╯∶⊙⊙|]", "");
				if (!isMessyCode(line)) {
					System.out.println(line);
				} else {
//					System.out.println(line);
				}
				
			}
			System.out.println("done.");
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isChinese(char c) {    
	    Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);    
	    if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS    
	        || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS    
	        || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A    
	        || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION    
	        || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION    
	        || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {    
	      return true;    
	    }    
	    return false;    
	  }    
	    
	  public static boolean isMessyCode(String strName) {    
	    Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");    
	    Matcher m = p.matcher(strName);    
	    String after = m.replaceAll("");    
	    String temp = after.replaceAll("\\p{P}", "");    
	    char[] ch = temp.trim().toCharArray();    
	    float chLength = ch.length;    
	    float count = 0;    
	    for (int i = 0; i < ch.length; i++) {    
	      char c = ch[i];    
	      if (!Character.isLetterOrDigit(c)) {    
	    
	        if (!isChinese(c)) { 
	          count = count + 1;    
//	          System.out.print(c);
	        }    
	      }    
	    }    
	    float result = count / chLength;    
	    if (count > 0) {    
	      return true;    
	    } else {    
	      return false;    
	    }    
	    
	  }   
}
