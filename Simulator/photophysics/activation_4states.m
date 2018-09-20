function rateEffectiveActivations = activation_4states(Ton, Tdark, Tbleaching, nframes, framerate, fov, pixelsize, dye, power_laser, path)
% ACTIVATION_4STATES - Activate a list of fluorophores with a 4-states model
%
% Description
%
%        UNIFORM    1/Tbl
%   OFF ------> ON ------> BLEACH
%             | /\
%             | |
%      1/Ton  | | 1/Tdark
%             | |
%            \/ |
%             DARK
%
%   The input is a list of position (X, Y, Z) stored in a CSV file.
%       The filename is hardcoded 'positions.csv', this input file is
%       required.
%   The output is a list of activation (count, frame, X, Y, Z, number of photons) stored in a CSV file.
%       The filename is harcoded 'activations.csv.'
%
% Project
%   Simulator for the challenge SMLMS 2016
%   Benchmarking of single-molecule localization software
%
% Reference
%   Citation: submitted
%   Website: http://bigwww.epfl.ch/smlm/
% 
% Authors
%   Seamus Holden, 
%   Tomas Lukes,
%   Daniel Sage, daniel.sage@epfl.ch, Biomedical Imaging Group, EPFL
%
% Date
%   May 2016
%
% Input parameters 
%   Ton           On (recommended value 3)
%   Tdark         Dark (recommended value 2.5
%   Tbleaching:   Bleaching (recommended value 1.5
%   nframes:      number of frames
%   framerate:    frame rate in Hz (typical value 10)
%   fov:          field of view in nm (typical value 6400)
%   pixelsize:    size of camera pixel in nm
%   path:         directory to store the results
%   dye:          'Alexa647' or 'Dendra2'
%   power_laser:  power of the laser in  W / cm^2 (typical value 300)
%
% Output parameters
%   rateEffectiveActivations: rate of activations per frame
% 
% Required
%   Other m-files required: none
%   MAT-files required: none
%   A CSV position file: required
%	
% Examples of usage
%    >> rate = activation_4states(3, 2.5, 1.5, 100, 10, 6400, 100, 'Alexa647', 200, pwd)
%    >> rate = activation_4states(3, 2.5, 1.5, 100, 10, 6400, 100, 'Dendra2', 200, pwd)
%
% Conditions of usage
%     This program is free software: you can redistribute it and/or modify
%     it under the terms of the GNU General Public License as published by
%     the Free Software Foundation, either version 3 of the License, or
%     (at your option) any later version.
%     This program is distributed in the hope that it will be useful,
%     but WITHOUT ANY WARRANTY; without even the implied warranty of
%     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
%     GNU General Public License for more details.
%     You should have received a copy of the GNU General Public License
%     along with this program.  If not, see <http://www.gnu.org/licenses/>.
%
%
	
    %% Other Fixed parameters
    fstart      = 20;   % number of removed first frames
   
    Q = -1;
    %% Emission parameters Alexa647
	if (strcmpi(dye, 'Alexa647') == 1)
        Q = 0.33;            % Quantum yeild of the dye
        lambda = 647e-9;     % Wavelength in m
        EC = 239000;         % Absorbtivity, Molar extinction coefficient in cm^2 / mol
	end
    
	%% Emission parameters Dendra2
	if (strcmpi(dye, 'Dendra2') == 1)
        Q = 0.55;            % Quantum yeild of the dye
        lambda = 573e-9;     % Wavelength in m
        EC = 4000;           % Absorbtivity, Molar extinction coefficient in cm^2 / mol
	end

    if Q <= 0
        fprintf('This dye (%s) is unkown \n', dye);
        return;
    end
 
	rng(123);         % Initialize the seed of the random generator
    
	%% Read the positisons file
    cd(path);
    positions = csvread([path filesep 'positions.csv']);
    path = [path filesep  'activations-' num2str(nframes) '-' num2str(fov) filesep];
    if ~exist(path, 'dir')
        mkdir(path);
    end
    cd(path);
    fprintf('Lifetime Ton=%4.3f Tdark=%4.3f Tbleach=%4.3f\n', Ton, Tdark, Tbleaching);
   
    fprintf('Activation of the positions %s \n', path);
    nfluos = size(positions,1);
    fprintf('Number of fluorophore positions: %6.0f on %6.0f frames \n', nfluos, nframes);
    xmin = min(positions(:,1));
    xmax = max(positions(:,1));
    ymin = min(positions(:,2));
    ymax = max(positions(:,2));
    fprintf('Range in X: %6.3f ... %6.3f nm (fov %4.1f) \n', xmin, xmax, fov);
    fprintf('Range in Y: %6.3f ... %6.3f nm (fov %4.1f) \n', ymin, ymax, fov);
    fprintf('Depth in Z: %6.3f ... %6.3f nm \n', min(positions(:,3)), max(positions(:,3)));
        
    h = 6.626E-34;               % Planck constant in mol^-1
    Na = 6.022e23;               % Number of Avogadro
    c = 3e8;                     % Celerity in m/s
    e = h * c / lambda;          % Energy of a Photon in J
    s = 1000 * log(10)*EC / Na;  % Absorption cross section in cm^2
    
    %% Non uniform power illumination (Radial sigmoid function)
    xcenter = fov*0.5 + 0.2*fov*(0.5-rand()); % simulate misalignment
    ycenter = fov*0.5 + 0.2*fov*(0.5-rand()); % simulate misalignment
    DistanceAtHalfMax = fov*0.55;    % Distance form the center, half maximum, in nm
    DistanceAtQuarterMax = fov*0.7; % Distance form the center, quarter maximum in nm
    slope = log(3)/(DistanceAtQuarterMax-DistanceAtHalfMax);
    
    %% Display the power illumination
    fovPixel = int16(fov / pixelsize);
    power = zeros(fovPixel, fovPixel);
    for x=1 : fovPixel
    for y=1 : fovPixel
         power(x, y) = powerIllumination(double(x)*pixelsize, double(y)*pixelsize, xcenter, ycenter, slope, DistanceAtHalfMax); 
    end
    end
    average_power = mean(mean(power));
    fprintf('Average of power: %6.6f \n', average_power);
    
    %% Flux of photons
    flux = Q * s * power_laser / e;% Flux of photons per second
    fprintf('Flux of photons per seconds: %6.6f \n', flux);
    flux = flux / framerate;       % Flux of photons per frame
    fprintf('Flux of photons per frame: %6.6f \n', flux);
    epsilon = 0.01 * flux;
    
    %% Generate fluorophore time trace
 	count = 0;
    onTime=[];
    stats = zeros(nframes, 2);
    activate = zeros(nfluos,1);
	fid = fopen([path filesep 'activations.csv'], 'w');
    fif = fopen([path filesep 'stats_activation_fluos.csv'], 'w');
	fprintf(fid, 'Ground-truth,frame,xnano,ynano,znano,intensity \n');
	fprintf(fif, 'fluos,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25 \n');
    totalPhotons = 0;
    for k=1:nfluos
        pos = positions(k,:);
        attenuation = powerIllumination(pos(1), pos(2), xcenter, ycenter, slope, DistanceAtHalfMax) / average_power; 
        [photons] = brightness_palmSubframe(attenuation*flux,Ton,Tdark,Tbleaching,nframes+fstart);
        progression('Activate ', k);
        fprintf(fif, '%5.0f ', k);
        for f=1:nframes
            if photons(f+fstart) > epsilon
                stats(f, 1) = stats(f, 1) + 1;
                stats(f, 2) = stats(f, 2) + photons(f+fstart);
                activate(k) = activate(k)+1;
                count = count + 1;
                totalPhotons = totalPhotons + photons(f+fstart);
                fprintf(fid, '%5.0f, %5.0f, %4.3f, %4.3f, %4.3f, %4.3f \n', count, f, positions(k,1), positions(k,2), positions(k,3), photons(f+fstart));
                fprintf(fif, ', %5.0f', f);
            end
        end
        fprintf(fif, '\n');
     end
	fclose(fid);
    fclose(fif);

	disp(strcat('End of storage on ', path, 'activations.csv'));
    rateEffectiveActivations = count / nfluos;
      
    %% Plot stats
    figure('Position',[100, 100, 900, 900]),
    axes('Position', [0.05 0.05 .44 .27]);
    plot(stats(:,1));
    xlabel('Frames');
    ylabel('Act. Fluorophore per frame');
    legend(sprintf('total: %4.0f activations', sum(stats(:,1))));
  
    axes('Position', [0.05 0.38 .44 .27]);
    plot(stats(:,2));
    xlabel('Frames');
    ylabel('Photons per frame');
    legend(sprintf('total: %4.1f photons', sum(stats(:,2))));

    axes('Position', [0.05 0.72 .44 .27]);
    histogram(stats(:,1));
    xlabel('Nb of act. fluorophore per frame'); 
    ylabel('Number of frames');
	legend(sprintf('mean: %4.2f fluos/frame', mean(stats(:,1))));

    axes('Position', [0.55 0.72 .44 .27]);
    histogram(stats(:,2));
    xlabel('Number of photons per fluo.'); 
    ylabel('Freq.');
    legend(sprintf('mean: %4.1f photons/frame', mean(stats(:,2))));

    axes('Position', [0.55 0.38 .44 .27]);
    histogram(activate);
    xlabel('Total ON time per fluo.'); 
    ylabel('Freq.');
    print('stats', '-dpng', '-r300'); 

    axes('Position', [0.55 0.05 .44 .27]);
    histogram(onTime);
    xlabel('On time per blink');
    ylabel('Freq.');
    legend(sprintf('Mean on time per blink %4.1f', mean(onTime)));  

    
    %% Save stats frame
    fid = fopen(strcat(path, 'stats_activation_frames.csv'), 'w');
    fprintf(fid, 'frame,nbfluos,nbphotons \n');
    for f=1:nframes
        fprintf(fid, '%5.0f, %5.0f, %6.6f \n', f, stats(f,1), stats(f,2));
    end
    fclose(fid);
  
	%% Save stats frame
    
    fprintf('Number of activations, %6.0f\n', count);
	fprintf('Average number of photons / activation, %6.6f\n', totalPhotons / count);

    fid = fopen(strcat(path, 'stats_activation_sumary.csv'), 'w');
    fprintf(fid, 'feature,value \n');
    fprintf(fid, 'Number of fluos, %6.0f\n', nfluos);
    fprintf(fid, 'Number of frames, %6.0f\n', nframes);
	fprintf(fid, 'Field of view (nm), %6.2f\n', fov);

    fprintf(fid, 'Number of activations, %6.0f\n', count);
	fprintf(fid, 'Average number of photons / activation, %6.6f\n', totalPhotons / count);
	fprintf(fid, 'Density - number of fluorophores per frame, %6.6f\n',  count / nframes);
	fprintf(fid, 'Density - number of photons per frame, %6.6f\n', totalPhotons / nframes);
 
    fprintf(fid, 'Center of power illumination X (nm), %6.3f\n', xcenter);
    fprintf(fid, 'Center of power illumination X (nm), %6.3f\n', ycenter);
    fprintf(fid, 'Distance at half maximum illumination (nm), %6.3f\n', DistanceAtHalfMax);
    fprintf(fid, 'Distance at quarter maximum illumination (nm), %6.3f\n', DistanceAtQuarterMax);
    fprintf(fid, 'Power illumination (W), %6.3f\n', power_laser);
    
    fprintf(fid, 'Framerate, %6.3f\n', framerate);
    fprintf(fid, 'Maximum flux of photons per frame, %6.3f\n', flux);
    fprintf(fid, 'Threshold for activation (min photons), %6.3f\n', epsilon);
    fprintf(fid, 'Ton, %6.3f\n', Ton);
    fprintf(fid, 'Tdark, %6.3f\n', Tdark);
    fprintf(fid, 'Tbleaching, %6.3f\n', Tbleaching);
	fclose(fid);
    
    cd(path);
end

% Progression
function progression(message, k) 
    if mod(k, 100) == 0
        fprintf('%5.0f: %s \n', k, message);
    end
end

% Spatial non-uniform illumation
function P=powerIllumination(x, y, xcenter, ycenter, slope, DistanceAtHalfMax) 
        dm = DistanceAtHalfMax +  DistanceAtHalfMax*0.05*(0.5-rand());
        d = sqrt((x - xcenter)^2 + (y - ycenter)^2);
        P = 1 - 1 / (1+exp(-slope*(d-dm))); 
end

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
% Switching events are integrated over each individual frame (ie half-frame blink gives
% half frame intensity
%
% Note actual mean lifetime in On state is 1/(1/Ton + 1/Tbleach) due to two decay paths
% ARGUMENTS
% OUTPUTS
% photons: photon count for each frame (no noise)
% Note, molecules alway starts in off state
%

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

%integrate the switching to single frame resolution
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
    photons=Ion*isOn;
end

%-----------------------------------------------------
function stateVector = getStateVector(tSwitch);
    %helper function for debugging

    nSwitch = numel(tSwitch);
    stateVector = zeros(size(tSwitch));
    if nSwitch>0
        stateVector(1:2:end) = 1;
    end
end
end

