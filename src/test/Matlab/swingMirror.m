function result = swingMirror(freqs,model,gmoverid,vr,targetR,Iref)
%This function takes in a target output resistence for a swing current
%mirror and optimizes the frequencies of the transistors (as well as the
%drain voltage of the first transistor) in order to find the correct
%frequency, and then the corresponding width and length for each transistor

%Calculate an estimate as to the vdsat of the transistor at a given
%frequency
Q = model.useModel([gmoverid,freqs(1),1.65,0.0]);

%use the estimated vdsat and the given value vr to calculate the drain
%voltage of the next transistor
Vx = Q(5) + vr;

%With the resulting input charecteristics, the models are run
trans1 = model.useModel([gmoverid,freqs(1),Vx,0.0]);
trans2 = model.useModel([gmoverid,freqs(2),trans1(4)-Vx,-Vx]);

%Widths are found for the two transistors
W1 = Iref/trans1(2);
W2 = Iref/trans2(2);

%The gds values of the transistors are calculated
gds1 = (trans1(3)*W1);
gds2 = (trans2(3)*W2);

%the Rout is calculated for the swing current mirror
Rout = 1/gds1+1/gds2+(gmoverid*Iref+trans2(6)*W2)/(gds1*gds2);

%The Rout is compared to the target
result = abs(Rout-targetR);
