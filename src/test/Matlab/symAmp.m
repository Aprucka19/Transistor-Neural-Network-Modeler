function A = symAmp(freqs,model,modelP,target,gmoverid,I,M)
%symAmp takes in an nmos and pmos NNmodel(where the inputs are gm/id, fug,
%and drain voltage. The models return the gate voltage, id/w, length of
%the transistor, and gds/w with the given input charecteristics. This funciton uses a
%target amplification, model files, gmoverid value, M of your symmetrical
%amplifier, and current, and returns the optimal frequencies of the
%relevant transistors to allow you to automate designing a certain
%performance symmetrical amplifier




%Run input paramenters through the given models
Ncm2 = model.useModel([gmoverid,freqs(1),1.65]);
Pcm = modelP.useModel([gmoverid,freqs(2),1.65]);


%calculate the width of both transistors
Wncm2 = M*I/(Ncm2(2)*2);
Wpcm = M*I/(2*Pcm(2));

%calculate gm and gds of relevant transistors
gmndp = gmoverid*I/2;
gdsP = Wpcm*Pcm(3);
gdsN = Wncm2*Ncm2(3);

%Use the gain equation to calculate the gain, and compare to the given
%target
Amag = M*gmndp/(gdsP+gdsN);
A = 20*log10(Amag);
A = abs(A-target);
end
