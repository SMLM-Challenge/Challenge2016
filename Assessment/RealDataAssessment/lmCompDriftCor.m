function savename = lmCompDriftCor(fname )

data =importdata(fname);
fr = data(:,1);
x= data(:,2);
y= data(:,3);
z= data(:,4);
if size(data,2)>4
    phot= data(:,5);
end

drift=driftcorrection3D_so(x,y,z,fr,[]);%4th arg is the parameters arg
%have to supply p even if its just empty (standard)
figname = [fname(1:end-4),'_drift.fig'];
saveas(gcf,figname);

pos.xnm=x;
pos.ynm=y;
pos.znm=z;
pos.frame=fr;

poso=applydriftcorrection(drift,pos);

xcorr=poso.xnm;
ycorr=poso.ynm;
zcorr=poso.znm;

if size(data,2)>4
    dataout = [fr,xcorr,ycorr,zcorr,phot];
else
    dataout = [fr,xcorr,ycorr,zcorr];
end
savename = [fname(1:end-4),'_driftCorr.csv'];
dlmwrite(savename,dataout,',');
