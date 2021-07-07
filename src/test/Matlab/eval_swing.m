function Rout = eval_swing(Iref,L11,L21,W11,W21)

timestamp=num2str(posixtime(datetime('now')) * 1e6);
    
system("cp swing.txt swing" + timestamp + ".scs");

%replace placeholders in netlist with transistor parameters
system("sed -i -e ""s/\${Iref}/"  + Iref  + "/""" + ...
             " -e ""s/\${L11}/"  + L11  + "/""" + ...
             " -e ""s/\${L21}/" + L21 + "/""" + ...
             " -e ""s/\${W11}/" + W11 + "/""" + ...
             " -e ""s/\${W21}/" + W21 + "/""" + ...
             " swing" + timestamp + ".scs");


system("module load spectre && "+ ... 
    "CDS_DIR=/home/f_plasma/plasma_ic/plasma_ic_xh035/users/pruckaa/cds " + ...
    "spectre swing" + timestamp + ".scs &> /dev/null");


file = "swing" + timestamp + ".raw";

plot = readNutascii(file);
plots = plot(1).waveData(:,:);
x = plots(:,3);
y = plots(:,2);
z = round(100*plots(1,1))/100;
grads = gradient(y(:)) ./ gradient(x(:));

rmdir("swing" + timestamp + ".raw.psf",'s');
delete("swing" + timestamp + ".log");
delete("swing" + timestamp + ".scs");
delete("swing" + timestamp + ".raw");

Rout = grads(y == z);


