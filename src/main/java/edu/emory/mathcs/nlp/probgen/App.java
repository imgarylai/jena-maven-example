// Note: All classes are in the same file because its easy to work with over sftp,
// 		 will reorganize later

package edu.emory.mathcs.nlp.probgen;

import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Random;
import java.util.*;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.*;

import static edu.emory.mathcs.nlp.probgen.InferenceModel.dbpediaInfModel;

public class App {

	public static void main(String[] args) {
		//System.out.println("Starting!");


		LoadLogger logger = new LoadLogger();
		logger.loadLogger();
		InferenceModel inf = new InferenceModel();
		inf.makeInferenceModel();


		// Walk parameters
		//---------------------------------------------------------------------------------
		int walks = 10;					//Unique number of resources to begin walks at
		int walksPerResource = 10;		//Do this many walks per unique resource
		int maxSteps = 4;				//Number of steps to take per walk (Step = subject,predicate,object)
		int minSteps = 4;				//Minimum steps required to save a chain
		int anotherPropertyTries = 5;	//Retries another random property if next step is not valid
		int txtNum = 3;					//Naming for text file
		int startWalksAt = 2;			//Begins walk at resource number __ in the subject list
		//---------------------------------------------------------------------------------

		//SubjectList writer = new SubjectList();
		//writer.makeSubjectList(dbpediaInfModel);

		SubjectList reader = new SubjectList();

		int fileindex = 0;
		ArrayList<Resource> zero = reader.loadSubjectList(fileindex);

		ConnectionsList cl = new ConnectionsList();
		cl.makeConnectionsList(dbpediaInfModel, zero, fileindex);
		System.out.println("Finished connections list call");

		/*
		HashMap incoming = new HashMap();
		HashMap outgoing = new HashMap();


		int count = 0;
		for(Resource x: zero){
			InOut x_io = new InOut(x);


			Model inWriteModel = ModelFactory.createDefaultModel();
			Model outWriteModel = ModelFactory.createDefaultModel();

			inWriteModel.add(x_io.getIncoming());
			outWriteModel.add(x_io.getOutgoing());

			incoming.put(x.toString(), inWriteModel);
			outgoing.put(x.toString(), outWriteModel);

			count++;

			if(count > 1){
				break;
			}
		}

		//Save incoming file
		try{
			File fileIncoming = new File("/home/wkelly3/jena-projects/jena-maven-example/subjectInOutHashes/"+fileindex+"-incoming.txt");

			FileOutputStream fos = new FileOutputStream(fileIncoming);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(incoming);
			oos.close();
			fos.close();
			System.out.println("Incoming map for "+ fileindex + " saved");
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}

		//Save outgoing file
		try{
			File fileOutgoing = new File("/home/wkelly3/jena-projects/jena-maven-example/subjectInOutHashes/"+fileindex+"-outgoing.txt");

			FileOutputStream fos = new FileOutputStream(fileOutgoing);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(incoming);
			oos.close();
			fos.close();
			System.out.println("Outgoing map for "+ fileindex + " saved");
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
		*/

		//Printing for hashmap
		/*
		Set incSet = incoming.entrySet();
		Iterator incIter = incSet.iterator();
		while(incIter.hasNext()){
			Map.Entry me = (Map.Entry) incIter.next();
			System.out.println(me.getKey()+ ": ");
			System.out.println(me.getValue());
		}
		*/


		//Need to create another program to do the Importance function,
		// based off the InOut code obviously.


		//Walker trial = new Walker(startingPoints, walksPerResource, maxSteps, minSteps, anotherPropertyTries, txtNum);

		/*
			Need to change Walker to be compatible with InOut
				1. Not just print
				2. Use the last resource of the random walk as in/out
		*/

		/*
			For the cloze
				1. Randomly pick X(4) of the same type
					eg. <usa, state, ky>
						1. tn
						2. hi
						3. mi
						4. vt

				2. Then for each of the X(4) 'candidates' (including answer?)
						1. get both top Y(5) 'pairs' (see classes)
							* Top determined by 'importance function'
							  where important == number of incoming &
							  outgoing connections

		*/

	}
}



class Walker{
	Set<String> toPrint;

