

%MUST BE CHANGED DEPENDING ON SYSTEM
pathToRepository = "C:/Users/Alex Prucka/IdeaProjects/Transistor-Neural-Network-Modeler/";


%Path to the NNModel Class
modelPath = pathToRepository + "src/test/resources/pmos1.bin";
classPath = pathToRepository + "src\main\Matlab";
addpath(classPath)


%initializing model object with path to model file
%Will throw a warning, not a problem in practice
model = NNModel(modelPath);
%displays input and output columns of the model
model.properties

csvFile = pathToRepository +"src\test\resources\simDataPmos.csv";
csvData = dlmread(csvFile, ",", 0, 0);


%calculates result data on all examples

tic
data = model.useModel(csvData(:,1:2));
toc

%show a scatter plot of simulated versus nn data
scatter3(csvData(:,1),csvData(:,4),csvData(:,3))
hold
scatter3(csvData(:,1),data(:,2),data(:,1))
set(gca, 'YScale', 'log')
set(gca, 'ZScale', 'log')



%plot used to show entire mapping within bounds of the neural network model
%will take a few minutes to run
fsurf(@(X,Y) log10(model.useModelCol([X' Y'],2)),[1 20 1e7 1.5e10])
%set(gca, 'ZScale', 'log')
xlabel("gm/id")
ylabel("fug")
zlabel("id/w")

latex2markdown("NNLiveScript.tex")
