function Vdsat = VxFinder(var,fug,gmoverid,vb,model,vr)
%This function takes a model and calculates the propper drain voltage when
%optimized for a certain vr value to ensure the transistor is in
%saturation. It is used in the optimization loop for the swing mirror
%optimizer



a = model.useModel([gmoverid,fug,var,vb]);
vdst = a(5);
Vdsat = abs(var- vr - vdst);
end