%This file contains the implementations of two seperate optimizers, one for
%a symmetrical amplifier, and the other for a swing current mirror. Giving
%a desired amplification or output resistance respectively, the optimizer
%will determine the exact widths and lengths for the transistors within the
%circuit

%%Defining paths to all the model files used
%NOTE: You will need to train new models for your own transistors to test
%this optimizer because the model files have not been
%included as the trained models used are under NDA
pathToFiles = "C:\Users\Alex Prucka\IdeaProjects\Transistor-Neural-Network-Modeler\src\test\resources\";
modelPath = pathToFiles + "nmosX350moreGdsoverw2.bin";
modelPath46 = pathToFiles + "nmosX350fourtosix.bin";
modelPathP = pathToFiles + "pmosX350moreGdsoverw.bin";



classPath = "C:\Users\Alex Prucka\IdeaProjects\Transistor-Neural-Network-Modeler\src\test\Matlab";
modulePath = "C:\Users\Alex Prucka\IdeaProjects\Transistor-Neural-Network-Modeler\src\main\Matlab";
addpath(classPath)
addpath(modulePath)

nutmegPath = "C:\Users\Alex Prucka\IdeaProjects\nutmeg-reader\src\main\matlab";
addpath(nutmegPath);

%jarPath = "/home/pruckaa/IdeaProjects/Transistor-Neural-Network-Modeler/target/Transistor-Neural-Network-Modeler-1.0.0-beta7-bin.jar";
%javaaddpath(jarPath);
%% swingCurrentMirror
%This section gives the sizes of transistors for a swing current mirror,
%given a desired Rout and an Nmos model file for the desired transistor
%which takes as an input gmoverid, fug, D, B, and outputs the L, id/w,
%gds/W, G, Vdsat, and gmbs/w of the given transistor

%creating the model object

model46 = NNModel(modelPath46);
model46.properties

%defining desired charecteristics
vb = 0.0;
vr = 0.17;
gmoverid = 10;
targetR = 1.32e9;
Iref = 5e-6;

%Calling swingMirror with fminsearch in order to find the correct
%frequencies for the two relevant transistor pairs
func2 = @(x)swingMirror(x,model46,gmoverid,vr,targetR,Iref);
freqs = fminsearch(func2,[7e8,1e9]);

%Using the calculated frequency to calculate the drain voltage of the first
%transistor pair
func3 = @(x)VxFinder(x,freqs(1),gmoverid,0.0,model46,vr);
Z2 = fminsearch(func3,1);


%Running the optimized parameters through the model for the last time
trans1 = model46.useModel([gmoverid,freqs(1),Z2,0.0]);
trans2 = model46.useModel([gmoverid,freqs(2),trans1(4)-Z2,-Z2]);

%Determining the widths and lengths of the transistors
W1 = Iref/trans1(2)
W2 = Iref/trans2(2)
L1 = trans1(1)
L2 = trans2(1)

%Displaying the gds and gmbs values of both of the transistors
gds1 = (trans1(3)*W1);
gds2 = (trans2(3)*W2);
gmbs1 = (trans1(6)*W1);
gmbs2 = (trans2(6)*W2);

%displaying the optimized frequencies that were found
freqs;

%calculating the corresponding Rout that was found
Rout = 1/gds1+1/gds2+(gmoverid*Iref+trans2(6)*W2)/(gds1*gds2)


%Alternatively, use the eval_swing function, along with the desired Iref
%value and the L and W values given from the fminsearch optimization loop,
%and return the Rout calculated from the spectre simulation
Rout = eval_swing(Iref,L1,L2,W1,W2)




%% Data Verification
%This section is an example of data verification for a given model file.
%Here the model that is used for the swingMirror optimization is analyzed,
%by comparing the 6 output characteristics versus gm/id from the model
%output to the simulation output that was used to train the model. This
%allows you to easily visualize any large errors in how the model has fit
%the data, which could lead to incorrect optimization results

%Path to data used to train the model
csvFile = pathToFiles + "simDatanmosX350fourtosix.csv";
csvData = dlmread(csvFile, ",", 0, 0);

%two random indexes in the dataset to pull random lengths
index1 = 200000;
index2 = 230000;

%create two data sets, which each have a single length, bulk, and drain
%voltage
data1 = csvData(csvData(:,5)==csvData(index1,5),:);
data1 = data1(data1(:,3)==csvData(index1,3),:);
data1 = data1(data1(:,4)==csvData(index1,4),:);
data2 = csvData(csvData(:,5)==csvData(index2,5),:);
data2 = data2(data2(:,3)==csvData(index2,3),:);
data2 = data2(data2(:,4)==csvData(index2,4),:);

