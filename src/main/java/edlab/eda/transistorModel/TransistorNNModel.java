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
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.ui.api.UIServer;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class which can be used to model device characteristics of a transistor using a nerual network, then
 * return outputs based on inputs passed through the trained network
 */
public class TransistorNNModel {


    private String[] colNames;
    private String[] transformCols;
    private String[] inputColNames;
    private String[] outputColNames;
    private String[] inputTransformCols;
    private String[] outputTransformCols;
    private int numInputs;
    private int numOutputs;
    private double lambda;
    private double learningRate;
    private double beta1;
    private double beta2;
    private int batchSize;
    private int numEpochs;
    private Schema schema;
    private  Schema inputSchema;
    private Schema outputSchema;
    private TransformProcess transformProcess;
    private TransformProcess inputTransformProcess;
    private TransformProcess outputTransformProcess;
    private List<List<String>> inputData;
    private MultiLayerConfiguration config;
    private MultiLayerNetwork model;
    private DataNormalization normalizer;
    private TransistorModelMetaData metaData;


    /**
     * Constructor for a new TransistorNNModel
     * @param metaData A metadata object used to return a metaData text file
     * @param givenCols the column names of all of the columns in the input matrix
     * @param colNames The names of the desired data columns in desired order
     * @param transformCols The names of the columns to be transformed (standard transform is the boxCox transform)
     * @param numInputs The number of input columns
     * @param lambda The lambda value for the boxCox transform
     * @param learningRate The learning rate of the NeuralNetwork
     * @param beta1 The beta1 learning value of the NeuralNetwork
     * @param beta2 The beta2 learning value of the NeuralNetwork
     * @param batchSize The size of batches used to train the NeuralNetwork
     * @param numEpochs The number of Epochs used when training the NeuralNetwork
     * @param inputData The input data used to train the model, given in the format of a List<List<String>> Array
     */
    public TransistorNNModel(TransistorModelMetaData metaData, String[] colNames, String[] transformCols, int numInputs, double lambda, double learningRate,
                             double beta1, double beta2, int batchSize, int numEpochs, List<List<String>> inputData){
        this.metaData = metaData;

        this.colNames = colNames;
        this.transformCols = transformCols;
        this.numInputs = numInputs;
        this.lambda = lambda;
        this.learningRate = learningRate;
        this.beta1 = beta1;
        this.beta2 = beta2;
        this.batchSize = batchSize;
        this.numEpochs = numEpochs;
        this.inputData = inputData;
        this.numOutputs = colNames.length - this.numInputs;
        this.setDefaultConfig();
        this.setDefaultTransform();
        this.setInputOutputColumnArrays();

    }

    /**
     * Function used to set a default NerualNetwork configuration that is used unless a separate one is manually set
     */
    public void setDefaultConfig(){
        this.config = new NeuralNetConfiguration.Builder()
            .seed(0xC0FFEE)
            .weightInit(WeightInit.NORMAL)
            .activation(Activation.RELU)
//            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(new Adam(this.learningRate,this.beta1,this.beta2,1e-8))
//            .l2(Math.sqrt(batchSize/(inputData.size()*numEpochs)))
            .list()
            .layer(new DenseLayer.Builder().nIn(this.numInputs).nOut(128).build())
            .layer(new DenseLayer.Builder().nIn(128).nOut(256).build())
            .layer(new DenseLayer.Builder().nIn(256).nOut(512).build())
            .layer(new DenseLayer.Builder().nIn(512).nOut(1024).build())
            .layer(new DenseLayer.Builder().nIn(1024).nOut(512).build())
            .layer(new DenseLayer.Builder().nIn(512).nOut(256).build())
            .layer(new DenseLayer.Builder().nIn(256).nOut(128).build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .activation(Activation.RELU).nIn(128).nOut(this.numOutputs).build())
            .build();
    }

