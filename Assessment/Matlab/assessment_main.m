%% ASSESSMENT PROGRAM (MAIN) NOT THE MOST RECENT (SEE ASSESSMENT_MAIN_AUTOMATIC.M)
% ASSESSMENT SCRIPT written by Thanh-an Pham (EPFL): 12-Jul-2016
% Expect standardized input files in folder 'participant_name/standard'
% converted by participant-specific converter script "convert_(participant_name).m"
%% Parameters
clear
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
param_input.result_folder = 'test_photon';
param_input.participant = 'TVSTORM';

%below overwritten in for boucles
%param_input.int_thres = 0;%percentage or absolute value
%param_input.dim3D = true;%boolean false (true) for pairing distance: rmseXY (rmseXYZ) and tolerance XY (+ Z)
%param_input.wobble = true;

%% Settings

fnames = dir([param_input.participant,filesep,'standard',filesep,'MT*']);
fnames = [fnames;dir([param_input.participant,filesep,'standard',filesep,'ER*'])];

intensity = [0,0.15,0.25,0.35,0.45];%,0.25];%photon counts below to exclude (e.g. 0.1 => 90% are kept)
dim3D = [true];
wobble = [true,false];

nSettings = length(intensity)*length(dim3D)*length(wobble);
fprintf('%i dataset(s), %i setting(s) per dataset => %i run(s)\n',length(fnames),nSettings,nSettings*length(fnames));

param_input.wobble_files = dir([param_input.participant,filesep,'upload',filesep,'Wobble*']);
param_input.beads_files = dir([param_input.participant,filesep,'standard',filesep,'Beads*']);

maxCounter = 1;%choose a divider of nSettings*length(fnames) (e.g. multiple of 2)
lenPart = nSettings*length(fnames)/maxCounter;

%% Assessment for one participant's files ---Frame & Molecule based---
param_input.firstTime = true;%for folder existence verification

results = cell(lenPart,1);
results_mol = results;
overalltimer = tic;
l = 1;counter = 1;
for int_iter = 1:length(intensity)
    for dim3D_iter = 1:length(dim3D)
        for wobble_iter = 1:length(wobble)
            for k = 1:length(fnames)
                dataset_timer = tic;
                param_input.int_thres = intensity(int_iter);
                param_input.dim3D = dim3D(dim3D_iter);
                param_input.wobble = wobble(wobble_iter);
                param_input.test_name = fnames(k).name;
                results{l} = assessment_frame(param_input);
                %only requires name
                results_mol{l} = assessment_mol(results{l}.res_path,...
                    results{l}.fname_pairings);
                fprintf('Time for dataset/settings %i : %1.3f s\n',(counter-1)*lenPart + l,toc(dataset_timer));
                param_input.firstTime = false;
                l = l + 1;
                if l > lenPart && maxCounter > 1 %memory trouble ? One mat file > 700mb
                    save(sprintf('%s____results_part_%i.mat',param_input.participant,counter),'results','results_mol','-v7.3');
                    l = 1;
                    counter = counter + 1;
                end
            end
        end
    end
end
fprintf('Assessment done for %s in %f\n',param_input.participant,toc(overalltimer))

%% Regroup in one variable
if maxCounter > 1
    results = cell(nSettings*length(fnames),1);
    results_mol = results;
    for k = 1:maxCounter
        tmp = load(sprintf('%s____results_part_%i.mat',param_input.participant,k));
        results(1+(k-1)*lenPart:k*lenPart) = tmp.results;
        results_mol(1+(k-1)*lenPart:k*lenPart) = tmp.results_mol;
    end
    clear tmp
end
fprintf('Results loaded and regrouped\n');

%% Figures production

%uncomment only if necessary, can super slow down (due to matlab 2016)
%addpath(genpath([param_input.result_folder,filesep,param_input.participant]));

filesOI = dir([param_input.result_folder,filesep,param_input.participant,filesep,'pairings*']);
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
NphotonSettings = numel(intSet);
%only wobble on/off is shown on same graph
NelPerFig = numel(wobSet);

if mod(NelPerFig,1)~=0
    error('Number of files in results incorrect');
end
input = cell(NelPerFig,1);
m = 1;
clear results_graph

for ii = 1:length(dataSet)
    for jj = 1:length(modalSet)
        for k = 1:NphotonSettings
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
            results_graph{m} = assessment_graph(input, 'fov', param_input.fov,...
                'RMSE',62.5,'metrics', [0.5,0.5,0.4]);%[recall, precision, jaccard]
            m = m + 1;
        end
    end
end
results_graph = cat(2,results_graph{:});
fprintf('Figures saved and additional metrics calculated\n');

%% Save the results file (private and public)
saveResults(results, results_mol, [], param_input.result_folder);
savePublic(results, results_mol, [], param_input.result_folder);
fprintf('Results saved\n');