	public Walker(Set<Resource> resources, int walksPerResource, int maxSteps, int minSteps, int anotherPropertyTries, int txtNum){
		Set<String> tp = new LinkedHashSet<String>();
		this.toPrint = tp;

		for(Resource start : resources){
			System.out.println("Start RESOURCEOBJ");
			ResourceObject startingObj = new ResourceObject(start);
			System.out.println("End RESOURCEOBJ");

			for(int k = 0; k < walksPerResource; k++){
				RandomWalk r = new RandomWalk(startingObj);
				System.out.println("End RANDOMWALK");

				ArrayList<String> printable = new ArrayList<String>();

				String result = "";

				// take steps until dead-end or hit max
				for(int i = 0; i < maxSteps; i++){
					result = r.takeStep(anotherPropertyTries);

					if(result.equals("err")){
						break;
					}
					printable.add(result);
					System.out.println(result);
				}

				// erase if less than length of minimum steps,
				// next if will deal with it
				if(printable.size() < minSteps){
					printable.clear();
				}

				System.out.println("One walk completed");

				printOneWalk(printable);
			}
		}
		// This is going to have to probably be altered once there is a big enough list
		writeToFile(txtNum);

	}

	// This is going to have to probably be altered once there is a big enough list
	public void writeToFile(int txtNum){
		try{
			// designates output file
			PrintWriter writer = new PrintWriter("randomwalk-output" + txtNum + ".txt", "UTF-8");

			for(String s: toPrint){
				writer.println(s);
			}
			writer.close();

		} catch (IOException e){
			System.out.println(e);
		}

		System.out.println("Printed " + toPrint.size() + " lines");
	}

	public void printOneWalk(ArrayList<String> printable){
		String wlk = "";
		for(String s: printable){
			wlk = wlk + s;
		}
		toPrint.add(wlk);
	}
}


class RandomWalk{
	ResourceObject seed;
	ArrayList<ResourceObject> chain;

	public RandomWalk(ResourceObject ro){
		ArrayList<ResourceObject> chain = new ArrayList<ResourceObject>();
		this.seed = ro;
		chain.add(ro);
		this.chain = chain;
	}

	public String takeStep(int tryAnotherProperty){
		ResourceObject previous = chain.get(chain.size() - 1);
		Resource previousResource = previous.getOriginalResource();

		ArrayList<Literal> nextCandidateLiteralList = new ArrayList<Literal>();
		ArrayList<Resource> nextCandidateResourceList = new ArrayList<Resource>();

		Property previousRandomProperty = previous.getRandomProperty();


		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		//	Get resource list
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

		// if the previousRandomProperty was null, there are no valid
		// properties to choose from, so walk hit a wall.
		if(previousRandomProperty == null){
			return "err";
		}

		NodeIterator candidates = dbpediaInfModel.listObjectsOfProperty(previousResource, previousRandomProperty);

		while(candidates.hasNext()){
			RDFNode node = candidates.next();

			// splits Objects into either literals or resources
			if(node.isLiteral()){
				nextCandidateLiteralList.add(node.asLiteral());
			}
			if(node.isResource()){
				nextCandidateResourceList.add(node.asResource());
			}
		}

		int attempt = 0;
		while(nextCandidateResourceList.isEmpty()){
			// the candidate resource list is probably empty because
			// for the verb chosen, the answer was a literal

			// So clear the canditate literals and resources, because
			// we actually have no resources
			nextCandidateLiteralList.clear();
			nextCandidateResourceList.clear();

			// Gets a new path from the previous item on the walk
			previousRandomProperty = previous.getRandomProperty();

			//if no valid properties to choose from, unable to continue
			if(previousRandomProperty == null){
				return "err";
			}

			NodeIterator newCandidates = dbpediaInfModel.listObjectsOfProperty(previousResource, previousRandomProperty);

			while(newCandidates.hasNext()){
				RDFNode newNode = newCandidates.next();

				// splits Objects into either literals or resources
				if(newNode.isLiteral()){
					nextCandidateLiteralList.add(newNode.asLiteral());
				}
				if(newNode.isResource()){
					nextCandidateResourceList.add(newNode.asResource());
				}
			}

			// set number of attempts, may hit inf loop
			attempt++;
			if(attempt > tryAnotherProperty){
				return "err";
			}
		}
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

		Resource resultResource = nextCandidateResourceList.get(new Random().nextInt(nextCandidateResourceList.size()));

		String out = "<\"" + previousResource.toString().replace("http://dbpedia.org/resource/","") + "\", \"" + previousRandomProperty.toString().replace("http://dbpedia.org/ontology/","") + "\", \"" + resultResource.toString().replace("http://dbpedia.org/resource/","") + "\">";

		ResourceObject next = new ResourceObject(resultResource);
		chain.add(next);

		return out;
	}
}


class ResourceObject{
	Resource resource;
	ArrayList<Resource> typeList;
	ArrayList<Property> propertyList;