    /**
     * Function used to set the default boxCox transformation on the given transformColumns and is used unless another
     * transformProcess is set
     */
    public void setDefaultTransform(){

        //region Initial Schema Build
        this.schema = new Schema.Builder()
            .addColumnsDouble(colNames)
            .build();
        //endregion

        //region Building the transformProcess based on which cols in the input file are specified to be transformed
        List<DataAction> actionList = new ArrayList<>();

//        actionList.add(new DataAction(new RemoveAllColumnsExceptForTransform(colNames)));
//        actionList.add(new DataAction(new ReorderColumnsTransform(colNames)));


        for (String changeCol : transformCols) {
            if(lambda != 0){
                actionList.add(new DataAction(new DoubleMathFunctionTransform(changeCol,MathFunction.LOG)));
                actionList.add(new DataAction(new DoubleMathOpTransform(changeCol,MathOp.Multiply,lambda)));
                actionList.add(new DataAction(new DoubleMathFunctionTransform(changeCol,MathFunction.EXP)));
                actionList.add(new DataAction(new DoubleMathOpTransform(changeCol, MathOp.Subtract, 1)));
                actionList.add(new DataAction(new DoubleMathOpTransform(changeCol, MathOp.Divide, lambda)));
            }
            else{
                actionList.add(new DataAction(new DoubleMathFunctionTransform(changeCol,MathFunction.LOG)));
            }

        }

        this.transformProcess = new TransformProcess(schema, actionList);
        //endregion
    }

    /**
     * Sets the input and output column string arrays correctly
     */
    public void setInputOutputColumnArrays(){
        this.inputColNames = new String[numInputs];
        this.outputColNames = new String[numOutputs];

        List<String> outputTransformColsAL = new ArrayList<>();
        List<String> inputTransformColsAL = new ArrayList<>();

        //Add names for input column names and input transform names
        for(int i = 0; i<numInputs;i++){
            inputColNames[i] = colNames[i];
//            System.out.println("input Col Names");
//            System.out.println(inputColNames[i]);
            for(String s: transformCols){
                if(colNames[i].equals(s)){
                    inputTransformColsAL.add(colNames[i]);
                }
            }
        }

        //Convert inputTransformCols to an array of strings
        this.inputTransformCols = new String[inputTransformColsAL.size()];
        int ctr2 = 0;
        for(String s: inputTransformColsAL){
//            System.out.println(s);
            inputTransformCols[ctr2] = s;
            ctr2++;
        }

        //Add names for output column names and output transform names
        for(int i = 0; i<numOutputs;i++){
            outputColNames[i] = colNames[i+numInputs];
//            System.out.println("output Col Names");
//            System.out.println(outputColNames[i]);
            for(String s: transformCols){
                if(colNames[i+numInputs].equals(s)){
                    outputTransformColsAL.add(colNames[i+numInputs]);
                }
            }
        }

        //Convert outputTransformCols to an array of Strings
        this.outputTransformCols = new String[outputTransformColsAL.size()];
        int ctr3 = 0;
        for(String s: outputTransformColsAL){
            outputTransformCols[ctr3] = s;
//            System.out.println("output Change Col Names");
//            System.out.println(s);
            ctr3++;
        }
    }

    /**
     * Function used to fit the MultiLayerNetwork model to the input data. A local host UI can be displayed if trainUI
     * is set to true
     * @param trainUI boolean value for displaying the training UI or not
     * @throws IOException
     * @throws InterruptedException
     */
    public void fitNetworkToInputData(boolean trainUI) throws IOException, InterruptedException {

        ListStringSplit inputSplit = new ListStringSplit(this.inputData);
        TransformProcessRecordReader inputRecordReader = new TransformProcessRecordReader(new ListStringRecordReader(), this.transformProcess);
        inputRecordReader.initialize(inputSplit);


        DataSetIterator trainIterator = new RecordReaderDataSetIterator(inputRecordReader,batchSize,numInputs,numOutputs+numInputs-1, true);
        this.normalizer = new NormalizerMinMaxScaler();
        normalizer.fitLabel(true);
        normalizer.fit(trainIterator);
        trainIterator.setPreProcessor(normalizer);


        this.model = new MultiLayerNetwork(config);
        model.init();
        if(trainUI){
            //region Model Training UI Initialization
            //Initialize the user interface backend
            UIServer uiServer = UIServer.getInstance();

            //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
            StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later

            //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
            uiServer.attach(statsStorage);

            model.setListeners(new StatsListener(statsStorage));
            //endregion
        }
        model.fit(trainIterator,numEpochs);

    }

    /**
     * Function used to evaluate the fit of the model against a test data set
     * @param testData Test data given in the form of a matrix List<List<String>> to evaluate the fit of the model against
     * @throws IOException
     * @throws InterruptedException
     */
    public void evaluateFit(List<List<String>> testData) throws IOException, InterruptedException {
        if(model != null) {
            ListStringSplit inputSplit = new ListStringSplit(testData);
            TransformProcessRecordReader testRecordReader = new TransformProcessRecordReader(new ListStringRecordReader(), this.transformProcess);
            testRecordReader.initialize(inputSplit);

            RecordReaderDataSetIterator testIterator = new RecordReaderDataSetIterator(testRecordReader, batchSize, numInputs, numOutputs + numInputs - 1, true);
            testIterator.setPreProcessor(normalizer);
            RegressionEvaluation evaluate = model.evaluateRegression(testIterator);
            System.out.println(evaluate.stats());
        }
        else{
            System.out.print("Model must be fit to the data using the fitNetworkToInputData function first");
        }
    }



