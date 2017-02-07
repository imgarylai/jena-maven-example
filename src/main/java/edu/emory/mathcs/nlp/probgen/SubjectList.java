package edu.emory.mathcs.nlp.probgen;


import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node;

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
				writeSubjectsOut(towrite, fileindex);

				//clear arraylist for next 100,000
				towrite.clear();
				fileindex++;
			}
		}
		// the last one that will be less than 100,000
		writeSubjectsOut(towrite, fileindex);
	}

	private void writeSubjectsOut(ArrayList<Statement> towrite, int fileindex){
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


	public ArrayList<Resource> loadSubjectList(int fileindex){

		ArrayList<Resource> output = new ArrayList<Resource>();
 		try{
 			File file = new File("/home/wkelly3/jena-projects/jena-maven-example/subjectResourceFiles/"+fileindex+".txt");
			FileInputStream fis = new FileInputStream(file);

			Model readModel = ModelFactory.createDefaultModel();

			String n = null;
			readModel.read(fis, n);

			ResIterator subjectList = readModel.listSubjects();

			int count = 0;
			while(subjectList.hasNext()){
				Resource r = subjectList.nextResource();
				output.add(r);
				count++;

				/*
				if(count % 1000 == 0){
					System.out.println(r.toString());
				}
				*/
			}
			System.out.println(count + " subjects loaded from " + fileindex + ".txt");
		}

		catch (Exception e){
			System.out.println(e);
		}

		return output;
	}
}



class ConnectionsList{
	public void makeConnectionsList(InfModel dbpediaInfModel, ArrayList<Resource> subjects, int bigFileIndex){

		//Resource o = dbpediaInfModel.getResource("http://dbpedia.org/resource/Barack_Obama");
		//Property p = dbpediaInfModel.getProperty("http://dbpedia.org/ontology/type");
		System.out.println("Start makeConnectionsList!");

		long startTime = System.currentTimeMillis();
		long endTime;

		Property pIn = dbpediaInfModel.createProperty("http://dbpedia.org/ontology/",  "incomingConnections");
		Property pOut = dbpediaInfModel.createProperty("http://dbpedia.org/ontology/", "outgoingConnections");

		ArrayList<Statement> toWriteIn = new ArrayList<Statement>();
		ArrayList<Statement> toWriteOut = new ArrayList<Statement>();

		int localResourceCount = 0;
		int globalResourceCount = 0;

		int fileindex = 0;

		for(Resource r : subjects){
			//long innerStartTime = System.currentTimeMillis();
			System.out.println(localResourceCount);
			InOut rIO = new InOut(r);
			//long inoutTime = System.currentTimeMillis();
			//System.out.println("Time to get InOut for Resource: " + (inoutTime - innerStartTime) );


			// This all takes 1 or less milliseconds
			String incomingConnections = Integer.toString(rIO.getSizeIncoming());
			String outgoingConnections = Integer.toString(rIO.getSizeOutgoing());

			Literal iCL = ResourceFactory.createPlainLiteral(incomingConnections);
			Literal oCL = ResourceFactory.createPlainLiteral(outgoingConnections);

			Statement incomingStatement = ResourceFactory.createStatement(r,  pIn, iCL);
			Statement outgoingStatement = ResourceFactory.createStatement(r, pOut, oCL);

			toWriteIn.add(incomingStatement);
			toWriteOut.add(outgoingStatement);

			localResourceCount++;
			globalResourceCount++;


			if(globalResourceCount % 100 == 0){
				System.out.println("" + globalResourceCount + "recorded!");
			}

			if(localResourceCount > 100){ // was 2500
				writeConnectionsOut(toWriteIn, bigFileIndex, fileindex, "in");
				writeConnectionsOut(toWriteOut, bigFileIndex, fileindex, "out");

				System.out.println("Wrote in and out for file number " + fileindex + "!");

				//clear arraylist for next 100,000
				toWriteIn.clear();
				toWriteOut.clear();

				localResourceCount = 0;
				fileindex++;

				endTime = System.currentTimeMillis();
				System.out.println("Total for full loop time: " + (endTime - startTime) );
				break;
			}

		}
		// the last one that will be less than 100,000
		writeConnectionsOut(toWriteIn, bigFileIndex, fileindex, "in");
		writeConnectionsOut(toWriteOut, bigFileIndex, fileindex, "out");
	}


	private void writeConnectionsOut(ArrayList<Statement> towrite, int bigFileIndex, int fileindex, String inOrOut){
		try{
			Model writeModel = ModelFactory.createDefaultModel();
			writeModel.add(towrite);

			File file = new File("/home/wkelly3/jena-projects/jena-maven-example/inOutStatementFiles/" + bigFileIndex + "/" + fileindex + inOrOut + ".txt");
			FileOutputStream fos = new FileOutputStream(file);

			writeModel.write(fos);

			fos.close();
		}
		catch (IOException e){
			System.out.println(e);
		}
	}


}