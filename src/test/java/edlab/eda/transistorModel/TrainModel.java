
package edlab.eda.transistorModel;


import java.io.File;

public class TrainModel {

    /**
     * Train Model method which parses a CSV dataset, trains a NN model to the dataset, evaluates the resulting
     * fit, and saves the trained model and metadata to files
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        //Initialize paths to input data
        String absPath = new File("").getAbsolutePath();
        String baseDir = absPath + "/src/test/resources/";
        String trainFileName = "gmIdTrain.csv";
        String testFileName = "gmIdTrain.csv";
        String inputPath = baseDir + trainFileName;
        String inputPathTest = baseDir + testFileName;

        //Parse metadata file into format which the NNModel class can take in
        TransistorModelMetaData metaData = new TransistorModelMetaData(baseDir + "MetaDataSample.csv",true);

        //Parse 11000 training examples from the csv file, skipping the first line (containing headers)
        CSVtoStringMatrix inputData = new CSVtoStringMatrix(inputPath, 1,11000);

        //Build the model trainer object, with relevant training parameters (these can be tuned to fit the model to the
        //data much more effectively depending on your input data)
        TransistorNNModel model1 = new TransistorNNModel(metaData, new String[]{"gm/id","fug","L","id/w"},
                new String[]{"fug","id/w"}, 2,0.25, 0.01, 0.9, 0.999, 2000,
            40, inputData.getData());

        //fit the model to the input data
        model1.fitNetworkToInputData(inputData.getData(),true);

        //evaluate the fit on the last 1000 examples in the input file and print resulting relevant metrics
        CSVtoStringMatrix evaluationData = new CSVtoStringMatrix(inputPathTest,11001,1000);
        model1.evaluateFit(evaluationData.getData());

        //Save the trained model and metadata to files
        model1.saveModelAsFile(baseDir,"Model1.bin","MetaData.txt");

    }
}