    /**
     * Function to use the already fit TransistorNNModel where modelInput is the inputs, and the outputs are returned as
     * calculated by the model
     * The functionality in this method is also housed in the UseTransistorModel class which is more flexible as it
     * takes a saved model, and thus retraining each time is not required when using the model
     * @param modelInput List<List<String>> Matrix of the input data for which outputs are desired
     * @return a List<List<String>> of outputs that correspond with the inputs to the function
     * @throws IOException
     * @throws InterruptedException
     */
    public List<List<String>> useModel(List<List<String>> modelInput) throws IOException, InterruptedException {

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


        //region Parse the inputs to transform them and create an iterator to run through the ML model
        ListStringSplit inputSplit = new ListStringSplit(modelInput);
        TransformProcessRecordReader inputTPRR = new TransformProcessRecordReader(new ListStringRecordReader(), inputTransformProcess);
        inputTPRR.initialize(inputSplit);
        DataSetIterator inputDataIterator = new RecordReaderDataSetIterator(inputTPRR,1);
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


    /**
     * Saves the built model and relevant varables in a .bin file specified at the end of the filePath, and saves
     * the metadata given to the object in a .txt file.
     * @param
     * @throws IOException
     */
    public void saveModelAsFile(String dirPath, String modelName, String metaDataName) throws IOException {

        File modelSave = new File(dirPath+modelName);
        this.model.save(modelSave);
        ModelSerializer.addNormalizerToModel(modelSave, normalizer);
        ModelSerializer.addObjectToFile(modelSave, "transformCols", this.transformCols);
        ModelSerializer.addObjectToFile(modelSave, "colNames", colNames);
        ModelSerializer.addObjectToFile(modelSave,"inputNum",numInputs);
        ModelSerializer.addObjectToFile(modelSave,"lambda",lambda);
        ModelSerializer.addObjectToFile(modelSave,"inputTransformCols",this.inputTransformCols);
        ModelSerializer.addObjectToFile(modelSave,"outputTransformCols",this.outputTransformCols);
        ModelSerializer.addObjectToFile(modelSave,"inputColNames",this.inputColNames);
        ModelSerializer.addObjectToFile(modelSave,"outputColNames",this.outputColNames);

        BufferedWriter output = null;
        try {
            File file = new File(dirPath + metaDataName);
            output = new BufferedWriter(new FileWriter(file));
            int text;
            output.write(metaData.toString());
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( output != null ) {
                output.close();
            }
        }

    }



    public void setConfig(MultiLayerConfiguration config){
        this.config = config;
    }

    public void setNumEpochs(int numEpochs){
        this.numEpochs = numEpochs;
    }

    public void setLearningVariables(double learningRate, double beta1, double beta2){
        this.learningRate = learningRate;
        this.beta1 = beta1;
        this.beta2 = beta2;

    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public void setBeta1(double beta1) {
        this.beta1 = beta1;
    }

    public void setBeta2(double beta2) {
        this.beta2 = beta2;
    }

    public void setLambda(double lambda){
        this.lambda = lambda;
    }

    public void setTransformCols(String[] transformCols){
        this.transformCols = transformCols;
    }

    public String[] getInputColNames() {
        return inputColNames;
    }

    public String[] getOutputColNames() {
        return outputColNames;
    }

    public String[] getInputTransformCols() {
        return inputTransformCols;
    }

    public String[] getOutputTransformCols() {
        return outputTransformCols;
    }


    public String[] getColNames() {
        return colNames;
    }

    public String[] getTransformCols() {
        return transformCols;
    }

    public int getNumInputs() {
        return numInputs;
    }

    public int getNumOutputs() {
        return numOutputs;
    }

    public double getLambda() {
        return lambda;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double getBeta1() {
        return beta1;
    }

    public double getBeta2() {
        return beta2;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getNumEpochs() {
        return numEpochs;
    }

    public Schema getSchema() {
        return schema;
    }

    public TransformProcess getTransformProcess() {
        return transformProcess;
    }

    public List<List<String>> getInputData() {
        return inputData;
    }

    public MultiLayerConfiguration getConfig() {
        return config;
    }

    public MultiLayerNetwork getModel() {
        return model;
    }

    public DataNormalization getNormalizer() {
        return normalizer;
    }



}
