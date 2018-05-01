%% FILE STANDARDISATION : mlePALM
% Specific to each participant, standardised output file saved in csv
% format : Dataset Localisation & Beads localisation standard format in
% 'standard' folder
clear
participant_name = 'mlePALM';%participant name
% user related parameters
sep = '';%'	';%separator type
%2 elements vector : [2D, 3D], if 1D => for all
param.indframe = 1;%ind frame,2
param.indx = 2;%ind x nm
param.indy = 3;%ind y nm
param.indz = [0,4];%ind z nm, set 0 if unavailable
param.indint = [4,5];%ind intensity (photons). If not available, put [],8
header = 1;%# header lines
header_beads = 1;%same but for beads file
unit = 1;%ratio for conversion from current unit to nm, or vector 1x3 for each dimension
%e.g. original unit in camera pixel (100nm) => unit = 100;
%e.g. already in nm => unit = 1;
Yinv = false;%boolean inversed Y axis
x_shift_nm = 0;%value of pixel x shifting, nm
y_shift_nm = 0;%value of pixel y shifting, nm
frameIsOneIndexed = true;%Boolean frame numbering starting at 0 or 1

%data related parameters
%raw_pix_siz = 100;%nm raw pixel size
fov = [6400, 6400, 1500];%nm
fov_beads = [12800, 12800, 1500];%nm
%folders related parameters
upload_path = [participant_name,filesep,'upload'];
if exist([participant_name,filesep,'standard'],'dir')
    error('folder standard already exists !');
end
%% Loop over uploaded files
fnames = dir([upload_path,filesep,'MT*']);
fnames = [fnames;dir([upload_path,filesep,'ER*'])];

