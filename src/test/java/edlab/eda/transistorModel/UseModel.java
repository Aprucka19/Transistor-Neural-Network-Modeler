package edlab.eda.transistorModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UseModel {

    /**
     * Uses the trained model from the TrainModel test class to graph the difference between simulated data
     * and data fit by the neural network model
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String baseDir = "C:/Users/prucka/Transistor-Neural-Network-Modeler/src/test/resources/";


        //Get the relevant datasets from two csv files, both which contain transistor data for one length of transistor
        //Input is just the two input columns, where All is all 4 columns as were used to train the model
        CSVtoStringMatrix testInput2 = new CSVtoStringMatrix(baseDir + "subsetInput.csv",0,1200);
        CSVtoStringMatrix testInput3 = new CSVtoStringMatrix(baseDir + "subsetAll.csv",0,1200);

        //Create a test example to determine the relevant length and id/w for a given gm and fug
        List<List<String>> input = new ArrayList<>();
        List<String> testCase = new ArrayList<>(Arrays.asList("12","4.7e7"));
        input.add(testCase);
        UseTransistorModel modelUse2 = new UseTransistorModel(baseDir + "Model1.bin");
        //print the resulting id/w and L
        System.out.println(modelUse2.useModel(input).toString());

        //get the id/w and L data from the neural network for all input examples
        List<List<String>> resultMatrix = modelUse2.useModel(testInput2.getData());

        //Use a data parser class which can transform the data from List<List<String>> to arrays of doubles to be plotted
        StringMatrixTransforms doubleFinder = new StringMatrixTransforms(resultMatrix);
        StringMatrixTransforms doubleFinderInput = new StringMatrixTransforms(testInput3.getData());


        //get the id/w data from the result of the NN Model
        double[] y = doubleFinder.getColumnAsDouble(2);

        //parse the id/w data from the input simulation
        double[] y2 = doubleFinderInput.getColumnAsDouble(4);
        double maxy = doubleFinderInput.maxInColumn(4);
        double miny = doubleFinderInput.minInColumn(4);

        //parse the input gm/id
        double[] x = doubleFinderInput.getColumnAsDouble(1);
        double maxx = doubleFinderInput.maxInColumn(1);
        double minx = doubleFinderInput.minInColumn(1);


        //Use a plotting class to plot the id/W vs the gm/Id, for both the data the model was trained on and the data
        //given by the model itself to compare how well the data is fit
        MatlabChart figure = new MatlabChart();
        figure.plot(x, y, "-r", 2.0f, "fit with NN");
        figure.plot(x,y2,"-b", 2.0f,"from original Data");
        figure.RenderPlot(true);
        figure.title("Id/W vs gm/Id for 6.72e-6 L");
        figure.xlim(minx,maxx);
        figure.ylim(miny,maxy);
        figure.xlabel("gm/Id");
        figure.ylabel("Id/W");
        figure.grid("on","on");
        figure.legend("northeast");
        figure.font("Helvetica",15);
        figure.saveas(baseDir + "IdOverW_vs_gmOverId.jpeg",640,480);




        //Similarly to above plot the gm/Id vs L data to analyze the fit of the other column
        double[] x1 = doubleFinderInput.getColumnAsDouble(1);
        double maxx1 = doubleFinderInput.maxInColumn(1);
        double minx1 = doubleFinderInput.minInColumn(1);

        double[] y1 = doubleFinder.getColumnAsDouble(1);
        double maxy1 = doubleFinder.maxInColumn(1)*2;
        double miny1 = doubleFinder.minInColumn(1)/2;


        double[] y3 = doubleFinderInput.getColumnAsDouble(3);
        double maxy3 = doubleFinderInput.maxInColumn(3)*2;
        double miny3 = doubleFinderInput.minInColumn(3)/2;


        MatlabChart figure2 = new MatlabChart();
        figure2.plot(x1, y1, "-r", 2.0f, "fit with NN");
        figure2.plot(x1,y3,"-b", 2.0f,"from original Data");
        figure2.RenderPlot(false);
        figure2.title("gm/Id vs L");
        figure2.xlim(minx1,maxx1);
        figure2.ylim(Double.min(miny1,miny3),Double.max(maxy1,maxy3));
        figure2.xlabel("gm/Id");
        figure2.ylabel("L");
        figure2.grid("on","on");
        figure2.legend("northeast");
        figure2.font("Helvetica",15);
        figure2.saveas(baseDir + "gmOverId_vs_L.jpeg",640,480);





    }
}
