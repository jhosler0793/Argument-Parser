package edu.jsu.mcis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.jsu.mcis.ArgumentParser.Types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** Used to read or write argument information into an ArgumentParser object */
public class XMLManager{
    private static final String ARGUMENT = "argument";
    private static final String NAMED_ARGUMENT = "named";
    private static final String POS_ARGUMENT = "positional";
    private static final String REQUIRED_ARGUMENT = "required";
    private static final String RESTRICTED = "restricted";
    private static final String RESTRICTED_COUNT = "restrictedcount";
    private static final String NAME = "name";
    private static final String GROUP = "group";
    private static final String SHORTHAND = "shorthand";
    private static final String DESCRIPTION = "description";
    private static final String TYPE = "type";
    private static final String DEFAULT = "default";
    private static final String FLAG = "flag";
    private static final String VALUE = "value";
    private static final String COUNT_OF_VALUES = "count_of_values";
    
    /**
     *  Writes all information from the provided ArgumentParser object to a file
     *
     *  @param fileName the name of the file to be written to
     *  @param p the ArgumentParser object to be written
     */
    public static void writeArguments(String fileName, ArgumentParser p){
        try{
            PrintWriter writer = new PrintWriter(fileName);
            List<String> argNames = p.getPositionalArgumentNames();
            List<String> namedArgNames = p.getNamedArgumentNames();
            Map<String, String> namedArgShorthand = p.getNamedArgumentShorthand();
            Map<String, NamedArgument> namedArgMap = p.getNamedArgumentMap();
            Map<String, Argument> positionalArgMap = p.getPositionalArgumentMap();
            
            writer.write("<?xml version=\"1.0\"?>\n");
            writer.write("<arguments>\n");
            
            for(int i = 0; i < argNames.size(); i++){
                writer.write("\t<" + ARGUMENT + " type = \"positional\"" + ">\n");
                writer.write("\t\t<" + NAME + ">" + argNames.get(i) + "</" + NAME + ">\n");
                writer.write("\t\t<" + DESCRIPTION + ">" + p.getArgumentDescription(argNames.get(i)) 
                                + "</" + DESCRIPTION + ">\n");
                writer.write("\t\t<" + TYPE + ">" + p.getArgumentTypeAsString(argNames.get(i))
                                + "</" + TYPE + ">\n");                
                if(positionalArgMap.get(argNames.get(i)).containsRestrictedValues()){
                    writer.write("\t\t<" + RESTRICTED_COUNT + ">" + p.getNumberOfRestrictedArguments(argNames.get(i)) + "</" + RESTRICTED_COUNT + ">\n");
                    Object[] objArr = new Object[positionalArgMap.get(argNames.get(i)).numOfRestrictedValues()];
                    for(int j = 0; j < objArr.length; j++){
                        writer.write("\t\t<" + RESTRICTED + ">" + p.getRestrictedValue(argNames.get(i), j) + "</" + RESTRICTED + ">\n");
                    }
                }
                writer.write("\t</" + ARGUMENT + ">\n");
                writer.write("\n");
            }
            
            for(int i = 0; i<namedArgNames.size(); i++){
                if(namedArgMap.get(namedArgNames.get(i)).isThisRequired()){
                    writer.write("\t<" + ARGUMENT +  " type = \"required\"" + ">\n");
                }else{
                    writer.write("\t<" + ARGUMENT +  " type = \"named\"" + ">\n");
                }
                writer.write("\t\t<" + NAME + ">" + namedArgNames.get(i) + "</" + NAME + ">\n");
                if(namedArgMap.get(namedArgNames.get(i)).isInAGroup()){
                    writer.write("\t\t<" + GROUP + ">" + p.getArgumentGroup(namedArgNames.get(i)) + "</" + GROUP + ">\n");
                }
                if(namedArgMap.get(namedArgNames.get(i)).isArgumentShorthand()){
                        writer.write("\t\t<" + SHORTHAND + ">" + namedArgShorthand.get(namedArgNames.get(i)) + "</" + SHORTHAND + ">\n");
                }
                writer.write("\t\t<" + DESCRIPTION + ">" + p.getArgumentDescription(namedArgNames.get(i))
                                + "</" + DESCRIPTION + ">\n");
                writer.write("\t\t<" + TYPE + ">" + p.getArgumentTypeAsString(namedArgNames.get(i))
                                + "</" + TYPE + ">\n");                
                if(namedArgMap.get(namedArgNames.get(i)).containsRestrictedValues()){
                    writer.write("\t\t<" + RESTRICTED_COUNT + ">" + p.getNumberOfRestrictedArguments(namedArgNames.get(i)) + "</" + RESTRICTED_COUNT + ">\n");
                    Object[] objArr = new Object[namedArgMap.get(namedArgNames.get(i)).numOfRestrictedValues()];
                    for(int j = 0; j < objArr.length; j++){
                        writer.write("\t\t<" + RESTRICTED + ">" + p.getRestrictedValue(namedArgNames.get(i), j) + "</" + RESTRICTED + ">\n");
                    }
                }
                writer.write("\t\t<" + COUNT_OF_VALUES + ">" + p.getNumberOfAdditionalValues(namedArgNames.get(i))
                                + "</" + COUNT_OF_VALUES + ">\n"); 
                writer.write("\t\t<" + DEFAULT + ">" + p.getDefaultValueOf(namedArgNames.get(i))
                                + "</" + DEFAULT + ">\n");                    
                writer.write("\t</" + ARGUMENT + ">\n");
                writer.write("\n");
            }            
            writer.write("</arguments>");
            writer.close();            
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
    }
    
    /**
     *  Loads information from a file to the provided ArgumentParser object
     *
     *  @param fileName the name of the file to be read from
     *  @param p the ArgumentParser object to be loaded to
     */
    @SuppressWarnings("unchecked")
    public static void loadArguments(String fileName, ArgumentParser p){
        try{
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream(new File(fileName));
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            
            String name = "";
            boolean isInGroup = false;
            String groupName = "";
            String description = "";
            Types type = Types.valueOf("STRING");
            String value = "";
            boolean hasRestrictedValues = false;
            Object[] restrictedValues = new Object[1];
            int currentRestrictedValue = 0;
            String argumentType = "";
            int numOfValues = 0;
                
            System.out.println("\n");
            
            while(eventReader.hasNext()){
                XMLEvent event = eventReader.nextEvent();
                Argument arg;
                NamedArgument optArg;
                
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    if (startElement.getName().getLocalPart().equals(NAME)) {
                        event = eventReader.nextEvent();
                        name = event.asCharacters().getData();
                        continue;
                    }
                    if (startElement.getName().getLocalPart().equals(ARGUMENT)){
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while(attributes.hasNext()){
                            Attribute attribute = attributes.next();
                            if(attribute.getName().toString().equals(TYPE)){
                                event = eventReader.nextEvent();
                                argumentType = attribute.getValue();
                            }
                        }
                        continue;
                        
                    }
                    if (startElement.getName().getLocalPart().equals(GROUP)) {
                        event = eventReader.nextEvent();
                        groupName = event.asCharacters().getData();
                        isInGroup = true;
                        continue;
                    }
                    if (startElement.getName().getLocalPart().equals(DESCRIPTION)) {
                        event = eventReader.nextEvent();
                        description = event.asCharacters().getData();
                        continue;
                    }
                    if (startElement.getName().getLocalPart().equals(TYPE)) {
                        event = eventReader.nextEvent();
                        type = Types.valueOf(event.asCharacters().getData());
                        continue;
                    }
                    if (startElement.getName().getLocalPart().equals(DEFAULT)) {
                        event = eventReader.nextEvent();
                        value = event.asCharacters().getData();
                        continue;
                    }
                    if (startElement.getName().getLocalPart().equals(RESTRICTED_COUNT)){
                        event = eventReader.nextEvent();
                        restrictedValues = new Object[Integer.parseInt(event.asCharacters().getData())];
                        hasRestrictedValues = true;
                        continue;
                    }
                    if (startElement.getName().getLocalPart().equals(RESTRICTED)){                        
                        event = eventReader.nextEvent();
                        restrictedValues[currentRestrictedValue] = event.asCharacters().getData();
                        currentRestrictedValue++;
                        continue;
                    }
                    if (startElement.getName().getLocalPart().equals(COUNT_OF_VALUES)){                        
                        event = eventReader.nextEvent();
                        numOfValues = Integer.valueOf(event.asCharacters().getData());
                        continue;
                    }
                }
                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(ARGUMENT)) {
                        if(argumentType.equals(POS_ARGUMENT)){
                            p.addPositionalArgument(name, description, type);
                        } else if (argumentType.equals(NAMED_ARGUMENT)){
                            p.addNamedArgument(name, description, type, value);
                        } else if (argumentType.equals(REQUIRED_ARGUMENT)){
                            p.addRequiredNamedArgument(name, description, type, value);
                        }
                        if(isInGroup){
                            p.addArgumentToGroup(name, groupName);
                        }
                        if(hasRestrictedValues){
                            p.setRestrictedValues(name, restrictedValues);
                        }
                        if(numOfValues > 1){
                            p.readyAdditionalValues(name, numOfValues);
                        }
                        name = "";
                        isInGroup = false;
                        groupName = "";
                        description = "";
                        type = Types.valueOf("STRING");
                        value = "";
                        restrictedValues = new Object[1];
                        hasRestrictedValues = false;
                        currentRestrictedValue = 0;
                    }
                    
                }
            }
            
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(XMLStreamException e){
            e.printStackTrace();
        }
        
    }
}