function wobbleCorrectSimBead(xnm,ynm,frame,gt,zmin,zstep,zmax,roiRadius,frameIsOneIndexed)

nBead = size(gt,1);
for ii = 1:nBead
    beadPos = gt(ii,:);
    ROInm(ii,1:2) = beadPos-roiRadius;%xmin ymin
    ROInm(ii,3:4) = 2*roiRadius;%width height
end

zSlice = zmin:zstep:zmax;
if ~frameIsOneIndexed
    frame = frame+1;%have to account for possilbe zero-indexing or everthing will get screwed up
end
znm = zSlice(frame)';

wobbleMatrix = wobbleCalibration(xnm, ynm, znm, nBead, 'ROI', ROInm, 'Zfit', znm, 'NumSplineBreak', 10,...
    'GT', gt);
[~, indCorr] = unique(wobbleMatrix(:,1));
wobbleMatrixUnique = wobbleMatrix(indCorr,[2,3,1]);
%save in csv file, units : nm, column order : X Y Z
csvwrite('wobbleCorr.csv', wobbleMatrixUnique);
