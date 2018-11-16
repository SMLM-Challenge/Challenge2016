%run this on a directory containing the real data csv files
% Or copy to said directory
%modify this list to match the files you want to analyse
%Need to add to path:
%   'lm-challenge2016\Challenge2016\Assessment\Matlab\3dPlotTools'
%   'lm-challenge2016\Challenge2016\Assessment\Matlab\driftCorrection\driftcorrection3D'
%   'lm-challenge2016\Challenge2016\Assessment\RealDataAssessment'

%fnameTubulinList = {'RapidSTORM____loca___Tubulin.csv'}
%fnameNPCList = {'RapidSTORM____loca___NPC.csv'};

%1. Tubulin plots
%fnameTubulinList = {...
%'SMAP-2018____loca___Tubulin_driftCorr.csv'}
fnameTubulinList = {...
'3D-DAOSTORM-WOBBLE____loca___Tubulin_driftCorr.csv',...
'Cspline____loca___Tubulin_driftCorr.csv',...             
'MIAtool-WOBBLE____loca___Tubulin_driftCorr.csv',...      
'QuickPALM____loca___Tubulin_driftCorr.csv',...           
'RapidSTORM____loca___Tubulin_driftCorr.csv',...          
'SMAP-2018____loca___Tubulin_driftCorr.csv',...           
'WaveTracer____loca___Tubulin_driftCorr.csv'}

nTub = numel(fnameTubulinList);
for ii=1:nTub
    close all
    f = fnameTubulinList{ii}
    dirname = [f(1:end-4)];
    if ~exist(dirname,'dir')
        mkdir(dirname)
    end
    savename = [dirname,filesep(),dirname];
    tubulin_plotter(f,savename);
end

%fnameNPCList = {'SMAP-2018____loca___NPC_driftCorr.csv'};
fnameNPCList = {...
'3D-DAOSTORM-WOBBLE____loca___NPC_driftCorr.csv',...
'Cspline____loca___NPC_driftCorr.csv',...             
'MIAtool-WOBBLE____loca___NPC_driftCorr.csv',...      
'QuickPALM____loca___NPC_driftCorr.csv',...           
'RapidSTORM____loca___NPC_driftCorr.csv',...          
'SMAP-2018____loca___NPC_driftCorr.csv',...           
'WaveTracer____loca___NPC_driftCorr.csv'}
nNPC = numel(fnameNPCList);
%2. NPC plots 
for ii=1:nNPC
    close all
    f = fnameNPCList{ii}
    dirname = [f(1:end-4)];
    if ~exist(dirname,'dir')
        mkdir(dirname)
    end
    savename = [dirname,filesep(),dirname];
    NPC_plotter(f,savename);
end

