fname= 'NPC-A647-3D-stack_.csv';
fdata=importdata(fname);
x   = fdata.data(:,1);
y   = fdata.data(:,3);
z   = fdata.data(:,5);
fr  = fdata.data(:,6);
drift=driftcorrection3D_so(x,y,z,fr,[]);%4th arg is the parameters arg
%have to supply p even if its just empty (standard)

pos.xnm=x;
pos.ynm=y;
pos.znm=z;
pos.frame=fr;

poso=applydriftcorrection(drift,pos);

xcorr=poso.xnm;
ycorr=poso.ynm;
zcorr=poso.znm;

