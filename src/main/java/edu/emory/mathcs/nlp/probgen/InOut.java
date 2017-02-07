package edu.emory.mathcs.nlp.probgen;


import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;
import java.util.ArrayList;

import static edu.emory.mathcs.nlp.probgen.InferenceModel.dbpediaInfModel;

public class InOut{
	Resource core;
	ArrayList<Statement> incoming;
	ArrayList<Statement> outgoing;

	int incomingLength;
	int outgoingLength;

	public InOut(Resource r){
		long startTime = System.currentTimeMillis();
		this.core = r;

		ArrayList<Statement> inc = new ArrayList<Statement>();
		ArrayList<Statement> out = new ArrayList<Statement>();

		// This is like all of the time.
		inc = makeIncoming(core);
		long incomingTime = System.currentTimeMillis();
		System.out.println("Time for make incoming: " + (incomingTime - startTime) );

		out = makeOutgoing(core);
		long makeOutgoing = System.currentTimeMillis();
		System.out.println("Time for make outgoing: " + (makeOutgoing - incomingTime) );


		this.incoming = inc;
		this.outgoing = out;

		this.incomingLength = incoming.size();
		this.outgoingLength = outgoing.size();
		long endTime = System.currentTimeMillis();;
		System.out.println("Time for full: " + (endTime - startTime) );
	}

	public int getSizeIncoming(){
		return incomingLength;
	}
	public int getSizeOutgoing(){
		return outgoingLength;
	}

	public ArrayList<Statement> getIncoming(){
		return incoming;
	}
	public ArrayList<Statement> getOutgoing(){
		return outgoing;
	}

	private ArrayList<Statement> makeIncoming(Resource resourceIn){
		// incoming

		Resource s = null;
		Property p = null;

		ArrayList<Statement> result = new ArrayList<Statement>();

		//System.out.println(resourceIn);

		StmtIterator oi = dbpediaInfModel.listStatements(s, p, resourceIn);
		int count = 0;
		while(oi.hasNext()){
			Statement statement = oi.nextStatement();
			Resource  subject   = statement.getSubject();
			Property  predicate = statement.getPredicate();


			if(checkProperty(predicate.toString()) && checkResource(subject.toString())) {
				result.add(statement);
			}
			count++;
		}
		System.out.println("loops made incoming: " + count);

		return result;
	}


	private ArrayList<Statement> makeOutgoing(Resource resourceIn){
		// outgoing
		Property p = null;
		RDFNode r = null;


		ArrayList<Statement> result = new ArrayList<Statement>();

		//System.out.println(resourceIn);
		long startTime = System.currentTimeMillis();

		// is the iterator getting to many candidates?
		//
		StmtIterator oo = dbpediaInfModel.listStatements(resourceIn, p, r);

		int count = 0;

		// This is the culprit!
		// either there are too many items to be looped over, or the check regex is way to slow
		// probably can trim down a bit.

		//Why is incoming taking so much less time?
		while (oo.hasNext()){
			Statement statement = oo.nextStatement();
			Property  predicate = statement.getPredicate();
			RDFNode   object    = statement.getObject();

			if(checkProperty(predicate.toString()) && checkResource(object.toString())) {
				result.add(statement);
			}
			count++;

		}
		System.out.println("loops made outgoing: " + count);
		long loopTime = System.currentTimeMillis();
		System.out.println("Time for make outgoing loop: " + (loopTime - startTime) );

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
