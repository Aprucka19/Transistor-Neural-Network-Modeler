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

public class TransformData {

    private List<List<String>> data;

    public TransformData(List<List<String>> data){
        this.data = data;
    }

    public List<List<String>> transform(String[] colNames) throws IOException, InterruptedException {

        Schema schema = new Schema.Builder()
            .addColumnsDouble(colNames)
            .build();
        //endregion


        TransformProcess transform = new TransformProcess.Builder(schema)
            .addConstantDoubleColumn("w",1e-5)
            .doubleColumnsMathOp("id/w",MathOp.Divide, new String[]{"M0.m1:id", "w"})
            .removeAllColumnsExceptFor(new String[]{"id/w","M0.m1:fug","M0.m1:gmoverid","L"})
            .reorderColumns((new String[]{"M0.m1:gmoverid","M0.m1:fug","L","id/w"}))
            .build();


        ListStringSplit inputSplit = new ListStringSplit(this.data);
        TransformProcessRecordReader inputRecordReader = new TransformProcessRecordReader(new ListStringRecordReader(), transform);
        inputRecordReader.initialize(inputSplit);
        List<List<Writable>> inputData = inputRecordReader.next(data.size());

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
