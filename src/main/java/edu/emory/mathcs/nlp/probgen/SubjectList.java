package edu.emory.mathcs.nlp.probgen;


import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;

import java.io.*;

import java.util.List;
import java.util.ArrayList;




public class SubjectList{
	public void makeSubjectList(InfModel dbpediaInfModel){

		ResIterator subjectList = dbpediaInfModel.listSubjects();
		System.out.println("Subject list loaded");

		Resource o = dbpediaInfModel.getResource("http://dbpedia.org/resource/Barack_Obama");
		Property p = dbpediaInfModel.getProperty("http://dbpedia.org/ontology/type");

		ArrayList<Statement> towrite = new ArrayList<Statement>();

		int resourceCount = 0;
		int fileindex = 0;



		while(subjectList.hasNext()){
			Resource r = subjectList.nextResource();

			//Needs to be a full statement to be added to the model.
			//    so has dummy predicate & object
			Statement justResource = ResourceFactory.createStatement(r, p, o);
			towrite.add(justResource);

			if(towrite.size() > 100000){
				writeOut(towrite, fileindex);

				//clear arraylist for next 100,000
				towrite.clear();
				fileindex++;
			}
		}
		// the last one that will be less than 100,000
		writeOut(towrite, fileindex);
	}

	private void writeOut(ArrayList<Statement> towrite, int fileindex){
		try{
			Model writeModel = ModelFactory.createDefaultModel();
			writeModel.add(towrite);

			File file = new File("/home/wkelly3/jena-projects/jena-maven-example/subjectResourceFiles/"+fileindex+".txt");
			FileOutputStream fos = new FileOutputStream(file);

			writeModel.write(fos);

			fos.close();
		}
		catch (IOException e){
			System.out.println(e);
		}
	}



	public void loadSubjectList(int fileNumber){
 		try{
 			File file = new File("/home/wkelly3/jena-projects/jena-maven-example/subjectResourceFiles/"+fileNumber+".txt");
			FileInputStream fis = new FileInputStream(file);

			Model readModel = ModelFactory.createDefaultModel();

			String n = null;
			readModel.read(fis, n);

			ResIterator subjectList = readModel.listSubjects();

			int count = 0;
			while(subjectList.hasNext()){
				Resource r = subjectList.nextResource();
				count++;

				if(count % 1000 == 0){
					System.out.println(r.toString());
				}
			}
			System.out.println(count);
		}

		catch (Exception e){
			System.out.println(e);
		}
	}
}
