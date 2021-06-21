package edlab.eda.transistorModel;

import java.util.List;


/**
 * Class which takes in a dataset in the form List<List<String>> and can return desired columns in the form of
 * Double[] and column max and min values. Used for plotting data
 */
public class StringMatrixTransforms {
    List<List<String>> data;


    public StringMatrixTransforms(List<List<String>> data){
        this.data = data;
    }

    public double[] getColumnAsDouble(int column){
        double[] y = new double[data.size()];

        int ctr = 0;
        for(List<String> l:data){
            y[ctr] = Double.parseDouble(l.get(column - 1));

            ctr++;
        }
        return y;
    }

    public double maxInColumn(int column){

        double max = -1e20;
        int ctr = 0;
        for(List<String> l:data){
            if(Double.parseDouble(l.get(column-1)) > max){
                max = Double.parseDouble(l.get(column-1));

            }

            ctr++;
        }
        return max;
    }

    public double minInColumn(int column){
        double min = 1e20;
        int ctr = 0;
        for(List<String> l:data){
            if(Double.parseDouble(l.get(column-1)) < min){
                min = Double.parseDouble(l.get(column-1));

            }

            ctr++;
        }
        return min;
    }
}
