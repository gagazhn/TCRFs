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
import template.ChainTemplate;
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
//		InstanceList instanceList = InstanceReader.read("test.data");
		InstanceList instanceList = InstanceReader.read("trainG.data");
//		InstanceList instanceList = InstanceReader.read("train.data");
		
//		InstanceList instanceList2 = InstanceReader.read("train.data");
		InstanceList instanceList2 = InstanceReader.read("testG.data");
//		InstanceList instanceList2 = InstanceReader.read("test.data");
		System.out.println("Train data done.");
		
		TemplateQueue q = new TemplateQueue();
		
//		q.add(new ChainTemplate());
//		Inference inference = new Viterbi();
//		
		q.add(new TreeTemplate());
		Inference inference = new TreeViterbi();
		
		String file = "./model.G.ser.gz";
		Model model = null;
		if (true) {
			model = new Model(instanceList, instanceList2, q, inference);
			model.train(200);
			writeGzippedObject(new File(file), model);
			System.out.println("Model saved!");
		} else {
			model = (Model)readGzippedObject(new File(file));
		}
		
		model.test(instanceList2);
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
