function perf_metrics = assessment_frame(param_input)
%ASSESSMENT Performance assessment for SMLM Challenge 2016
% INPUT
% param_input : parameters structure
% OUTPUT
% perf_metrics : structure regrouping the performance assessment results
% and Gaussian rendered of gt and res in addition
%   Written by Thanh-an Pham, 2016
%% Parameters initialization
fov = param_input.fov;%nm, field of view
pix_siz = param_input.pix_siz;%nm, pixel size for rendering
radTol = param_input.radTol;%nm, XY tolerance radius (circle)
exclusion = param_input.exclusion;%nm, exclusion of results from the border
int_thres = param_input.int_thres;%percentage for thresholded intensity (wrt maximum per frame ?) or absolute value
sig = param_input.FWHM/(2*sqrt(2*log(2)));%nm, sigma for Gaussian rendering (expect the FWHM)
saveFig = param_input.saveFig;

files_path = fullfile(param_input.participant,'standard');%String, path/folder name containing the results
test_name = param_input.test_name;%String, name of the results file
splitPos = strfind(test_name,'____');
dataset_name = test_name(1:splitPos(1)-1);
modality = test_name(splitPos(1)+4:splitPos(2)-1);
participant = test_name(splitPos(2)+4:splitPos(3)-1);
gt_fname = fullfile('Ground_truth',dataset_name,'activations.csv');

doFSC = param_input.doFSC && ~strcmp(modality,'2D');

detail_fname = fullfile('Ground_truth',dataset_name, strcat(modality,...
    ('-Exp'*~strcmp(modality,'BP') + '-250'*strcmp(modality,'BP'))*~strcmp(modality,'DHNPC')),...
    'oracle', 'activation-snr.csv');

res.res_path = fullfile(param_input.result_folder, participant);

if exist(res.res_path,'dir') && param_input.firstTime
    %means a previous run (files) might exist, must rename the folder
    k = 1; done = false;
    while ~done
        folder_name = [res.res_path,'_attempt_', num2str(k)];
        if ~exist(folder_name,'dir')
            movefile(res.res_path, folder_name);
            done = true;
        end
        k = k + 1;
    end
    clear k
    mkdir(param_input.result_folder, participant);
end

if ~exist(fullfile(res.res_path, modality),'dir')
    mkdir(res.res_path, modality);
    mkdir(fullfile(res.res_path, modality),'png');
    mkdir(fullfile(res.res_path, modality),'data');
end
%copy converter to public folder
participant_tmp = participant;
participant_tmp(strfind(participant_tmp,'-')) = '';%filename cannot have this character
copyfile(fullfile('code','converter',sprintf('convert_%s.m', participant_tmp)),...
    fullfile(res.res_path, modality));
%copy index.html to public folder
%copyfile('index.html',fullfile(res.res_path, modality));
%% Data reading

res.loc = csvread(fullfile(files_path, test_name));%participant localizations : frame,x,y,z,photons
gt = csvread(gt_fname,1,1);%activation.csv : frame,xyz,intensity (first column ignored)
pairings = dlmread(detail_fname);%activation-snr.csv
%% Assessment settings

%Check wobble setting
if param_input.wobble %boolean to deactivate wobble
    fileID = fopen(fullfile(participant,'upload','wobble.txt'),'r');
    
    res.wobble = fscanf(fileID,'%s');
    
    switch res.wobble
        case 'no'
            fprintf('No wobble correction required\n');
            res.wobble_file = [];
            wobble_corr = [];
        case 'file'
            fprintf('Wobble correction loaded from uploaded file...\n');
            w=1;
            while w<=length(param_input.wobble_files)
                if ~isempty(strfind(param_input.wobble_files(w).name,...
                        strcat('Wobble____',modality,'____',participant)))
                    wob_file = param_input.wobble_files(w).name;
                    w=inf;
                end
                w = w+1;
            end
            try
                wobble_corr = csvread(fullfile(param_input.participant,'upload',wob_file));
            catch
                wobble_corr = csvread(fullfile(param_input.participant,'upload',wob_file),1,0);
            end
            fprintf(['   ',wob_file,'\n']);
            res.wobble_file = fullfile(files_path, wob_file);
        case 'beads'
            fprintf('Wobble correction calculated from uploaded (standardized) beads localization\n');
            w=1;
            while w <= length(param_input.beads_files)
                if ~isempty(strfind(param_input.beads_files(w).name,...
                        strcat('Beads____',modality,'____',participant)))
                    beads_file = param_input.beads_files(w).name;
                    w = inf;
                end
                w = w+1;
            end
            wobble_fname = ['wobble____',modality,'____',participant,'.csv'];
            zmin = -750;zmax = 750;zstep = 10;roiRadius = 500;%nm
            beads_loc = csvread(fullfile(files_path, beads_file),0,0);
            beads_gt = csvread(fullfile('Ground_truth','Beads','activations.csv'));
            beads_gt = unique(beads_gt(:,3:4),'rows');
            xnm = beads_loc(:,2); ynm = beads_loc(:,3); frame = beads_loc(:,1);
            %not used, because makes it worse for ThunderSTORM
            %[xnm, ynm, frame] = simBeadLocCorr(beads_loc(:,2),...
            %    beads_loc(:,3), beads_loc(:,1), beads_gt);%indices should be the same
            wobbleCorrectSimBead(xnm,ynm,frame,beads_gt,zmin,zstep,zmax,...
                roiRadius,fullfile(res.res_path,wobble_fname));
            wobble_corr = csvread(fullfile(res.res_path, wobble_fname));
            fprintf(['    ',beads_file,'\n']);
            res.wobble_file = fullfile(res.res_path,wobble_fname);
        otherwise
            error('Error in reading wobble.txt');
    end
    fclose(fileID);
    
    if ~isempty(wobble_corr)
        %copy wobble csv file to public folder
        csvwrite(fullfile(res.res_path,modality,...
            sprintf('Wobble____%s.csv',participant)), wobble_corr);
        zdiff = arrayfun(@(x) abs(x - gt(:,4)), wobble_corr(:,end),'UniformOutput',false);
        zdiff = squeeze(cat(3,zdiff{:}));
        [~, ind_zdiff] = min(zdiff,[],2);
        gt(:,2:3) = gt(:,2:3) + wobble_corr(ind_zdiff,1:2);
        zdiff = arrayfun(@(x) abs(x - pairings(:,4)), wobble_corr(:,end),'UniformOutput',false);
        zdiff = squeeze(cat(3,zdiff{:}));
        [~, ind_zdiff] = min(zdiff,[],2);
        pairings(:,2:3) = pairings(:,2:3) + wobble_corr(ind_zdiff,1:2);
    end
