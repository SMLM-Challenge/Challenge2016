%% FILE STANDARDISATION NO BEADS : WaveTracer
% Specific to each participant, standardised output file saved in csv
% format : Dataset Localisation & Beads localisation standard format in
% 'standard' folder
clear
participant_name = 'WaveTracer';%participant name
% user related parameters
sep = '';%'	';%separator type
%2 elements vector : [2D, others], if 1D => for all
param.indframe = 1;%ind frame,2
param.indx = 3;%ind x nm
param.indy = 4;%ind y nm
param.indz = 5;%ind z nm, set 0 if unavailable
param.indint = 2;%ind intensity (photons). If not available, put [],8
header = 1;%has header or not
unit = 1000;%ratio current_unit to nm : conversion to nm from current unit
%e.g. original unit in camera pixel (100nm) => unit = 100;
%e.g. already in nm => unit = 1;
Yinv = false;%boolean inversed Y axis
x_shift_nm = 0;%value of pixel x shifting, nm
y_shift_nm = 0;%value of pixel y shifting, nm
frameIsOneIndexed = true;%Boolean frame numbering starting at 0 or 1

%data related parameters
%raw_pix_siz = 100;%nm raw pixel size
fov = [6400, 6400, 1500];%nm

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
    try
        if isempty(sep)
            [~, sep] = importdata([upload_path filesep test_name]);
            fprintf('Detected separator : %s\n',sep);
        end
        loc = dlmread([upload_path filesep test_name], sep, header,0);
        Nerrorline = 0;
    catch ME
        %dataset localisation
        fid = fopen([upload_path filesep test_name]);
        nCol = fgetl(fid);
        nCol = length(find(nCol==sep))+1;
        out = textscan(fid,repmat('%s ',1,nCol),'delimiter',sep);
        fclose(fid);
        loc = zeros(length(out{1}),nCol);
        for m = 1:nCol
            loc(:,m) = str2double(out{m});
        end
        if indz==0
            line2rm = sum(isnan(loc(:,[indframe indx indy indint])),2) > 0;
        else
            line2rm = sum(isnan(loc(:,[indframe indx indy indz indint])),2) > 0;
        end
        loc(line2rm,:) = [];
        Nerrorline = sum(line2rm);
        
    end
    
    % Standardization : comments describe the standard format
    % Z-column : add a Z-column if missing
    if indz == 0
        loc = [loc(:,1:end),zeros(size(loc,1),1)];
        indz = size(loc,2);
    end
    % Photon-column : add a photon-column if missing
    if indint==0
        loc = [loc,zeros(size(loc,1),1)];
        indint = size(loc,2);
    end
    % Unit : conversion to nm
    
    loc(:,[indx,indy,indz]) = loc(:,[indx,indy,indz])*unit;
    
    % Y axis direction : (0,0) at the top left corner, Y axis direction toward bottom
    if Yinv
        loc(:,indy) = fov(2) - loc(:,indy);
    end
    % Frame index : starts at 1
    loc(:,indframe) = loc(:,indframe) + ~frameIsOneIndexed*1;
    
    % Origin : (0,0) at the top left corner (of the top left pixel)
    loc(:,indx) = loc(:,indx) - x_shift_nm;
    loc(:,indy) = loc(:,indy) - y_shift_nm;
    
    % Z = 0 at the focal plane
    if min(loc(:,indz)) >= 0 && sum(loc(:,indz))~=0
        loc(:,indz) = loc(:,indz) - fov(3)/2;
    end
    
    loc = loc(:,[indframe indx indy indz indint]);
    
    figure;
    scatter(loc(:,2),loc(:,3),1,'filled');title(['Software Localizations-',modality]);drawnow
    
    % Save standardised file in csv format
    if ~exist([participant_name,filesep,'standard'],'dir')
        mkdir(participant_name,'standard');
    end
    csvwrite([participant_name,filesep,'standard',...
        filesep,test_name(1:end-4),'____standard____Nerror_',num2str(Nerrorline),...
        '____Nfluor_',num2str(size(loc,1)),'____date_',date,'.csv'],loc);
    
end
addpath([participant_name,filesep,'standard']);
close all