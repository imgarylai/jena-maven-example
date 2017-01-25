package edu.emory.mathcs.nlp.probgen;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import java.util.List;
import java.io.PrintWriter;
import java.io.IOException;

public class SubjectList{
	public static void makeSubjectList(InfModel dbpediaInfModel){
		ResIterator randomList = dbpediaInfModel.listSubjects();

		int resourceCount = 0;
		try{
			// designates output file
			PrintWriter writer = new PrintWriter("all-resources.txt", "UTF-8");


			while (randomList.hasNext()){
				resourceCount++;
				writer.println(randomList.next().toString());
				if (resourceCount % 1000 == 0) {
					System.out.println(resourceCount);
				}
			}
			writer.close();

		} catch (IOException e){
			System.out.println(e);
		}

		System.out.println(resourceCount);
	}
}