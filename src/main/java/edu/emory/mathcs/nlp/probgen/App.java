// Note: All classes are in the same file because its easy to work with over sftp,
// 		 will reorganize later

package edu.emory.mathcs.nlp.probgen;
import org.apache.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Random;
import java.io.PrintWriter;
import java.io.IOException;

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
        // Note: The randomList is the same order every time, so can batch jobs to start
        //		 where the last one left off.
        //
        // walks = Unique number of resources to begin walks at
		// walksPerResource = Do this many walks per unique resource
		// maxSteps = Number of steps to take per walk (Step = subject,predicate,object)
		// minSteps = Minimum steps required to save a chain
		// anotherPropertyTries = Retries another random property if next step is not valid
		// txtNum = Naming for text file
		// startWalksAt = Begins walk at resource number;
        //---------------------------------------------------------------------------------
		int walks = 100;
		int walksPerResource = 20;
		int maxSteps = 4;
		int minSteps = 4;
		int anotherPropertyTries = 5;
		int txtNum = 3;
		int startWalksAt = 211;
		//---------------------------------------------------------------------------------

		//Set search criteria
        Resource nullS = null;
        Property type = dbpediaInfModel.getProperty("http://dbpedia.org/ontology/type");
        RDFNode  nullO = null;

        // Get random list of resources, had to use type for memory constraints
        StmtIterator randomList = dbpediaInfModel.listStatements(nullS, type, nullO);


		// Puts randomList into a set to get uniques only
        Set<Resource> startingPoints = new LinkedHashSet<Resource>();
        int counts = 1;
        while(startingPoints.size() < walks){
        	Statement st = randomList.next();
        	if(startWalksAt > counts){
        		counts++;
        		continue;
        	}
        	Resource start = st.getResource();
        	startingPoints.add(start);
        	counts++;
        }

        Walker trial = new Walker(startingPoints, walksPerResource, maxSteps, minSteps, anotherPropertyTries, txtNum);

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

		for(Resource start: resources){
			ResourceObject startingObj = new ResourceObject(start);

			for(int k = 0; k < walksPerResource; k++){
				RandomWalk r = new RandomWalk(startingObj);
				ArrayList<String> printable = new ArrayList<String>();

				String result = "";

				// take steps until dead-end or hit max
				for(int i = 0; i < maxSteps; i++){
					result = r.takeStep(anotherPropertyTries);

					if(result.equals("err")){
						break;
					}
					printable.add(result);
				}

				// erase if less than length of minimum steps,
				// next if will deal with it
				if(printable.size() < minSteps){
					printable.clear();
				}

				// if its an empty walk, don't print it
				if(printable.isEmpty()){
					continue;
				}

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


class InOut{
	Resource core;
	ArrayList<SubjectPredicatePair> incoming;
	ArrayList<PredicateObjectPair> outgoing;

	int incomingLength;
	int outgoingLength;

	public InOut(Resource r){
		this.core = r;

		ArrayList<SubjectPredicatePair> inc = new ArrayList<SubjectPredicatePair>();
		ArrayList<PredicateObjectPair> out = new ArrayList<PredicateObjectPair>();

		inc = getIncoming(core);
		out = getOutgoing(core);

		this.incoming = inc;
		this.outgoing = out;

		this.incomingLength = incoming.size();
		this.outgoingLength = outgoing.size();
	}

	public int getSizeIncoming(){
		return incomingLength;
	}
	public int getSizeOutgoing(){
		return outgoingLength;
	}

	public ArrayList<SubjectPredicatePair> getIncoming(Resource resourceIn){
    	// incoming
        Resource s = null;
        Property p = null;
        StmtIterator oi = dbpediaInfModel.listStatements(s, p, resourceIn);

        ArrayList<SubjectPredicatePair> result = new ArrayList<SubjectPredicatePair>();

        while(oi.hasNext()){
        	Statement statement = oi.nextStatement();
        	Resource  subject   = statement.getSubject();
            Property  predicate = statement.getPredicate();


            if(checkProperty(predicate.toString()) && checkResource(subject.toString())) {
            	SubjectPredicatePair pair = new SubjectPredicatePair(subject, predicate);
            	result.add(pair);
            }
        }
    	return result;
    }


	public ArrayList<PredicateObjectPair> getOutgoing(Resource resourceIn){
    	// outgoing
        Property p = null;
        RDFNode r = null;
        StmtIterator oo = dbpediaInfModel.listStatements(resourceIn, p, r);

        ArrayList<PredicateObjectPair> result = new ArrayList<PredicateObjectPair>();

        while (oo.hasNext()){
            Statement statement = oo.nextStatement();
            Property  predicate = statement.getPredicate();
            RDFNode   object    = statement.getObject();

            if(checkProperty(predicate.toString()) && checkResource(object.toString())) {
            	PredicateObjectPair pair = new PredicateObjectPair(predicate, object.asResource());
            	result.add(pair);
            }
        }
        return result;
    }

    public boolean checkResource(String object) {
        String dbo = "dbpedia.org/resource";
        Pattern dboPattern = Pattern.compile(dbo);
        Matcher dboMatcher = dboPattern.matcher(object);
        return dboMatcher.find();
    }

    public boolean checkProperty(String object) {
        String dbo = "dbpedia.org/ontology";
        Pattern dboPattern = Pattern.compile(dbo);
        Matcher dboMatcher = dboPattern.matcher(object);
        return dboMatcher.find();
    }
}




class PredicateObjectPair{
	Property predicate;
	Resource object;
	public PredicateObjectPair(Property p, Resource r){
		this.predicate = p;
		this.object = r;
	}

	public Property getPredicate(){
		return predicate;
	}
	public Resource getObject(){
		return object;
	}
}

class SubjectPredicatePair{
	Resource subject;
	Property predicate;

	public SubjectPredicatePair(Resource r, Property p){
		this.subject = r;
		this.predicate = p;
	}

	public Resource getSubject(){
		return subject;
	}
	public Property getPredicate(){
		return predicate;
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
		this.propertyList = getResourcePropertyList(r);
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
        StmtIterator rIter = r.listProperties();
        while (rIter.hasNext()){
            Statement statement = rIter.nextStatement();
            //Resource  subject   = statement.getSubject();       // get the subject
            Property  predicate = statement.getPredicate();     // get the predicate
            RDFNode   object    = statement.getObject();        // get the object

            if (checkType(predicate.toString()) && checkDBO(object.toString()) && object instanceof Resource) {
                if(object.isResource()){
                	resourceArray.add(object.asResource());
                }
            }
        }
        ArrayList<Resource> resourceArrayND = removeDuplicateResources(resourceArray);

        return resourceArrayND;
    }

    private ArrayList removeDuplicateResources(ArrayList<Resource> resourceList){
    	Set<Resource> hs = new LinkedHashSet<Resource>();
    	hs.addAll(resourceList);
    	resourceList.clear();
    	resourceList.addAll(hs);
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