	public ResourceObject(Resource r){
		this.resource = r;
		this.typeList = getResourceTypeList(r);
		System.out.println("End GETRESOURCETYPELIST R");
		this.propertyList = getResourcePropertyList(r);
		System.out.println("End GETRESOURCEPROPLIST R");

	}

	public Resource getOriginalResource(){
		return resource;
	}
	public ArrayList<Resource> getResourceTypeList(){
		return typeList;
	}
	public ArrayList<Property> getPropertyList(){
		return propertyList;
	}

	// Evenly distributed??? With removed duplicated may
	// bias toward those without since duplicates were pruned
	public Resource getRandomTypeResource(){
		if(typeList.isEmpty()){
			return null;
		}

		Resource randomTypeResource = typeList.get(new Random().nextInt(typeList.size()));
		return randomTypeResource;
	}
	public Property getRandomProperty(){
		if(propertyList.isEmpty()){
			return null;
		}

		Property randomProperty = propertyList.get(new Random().nextInt(propertyList.size()));
		return randomProperty;
	}


	public void printResourceTypeList(){
		System.out.println(resource.toString() + " types:");
		for(Resource r : typeList){
			System.out.println("	" + r);
		}
	}
	public void printPropertyList(){
		System.out.println(resource.toString() + " properties:");
		for (Property p: propertyList) {
			System.out.println("	" + p);
		}
	}
	public void printBoth(){
		printResourceTypeList();
		System.out.println();
		printPropertyList();
		System.out.println();
		System.out.println();
	}


	// Resources
	private ArrayList getResourceTypeList(Resource r) {
		ArrayList<Resource> resourceArray = new ArrayList<Resource>();
		System.out.println(r.toString());
		System.out.println("Start LISTPROPS");
		List<Statement> statements = r.listProperties().toList();
		System.out.println("End LISTPROPS");

		for (Statement statement : statements) {
			// System.out.println("Start statement");

			// Statement statement = rIter.nextStatement();
			// System.out.println("End statement");

			//Resource  subject   = statement.getSubject();       // get the subject
			Property  predicate = statement.getPredicate();     // get the predicate
			System.out.println("End predicate");

			RDFNode   object    = statement.getObject();        // get the object
			System.out.println("End object");
			/*
			if (object.isResource() && predicate.contains("#type") && object.contains("dbpedia.org/ontology")) {


			if (object.isResource() && checkType(predicate.toString()) && checkDBO(object.toString())) {
				Resource resourceObject = object.asResource();
				System.out.println("End resourceObject");
				resourceArray.add(resourceObject);
				System.out.println("End Add");
			}
			*/
		}
		System.out.println("End Loop");
		ArrayList<Resource> resourceArrayND = removeDuplicateResources(resourceArray);
		System.out.println("End removeDuplicateResources");

		return resourceArrayND;
	}

	private ArrayList removeDuplicateResources(ArrayList<Resource> resourceList){
		System.out.println("Size " + resourceList.size());
		Set<Resource> hs = new LinkedHashSet<Resource>();
		System.out.println("End LinkedHashSet");
		hs.addAll(resourceList);
		System.out.println("End hs.addAll");
		resourceList.clear();
		System.out.println("End clear");
		resourceList.addAll(hs);
		System.out.println("End resourceList.addAll");
		return resourceList;
	}

	// Properties
	private ArrayList getResourcePropertyList(Resource r) {
		ArrayList<Property> propertyArray = new ArrayList<Property>();
		StmtIterator oIter = r.listProperties();
		while (oIter.hasNext()){
			Statement statement = oIter.nextStatement();
			Property  predicate = statement.getPredicate();     // get the predicate

			if (checkDBO(predicate.toString())) {
				propertyArray.add(predicate);
			}
		}
		ArrayList<Property> propertyArrayND = removeDuplicateProperties(propertyArray);

		return propertyArrayND;
	}

	private ArrayList removeDuplicateProperties(ArrayList<Property> propertyList){
		Set<Property> hs = new LinkedHashSet<Property>();
		hs.addAll(propertyList);
		propertyList.clear();
		propertyList.addAll(hs);
		return propertyList;
	}

	// Checkers
	public static boolean checkType(String predicate) {
		String type = "#type";
		Pattern typePattern = Pattern.compile(type);
		Matcher typeMatcher = typePattern.matcher(predicate);
		return typeMatcher.find();
	}

	public static boolean checkDBO(String object) {
		String dbo = "dbpedia.org/ontology";
		Pattern dboPattern = Pattern.compile(dbo);
		Matcher dboMatcher = dboPattern.matcher(object);
		return dboMatcher.find();
	}
}