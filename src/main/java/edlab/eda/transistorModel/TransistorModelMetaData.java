package edlab.eda.transistorModel;

import org.datavec.api.records.Record;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.writable.Writable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransistorModelMetaData {
    private String library;
    private String cell;
    private String[] technology;
    private String[] terminals;
    private String model;
    private String modelName;
    private String[] valueLabels;
    private String[] valueUnits;
    private String[] valueRange;
    private String[] numInputs;
    private String[] constantLabels;
    private String[] constantValues;
    private String[] constantUnits;
    private String[] modelFileNames;
    private String[] modelFileSections;

    public TransistorModelMetaData(String filePath,boolean isCSV) throws IOException, InterruptedException {
        if(isCSV){

            //Set reader for taking data parameters (name, transforms, batch size etc)
            FileSplit inputSplitInfo = new FileSplit(new File(filePath));
            RecordReader recordReaderInfo = new CSVRecordReader(0);
            recordReaderInfo.initialize(inputSplitInfo);


            this.library = recordReaderInfo.nextRecord().getRecord().get(1).toString();
            this.cell = recordReaderInfo.nextRecord().getRecord().get(1).toString();
            this.technology = recordToStringList(recordReaderInfo.nextRecord().getRecord());
            this.terminals = recordToStringList(recordReaderInfo.nextRecord().getRecord());
            this.model = recordReaderInfo.nextRecord().getRecord().get(1).toString();
            this.modelName = recordReaderInfo.nextRecord().getRecord().get(1).toString();
            this.valueLabels = recordToStringList(recordReaderInfo.nextRecord().getRecord());
            this.valueUnits = recordToStringList(recordReaderInfo.nextRecord().getRecord());
            this.valueRange = recordToStringList(recordReaderInfo.nextRecord().getRecord());
            this.numInputs = recordToStringList(recordReaderInfo.nextRecord().getRecord());
            this.constantLabels = recordToStringList(recordReaderInfo.nextRecord().getRecord());
            this.constantValues = recordToStringList(recordReaderInfo.nextRecord().getRecord());
            this.constantUnits = recordToStringList(recordReaderInfo.nextRecord().getRecord());
            this.modelFileNames = recordToStringList(recordReaderInfo.nextRecord().getRecord());
            this.modelFileSections = recordToStringList(recordReaderInfo.nextRecord().getRecord());

        }
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String[] getTechnology() {
        return technology;
    }

    public void setTechnology(String[] technology) {
        this.technology = technology;
    }

    public String[] getTerminals() {
        return terminals;
    }

    public void setTerminals(String[] terminals) {
        this.terminals = terminals;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String[] getValueLabels() {
        return valueLabels;
    }

    public void setValueLabels(String[] valueLabels) {
        this.valueLabels = valueLabels;
    }

    public String[] getValueUnits() {
        return valueUnits;
    }

    public void setValueUnits(String[] valueUnits) {
        this.valueUnits = valueUnits;
    }

    public String[] getValueRange() {
        return valueRange;
    }

    public void setValueRange(String[] valueRange) {
        this.valueRange = valueRange;
    }

    public String[] getNumInputs() {
        return numInputs;
    }

    public void setNumInputs(String[] numInputs) {
        this.numInputs = numInputs;
    }

    public String[] getConstantLabels() {
        return constantLabels;
    }

    public void setConstantLabels(String[] constantLabels) {
        this.constantLabels = constantLabels;
    }

    public String[] getConstantValues() {
        return constantValues;
    }

    public void setConstantValues(String[] constantValues) {
        this.constantValues = constantValues;
    }

    public String[] getModelFileNames() {
        return modelFileNames;
    }

    public void setModelFileNames(String[] modelFileNames) {
        this.modelFileNames = modelFileNames;
    }

    public String[] getModelFileSections() {
        return modelFileSections;
    }

    public void setModelFileSections(String[] modelFileSections) {
        this.modelFileSections = modelFileSections;
    }

    /**
     * Takes a record read from a record reader and returns the corresponding String array
     * @param record given record
     * @return
     */
    private String[] recordToStringList(List<Writable> record){
        for(int i = record.size()-1; i >= 0;i--){
            if(record.get(i).toString().equals("")){
                record.remove(i);
            }
        }
        String[] returnVal = new String[record.size()-1];
        for(int i = 1;i<record.size();i++){
            returnVal[i-1] = record.get(i).toString();
        }
        return returnVal;
    }

    public String toString(){


        return ("Library: "+library+"\nCell: "+cell+"\nTechnology: "+ Arrays.toString(technology)
            +"\nTerminals: "+ Arrays.toString(terminals)+"\nSimulation_Model: "+model+"\nModel_Name: "+modelName
            +"\nValue_Labels: "+Arrays.toString(valueLabels)+"\nValue_Units: "+Arrays.toString(valueUnits)
            +"\nValue_Range: "+Arrays.toString(valueRange)+"\nInput_Values: "+Arrays.toString(numInputs)
            +"\nConstant_Labels: " +Arrays.toString(constantLabels)+ "\nConstant_Values: "+Arrays.toString(constantValues)
            + "\nConstant_Units: "+Arrays.toString(constantUnits)
            +"\nModel File Names: "+Arrays.toString(modelFileNames)+"\nModel File Sections: "+Arrays.toString(modelFileSections));
    }




}
