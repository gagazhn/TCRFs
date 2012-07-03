package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Scanner;

import type.Feature;
import type.FeatureSet;
import type.Timestamp;
import type.TimestampSequence;
import type.Instance;
import type.InstanceList;
import type.LabelSet;

public class InstanceReader {

	/**
	 * @param args
	 */
	public static InstanceList read(String fileString) {
		InstanceList instanceList = new InstanceList();
		
		try {
			Scanner scanner = new Scanner(new File(fileString));
			TimestampSequence timestampSequence = new TimestampSequence();
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!"".equals(line)) {
					String[] columes = line.split("---");
					String graph = columes[1].trim();
					String lString = columes[0].trim();
					// 前继节点的索引号
					int parent = Integer.parseInt(graph.substring(graph.indexOf('@') + 1));
					
					Timestamp timestamp = new Timestamp(parent, lString);
					String[] features = columes[2].trim().split(" ");
					for (String fString : features) {
						timestamp.getFeatures().add(fString);
					}
					timestampSequence.add(timestamp);
				} else {
					Instance instance = new Instance();
					instance.setTimestampSequence(timestampSequence);
					instanceList.add(instance);
					
					timestampSequence = new TimestampSequence();
				}
			}
			
			// if源文件没有以两换行结尾
			if (timestampSequence.size() != 0) {
				Instance instance = new Instance();
				instance.setTimestampSequence(timestampSequence);
				instanceList.add(instance);
				
				timestampSequence = new TimestampSequence();
			}
			
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return instanceList;
	}
	
	public static void main(String[] args) {
		InstanceReader.read("train.data");
	}
}
