function perf_metrics = assessment_mol(fpath, result_fname)
%ASSESSMENT_MOL : produces some metrics based on molecule
%   -Averaged position among paired ones
%   -TP as long as one there is one pairing
%   Written by Thanh-an Pham, 2016
try
    pairings = csvread([fpath, filesep, result_fname]);
catch
    warning('No pairings at all !\n');
    perf_metrics = setMetrics(0,nan,0,0,0,0,0, 0,0);
    return;
end

%FP = result.FP;
nFeat = str2double(result_fname(strfind(result_fname,'____nFeat_')+10:strfind(result_fname,'.csv')-1));
[~, ind_order] = sort(pairings(:,1));

pairings_sorted = pairings(ind_order,:);

%diff on [x,y,z], hoping no round-error => worst case : apply round(...)
Nmol = diff(find([1;diff(pairings_sorted(:,2))]...
    | [1;diff(pairings_sorted(:,3))]...
    | [1;diff(pairings_sorted(:,4))]));

TP = 0;
FN = 0;
RMSExy = zeros(length(Nmol),1);
RMSExyz = RMSExy; RMSEz = RMSExy;
MADxy = RMSExy; MADz = MADxy; MADxyz = MADxy;
ratio_det_per_mol = RMSExy;

for m = 1:length(Nmol)
    curr_ind = 1 + sum(Nmol(1:m-1));
    paired_mol = pairings_sorted(curr_ind:curr_ind + Nmol(m)-1,1+end - nFeat:end);
    gt_pos = pairings_sorted(curr_ind,1:end-nFeat);%same position
    if all(isnan(paired_mol(:)))
        FN = FN + 1;
    else
        TP = TP + 1;
        Ndetection = sum(~isnan(paired_mol(:,1)));
        avePos = sum(paired_mol(:,2:4),1,'omitnan')/Ndetection;
        RMSExyz(m) = norm(avePos - gt_pos(2:4),2)^2;
        RMSExy(m) = sum((avePos(1:2) - gt_pos(2:3)).^2);
        RMSEz(m) = (avePos(end) - gt_pos(4))^2;
        MADxyz(m) = sum(abs(avePos - gt_pos(2:4)));
        MADxy(m) = sum(abs(avePos(1:2) - gt_pos(2:3)));
        MADz(m) = abs(avePos(3) - gt_pos(4));
        ratio_det_per_mol(m) = Ndetection/Nmol(m);
    end
end
perf_metrics = setMetrics(TP,FN,RMSExy,RMSEz,RMSExyz,...
    MADxy, MADz, MADxyz,ratio_det_per_mol);
end

function perf_metrics = setMetrics(TP,FN,RMSExy,RMSEz,RMSExyz,...
    MADxy, MADz, MADxyz,ratio_det_per_mol)
perf_metrics.TPmol = TP;
perf_metrics.FNmol = FN;
perf_metrics.RMSExy_mol = sqrt(sum(RMSExy)/TP);
perf_metrics.RMSEz_mol = sqrt(sum(RMSEz)/TP);
perf_metrics.RMSExyz_mol = sqrt(sum(RMSExyz)/TP);
perf_metrics.MADxy_mol = sum(MADxy)/TP;
perf_metrics.MADz_mol = sum(MADz)/TP;
perf_metrics.MADxyz_mol = sum(MADxyz)/TP;
%perf_metrics.Jaccard_mol = TP/(FN + FP + TP);
perf_metrics.recall_mol = TP/(TP + FN);
%perf_metrics.precision_mol = TP/(TP + FP);
%perf_metrics.Fscore_mol = 2*perf_metrics.precision_mol*perf_metrics.rate_detection_mol...
%    /(perf_metrics.precision_mol + perf_metrics.recall_mol);
perf_metrics.ratio_det_per_mol_ave = sum(ratio_det_per_mol)/TP;
end

