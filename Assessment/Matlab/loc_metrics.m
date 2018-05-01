function [out,pairings,fig_corr] = loc_metrics(testPos, truePos, radTol, pairings, dim3D, varargin)
%EVALUATION localization assessment with radius tolerance radTol
% INSPIRED BY JAVA CODE OF LOCALIZATION AVAILABLE ON 2016 ISBI CHALLENGE WEBSITE
% Provides usual metrics such as recall, precision, etc.
% Follows Sage's definition
% positions are N molecules x 3 vectors with columns for frame, indx & indy
%           in nm
% varargin : 'name', value
% 'index' : indices for param.frames, param.indx and param.indy (struct
% expected)
% Output : Metrics with max values are 1 except for accuracy, distX and distY (nm)
%          pairings between reference and tested positions
% NOTE : If dim3D -> 2 tolerances (XY & Z) and rmse XYZ used for comparison
%        If ~dim3D -> 1 tolerance (XY) and rmse XY used for comparison
%   Written by Thanh-an Pham, 2016
param = struct;

radTolZ = inf;
for k=1:length(radTol)
    if k==1
        radTolXY = radTol(k);
    elseif k==2 && dim3D
        radTolZ = radTol(k);
    end
end
out.radTolXY = radTolXY;
out.radTolZ = radTolZ;
colPhotons = 6;
out.thresPhotons = 0;
k=1;
while k <= nargin - 5
    switch varargin{k}
        case 'index'
            param = varargin{k+1};
        case 'estFluor'
            out.estFluor4metrics = varargin{k+1};
        case 'trueFluor'
            out.trueFluor4metrics = varargin{k+1};
        case 'thresPhotons'
            out.thresPhotons = varargin{k+1};
        case 'colPhotons'
            colPhotons = varargin{k+1};
    end
    k = k+2;
end

nFeat = size(testPos, 2);

pairings = [pairings, nan(size(pairings,1), nFeat)];

if isempty(fieldnames(param))
    param.frames = 1;
    param.indx = 2;
    param.indy = 3;
    param.indz = 4;
end
nframes = max(truePos(:, param.frames));
A = cell(nframes,1);%REFERENCE
B = cell(nframes,1);%TEST

TPframe = zeros(nframes,1);
Na = zeros(nframes,1);
Nb = zeros(nframes,1);
RMSExy = 0;
RMSEz = 0;
RMSExyz = 0;
MADxy = 0;
MADz = 0;
MADxyz = 0;
dX = 0;
dY = 0;
dZ = 0;
iter_pair = 0;

for k=1:nframes
    %fprintf('%i,',k);
    A{k} = truePos(truePos(:,param.frames) == k, [param.indx,param.indy,param.indz]);%REFERENCE
    B{k} = testPos(testPos(:,param.frames) == k, :);%TEST
    Na(k) = size(A{k},1);
    Nb(k) = size(B{k},1);
    [distXYZ,distXY, distX, distY, distZ] = arrayfun(@(x,y,z) distEuc(x, y, z,...
        B{k}(:,[param.indx,param.indy,param.indz])),...
        A{k}(:,1), A{k}(:,2), A{k}(:,3),'UniformOutput',false);
    if dim3D
        distMat = distXYZ;
    else
        distMat = distXY;
    end
    distMat = reshape(cell2mat(distMat),[Nb(k),Na(k)]);
    distX = reshape(cell2mat(distX),[Nb(k),Na(k)]);
    distY = reshape(cell2mat(distY),[Nb(k),Na(k)]);
    distZ = reshape(cell2mat(distZ),[Nb(k),Na(k)]);
    distXYZ = reshape(cell2mat(distXYZ),[Nb(k),Na(k)]);
    distXY = reshape(cell2mat(distXY),[Nb(k),Na(k)]);
    done = isempty(distMat);
    while ~done
        [~, ind] = min(distMat(:));
        if distXY(ind) <= radTolXY %comme dans java
            if abs(distZ(ind)) <= radTolZ
                [row, col] = ind2sub(size(distMat), ind);
                pairings(iter_pair + col, end-nFeat + 1:end) = B{k}(row,:);
                if pairings(iter_pair + col, colPhotons) > out.thresPhotons
                    RMSExyz = RMSExyz + distXYZ(ind)^2;
                    RMSExy = RMSExy + distXY(ind)^2;
                    RMSEz = RMSEz + distZ(ind)^2;
                    dX = dX + distX(ind);
                    dY = dY + distY(ind);
                    dZ = dZ + distZ(ind);
                    MADxyz = MADxyz + abs(distX(ind)) + abs(distY(ind)) + abs(distZ(ind));
                    MADxy = MADxy + abs(distX(ind)) + abs(distY(ind));
                    MADz = MADz + abs(distZ(ind));
                    TPframe(k) = TPframe(k) + 1;
                end
                distMat(row,:) = nan; distMat(:,col) = nan;
                distZ(row,:) = nan; distZ(:,col) = nan;
                distXY(row,:) = nan; distXY(:,col) = nan;
                done = all(isnan(distMat(:)));
            elseif min(abs(distZ(:))) > radTolZ
                done = true;
            else %might still have some points acceptable
                distMat(ind) = nan;
                distZ(ind) = nan;
                distXY(ind) = nan;
            end
        elseif min(distXY(:)) > radTolXY
            done = true;
        else %might still have some points acceptable, should never be reached
            distMat(ind) = nan;
            distZ(ind) = nan;
            distXY(ind) = nan;
        end
    end
    iter_pair = iter_pair + Na(k);