%calculate the model output on the two datasets
results = model46.useModel(data1(:,1:4));
results2 = model46.useModel(data2(:,1:4));

%Plot L vs Gm/id for both lengths, model vs simulation
figure
subplot(2,3,1)
hold on
plot(data1(:,1),results(:,1))
plot(data1(:,1),data1(:,5))
plot(data2(:,1),results2(:,1))
plot(data2(:,1),data2(:,5))
title("L vs gm/id for L = "+csvData(index1,5)+"m and "+csvData(index2,5)+"m")
set(gca, 'YScale', 'log')
xlabel("gm/id (1/V)")
ylabel("L (m)")
legend("Model "+csvData(index1,5)+"m","Simulation "+csvData(index1,5)+"m", "Model "+csvData(index2,5)+"m", "Simulation "+csvData(index2,5)+"m")
hold off

%Plot id/w vs Gm/id for both lengths, model vs simulation
subplot(2,3,2)
hold on
plot(data1(:,1),results(:,2))
plot(data1(:,1),data1(:,6))
plot(data2(:,1),results2(:,2))
plot(data2(:,1),data2(:,6))
title("id/w vs gm/id for L = "+csvData(index1,5)+"m and "+csvData(index2,5)+"m")
set(gca, 'YScale', 'log')
xlabel("gm/id (1/V)")
ylabel("id/w (A/m)")
legend("Model "+csvData(index1,5)+"m","Simulation "+csvData(index1,5)+"m", "Model "+csvData(index2,5)+"m", "Simulation "+csvData(index2,5)+"m")
hold off

%Plot gds/w vs Gm/id for both lengths, model vs simulation
subplot(2,3,3)
hold on
plot(data1(:,1),results(:,3))
plot(data1(:,1),data1(:,7))
plot(data2(:,1),results2(:,3))
plot(data2(:,1),data2(:,7))
title("gds/w vs gm/id for L = "+csvData(index1,5)+"m and "+csvData(index2,5)+"m")
set(gca, 'YScale', 'log')
xlabel("gm/id (1/V)")
ylabel("gds/w (A/m)")
legend("Model "+csvData(index1,5)+"m","Simulation "+csvData(index1,5)+"m", "Model "+csvData(index2,5)+"m", "Simulation "+csvData(index2,5)+"m")
hold off

%Plot G vs Gm/id for both lengths, model vs simulation
subplot(2,3,4)
hold on
plot(data1(:,1),results(:,4))
plot(data1(:,1),data1(:,8))
plot(data2(:,1),results2(:,4))
plot(data2(:,1),data2(:,8))
title("G vs gm/id for L = "+csvData(index1,5)+"m and "+csvData(index2,5)+"m")
set(gca, 'YScale', 'log')
xlabel("gm/id (1/V)")
ylabel("G (V)")
legend("Model "+csvData(index1,5)+"m","Simulation "+csvData(index1,5)+"m", "Model "+csvData(index2,5)+"m", "Simulation "+csvData(index2,5)+"m")
hold off

%Plot vdsat vs Gm/id for both lengths, model vs simulation
subplot(2,3,5)
hold on
plot(data1(:,1),results(:,5))
plot(data1(:,1),data1(:,9))
plot(data2(:,1),results2(:,5))
plot(data2(:,1),data2(:,9))
title("vdsat vs gm/id for L = "+csvData(index1,5)+"m and "+csvData(index2,5)+"m")
set(gca, 'YScale', 'log')
xlabel("gm/id (1/V)")
ylabel("vdsat (V)")
legend("Model "+csvData(index1,5)+"m","Simulation "+csvData(index1,5)+"m", "Model "+csvData(index2,5)+"m", "Simulation "+csvData(index2,5)+"m")
hold off

%Plot gmbs/w vs Gm/id for both lengths, model vs simulation
subplot(2,3,6)
hold on
plot(data1(:,1),results(:,6))
plot(data1(:,1),data1(:,10))
plot(data2(:,1),results2(:,6))
plot(data2(:,1),data2(:,10))
title("gmbs/w vs gm/id for L = "+csvData(index1,5)+"m and "+csvData(index2,5)+"m")
set(gca, 'YScale', 'log')
xlabel("gm/id (1/V)")
ylabel("gmbs/w (A/m)")
legend("Model "+csvData(index1,5)+"m","Simulation "+csvData(index1,5)+"m", "Model "+csvData(index2,5)+"m", "Simulation "+csvData(index2,5)+"m")
hold off


