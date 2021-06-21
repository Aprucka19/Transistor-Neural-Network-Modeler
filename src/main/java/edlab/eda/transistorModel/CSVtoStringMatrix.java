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
 * Class that
 */
public class CSVtoStringMatrix {
    private List<List<String>> data;

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

    public List<List<String>> getData() {
        return data;
    }
}
