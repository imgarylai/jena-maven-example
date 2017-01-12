package edu.emory.mathcs.nlp.probgen;

import org.apache.jena.rdf.model.*;

import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;

import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;


import static edu.emory.mathcs.nlp.probgen.InferenceModel.dbpediaInfModel;

public class NumericProb {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting!");

        LoadLogger logger = new LoadLogger();
        logger.loadLogger();
        InferenceModel inf = new InferenceModel();
        inf.makeInferenceModel();
        csvWriter();

    }

    /**
     * An example of writing using CsvBeanWriter.
     */
    private static void csvWriter() throws Exception {

        final String[] header = new String[] { "subject", "predicate", "num", "unit" };
        ICsvListWriter listWriter = null;
        try {
            String fileName = System.getProperty("user.dir")+"/data/num.csv";
            listWriter = new CsvListWriter(new FileWriter(fileName),
                    CsvPreference.STANDARD_PREFERENCE);

            // assign a default value for married (if null), and write numberOfKids as an empty column if null
            final CellProcessor[] processors = new CellProcessor[] {new NotNull(), new NotNull(),new NotNull(), new NotNull()};

            // write the header
            listWriter.writeHeader(header);

            // write the beans
            StmtIterator iterator = dbpediaInfModel.listStatements();

            String numCheckPattern = ".+\\^+.+";
            Pattern p = Pattern.compile(numCheckPattern);

            while (iterator.hasNext()) {
                Statement statement = iterator.nextStatement();
                Num statementNum = new Num(statement);
                if (statementNum.checkNumeric(p)) {
                    Resource subject = statement.getSubject();     // get the subject
                    Property predicate = statement.getPredicate();   // get the predicate
                    RDFNode object = statement.getObject();      // get the object
//                    System.out.println(subject);
//                    System.out.println(predicate);
//                    System.out.println(object);
//                    System.out.println(getNum(object));
//                    System.out.println(getUnit(object));
                    final List<Object> stat = Arrays.asList(new Object[] { subject.toString(), predicate.toString(), getNum(object), getUnit(object) });
                    listWriter.write(stat, processors);
                }
            }

        }
        finally {
            if( listWriter != null ) {
                listWriter.close();
            }
        }
    }

    private static String getNum(RDFNode object) {
        String line = object.toString();
        String numPattern = ".*?(?=\\^)";
        Pattern r = Pattern.compile(numPattern);
        Matcher m = r.matcher(line);
        try {
            m.find();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
        return m.group();
    }

    private static String getUnit(RDFNode object) {
        String line = object.toString();
        String numPattern = "[^\\^^]*$";
        Pattern r = Pattern.compile(numPattern);
        Matcher m = r.matcher(line);
        try {
            m.find();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
        return m.group();
    }
}



class Num {
    private Statement s;

    public Num (Statement s) {
        this.s = s;
    }

    public boolean checkNumeric(Pattern p) {
        String line = s.toString();
        Matcher m = p.matcher(line);
        boolean b = m.matches();
        return b;
    }
}
