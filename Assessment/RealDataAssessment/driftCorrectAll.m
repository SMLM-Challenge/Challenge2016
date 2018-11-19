%run this on a directory containing the real data csv files
% Or copy to said directory
%modify this list to match the files you want to analyse
%Need to add to the matlab path:
%    'Challenge2016\Assessment\RealDataAssessment'
%    'Challenge2016\Assessment\Matlab\driftCorrection'

fnameTubulinList = {...
'3D-DAOSTORM-WOBBLE____loca___Tubulin.csv',...
'Cspline____loca___Tubulin.csv',...             
'MIAtool-WOBBLE____loca___Tubulin.csv',...      
'QuickPALM____loca___Tubulin.csv',...           
'RapidSTORM-WOBBLE____loca___Tubulin.csv',...          
'SMAP-2018____loca___Tubulin.csv',...
'ThunderSTORM-WOBBLE____loca___Tubulin.csv',...
'WaveTracer____loca___Tubulin.csv'};
fnameNPCList = {...
'3D-DAOSTORM-WOBBLE____loca___NPC.csv',...      
'Cspline____loca___NPC.csv',...                 
'MIAtool-WOBBLE____loca___NPC.csv',...          
'QuickPALM____loca___NPC.csv',...               
'RapidSTORM-WOBBLE____loca___NPC.csv',...              
'ThunderSTORM-WOBBLE____loca___NPC.csv',...
'SMAP-2018____loca___NPC.csv',...               
'WaveTracer____loca___NPC.csv'};
nTub = numel(fnameTubulinList);
nNPC = numel(fnameNPCList);
%1. apply drift correction to all files
for ii=1:nTub
    f= fnameTubulinList{ii}
    lmCompDriftCor(f);
end
for ii=1:nNPC
    f= fnameNPCList{ii}
    lmCompDriftCor(f);
end

