classdef NNModel
    properties
        inputColumns
        outputColumns
        modelObject
    end
    methods
        function model = NNModel(modelPath)
            model.modelObject = javaObject("edlab.eda.transistorModel.UseTransistorModel",modelPath);
            model.inputColumns = javaMethod("getInputColNames", model.modelObject);
            model.outputColumns = javaMethod("getOutputColNames", model.modelObject);
        end
        function output = useModel(model, input)
            
            A = input;
            
            if(size(A,1) < 1001)
                dblArray = javaArray('java.lang.Double',size(A,1),size(A,2));
                JavaMatlabConvert = "edlab.eda.transistorModel.JavaMatlabConvert";

                for m = 1:size(A,1)
                    for n = 1:size(A,2)
                        dblArray(m,n) = java.lang.Double(A(m,n));
                    end
                end

                inputSArray = javaMethod("toJava",JavaMatlabConvert,dblArray);
                outputSArray = javaMethod("useModel",model.modelObject,inputSArray);            
                outputDArray = javaMethod("toMatlab",JavaMatlabConvert,outputSArray);
                output = double(outputDArray);
            end
            if(size(A,1) > 1000)
               %fprintf("Computing 1000 results\n")

                %C = ceil(size(A,1)/1000);

                
                
                
%                parfor i = 1:C
%                    j = ((i-1)*1000+1);
%                    if(i < C)
%                        B(j:j+1000,:) = model.useModel(A(j:j+1000,:));
%                   end
%                    if(i == C)
%                        B(j:end,:) = model.useModel(A(j:end,:));
%                    end
                
                output = [model.useModel(A(1:1000,:)) ; model.useModel(A(1001:end,:))];
%                end
                
               
               
                
            end
    
       
        end
        
        function output = useModelPar(model, input)
                output = input;
                
                parfor i = 1:size(input,1)
                    output(i,:) = model.useModel(input(i,:));
                end
        end
                    
            
        function output = useModelCol(model, input, colNum)

            output = model.useModel(input);
            output = output(:,colNum);
        end
        
        function prop = properties(model)
            prop = [javaObject("java.lang.String","Input Columns: ") model.inputColumns ...
                javaObject("java.lang.String","Output Columns: ") model.outputColumns];
        end
        
    end
end


        