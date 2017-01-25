package edu.emory.mathcs.nlp.probgen;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import java.util.List;
import java.io.PrintWriter;
import java.io.IOException;

public class SubjectList{
	public static void makeSubjectList(InfModel dbpediaInfModel){
		ResIterator subjectList = dbpediaInfModel.listSubjects();

		int resourceCount = 0;
		int fileindex = 0;

		//because of IO
		try{
			while (subjectList.hasNext()){
				resourceCount++;
				towrite.add(subjectList.next());

				if (resourceCount % 1000 == 0) {
					System.out.println(resourceCount);
				}

				// should be 47 files of 100,000
				if (resourceCount % 100000 = 0){
					// serialize arraylist
					WriteArrayList wal = new WriteArrayList(towrite);

					// write file
					File file = new File("../../../../../../../../subjectResourceFiles/"+fileindex+".txt");

					// output streams
					FileOutputStream fos = new FileOutputStream(file);
					ObjectOutputStream oos = new ObjectOutputStream(fos);

					oos.writeObject(wal);

					oos.close();
					fos.close();

					//clear arraylist for next 100,000
					towrite.clear();
					fileindex++;
				}
			}


		} catch (IOException e){
			System.out.println(e);
		}

		System.out.println(resourceCount);
	}
}

class WriteArrayList implements Serializable{
	ArrayList<Resource> resourcesToWrite;
	public void setArrayList(ArrayList<Resource> resourceList){
		this.resourcesToWrite = resourceList;
	}
}

/*
	Reading:

	FileInputStream fis = new FileInputStream("t.tmp");
	ObjectInputStream ois = new ObjectInputStream(fis);
	List<Club> clubs = (List<Club>) ois.readObject();
	ois.close();

*/