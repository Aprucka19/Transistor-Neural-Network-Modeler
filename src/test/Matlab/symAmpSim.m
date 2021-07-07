function A = symAmpSim(freqs,model,modelP,target,gmoverid,I,M,fileID)
%symAmp takes in an nmos and pmos NNmodel(where the inputs are gm/id, fug,
%and drain voltage. The models return the gate voltage, id/w, length of
%the transistor, and gds/w with the given input charecteristics. This funciton uses a
%target amplification, model files, gmoverid value, M of your symmetrical
%amplifier, and current, and returns the optimal frequencies of the
%relevant transistors to allow you to automate designing a certain
%performance symmetrical amplifier
for m = 1:4
    if freqs(m) < 1
        freqs(m) = 1;
    end
end




%Run input paramenters through the given models
Ncm1 = model.useModel([gmoverid,freqs(1),0.7]);
Ncm2 = model.useModel([gmoverid,freqs(2),1.65]);
Ndp = model.useModel([gmoverid,freqs(3),1.55]);
Pcm = modelP.useModel([gmoverid,freqs(4),1.65]);

%calculate the width of both transistors
Lncm1 = Ncm1(1);
Lncm2 = Ncm2(1);
Lndp = Ndp(1);
Lpcm = Pcm(1);

%display the widths
Wncm1 = I/Ncm1(2);
Wncm2 = M*I/(Ncm2(2)*2);
Wndp = I/(Ndp(2)*2);
Wpcm1 = I/(2*Pcm(2));

fprintf(fileID,"Freqs: %f %f %f %f \nWidths: %f %f %f %f\nLengths: %f %f %f %f\nI and M: %f %f\n\n\n",freqs,Wncm1,Wncm2,Wndp,Wpcm1,Lncm1,Lncm2,Lndp,Lpcm,I,M);

Aval = eval_amp(Wncm1,Wncm2,Wndp,Wpcm1,Lncm1,Lncm2,Lndp,Lpcm,I,M);
A = abs(Aval - target);
end