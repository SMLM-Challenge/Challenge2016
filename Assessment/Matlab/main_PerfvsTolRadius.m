%% Plot the performance vs radius tolerance curves

clear pos
path = '~/Dropbox/smlm/figures/';
software = 'MIATool-RMS';%'SMAP'
modality = 'AS';
dataset = 'MT1.N1.LD';
wobble = false;
photonT = true;
%File must be in the path
if wobble
    fname = dir(fullfile('assessment_results',software,...
        sprintf('pairings____%s____%s____%s____wobble_%s____border_450____photonT_*',...
        dataset,modality,software,'beads')));
    if isempty(fname)
        fname = dir(fullfile('assessment_results',software,...
            sprintf('pairings____%s____%s____%s____wobble_%s____border_450____photonT_*',...
            dataset,modality,software,'file')));
    end
    if isempty(fname)
        error('No file found');
    end
else
    fname = dir(fullfile('assessment_results',software,...
        sprintf('pairings____%s____%s____%s____wobble_%s____border_450____photonT_*',...
    dataset,modality,software,'no')));
end

iter = 0;
for kk = 1:length(fname)
    if ~isempty(strfind(fname(kk - iter).name,'photonT_0_'))
        fname(kk - iter) = [];
        iter = iter + 1;
    end
end
fname = fullfile('assessment_results',software,fname(1).name);

fprintf('Reading file %s\n',fname);

%% Load files

pairings = importFullPairings(fname);
pairings = pairings(:,[1:24,27:end]);
pairings.Properties.VariableNames = {'ID','X','Y','Z','Frame','Photons',...
'Channel','Frame_ON','Total','Background_Mean','Background_Stdev',...
'Signal_Mean','Signal_Stdev','Signal_Peak','Sigma_X','Sigma_Y','Sigma_Z',...
'Uncertainty','Closest_ID','Closest_Distance','Closest_Count','CNR',...
'SNR','PSNR','FrameSoft','XSoft','YSoft','ZSoft','PhotonsSoft'};

res = importPublicRes(fullfile('assessment_results',software,modality,'public.csv'));
res = res(2:end,:);
%% Calculate distance
dist2D = sqrt((pairings.X - pairings.XSoft).^2 + (pairings.Y - pairings.YSoft).^2);
if strcmp(modality,'2D')
    dist = dist2D;
else
    dist = dist2D + (pairings.Z - pairings.ZSoft).^2;
end

dist = sqrt(dist);

[sort_dist,ind_sort] = sort(dist);

%% Compute the metrics wrt lateral tolerance

TolRad = 0:250;
Npoints = length(TolRad);
R = height(pairings);
ind_res = strcmpi(res.Dataset,dataset) ...
    & strcmpi(res.Modality,modality) ...
    & xor(wobble,strcmpi(res.Wobble,'no'))...
    & xor(photonT>0,res.ThresPhoton==0);
S = res.TP(ind_res) + res.FP(ind_res);
alp = 1;
TP = zeros(Npoints,1); Recall = TP; FP = TP; Precision=TP;
JAC=TP;RMSE2D =TP; Efficiency = TP;MAD2D = TP;

for kk = 1:Npoints
    curr_mol = dist2D <= TolRad(kk);
    TP(kk) = nnz(curr_mol);
    FP(kk) = S - TP(kk);
    Recall(kk) = TP(kk)/R;
    Precision(kk) = TP(kk)/S;
    JAC(kk) = TP(kk)/(S + R - TP(kk));
    RMSE2D(kk) = sqrt(mean(dist2D(curr_mol).^2));
    MAD2D(kk) = mean(abs(pairings.X(curr_mol) - pairings.XSoft(curr_mol))...
        + abs(pairings.Y(curr_mol) - pairings.YSoft(curr_mol)));
    Efficiency(kk) = 100 - sqrt((100 - 100*JAC(kk))^2 + (alp*RMSE2D(kk))^2);
end
%%
siz = 2;
figure(10);
plot(TolRad,Recall,'LineWidth',siz);hold on;
plot(TolRad,Precision,'LineWidth',siz);
plot(TolRad,JAC,'LineWidth',siz);
title('Recall, Precision & Jaccard Index');
xlabel('Tolerance Radius [nm]');
ylabel('Metrics');
legend('Recall','Precision','Jaccard Index','Location','Best');
axis([0,250,0,1]);
hold off;
figure(11);
plot(TolRad,RMSE2D,'LineWidth',siz);
title('RMSE');
xlabel('Tolerance Radius [nm]');
ylabel('RMSE [nm]');
axis([0,250,0,150]);

figure(12);
plot(TolRad,Efficiency,'LineWidth',siz);hold on;
plot(TolRad,100*JAC,'LineWidth',siz);
plot(TolRad,RMSE2D,'LineWidth',siz);hold off;
title('Efficiency');
xlabel('Tolerance Radius [nm]');
ylabel('Efficiency, Jaccard Index, RMSE');
legend('Efficiency', 'Jaccard Index', 'RMSE','Location','Best');
%axis([0,250,0,150]);
%%
figure(13)
scatter3(TolRad,JAC,RMSE2D);