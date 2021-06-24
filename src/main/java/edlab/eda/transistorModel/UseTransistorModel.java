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
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Class that is used to operate a trained Transistor Model which takes the model file as the input
 */
public class UseTransistorModel {

    private MultiLayerNetwork model;
    private DataNormalization normalizer;
    private String[] colNames;
    private String[] inputColNames;
    private String[] outputColNames;
    private String[] transformCols;
    private String[] inputTransformCols;
    private String[] outputTransformCols;
    private int numInputs;
    private int numOutputs;
    private double lambda;
    Schema inputSchema;
    Schema outputSchema;
    TransformProcess inputTransformProcess;
    TransformProcess outputTransformProcess;


    /**
     * Constructor for the UseTransistorModel class which takes the path to the model file as an input
     * @param modelPath the path to the model file
     * @throws IOException
     */
    public UseTransistorModel(String modelPath) throws IOException {

        File modelSave = new File(modelPath);
        this.model = ModelSerializer.restoreMultiLayerNetwork(modelSave);
        this.normalizer = ModelSerializer.restoreNormalizerFromFile(modelSave);
        this.colNames = ModelSerializer.getObjectFromFile(modelSave,"colNames");
        this.transformCols = ModelSerializer.getObjectFromFile(modelSave,"transformCols");
        this.numInputs = ModelSerializer.getObjectFromFile(modelSave,"inputNum");
        this.numOutputs = colNames.length - numInputs;
        this.lambda = ModelSerializer.getObjectFromFile(modelSave,"lambda");
        this.inputColNames = ModelSerializer.getObjectFromFile(modelSave,"inputColNames");
        this.outputColNames = ModelSerializer.getObjectFromFile(modelSave,"outputColNames");
        this.inputTransformCols = ModelSerializer.getObjectFromFile(modelSave,"inputTransformCols");
        this.outputTransformCols = ModelSerializer.getObjectFromFile(modelSave,"outputTransformCols");
        this.setTransformProcess();
    }

    /**
     * Builds the schema and transform process for the input and output when using the model
     */
    public void setTransformProcess(){
        //region Building input Schema and input Transform Process
        this.inputSchema = new Schema.Builder()
            .addColumnsDouble(inputColNames)
            .build();

        List<DataAction> inputActionList = new ArrayList<>();

        for (String changeCol : inputTransformCols) {

            if (lambda != 0) {
                inputActionList.add(new DataAction(new DoubleMathFunctionTransform(changeCol, MathFunction.LOG)));
                inputActionList.add(new DataAction(new DoubleMathOpTransform(changeCol, MathOp.Multiply, lambda)));
                inputActionList.add(new DataAction(new DoubleMathFunctionTransform(changeCol, MathFunction.EXP)));
                inputActionList.add(new DataAction(new DoubleMathOpTransform(changeCol, MathOp.Subtract, 1)));
                inputActionList.add(new DataAction(new DoubleMathOpTransform(changeCol, MathOp.Divide, lambda)));
            }
            else {
                inputActionList.add(new DataAction(new DoubleMathFunctionTransform(changeCol, MathFunction.LOG)));
            }

        }

        this.inputTransformProcess = new TransformProcess(inputSchema, inputActionList);
        //endregion


        //region Building output Schema and output Transform Process
        this.outputSchema = new Schema.Builder()
            .addColumnsDouble(outputColNames)
            .build();

        List<DataAction> outputActionList = new ArrayList<>();

        for (String changeCol : outputTransformCols) {
            if(lambda != 0){
                outputActionList.add(new DataAction(new DoubleMathOpTransform(changeCol, MathOp.Multiply, lambda)));
                outputActionList.add(new DataAction(new DoubleMathOpTransform(changeCol, MathOp.Add, 1)));
                outputActionList.add(new DataAction(new DoubleMathFunctionTransform(changeCol,MathFunction.LOG)));
                outputActionList.add(new DataAction(new DoubleMathOpTransform(changeCol,MathOp.Divide, lambda)));
                outputActionList.add(new DataAction(new DoubleMathFunctionTransform(changeCol,MathFunction.EXP)));
            }
            else{
                outputActionList.add(new DataAction(new DoubleMathFunctionTransform(changeCol, MathFunction.EXP)));
            }

        }


        this.outputTransformProcess = new TransformProcess(outputSchema, outputActionList);
        //endregion
    }


    /**
     * Function to use the given model where modelInput is the inputs, and the outputs are returned as
     * calculated by the model
     * @param modelInput List<List<String>> Matrix of the input data for which outputs are desired
     * @return a List<List<String>> of outputs that correspond with the inputs to the function
     * @throws IOException
     * @throws InterruptedException
     */
    public List<List<String>> useModel(List<List<String>> modelInput) throws IOException, InterruptedException {




        //region Parse the inputs to transform them and create an iterator to run through the ML model
        ListStringSplit inputSplit = new ListStringSplit(modelInput);
        TransformProcessRecordReader inputTPRR = new TransformProcessRecordReader(new ListStringRecordReader(), inputTransformProcess);
        inputTPRR.initialize(inputSplit);
        DataSetIterator inputDataIterator = new RecordReaderDataSetIterator(inputTPRR,modelInput.size());
        normalizer.fitLabel(false);
        inputDataIterator.setPreProcessor(normalizer);
        //endregion


        //region Obtain results from NN and normalize the outputs
        INDArray results = this.model.output(inputDataIterator, false);
        normalizer.fitLabel(true);
        normalizer.revertLabels(results);
        //endregion


        //region Transform output array into String list matrix to transform outputs
        int testQuantity = 0;
        List<List<String>> outputData = new ArrayList<>();
        double[][] doubleMatrix = results.toDoubleMatrix();
        for(double[] row:doubleMatrix){
            testQuantity++;
            List<String> temp = new ArrayList<>();
            for(double number:row){
                temp.add(String.valueOf(number));

            }
            outputData.add(temp);
        }
        //endregion


        //region Transform the output and save results in a Writeable array
        ListStringSplit outputSplit = new ListStringSplit(outputData);
        TransformProcessRecordReader outputTPRR = new TransformProcessRecordReader(new ListStringRecordReader(), outputTransformProcess);
        outputTPRR.initialize(outputSplit);
        List<List<Writable>> finalOutput = outputTPRR.next(testQuantity);
        //endregion


        //region Convert to string array for output
        List<List<String>> finalStringOutput = new ArrayList<>();
        for(List<Writable> l:finalOutput){
            List<String> temp = new ArrayList<>();
            for(Writable w:l){
                temp.add(w.toString());
            }
            finalStringOutput.add(temp);
        }
        //endregion


        return finalStringOutput;
    }



    public String[] getInputColNames() {
        return inputColNames;
    }

    public String[] getOutputColNames() {
        return outputColNames;
    }




}