else
    res.wobble = 'no';
    res.wobble_file = [];
end
res.nloc_gt_initial = size(gt,1);
res.nloc_test_initial = size(res.loc,1);

if saveFig
    %Orthoview before border/photon exclusion
    close all
    save_folder = fullfile(res.res_path,'figures', 'orthoview');
    if ~exist(fullfile(save_folder,'eps'),'dir')...
            || ~exist(fullfile(save_folder,'png'),'dir')...
            || ~exist(fullfile(res.res_path, 'figures', '3D'),'dir')
        mkdir(save_folder,'eps');
        mkdir(save_folder,'png');
        mkdir(fullfile(res.res_path,'figures', '3D'),'eps');
        mkdir(fullfile(res.res_path,'figures', '3D'),'png');
    end
    fname_orth = sprintf('%s %s %s wobble %s',participant,dataset_name,modality,res.wobble);
    
    ortho_loc = res.loc(:,2:4);
    ortho_gt = gt(:,2:4);
    
    switch dataset_name(1:2)
        case 'ER'
            if strcmp(dataset_name(3),'1')
                cubeArea = [2850 950 -750 param_input.ofov];%5540 2730
            elseif strcmp(dataset_name(3),'2')
                cubeArea = [2160 1250 -750 param_input.ofov];
            end
        case 'MT'
            cubeArea = [param_input.oPos, param_input.ofov];
    end
    [Iref, Iest] =...
        im_metrics(ortho_loc, ortho_gt, sig,...
        pix_siz,fov/pix_siz,0,0,0,'renderOnly',true);
    
    im3D = Iest{1};%for 3D below
    
    h1 = dispOrthoView(fname_orth,Iest,Iref,[],...
        'cube',cubeArea./pix_siz,'2D',strcmp(modality,'2D'));
    
    %Zoomed area
    ortho_loc = ortho_loc - repmat(cubeArea(1:3),[res.nloc_test_initial,1]);
    ortho_gt = ortho_gt - repmat(cubeArea(1:3),[res.nloc_gt_initial,1]);
    
    [Iref, Iest] =...
        im_metrics(ortho_loc, ortho_gt, 2*sig/pix_siz*param_input.opix_siz, param_input.opix_siz,...
        param_input.ofov/param_input.opix_siz,0,0,0,'renderOnly',true,'doCorr',false);
    
    h2 = dispOrthoView([fname_orth,' zoom'], Iest, Iref,[],'2D',strcmp(modality,'2D'));
    
    h1.InvertHardcopy = 'off';
    h2.InvertHardcopy = 'off';
    
    saveas(h1,fullfile(save_folder,'eps',[fname_orth,'.eps']),'epsc');
    saveas(h1,fullfile(save_folder,'png',[fname_orth,'.png']),'png');
    saveas(h1,fullfile(res.res_path,modality,'png',[fname_orth,'.png']),'png');
    
    fname_orth = strcat(fname_orth,'_zoom');
    
    saveas(h2,fullfile(save_folder,'eps',[fname_orth,'.eps']),'epsc');
    saveas(h2,fullfile(save_folder,'png',[fname_orth,'.png']),'png');
    saveas(h2,fullfile(res.res_path,modality,'png',[fname_orth,'.png']),'png');
    
    %3D
    if ~strcmp(modality,'2D')
        fname_3D = sprintf('%s %s %s wobble %s',participant,dataset_name,modality,res.wobble);
        fig3D = disp3D(res.loc,fname_3D,im3D,pix_siz);
        fname_3D = sprintf('%s____%s____%s____wobble____%s____3D',participant,dataset_name,modality,res.wobble);
        saveas(fig3D{1},fullfile(res.res_path,'figures', '3D',...
            'eps',[fname_3D,'.eps']),'epsc');
        saveas(fig3D{1},fullfile(res.res_path,'figures', '3D',...
            'png',[fname_3D,'.png']),'png');
        saveas(fig3D{2},fullfile(res.res_path,'figures', '3D',...
            'eps',[fname_3D,'_noProj','.eps']),'epsc');
        saveas(fig3D{2},fullfile(res.res_path, 'figures', '3D',...
            'png',[fname_3D,'_noProj','.png']),'png');
        saveas(fig3D{1},fullfile(res.res_path,modality,...
            'png',[fname_3D,'.png']),'png');
        saveas(fig3D{2},fullfile(res.res_path,modality,...
            'png',[fname_3D,'_noProj','.png']),'png');
    end
