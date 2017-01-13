package edu.emory.mathcs.nlp.probgen;

import org.apache.log4j.PropertyConfigurator;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by gary on 10/26/16.
 */
public class LoadLogger {
    public static void loadLogger() {
        final String log4jFile = "/src/main/resources/edu/emory/mathcs/nlp/probgen/log4j.properties";
        try {
            //System.out.println("log4j loaded successfully");
            Properties props = new Properties();
            props.load(new FileInputStream(log4jFile));
            PropertyConfigurator.configure(props);
        } catch (Exception e) {
             //System.out.println("Error loading log4j");
        }
    }
}
