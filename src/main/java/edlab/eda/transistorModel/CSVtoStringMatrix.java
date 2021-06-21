package edlab.eda.transistorModel;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.writable.Writable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which parses a csv file into a matrix of strings suitable to use as inputs for the Transistor Neural Network
 */
public class CSVtoStringMatrix {
    private List<List<String>> data;

    /**
     * Constructor, takes in the filepath, the desired amount of rows, and the number of rows to skip at the beginning
     * of the file
     * @param filePath path to CSV file
     * @param numSkip int rows to skip
     * @param numExamples int examples to convert to List<List<String>>
     * @throws IOException
     * @throws InterruptedException
     */
    public CSVtoStringMatrix(String filePath,int numSkip,int numExamples) throws IOException, InterruptedException {

        FileSplit inputSplit = new FileSplit(new File(filePath));
        RecordReader recordReader = new CSVRecordReader(numSkip);
        recordReader.initialize(inputSplit);
        List<List<Writable>> inputData = recordReader.next(numExamples);

        this.data = new ArrayList<>();
        for(List<Writable> l:inputData){
            List<String> temp = new ArrayList<>();
            for(Writable w:l){
                temp.add(w.toString());
            }
            data.add(temp);
        }
    }

    /**
     * returns the List<List<String>> data corresponding to the given object created
     * @return data
     */
    public List<List<String>> getData() {
        return data;
    }
}
