%% MAIN FOR FIGURE WHEN MAT FILES AVAILABLE
clear
res_fold = 'assessment_results';
participants = dir(pwd);
ind2rm = false(length(participants),1);
for k=1:length(participants)
    ind2rm(k) = ~participants(k).isdir...
        || ~exist(fullfile(participants(k).name,'standard'),'dir');
end
participants(ind2rm) = [];

mat_dir = 'mat_files';%folder to save mat files

intensity = [0, 0.25];%photon counts below to exclude (e.g. 0.1 => 90% are kept)

maxCounter = 2;%choose a divider of nSettings*length(fnames) (e.g. multiple of 2)
%%
%parpool(4)

for ll = 1:length(participants)
param_input = [];
param_input.fov = [6400, 6400, 1500];%nm, Field Of View
param_input.radTol = [250,500];%nm, (XY and Z)
param_input.pix_siz = 10;%nm, pixel size for rendering
param_input.FWHM = 20;%nm, rendering with Gaussian convolution (FWHM)
param_input.winLen = 5120;%nm, window length XY for FSC
param_input.areaCenter = [3200,3200];%nm, area center for window in FSC
param_input.exclusion = 450;%nm, border exclusion
param_input.opix_siz = 2;%nm, orthoview zoomed pixel size
param_input.oPos = [1920,1920,-750];%(X,Y,Z) = (top, left, bottom),nm for MT
param_input.ofov = [1280,1280,1500];%nm
param_input.doFSC = false;%do FSC or not
param_input.saveFig = false;%save Orthoview (& 3D) or not
param_input.result_folder = res_fold;
param_input.thresRMSE = 62.5;%250/4
param_input.thresMetrics = [0.25,0.5;0.25,0.5;25,5];%[0.5,0.5,0.5];
param_input.Nsamples_smooth = 5;
param_input.Alpha_smooth = 2;
param_input.participant = participants(ll).name;

%Settings

fnames = dir(fullfile(param_input.participant,'standard','MT*'));
fnames = [fnames;dir(fullfile(param_input.participant,'standard','ER*'))];

param_input.wobble_files = dir(fullfile(param_input.participant,'upload','Wobble*'));
param_input.beads_files = dir(fullfile(param_input.participant,'standard','Beads*'));

%mkdir(mat_dir);

fileID = fopen(fullfile(param_input.participant,'upload','wobble.txt'),'r');
wobble = unique([~strcmp(fscanf(fileID,'%s'),'no'),false]);
nSettings = length(intensity)*length(wobble);
fprintf('%s : %i dataset(s), %i setting(s) per dataset => %i run(s)\n',...
    param_input.participant, length(fnames),nSettings,nSettings*length(fnames));
lenPart = nSettings*length(fnames)/maxCounter;

%% Regroup in one variable

if maxCounter > 1
    results = cell(nSettings*length(fnames),1);
    results_mol = results;
    for k = 1:maxCounter
        tmp = load(fullfile(mat_dir,sprintf('%s____results_part_%i.mat',param_input.participant,k)));
        results(1+(k-1)*lenPart:k*lenPart) = tmp.results;
        results_mol(1+(k-1)*lenPart:k*lenPart) = tmp.results_mol;
    end
    tmp = [];
    fprintf('Results loaded and regrouped for %s\n',param_input.participant);
end

%Figures production

%addpath(fullfile(param_input.result_folder,param_input.participant));
filesOI = dir(fullfile(param_input.result_folder,param_input.participant,'pairings*'));
Nfiles = length(filesOI);
modalSet = [];dataSet = [];wobSet = [];
intSet = cell(2,1); intSet{1} = 0;

for ii = 1:Nfiles
    sep = strfind(filesOI(ii).name,'____');
    strDataset = filesOI(ii).name(sep(1)+4:sep(2)-1);
    if isempty(find(strcmp(strDataset,dataSet),1))
        dataSet{end+1} = strDataset;
    end
    strMod = filesOI(ii).name(sep(2)+4:sep(3)-1);
    if isempty(find(strcmp(strMod,modalSet),1))
        modalSet{end+1} = strMod;
    end
    strInt = str2double(filesOI(ii).name(strfind(filesOI(ii).name,'photonT_')+8:sep(7)-1));
    if ~ismember(strInt,intSet{2}) && strInt~=0
        intSet{2} = [intSet{2}, strInt];
    end
    strWob = filesOI(ii).name(strfind(filesOI(ii).name,'wobble_')+7:sep(5)-1);
    if isempty(find(strcmp(strWob,wobSet),1))
        wobSet{end+1} = strWob;
    end
end

if isempty(intSet{2})
    intSet(2) = [];
end
%only wobble on/off is shown on same graph
NelPerFig = numel(wobSet);

if mod(NelPerFig,1)~=0
    error('Number of files in results incorrect');
end
input = cell(NelPerFig,1);
m = 1;
results_graph = cell(length(dataSet)*length(modalSet)*length(intSet),1);

for ii = 1:length(dataSet)
    for jj = 1:length(modalSet)
        for k = 1:length(intSet)
            ind_count = 1;
            for l = 1:Nfiles
                fname = filesOI(l).name;
                dim3Dread = str2double(fname(strfind(fname,'____dim3D_')...
                    +10));
                photon_t = str2double(fname(strfind(fname,'____photonT_')...
                    +12:strfind(fname,'____date')-1));
                if ismember(photon_t, intSet{k})...
                        && any(strfind(fname, dataSet{ii}))...
                        && any(strfind(fname, modalSet{jj}))...
                        && ((strcmp(modalSet{jj},'2D') && dim3Dread==0)...
                        || (~strcmp(modalSet{jj},'2D') && dim3Dread==1))
                    input{ind_count} = fname;
                    ind_count = ind_count + 1;
                end
            end
            if ~all(cellfun(@isempty, input))
                results_graph{m} = assessment_graph(input, 'fov', param_input.fov,...
                    'RMSE', param_input.thresRMSE,'metrics', param_input.thresMetrics,...
                    'Nsamples_smooth',param_input.Nsamples_smooth,...
                    'Alpha_smooth',param_input.Alpha_smooth);%[recall; precision; jaccard]
                m = m + 1;
                input = cell(NelPerFig,1);
            end
        end
    end
end
results_graph = cat(1,results_graph{:});
fprintf('Figures saved and additional metrics calculated\n');
% Save the results file (private and public)
saveResults(results, results_mol, results_graph, res_fold);
savePublic(results, results_mol, results_graph, res_fold);
fprintf('Results saved\n');
end