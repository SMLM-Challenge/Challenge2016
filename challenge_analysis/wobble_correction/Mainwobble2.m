%% SCRIPT : ESTIMATION OF THE WOBBLE
% Required files
% - WobbleCalibration.m (modified by Thanh-an)
% - beads_areas.mat : contains ROIs for each bead
% - Bead localizations file (csv) from the participants
% - Raw images (can be optional by commenting out the involved lines)

%% Parameters
localfname = 'stack-beads-100nm-AS-Exp-100x100x10-as-stack.tif-151-1.2.csv';%localisation file of the beads
%nSlice = 151;%# slices
zmin = -750;%nm
zmax = 750;%nm
zstep = 10;%nm
nBeads = 6;%# beads
roiRadius = 300;%assume the sim beads are well spaced and that the localizations are not crazy-far away
frameIsOneIndexed = true;

%Ground truth
gt = [1004.428595,1175.718123;
3457.478549,952.4193597;
2496.020979,3130.482598;
1233.052334,5317.726656;
4674.248962,5144.109624;
5130.118329,2808.354346;];%nm

%load localizations
fprintf('Loading bead localizations...\n');
labelCol = textscan(fopen(localfname),'%s%s%s%s%s%s%s%s%s','delimiter',',');
labelCol = cellfun(@(x) x{1},labelCol,'UniformOutput',false);
localData = csvread(localfname,1,0);
xnm = localData(:,~cellfun(@isempty,strfind(labelCol,'xnano')));
ynm = localData(:,~cellfun(@isempty,strfind(labelCol,'ynano')));
frame = localData(:,~cellfun(@isempty,strfind(labelCol,'frame')));

wobbleCorrectSimBead(xnm,ynm,frame,gt,zmin,zstep,zmax,roiRadius,frameIsOneIndexed)