end
%Exclusion of activations at the border (exclusion) for gt(s) & res.loc
gt = gt(gt(:,2) > exclusion & gt(:,3) > exclusion...
    & gt(:,2) < fov(1) - exclusion & gt(:,3) < fov(2) - exclusion,:);
res.nloc_gt_after_exclusion = size(gt,1);
pairings = pairings(pairings(:,2) > exclusion & pairings(:,3) > exclusion...
    & pairings(:,2) < fov(1) - exclusion & pairings(:,3) < fov(2) - exclusion,:);


res.loc = res.loc(res.loc(:,2) > exclusion & res.loc(:,3) > exclusion...
    & res.loc(:,2) < fov(1) - exclusion & res.loc(:,3) < fov(2) - exclusion,:);

res.nloc_test_after_exclusion = size(res.loc,1);
res.border = exclusion;%nm excluded
%Photons Threshold
if int_thres==0
    res.photonTperc = 0;
    res.photonT = 0;
elseif int_thres > 1 %absolute value
    %gt = gt(gt(:,5) > int_thres,:);
    %pairings = pairings(pairings(:,6) > int_thres,:);
    res.photonT = int_thres;
else %percentage
    res.photonT = floor(quantile(gt(:,5),int_thres));
    res.photonTperc = int_thres;%quantile percentage
    %gt = gt(gt(:,5) > res.photonT,:);
    %pairings = pairings(pairings(:,6) > res.photonT,:);
    
end

%% Pairing & performance evaluation (localization based metrics)

[perf_metrics, pairings,fig_corr] = loc_metrics(res.loc, gt, radTol, pairings,...
    param_input.dim3D,'trueFluor',res.nloc_gt_after_exclusion,...
    'estFluor',res.nloc_test_after_exclusion,'thresPhotons',res.photonT);%'index'

saveas(fig_corr,fullfile(res.res_path, modality,...
        'png',sprintf('photon correlation %s %s %i %s.png',dataset_name,modality,res.photonT,res.wobble)));
%% Image based metrics

[~,~,perf_metrics.SNR, perf_metrics.FRC, perf_metrics.FSC] =...
    im_metrics(res.loc(:,2:4), gt(:,2:4), sig, pix_siz,...
    fov/pix_siz,param_input.winLen,param_input.areaCenter,doFSC);

%copy fields res to perf_metrics'
for fn = fieldnames(res)'
    perf_metrics.(fn{1}) = res.(fn{1});
end

%% Regrouping all the results (and photon correlations) and save

perf_metrics.dataset = dataset_name;
perf_metrics.participant = participant;
perf_metrics.modality = modality;
perf_metrics.gt_fname = gt_fname;
perf_metrics.test_fname = test_name;
perf_metrics.Nerrorline = str2double(test_name(strfind(test_name,'____Nerror_')+11:strfind(test_name,'____Nfluor')-1));
perf_metrics.nFeatInPairings = size(res.loc,2);
perf_metrics.pairings = pairings;
perf_metrics.winLen = param_input.winLen;
perf_metrics.areaCenter = param_input.areaCenter;
perf_metrics.fov = fov;

fname = sprintf('pairings____%s____%s____%s____wobble_%s____border_%i____photonT_%i____date_%s____dim3D_%i____nFeat_%i.csv',...
    dataset_name, modality, participant,perf_metrics.wobble,...
    perf_metrics.border,perf_metrics.photonT,...
    date,1*perf_metrics.dim3D, perf_metrics.nFeatInPairings);

dlmwrite(fullfile(res.res_path,fname),...
    pairings,'precision','%5.3f');
perf_metrics.fname_pairings = fname;

close all
end