for k = 1:length(fnames)
    test_name =  fnames(k).name;
    splitPos = strfind(test_name,'____');
    modality = test_name(splitPos(1)+4:splitPos(2)-1);
    % Data reading
    if strcmp(modality,'2D')
        indframe = param.indframe(1);
        indx = param.indx(1);
        indy = param.indy(1);
        indz = param.indz(1);
        indint = param.indint(1);
    else %3D
        indframe = param.indframe(end);
        indx = param.indx(end);
        indy = param.indy(end);
        indz = param.indz(end);
        indint = param.indint(end);
    end
    beads_file = dir([upload_path,filesep,'Beads____',modality,'*']);
    
    beads_file = beads_file.name;
    
    try
        if isempty(sep)
            [~, sep] = importdata([upload_path filesep test_name]);
            fprintf('Detected separator : %s\n',sep);
        end
        loc = dlmread([upload_path filesep test_name], sep, header,0);
        Nerrorline = 0;
        loc_beads = dlmread([upload_path filesep beads_file],sep,header_beads,0);
    catch ME
        %dataset localisation
        fid = fopen([upload_path filesep test_name]);
        nCol = fgetl(fid);
        nCol = length(find(nCol==sep))+1;
        out = textscan(fid,repmat('%s ', 1, nCol),'delimiter',sep);
        fclose(fid);
        loc = zeros(length(out{1}),nCol);
        for m = 1:nCol
            loc(:,m) = str2double(out{m});
        end
        if indz==0
            if indint==0
                line2rm = sum(isnan(loc(:,[indframe indx indy])),2) > 0;
            else
                line2rm = sum(isnan(loc(:,[indframe indx indy indint])),2) > 0;
            end
        elseif indint==0
            line2rm = sum(isnan(loc(:,[indframe indx indz indy])),2) > 0;
        else
            line2rm = sum(isnan(loc(:,[indframe indx indy indz indint])),2) > 0;
        end
        loc(line2rm,:) = [];
        Nerrorline = sum(line2rm);
        
        %beads localization
        fid = fopen([upload_path filesep beads_file]);
        nCol = fgetl(fid);
        nCol = length(find(nCol==sep))+1;
        out = textscan(fid,repmat('%s ',1,nCol),'delimiter',sep);
        fclose(fid);
        loc_beads = zeros(length(out{1}),nCol);
        for m = 1:nCol
            loc_beads(:,m) = str2double(out{m});
        end
        if indz==0
            if indint==0
                line2rm = sum(isnan(loc_beads(:,[indframe indx indy])),2) > 0;
            else
                line2rm = sum(isnan(loc_beads(:,[indframe indx indy indint])),2) > 0;
            end
        elseif indint==0
            line2rm = sum(isnan(loc_beads(:,[indframe indx indz indy])),2) > 0;
        else
            line2rm = sum(isnan(loc_beads(:,[indframe indx indy indz indint])),2) > 0;
        end
        loc_beads(line2rm,:) = [];
        
    end
    
    % Standardization : comments describe the standard format
    % Z-column : add a Z-column if missing
    if indz == 0
        loc = [loc(:,1:end),zeros(size(loc,1),1)];
        loc_beads = [loc_beads(:,1:end), zeros(size(loc_beads,1),1)];
        indz = size(loc,2);
    end
    % Photon-column : add a photon-column if missing
    if indint==0
        loc = [loc,zeros(size(loc,1),1)];
        loc_beads = [loc_beads,zeros(size(loc_beads,1),1)];
        indint = size(loc,2);
    end
    % Unit : conversion to nm
    
    loc(:,[indx,indy,indz]) = loc(:,[indx,indy,indz]).*repmat(unit,size(loc,1),4-length(unit));
    loc_beads(:,[indx,indy,indz]) = loc_beads(:,[indx,indy,indz]).*repmat(unit,size(loc_beads,1),4-length(unit));
    
    % Y axis direction : (0,0) at the top left corner, Y axis direction toward bottom
    if Yinv
        loc(:,indy) = fov(2) - loc(:,indy);
        loc_beads(:, indy) = fov_beads(2) - loc_beads(:,indy);
    end
    % Frame index : starts at 1
    loc(:,indframe) = loc(:,indframe) + ~frameIsOneIndexed*1;
    loc_beads(:,indframe) = loc_beads(:,indframe) + ~frameIsOneIndexed*1;
    
    % Origin : (0,0) at the top left corner (of the top left pixel)
    loc(:,indx) = loc(:,indx) + x_shift_nm;
    loc(:,indy) = loc(:,indy) + y_shift_nm;
    
    loc_beads(:,indx) = loc_beads(:,indx) + x_shift_nm;
    loc_beads(:,indy) = loc_beads(:,indy) + y_shift_nm;
    
    % Z = 0 at the focal plane
    if min(loc(:,indz)) >= 0 && sum(loc(:,indz))~=0
        loc(:,indz) = loc(:,indz) - fov(3)/2;
        loc_beads(:,indz) = loc_beads(:,indz) - fov_beads(3)/2;
    end
    
    loc = loc(:,[indframe indx indy indz indint]);
    loc_beads = loc_beads(:,[indframe indx indy indz indint]);
    
    gt_beads = csvread(['Ground_truth',filesep,'Beads',filesep,'activations.csv']);
    
    if exist('dispOrthoView.m','file') %display bead positions in orthoview
        dispOrthoView(['Former Orthoview : ',participant_name,' ', modality],loc_beads,gt_beads,5);
    end
    
    % Save standardised file in csv format
    if ~exist([participant_name,filesep,'standard'],'dir')
        mkdir(participant_name,'standard');
    end
    dlmwrite([participant_name,filesep,'standard',...
        filesep,test_name(1:end-4),'____standard____Nerror_',num2str(Nerrorline),...
        '____Nfluor_',num2str(size(loc,1)),'____date_',date,'.csv'],loc,'precision',8);
    
    dlmwrite([participant_name,filesep,'standard',filesep,beads_file(1:end-4),'____standard','.csv'],loc_beads,'precision',8);
    
    fprintf('%s %i %i\n',test_name(1:splitPos(1)-1),Nerrorline,size(loc,1));
end
%addpath([participant_name,filesep,'standard']);
close all
figure; scatter3(loc_beads(:,2),loc_beads(:,3),loc_beads(:,1),'r');hold on;
scatter3(gt_beads(:,3),gt_beads(:,4),gt_beads(:,1),'g');