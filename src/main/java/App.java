import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.apache.log4j.PropertyConfigurator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.RDF;
import java.io.FileInputStream;
import java.util.Properties;


public class App {

  public static void main(String[] args) {
    System.out.println("Starting!");

    try {
      Properties props = new Properties();
      props.load(new FileInputStream("log4j.properties"));
      PropertyConfigurator.configure(props);
    } catch (Exception e) {

    }

    String schemaDirectory = "Database/dbpedia_2014";
    Dataset schemaDataset = TDBFactory.createDataset(schemaDirectory);
    Model dbpediaSchema = schemaDataset.getDefaultModel() ;

    String typeDirectory = "Database/instance_types_en";
    Dataset typeDataset = TDBFactory.createDataset(typeDirectory);
    Model typeData = typeDataset.getDefaultModel();

    String factDirectory = "Database/mappingbased_properties_en";
    Dataset factDataset = TDBFactory.createDataset(factDirectory);
    Model factData = factDataset.getDefaultModel();

    String numDirectory = "Database/specific_mappingbased_properties_en";
    Dataset numDataset = TDBFactory.createDataset(numDirectory);
    Model numData = numDataset.getDefaultModel();

    Model combinedDataModel = ModelFactory.createUnion(factData, numData);
    Model dbpediaBaseModel = ModelFactory.createUnion(combinedDataModel, typeData);


    //In documentation:
    // Model add(Model m)
    // Add all the statements in another model

    //Validate the model
    /*
    /System.out.println("dbpediaModel");
    validateModel(typeModel);
    System.out.println();
    */


    //Builds type based model
    //Reasoner typeReasoner = ReasonerRegistry.getRDFSReasoner();
    //typeReasoner = typeReasoner.bindSchema(schema);
    //InfModel typeModel = ModelFactory.createInfModel(typeReasoner, typeData);

    //Builds factual based model
    //Reasoner factReasoner = ReasonerRegistry.getRDFSReasoner();
    //factReasoner = factReasoner.bindSchema(schema);
    //InfModel factModel = ModelFactory.createInfModel(factReasoner, factData);

    //Build combined model
    Reasoner dbpediaReasoner = ReasonerRegistry.getRDFSReasoner();
    dbpediaReasoner = dbpediaReasoner.bindSchema(dbpediaSchema);
    InfModel dbpediaInfModel = ModelFactory.createInfModel(dbpediaReasoner, dbpediaBaseModel);



    //Two resource types needed. Keep note of resource-ontology diff
    Resource abe = dbpediaInfModel.getResource("http://dbpedia.org/resource/Abraham_Lincoln");
    Resource person = dbpediaInfModel.getResource("http://dbpedia.org/ontology/Person");
    Property birthPlace = dbpediaInfModel.getProperty("http://dbpedia.org/ontology/birthPlace");
    Resource abe_home;
    Property country = dbpediaInfModel.getProperty("http://dbpedia.org/ontology/country");
    // If Abraham Lincoln is type Person -> Is Abraham Lincoln a person?


    if (dbpediaInfModel.contains(abe, RDF.type, person)) {
      System.out.println("Abraham Lincoln is a person.");

      if (dbpediaInfModel.contains(abe, birthPlace, (RDFNode) null)){
        NodeIterator born = dbpediaInfModel.listObjectsOfProperty(abe, birthPlace);
        System.out.println("Abraham Lincoln has a birthPlace:");

        if(born.hasNext()){
          RDFNode abe_birthplace = born.next();
          abe_home = abe_birthplace.asResource();
          System.out.println(abe_home.getURI());
          NodeIterator countryIn = dbpediaInfModel.listObjectsOfProperty(abe_home, country);

          if(countryIn.hasNext()){
            RDFNode abeHomeCountry = countryIn.next();
            Resource abeCountry = abeHomeCountry.asResource();
            System.out.println("Abraham Lincoln home country:");
            System.out.println(abeCountry.getURI());
          }
        }
        else{
          System.out.println("no place.");
        }
      }
    }
    else {
      System.out.println("Abraham Lincoln was failed to be recognized as a person");
    }

    System.out.println("done");
  }

  /*
  public static void validateModel(InfModel m){
    ValidityReport validity = m.validate();
    if (validity.isValid()) {
        System.out.println("OK, valid.");
        return;
    } else {
        System.out.println("Conflicts");
        for (Iterator i = validity.getReports(); i.hasNext();){
            System.out.println(" - " + i.next());
        }
        return;
    }
  }

  public static void printStatements(Model m, Resource s, Property p, Resource o) {
    for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
        Statement stmt = i.nextStatement();
        System.out.println(" - " + PrintUtil.print(stmt));
    }
  }
  */
}