%% Symmetrical Amplifier
%This section gives the sizes of transistors for a symmetrical amplifier,
%given a desired Amplification and an Nmos and Pmos model file for the 
%desired transistors which takes as an input gmoverid, fug, D, and outputs 
%the L, id/w, gds/W, and G of the given transistor

%create the objects for the nmos and pmos models
model = NNModel(modelPath);
modelP = NNModel(modelPathP);

%define desired characteristsics for the amplifier
gmoverid = 10;
I = 5e-6;
M = 4;
targetGain = 54;
fug = 1e8;

%define the minimizing funciton used in fminsearch
func = @(x)symAmp(x,model,modelP,targetGain,gmoverid,I,M);

%calculate then display the resulting frequencies used in order to reach
%the target amplification
Z = fminsearch(func,[fug,fug]);



%Use the found frequencies and desired characterists in order to calculate
%the resulting widths and lengths of the transistors
Ncm2 = model.useModel([gmoverid,Z(1),1.65]);
Pcm = modelP.useModel([gmoverid,Z(2),1.65]);
Ndp = model.useModel([10,fug,1.55]);
Ncm1 = model.useModel([10,fug,0.7]);


%display the lengths
Lncm1 = Ncm1(1)
Lncm2 = Ncm2(1)
Lndp = Ndp(1)
Lpcm = Pcm(1)

%display the widths
Wncm1 = I/Ncm1(2)
Wncm2 = M*I/(Ncm2(2)*2)
Wndp = I/(Ndp(2)*2)
Wpcm1 = I/(2*Pcm(2))
Wpcm2 = M*Wpcm1

%calculate the gm and gds values relevant for the amplification of the
%circuit
gmndp = gmoverid*I/2;
gdsP = Wpcm2*Pcm(3);
gdsN = Wncm2*Ncm2(3);

%calculate and display the amplification in decibels 
Amag = M*gmndp/(gdsP+gdsN);
A = 20*log10(Amag)

%calculate and display the -3db frequency of the amplifier
F = (gdsP+gdsN)/(2*pi*20e-12)

%Alternatively, use the eval_amp function, along with the desired Iref and
%M value and the L and W values given from the fminsearch optimization loop,
%and return the gain calculated from the spectre simulation
A = eval_amp(Wncm1,Wncm2,Wndp,Wpcm1,Lncm1,Lncm2,Lndp,Lpcm,Iref,M)

%% Symmetrical Amp with Simulation during Optimization
%This section does essentially the same thing as the above code, however
%instead of using the equation Amag = M*gmndp/(gdsP+gdsN) to calculate the
%gain from the NNmodel outputs, each time within the optimization loop the
%model takes the given frequencies and input characteristics, and runs the
%spectre simulation viat the eval_amp function in order to return the
%amplification that way, then adjusts the frequencies accordingly to tune
%the amplification of the circuit. Depending on how fast your machine is,
%this can take quite a few minutes due to the amount of simulations ran.


%create the objects for the nmos and pmos models
model = NNModel(modelPath);
modelP = NNModel(modelPathP);

%define desired characteristsics for the amplifier
gmoverid = 10;
I = 5e-6;
M = 4;
targetGain = 54;

%define the minimizing funciton used in fminsearch
func = @(x)symAmpSim(x,model,modelP,targetGain,gmoverid,I,M);

%calculate then display the resulting frequencies used in order to reach
%the target amplification
Z = fminsearch(func,[1e8;1e8;1e8;1e8])

%Use the found frequencies and desired characterists in order to calculate
%the resulting widths and lengths of the transistors
Ncm2 = model.useModel([gmoverid,Z(2),1.65]);
Pcm = modelP.useModel([gmoverid,Z(4),1.65]);
Ndp = model.useModel([10,Z(3),1.55]);
Ncm1 = model.useModel([10,Z(1),0.7]);


%display the lengths
Lncm1 = Ncm1(1)
Lncm2 = Ncm2(1)
Lndp = Ndp(1)
Lpcm = Pcm(1)

%display the widths
Wncm1 = I/Ncm1(2)
Wncm2 = M*I/(Ncm2(2)*2)
Wndp = I/(Ndp(2)*2)
Wpcm1 = I/(2*Pcm(2))
Wpcm2 = M*Wpcm1

%Run one last simulation to calculate the resulting gain with the widths
%and lengths
A = eval_amp(Wncm1,Wncm2,Wndp,Wpcm1,Lncm1,Lncm2,Lndp,Lpcm,Iref,M)