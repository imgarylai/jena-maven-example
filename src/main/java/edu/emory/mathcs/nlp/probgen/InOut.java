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
		this.core = r;

		ArrayList<Statement> inc = new ArrayList<Statement>();
		ArrayList<Statement> out = new ArrayList<Statement>();

		inc = makeIncoming(core);
		out = makeOutgoing(core);

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

		while(oi.hasNext()){
			Statement statement = oi.nextStatement();
			Resource  subject   = statement.getSubject();
			Property  predicate = statement.getPredicate();


			if(checkProperty(predicate.toString()) && checkResource(subject.toString())) {
				result.add(statement);
			}
		}

		return result;
	}


	private ArrayList<Statement> makeOutgoing(Resource resourceIn){
		// outgoing
		Property p = null;
		RDFNode r = null;


		ArrayList<Statement> result = new ArrayList<Statement>();

		//System.out.println(resourceIn);

		StmtIterator oo = dbpediaInfModel.listStatements(resourceIn, p, r);

		while (oo.hasNext()){
			Statement statement = oo.nextStatement();
			Property  predicate = statement.getPredicate();
			RDFNode   object    = statement.getObject();

			if(checkProperty(predicate.toString()) && checkResource(object.toString())) {
				result.add(statement);
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
