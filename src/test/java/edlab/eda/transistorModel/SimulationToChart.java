package edlab.eda.transistorModel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.math3.util.Precision.round;

public class SimulationToChart {


    /**
     * A class which contains the entire workflow of parsing and transforming input data, using that data to train a
     * neural network, saving that neural network, then using the trained network to plot accuracy of the trained data
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        //initialize paths
        String rawPath = "C:/Users/prucka/Transistor-Neural-Network-Modeler/src/main/resources/SimulationToChart/nmos2.raw";
        String baseDir = "C:/Users/prucka/Transistor-Neural-Network-Modeler/src/main/resources/";


        //region Using NutParser convert the output statistical data to Arraylists of Strings, and retrieve the col names
        //retrieve data from raw file
        RAWToStringMatrix rtm = new RAWToStringMatrix(rawPath,false);
        List<List<String>> data = rtm.getData();
        String[] columnNames = rtm.getColNames();

        //checking statistics to analyze if data is imported correctly
        System.out.println(data.size());
        System.out.println(data.get(0).size());
        System.out.println(Arrays.toString(columnNames));
        System.out.println(data.get(10000));
        //endregion


        //region Using a custom transform function convert the 20 columns into 4 columns, rearranged s.t. inputs before outputs
        //Transform the 20 columns of data into the 4 we desire, and in the correct order
        TransformData transform = new TransformData(data);
        List<List<String>> fixedData = transform.transform(columnNames);

        //Removing data where the fug is 0 or the gm/id > 25
        for(int i = fixedData.size()-1; i >= 0;i--){
            if(round(Double.parseDouble(fixedData.get(i).get(1)),2) == 0 || Double.parseDouble(fixedData.get(i).get(0)) > 25){
                fixedData.remove(i);
            }
        }

        Random rand = new Random(0xDEADBEEF);
        //Shuffle the data
        Collections.shuffle(fixedData,rand);

        //set new column names
        columnNames = new String[]{"M0.m1:gmoverid","M0.m1:fug","L","id/w"};
        //endregion


        //region Create the NNModel object with the desired learning parameters and data and fit the model, evaluate the fit, and save the model to a file
        //create metadata object from CSV file
        TransistorModelMetaData metaData = new TransistorModelMetaData(baseDir + "MetaDataSample.csv",true);

        //Create NNModel trainer objet with data and input desired learning parameters
        TransistorNNModel tmodel = new TransistorNNModel(metaData, columnNames,new String[]{"M0.m1:fug","id/w"},
            2,.25,0.01,0.9,0.999,10000,50,fixedData.subList(0, fixedData.size()));

        //Fit the nework to the input data, and specify that the training UI is desired
        tmodel.fitNetworkToInputData(true);

        //Evaluate the fit of the data on another section of input data
        tmodel.evaluateFit(fixedData.subList(0,10000));

        //Save the model and metadata
        tmodel.saveModelAsFile(baseDir,"realDataModel22.bin","MetaDataReal.txt");
        //endregion


        //region Prepare the data to go through the model, sorted into sets by length to make the plots look good
        //Sort the input data by gm/id
        fixedData.sort(Comparator.comparingDouble(row -> Double.parseDouble(row.get(0))));

        //region Write output data to results CSV file
        BufferedWriter br = new BufferedWriter(new FileWriter(baseDir + "simData.csv"));
        StringBuilder sb = new StringBuilder();

        //put results in output file
        for(List<String> resultRow:fixedData){
            for (String element : resultRow) {

                sb.append(element);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append("\n");
        }
        br.write(sb.toString());
        br.close();
        //endregion


        //Create a list of sizes, as to pick a certain portion to plot
        Set<String> sizes1 = new HashSet<>();
        for(List<String> list:fixedData){
                sizes1.add(list.get(2));
        }
        List<String> sizes = new ArrayList<>(sizes1);
        Collections.sort(sizes);
        for(int j = sizes.size()-1; j >= 0;j--){
            if((j%50)!=0){
                sizes.remove(j);
            }
        }






        //Create seperate dataSets for each size selected, one with all 4 columns and the other with just the inputs
        List<List<List<String>>> sizeSets = new ArrayList<>();
        List<List<List<String>>> sizeSetsInputs = new ArrayList<>();
        for(String size:sizes){
            List<List<String>> temp1 = new ArrayList<>();
            List<List<String>> temp2 = new ArrayList<>();
            for(List<String> list:fixedData){
                if(list.get(2).equals(size)){
//                    System.out.println(list);
                    temp1.add(list);
                    temp2.add(list.subList(0,2));
                }
            }
            sizeSets.add(temp1);
            sizeSetsInputs.add(temp2);
        }
        //endregion


        //region Run the input data through the trained model
        //Create the UseTransistorModel object with the previously saved model File
        UseTransistorModel modelUse2 = new UseTransistorModel(baseDir + "realDataModel2.bin");

        //For each input data set, parse it through the UseModel function of the UseTransistorModel and collect the
        //resulting output data
        List<List<List<String>>> sizeSetsResults = new ArrayList<>();



        for(List<List<String>> sizeData:sizeSetsInputs){
            sizeSetsResults.add(modelUse2.useModel(sizeData));
        }
        //endregion


        //region Parse Output and Input data for Plotting
        //Create lists of data for inputs, outputs used for training, and outputs generated by the neural net
        List<double[]> yList = new ArrayList<>();
        List<double[]> y1List = new ArrayList<>();
        List<double[]> xList = new ArrayList<>();

        //initilize max min variables
        double xmax = 0;
        double xmin = 1000000000;
        double ymax = 0;
        double ymin = 1000000000;

        //Add the resulting data sets to the data lists, parsing out the desired columns and computing min and max for the graph
        for(int i = 0; i < sizes.size(); i++){
            yList.add(new StringMatrixTransforms(sizeSetsResults.get(i)).getColumnAsDouble(2));
            ymax = Double.max(ymax, new StringMatrixTransforms(sizeSetsResults.get(i)).maxInColumn(2));
            ymin = Double.min(ymin, new StringMatrixTransforms(sizeSetsResults.get(i)).minInColumn(2));

            y1List.add(new StringMatrixTransforms(sizeSets.get(i)).getColumnAsDouble(4));
            ymax = Double.max(ymax, new StringMatrixTransforms(sizeSets.get(i)).maxInColumn(4));
            ymin = Double.min(ymin, new StringMatrixTransforms(sizeSets.get(i)).minInColumn(4));

            xList.add(new StringMatrixTransforms(sizeSets.get(i)).getColumnAsDouble(1));
            xmax = Double.max(xmax,new StringMatrixTransforms(sizeSets.get(i)).maxInColumn(1));
            xmin = Double.min(xmin,new StringMatrixTransforms(sizeSets.get(i)).minInColumn(1));
        }
        //endregion


        //region Plot the parsed data in three separate graphs
        //The list of colors to plot an arbitrary amount of curves in different colors
        String[] colors = new String[]{"-y","-m","-c","-r","g","b","k"};

        //Using a class MatlabChart plotting is simple and follows a matlab-like structure

        //One plot with both nn and origional curves
        MatlabChart figure = new MatlabChart();
        for(int i = 0; i < yList.size(); i++){
            figure.plot(xList.get(i), yList.get(i), colors[i%7], 2.0f, "NN");
            figure.plot(xList.get(i), y1List.get(i),colors[(i+4)%7], 2.0f,"OG");
        }
        figure.RenderPlot(true);
        figure.title("Id/W vs gm/Id for 6.72e-6 L");
        figure.xlim(xmin,xmax);
        figure.ylim(ymin,ymax);
        figure.xlabel("gm/Id");
        figure.ylabel("Id/W");
        figure.grid("on","on");
        figure.legend("northeast");
        figure.font("Helvetica",15);
        figure.saveas(baseDir + "graph.jpeg",1000,800);

        //one plot with just the original curves
        MatlabChart figure2 = new MatlabChart();
        for(int i = 0; i < yList.size(); i++){
            figure2.plot(xList.get(i), y1List.get(i),colors[(i+4)%7], 2.0f,"OG");
        }
        figure2.RenderPlot(true);
        figure2.title("Id/W vs gm/Id for 6.72e-6 L");
        figure2.xlim(xmin,xmax);
        figure2.ylim(ymin,ymax);
        figure2.xlabel("gm/Id");
        figure2.ylabel("Id/W");
        figure2.grid("on","on");
        figure2.legend("northeast");
        figure2.font("Helvetica",15);
        figure2.saveas(baseDir + "graph2.jpeg",1000,800);

        //One plot with just the neural net curves
        MatlabChart figure3 = new MatlabChart();
        for(int i = 0; i < yList.size(); i++){
            figure3.plot(xList.get(i), yList.get(i), colors[i%7], 2.0f, "NN");
        }
        figure3.RenderPlot(true);
        figure3.title("Id/W vs gm/Id for 6.72e-6 L");
        figure3.xlim(xmin,xmax);
        figure3.ylim(ymin,ymax);
        figure3.xlabel("gm/Id");
        figure3.ylabel("Id/W");
        figure3.grid("on","on");
        figure3.legend("northeast");
        figure3.font("Helvetica",15);
        figure3.saveas(baseDir + "graph3.jpeg",1000,800);
        //endregion


    }
}
