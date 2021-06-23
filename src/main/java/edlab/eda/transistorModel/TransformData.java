package edlab.eda.transistorModel;

import org.datavec.api.records.reader.impl.collection.ListStringRecordReader;
import org.datavec.api.records.reader.impl.transform.TransformProcessRecordReader;
import org.datavec.api.split.ListStringSplit;
import org.datavec.api.transform.DataAction;
import org.datavec.api.transform.MathFunction;
import org.datavec.api.transform.MathOp;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.doubletransform.DoubleMathFunctionTransform;
import org.datavec.api.transform.transform.doubletransform.DoubleMathOpTransform;
import org.datavec.api.writable.Writable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * An class used to transform the input data in many different ways. Must be customized to each individual
 * dataset depending on what transformations are desired
 */
public class TransformData {

    private List<List<String>> data;

    public TransformData(List<List<String>> data){
        this.data = data;
    }

    public List<List<String>> transform(String[] colNames) throws IOException, InterruptedException {

        //using the column names of the input dataset, create a schema for the data
        Schema schema = new Schema.Builder()
            .addColumnsDouble(colNames)
            .build();


        //create a transform process which adds, removes, reorders, and manipulates the columns in the dataset
        TransformProcess transform = new TransformProcess.Builder(schema)
                .addConstantDoubleColumn("w",1e-5)
                .doubleMathFunction("M0.m1:id",MathFunction.ABS)
                .doubleColumnsMathOp("id/w",MathOp.Divide, new String[]{"M0.m1:id", "w"})
                .removeAllColumnsExceptFor(new String[]{"id/w","M0.m1:fug","M0.m1:gmoverid","L"})
                .reorderColumns((new String[]{"M0.m1:gmoverid","M0.m1:fug","L","id/w"}))
                .build();


        //Use a record reader to parse the input data through the transformation process
        ListStringSplit inputSplit = new ListStringSplit(this.data);
        TransformProcessRecordReader inputRecordReader = new TransformProcessRecordReader(new ListStringRecordReader(), transform);
        inputRecordReader.initialize(inputSplit);
        List<List<Writable>> inputData = inputRecordReader.next(data.size());

        //convert the transformed data from Writables to Strings
        List<List<String>> newData = new ArrayList<>();
        for(List<Writable> l:inputData){
            List<String> temp = new ArrayList<>();
            for(Writable w:l){
                temp.add(w.toString());
            }
            newData.add(temp);
        }
        return newData;
    }

}
