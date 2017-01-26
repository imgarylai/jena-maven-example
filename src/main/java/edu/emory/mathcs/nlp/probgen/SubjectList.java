package edu.emory.mathcs.nlp.probgen;


import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;

import java.io.*;

import java.util.List;
import java.util.ArrayList;




public class SubjectList{
	public void makeSubjectList(InfModel dbpediaInfModel){
		Resource nullS = null;
		Property nullP = null;
        Property type = dbpediaInfModel.getProperty("http://dbpedia.org/ontology/type");
        RDFNode  nullO = null;

        // Get random list of resources, had to use type for memory constraints
		StmtIterator randomList = dbpediaInfModel.listStatements(nullS, type, nullO);
		ArrayList<Statement> towrite = new ArrayList<Statement>();

		int resourceCount = 0;
		int fileindex = 0;

		Resource o = dbpediaInfModel.getResource("http://dbpedia.org/resource/Barack_Obama");
		Property p = dbpediaInfModel.getProperty("http://dbpedia.org/ontology/type");

		while(randomList.hasNext()){
			Statement s = randomList.nextStatement();
			Resource r  = s.getResource();

			//Needs to be a full statement to be added to the model
			Statement justResource = ResourceFactory.createStatement(r, p, o);
			towrite.add(justResource);


			if(towrite.size() > 100){
				try{
					Model writeModel = ModelFactory.createDefaultModel();
					writeModel.add(towrite);

					File file = new File("/home/wkelly3/jena-projects/jena-maven-example/subjectResourceFiles/"+fileindex+".txt");
					FileOutputStream fos = new FileOutputStream(file);

					writeModel.write(fos);

					fos.close();

					//clear arraylist for next 100,000
					towrite.clear();
					fileindex++;
				}
				catch (IOException e){
					System.out.println(e);
				}
			}

			if(fileindex > 9){
				return;
			}
		}
		/*
		ResIterator subjectList = dbpediaInfModel.listSubjects();
		System.out.println("Subject list loaded");

		int resourceCount = 0;
		int fileindex = 0;

		ArrayList<Resource> towrite = new ArrayList<Resource>();

		//because of IO
		try{
			while (subjectList.hasNext()){
				resourceCount++;
				towrite.add(subjectList.next());

				if (resourceCount % 1000 == 0) {
					System.out.println(resourceCount);
				}

				// should be 47 files of 100,000
				if (resourceCount % 100000 == 0){
					// serialize arraylist
					WriteArrayList wal = new WriteArrayList(towrite);

					// write file
					File file = new File("/home/wkelly3/jena-projects/jena-maven-example/subjectResourceFiles/"+fileindex+".txt");

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
		*/
	}
}
