package edlab.eda.transistorModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A function used in the matlab frontend implementation which converts data between List<List<String>> and Double[][]
 */
public class JavaMatlabConvert {

    /**
     * Converts the input Double[][] to List<List<String>>
     */
    public static List<List<String>> toJava(Double[][] input){
        List<List<String>> output = new ArrayList<>();
        for(Double[] oL:input){
            List<String> temp = new ArrayList<>();
            for(Double o:oL){
                temp.add(o.toString());
            }
            output.add(temp);
        }

        return output;
    }

    /**
     * Converts the input List<List<String>> to a Double[][] Matrix
     */
    public static Double[][] toMatlab(List<List<String>> input){
        Double[][] output = new Double[input.size()][input.get(0).size()];
        int i = 0;
        for(List<String> sL:input){
            Double[] temp = new Double[sL.size()];
            int j = 0;
            for(String s: sL){
                temp[j] = Double.parseDouble(s);
                j++;
            }
            output[i] = temp;
            i++;
        }
        return output;
    }
}