end

if ~isfield(out,'trueFluor4metrics')
    out.trueFluor4metrics = sum(Na);
end

if ~isfield(out,'estFluor4metrics')
    out.estFluor4metrics = sum(Nb);
end

%Threshold on photon
ThresfluorCountGT = sum(pairings(:, colPhotons) <= out.thresPhotons);
%Remove from FP count the localizations paired with photon-thresholded GT fluors
ThresfluorCountTest = sum(pairings(:, colPhotons) <= out.thresPhotons & ~isnan(pairings(:,end)));

pairings = pairings(pairings(:, colPhotons) > out.thresPhotons,:);

TP = sum(TPframe);
FN = out.trueFluor4metrics - TP - ThresfluorCountGT;
FP = out.estFluor4metrics - TP - ThresfluorCountTest;
recall = TP/(TP + FN);
precision = TP/(TP + FP);
Fscore = 2*precision*recall/(precision + recall);
Jaccard = TP/(FN + FP + TP);

out.RMSExy = sqrt(RMSExy/TP);
out.RMSEz = sqrt(RMSEz/TP);
out.RMSExyz = sqrt(RMSExyz/TP);
out.MADxy = MADxy/TP;
out.MADz = MADz/TP;
out.MADxyz =MADxyz/TP;

out.dim3D = dim3D;
out.estFluorFrame = Nb;
out.trueFluorFrame = Na;
out.TPframe = TPframe;
out.FNframe = Nb - TPframe;
out.FPframe = Na - TPframe;
out.TP = TP;
out.FN = FN;
out.FP = FP;
out.recall = recall;
out.precision = precision;
out.Fscore = Fscore;
out.Jaccard = Jaccard*100;%percentage
out.distX = dX/TP;
out.distY = dY/TP;
out.distZ = dZ/TP;
%coeff correlation # photons
if all(isnan(pairings(:,end)))
    out.corrPhoton = 0;
else
    out.corrPhoton = corr(pairings(~isnan(pairings(:,end)),6),...
        pairings(~isnan(pairings(:,end)),end));
end
fig_corr = figure;
scatter(pairings(~isnan(pairings(:,end)),6),...
    pairings(~isnan(pairings(:,end)),end),1,'filled');hold on;
xlabel('Emitted photons - Ground truth');
ylabel('Emitted photons - Software');
id = 1:max(pairings(~isnan(pairings(:,end)),6));
plot(id, id,'black');
end

function [distXYZ, distXY, diffX, diffY, diffZ] = distEuc(x,y,z,pos)

diffX = pos(:,1) - x;
diffY = pos(:,2) - y;
diffZ = pos(:,3) - z;

distXYZ = sqrt(diffX.^2 + diffY.^2 + diffZ.^2);

distXY = sqrt(diffX.^2 + diffY.^2);


end
