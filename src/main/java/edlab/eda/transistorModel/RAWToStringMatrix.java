package edlab.eda.transistorModel;

import edlab.eda.reader.nutmeg.NutReader;
import edlab.eda.reader.nutmeg.NutasciiReader;
import edlab.eda.reader.nutmeg.NutmegPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.writable.Writable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * A class which parses either a single plot or multiple plots from a .raw file, returning the List<List<String>>
 *     Datatype for the TransistorNeuralNetwork
 */
public class RAWToStringMatrix {
    private List<List<String>> dataSet;
    private String[] colNames;


    /**
     * Takes in the specific plot index that is desired, as well as a boolean saying weather the input .raw file is binary
     * or ascii
     * @param filePath path to the .raw file
     * @param plotNum plot index desired
     * @param isBin boolean isBin
     * @throws IOException
     * @throws InterruptedException
     */
    public RAWToStringMatrix(String filePath,int plotNum,boolean isBin) throws IOException, InterruptedException {


        // Create a new reader
        NutReader reader;
        if(isBin){
            reader = NutReader.getNutbinReader(filePath);
        }
        else{
            reader = NutReader.getNutasciiReader(filePath);
        }


        reader.read().parse();
        List<NutmegPlot> plots = reader.getPlots();
        NutmegPlot nutmegPlot = plots.get(plotNum);

        // Get set of all waves from plot
        Set<String> waves = nutmegPlot.getWaves();
        String[] waveNames = new String[waves.size()];
        waves.toArray(waveNames);
        this.colNames = waveNames;

        dataSet = new ArrayList<>();


        for(int i = 0; i < nutmegPlot.getNoOfPoints(); i++){
            List<String> temp = new ArrayList<>();
            for(String w: waves){
                if (nutmegPlot instanceof NutmegRealPlot) {
                    NutmegRealPlot nutmegRealPlot = (NutmegRealPlot) nutmegPlot;

                    double[] wave = nutmegRealPlot.getWave(w);
                    temp.add(String.valueOf(wave[i]));
                }
            }
            dataSet.add(temp);
        }

    }

    /**
     * Parses all plots from a .raw file into one dataset,takes as an argument a
     * boolean saying weather the input .raw file is binary. Input plots must be of same width dimensions
     * @param filePath path to file
     * @param isBin boolean isBin
     * @throws IOException
     * @throws InterruptedException
     */
    public RAWToStringMatrix(String filePath,boolean isBin) throws IOException, InterruptedException {


        // Create a new reader
        NutReader reader;
        if(isBin){
            reader = NutReader.getNutbinReader(filePath);
        }
        else{
            reader = NutReader.getNutasciiReader(filePath);
        }

        reader.read().parse();
        List<NutmegPlot> plots = reader.getPlots();
        dataSet = new ArrayList<>();
        for(NutmegPlot p: plots){
            // Get set of all waves from plot
            Set<String> waves = p.getWaves();
            String[] waveNames = new String[waves.size()];
            waves.toArray(waveNames);
            this.colNames = waveNames;




            for(int i = 0; i < p.getNoOfPoints(); i++){
                List<String> temp = new ArrayList<>();
                for(String w: waves){
                    if (p instanceof NutmegRealPlot) {
                        NutmegRealPlot nutmegRealPlot = (NutmegRealPlot) p;

                        double[] wave = nutmegRealPlot.getWave(w);
                        temp.add(String.valueOf(wave[i]));
                    }
                }
                dataSet.add(temp);
            }
        }




    }

    public List<List<String>> getData() {
        return dataSet;
    }

    public String[] getColNames(){ return colNames;}



}
