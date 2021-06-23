%Path to the NNModel created by the TransistorNNModel class
modelPath = "C:/Users/Alex Prucka/IdeaProjects/Transistor-Neural-Network-Modeler/src/test/resources/realDataModel2.bin";

addpath("C:\Users\Alex Prucka\IdeaProjects\Transistor-Neural-Network-Modeler\src\main\Matlab")

%initializing model object with path to model file
%Will throw a warning, not a problem in practice
model = NNModel(modelPath);
%displays input and output columns of the model
model.properties

csvFile = "C:\Users\prucka\Transistor-Neural-Network-Modeler\src\test\resources\simData.csv";
csvData = dlmread(csvFile, ",", 0, 0);

%data from two seperate lengths
data1 = csvData(csvData(:,3)==5e-7,:);
data2 = csvData(csvData(:,3)==csvData(30000,3),:);

%results from the two seperate lengths
results = model.useModel(data1(:,1:2));
results2 = model.useModel(data2(:,1:2));

%plot simulated data vs nn result data for the two seperate lengths
hold
plot(data1(:,1),results(:,2))
plot(data1(:,1),data1(:,4))
plot(data2(:,1),results2(:,2))
plot(data2(:,1),data2(:,4))
set(gca, 'YScale', 'log')
xlabel("gm/id")
ylabel("id/w")

%calculates result data on all 60k examples(Will take approx 3 minutes)
codegen -O enable:inline model.useModelPar
tic
data = model.useModel(csvData(1:1001,1:2));
toc

%show a scatter plot of simulated versus nn data
scatter3(csvData(:,1),csvData(:,4),csvData(:,3))
hold
scatter3(csvData(:,1),data(:,2),data(:,1))
set(gca, 'YScale', 'log')
set(gca, 'ZScale', 'log')



%plot used to show entire mapping within bounds of the neural network model
%will take a few minutes to run
fsurf(@(X,Y) model.useModelCol([X' Y'],2),[1 20 1e7 1.5e10])
set(gca, 'ZScale', 'log')


