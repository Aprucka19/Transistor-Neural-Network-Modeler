package edlab.eda.transistorModel;

import java.util.List;

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
