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


        Resource obamaResource = dbpediaInfModel.getResource("http://dbpedia.org/resource/Barack_Obama");
        Resource abeResource   = dbpediaInfModel.getResource("http://dbpedia.org/resource/Abraham_Lincoln");

        ResourceObject obama = new ResourceObject(obamaResource);
        ResourceObject abe   = new ResourceObject(abeResource);

        try{
	        PrintWriter writer = new PrintWriter("randomwalk-output.txt", "UTF-8");


			int maxNumberOfSteps = 4;
			int iterations = 10;

			for(int j = 0; j < iterations; j++){
				// Must restart walk
				RandomWalk r = new RandomWalk(obama);
				String result = "";

				for(int i = 0; i < maxNumberOfSteps; i++){
					result = r.takeStep();
					if(result.equals("err")){ break; }
					writer.print(result);
				}
				writer.println();
			}
			writer.close();

		} catch (IOException e){
			System.out.println(e);
		}


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

	public String takeStep(){
		int tryDifferentProperty = 5;

		//System.out.println("taking step...");
		ResourceObject previous = chain.get(chain.size() - 1);

		Resource previousResource = previous.getOriginalResource();
		Property previousRandomProperty = previous.getRandomProperty();

		// if the previousRandomProperty was null, there are no valid
		// properties to choose from, so walk hit a wall.
		if(previousRandomProperty == null){
			//System.out.println("	last resource has no valid properties...");
			return "err";
		}

		NodeIterator candidates = dbpediaInfModel.listObjectsOfProperty(previousResource, previousRandomProperty);

		ArrayList<Literal> nextCandidateLiteralList = new ArrayList<Literal>();
		ArrayList<Resource> nextCandidateResourceList = new ArrayList<Resource>();

		while(candidates.hasNext()){
			RDFNode node = candidates.next();

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

			//System.out.println("	reroll property, " + previousRandomProperty.toString().replace("http://dbpedia.org/ontology/","") + " didn't link to any resources...");

			// So clear the canditate literals and resources, because
			// we actually have no resources
			nextCandidateLiteralList.clear();
			nextCandidateResourceList.clear();

			// Gets a new 'link' from the previous item on the walk
			previousRandomProperty = previous.getRandomProperty();
			if(previousRandomProperty == null){
				return "err";
			}


			NodeIterator newCandidates = dbpediaInfModel.listObjectsOfProperty(previousResource, previousRandomProperty);
			while(newCandidates.hasNext()){
				RDFNode newNode = newCandidates.next();

				if(newNode.isLiteral()){
					nextCandidateLiteralList.add(newNode.asLiteral());
				}
				if(newNode.isResource()){
					nextCandidateResourceList.add(newNode.asResource());
				}
			}

			attempt++;
			if(attempt > tryDifferentProperty){
				return "err";
			}

		}

		Resource resultResource = nextCandidateResourceList.get(new Random().nextInt(nextCandidateResourceList.size()));

		String out = "<\"" + previousResource.toString().replace("http://dbpedia.org/resource/","") + "\", \"" + previousRandomProperty.toString().replace("http://dbpedia.org/ontology/","") + "\", \"" + resultResource.toString().replace("http://dbpedia.org/resource/","") + "\">";
		System.out.println(out);

		ResourceObject next = new ResourceObject(resultResource);
		chain.add(next);

		// Currently going to only use Resources, soon will use literals
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
