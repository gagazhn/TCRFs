package lang;

import inference.Inference;
import inference.TreeViterbi;
import inference.Viterbi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import gcrfs.Model;
import template.BaselineTemplate;
import template.ChainTemplate;
import template.LogTemplate;
import template.TemplateQueue;
import template.TreeTemplate;
import type.InstanceList;
import util.InstanceReader;

/**
 * @Description: 
 *
 */
public class TreeCRFTui {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String cmd = null;
		String type = null;
		String trainPath = null;
		String testPath = null;
		String modelPath = null;
		String encoding = null;
		
		if (args.length == 0) {
			System.err.println("Args error. Abort!");
			System.exit(1);
		} else {
			cmd = args[0];
			if (cmd.equals("--train")) {
				if (args.length != 6) {
					System.err.println("Args error. Abort...");
					System.exit(1);
				}
				
				type = args[1];
				modelPath = args[2];
				encoding = args[3];
				trainPath = args[4];
				testPath = args[5];
			} else if (cmd.equals("--test")) {
				if (args.length != 4) {
					System.err.println("Args error. Abort...");
					System.err.println(args.length);
					System.err.println(args[2]);
					System.exit(1);
				}
				
				modelPath = args[1];
				encoding = args[2];
				testPath = args[3];
			} else {
				System.err.println("Args error. Abort...");
				System.exit(1);
			}
		}
		
//		String file = "./model.G.ser.gz";
		Model model = null;
		if ("--train".equals(cmd)) {
			InstanceList instanceList = InstanceReader.read(trainPath, encoding);
			InstanceList instanceList2 = InstanceReader.read(testPath, encoding);
			
			TemplateQueue q = new TemplateQueue();
			Inference inference = null;
			
			if (type.equals("chain")) {
				q.add(new ChainTemplate());
				inference = new Viterbi();
			} else if (type.equals("tree")) {
				q.add(new TreeTemplate());
				inference = new TreeViterbi();
			} else if (type.equals("log")) {
				q.add(new LogTemplate());
				inference = new Viterbi();
			} else if (type.equals("baseline")) {
				q.add(new BaselineTemplate());
				inference = new Viterbi();
			} else {
				System.err.println("Only chain or tree be supported. Abort...");
				System.exit(1);
			}
			
			model = new Model(instanceList, instanceList2, q, inference);
			model.train(150);
			writeGzippedObject(new File(modelPath), model);
			System.err.println("Model saved!");
			
			model.test(instanceList2, Model.DEBUG_OUTPUT);
		} else if ("--test".equals(cmd)){
			InstanceList instanceList2 = InstanceReader.read(testPath, encoding);
			model = (Model)readGzippedObject(new File(modelPath));
			model.test(instanceList2, Model.DEBUG_OUTPUT);
		}
	}
	
	public static void writeGzippedObject (File f, Serializable obj) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream (new BufferedOutputStream (new GZIPOutputStream (new FileOutputStream(f))));
			oos.writeObject(obj);
			oos.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Object readGzippedObject (File f) {
		try {
			ObjectInputStream ois = new ObjectInputStream (new BufferedInputStream (new GZIPInputStream (new FileInputStream(f))));
			Object obj = ois.readObject();
			ois.close ();
			return obj;
		}
		catch (IOException e) {
			throw new RuntimeException (e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException (e);
		}
	}
	
}
