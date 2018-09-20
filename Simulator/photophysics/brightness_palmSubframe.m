function [photons]=brightness_palmSubframe(Ion,Ton,Tdark,Tbl,frames)
% INPUTS 
% Ion: mean photon emission during on state
% frames: # of frames
% Rates defined here:
% %
%for 4 state system:
%
%     UNIFORM    1/Tbl
% OFF ------> ON ------> BLEACH
%             |/\ 
%             | |
%      1/Ton  | | 1/Tdark
%             | |
%             | |
%            \/ |
%             DARK
%
% NB: the OFF to ON is not poisson distributed it is uniform random
% To reflect that in normal experimental conditions constant imaging density is 
% maintained
% 
% All switching is calculated at sub-frame resolution and then integrated o  er each frame.
%
% Note actual mean lifetime in On state is 1/(1/Ton + 1/Tbleach) due to two decay paths
% ARGUMENTS
% OUTPUTS
% photons: photon count for each frame (no noise)
% Note, molecules alway starts in off state
%

%nArg = numel(varargin);
%ii=1;
%while ii<=nArg
%    ii=ii+1;
%end


%calculate the frame where the molecule turns on
tToOn = max(1,frames*rand);%Uniform probability of on switching within the movie
tSwitch=[];
if tToOn<frames
    tSwitch(1) = tToOn;

    isBleach = false;
    %while not bleached
    while ~isBleach && tSwitch(end)<=frames
    %   calculate the frame where the molecule goes dark or bleaches
        tToBl = -Tbl*log(rand);
        tToDark = -Ton*log(rand);

    %   if bleaches before goes dark -> break
        if tToBl<tToDark
            isBleach = true;
            tSwitch= [tSwitch,tSwitch(end)+tToBl];
    %   else calculate when the molecule goes back on
        else 
            tSwitch= [tSwitch,tSwitch(end)+tToDark];

            %switch back from dark to on
            tDarkToOn = -Tdark*log(rand);
            tSwitch= [tSwitch,tSwitch(end)+tDarkToOn];
        end
    end
end

isOn = zeros(1,frames);
isOnCur =0;

tSwitchInt = floor(tSwitch);
for ii = 1:frames
    if ~(any(tSwitchInt==ii))
        isOn(ii)=isOnCur;%just propogate current state
    else
        subFrSwitchTime = tSwitch(find(tSwitchInt==ii)) - ii;
        totOnTimeFr = 0;
        tCurFr = 0;
        %add all the sub frame on states together
        for jj = 1:numel(subFrSwitchTime) 
            tState = subFrSwitchTime(jj)-tCurFr;
            if isOnCur
                totOnTimeFr = totOnTimeFr+tState;
            end
            isOnCur = ~isOnCur;
            tCurFr = subFrSwitchTime(jj);
        end
        %add the on time from the end of the frame if its still in the on state
        if isOnCur
            tState = 1-tCurFr;
            totOnTimeFr = totOnTimeFr+tState;
        end
        isOn(ii) = totOnTimeFr;
    end
end

photons=Ion*isOn;

%%DEBUG
%tSwitch
%switchLen = diff(tSwitch)
%tSwitch-tSwitchInt
%%on time check
%onTimeIntegrated = sum(isOn)
%onTimeTSwitch = sum(switchLen(1:2:end)) %wont get it right if switch is at the end but otherwise good
%hold off;
%plot(tSwitch,0*tSwitch+0.5,'x');
%hold all;
%stairs(1:frames,isOn);
%legend('Switch time', 'Integrated intensity');
%ylim([0 1]);
%pause

%-----------------------------------------------------
function stateVector = getStateVector(tSwitch);
%helper function for debugging

nSwitch = numel(tSwitch);
stateVector = zeros(size(tSwitch));
if nSwitch>0
    stateVector(1:2:end) = 1;
end


