function [Iref,Iest,varargout] = im_metrics(posEst,posRef,sig,pix_size,im_size,winLen,areaCenter,doFSC,varargin)
%im_metrics Provides image based metrics : SNR, FRC
%   image obtained by convolving the positions with a gaussian (sigma sig)
%   SNR = 10*log_10(norm(posRef_gauss,2)^2/norm(posRef_gauss-posEst_gauss,2)^2)
%   posEst : estimated positions
%   posRef : reference positions
%   sig : sigma of the gaussian. e.g. Sage's sig ten times lower than the PSF
%   im_size : size of the obtained image
renderOnly = false;doCorr = true;
k=1;
while k< nargin - 8
    switch varargin{k}
        case 'renderOnly'
            renderOnly = varargin{k+1};
        case 'doCorr'
            doCorr = varargin{k+1};
    end
    k=k+2;
end

Iref3D = gauss_render(posRef, sig, pix_size, im_size,doCorr);

Iest3D = gauss_render(posEst, sig, pix_size, im_size,doCorr);

Iref = cell(4,1); Iest = cell(4,1); SNR = cell(4,1); FRC = cell(3,1);
Iref{1} = Iref3D; Iest{1} = Iest3D;
for k=1:3
    Iref{1+k} = squeeze(sum(Iref3D, k));
    Iest{1+k} = squeeze(sum(Iest3D, k));
end
if ~renderOnly
    for k=1:4
        SNR{k} = 10*log10(sum(Iref{k}(:).^2)/sum((Iref{k}(:) - Iest{k}(:)).^2));
    end
    
    %2D: FRC
    %FRC = FRCtrueLoc(posEst(:,1:2), posRef(:,1:2), im_size(1:2), 1/pix_size, pix_size, true);%histogram
    %FRC = imres_ims(Iest{4}, Iref{4}, pix_size, false);%gauss
    %new file, same result for 2D
    if sum(Iest{1}(:))==0
        FRC = nan(3,1);
        FRC = mat2cell(FRC,ones(3,1));
    else
        for k=1:3
            FRC{k} = frc_mod(Iest{k+1}, Iref{k+1});
            FRC{k} = frctoresolution(FRC{k}, im_size(1))*pix_size;
        end
    end
    %3D: FSC
    if doFSC && sum(Iest{1}(:))~=0
        winLen = min(winLen/pix_size,im_size(1));%window length
        centerROI = areaCenter/pix_size;%center
        
        if centerROI(1) + winLen/2 > im_size(1) || centerROI(1) - winLen/2 < 0 ...
                || centerROI(2) + winLen/2 > im_size(2) || centerROI(2) - winLen/2 < 0
            warning('ROI in FSC out of boundary');
            return
        end
        areaROI{1} = 1 - winLen/2+centerROI(1):centerROI(1) + winLen/2;
        areaROI{2} = 1 - winLen/2+centerROI(2):centerROI(2) + winLen/2;
        
        timer_fsc = tic;
        FSC = frc_mod(Iest{1}(areaROI{1},areaROI{2},:), Iref{1}(areaROI{1},areaROI{2},:));
        FSC = frctoresolution3D(FSC, winLen)*pix_size;
        %FSC = frctoresolution(FSC, im_size(1))*pix_size;%zero-padded,not working for 3D
        fprintf('FSC calculation...%1.2f s\n',toc(timer_fsc))
    else
        FSC = nan;
    end
    varargout{1} = SNR;
    varargout{2} = FRC;
    varargout{3} = FSC;
end
end

function resolution = frctoresolution3D(frc_in, sz)

% Check that the curvefit toolbox function smooth exists
TB_curve=0;
try
    TB_d=toolboxdir('curvefit');
    TB_curve=1;
catch
    warning('Curvefit toolbox not available. Using another not optimal smoothing method for FRC.')
end
% Smoot the FRC curve
% Least squares interpolation for curve smoothing
sspan = ceil(sz/20);      % Smoothing span
if (sz/20)<5
    sspan = 5;
end
sspan = sspan  + (1-mod(sspan,2));

if TB_curve
    p = pwd; % hack to avoid the function shadwoing by smooth from dip_image
    cd([TB_d filesep 'curvefit'])
    frc_in = double(smooth(frc_in,sspan,'loess'));
    cd(p)
else
    frc_in = double(gaussf(frc_in,.9))';
end
q = (0:(length(frc_in)-1))'/sz;                   % Spatial frequencies

% Calculate intersections between the FRC curve and the threshold curve
% isects = polyxpoly(q,frc_in,q,thresholdcurve);
thresholdcurve = 1/7*ones(size(frc_in));%can use other threshold
isects = isect(q,frc_in,thresholdcurve);

% Find first intersection to obtain the resolution

% Throw away intersections at frequencies beyond the Nyquist frequency
isects = isects(isects<0.5);

if isempty(isects)
    resolution = 1/0.5;%set the "best" resolution reachable
else
    % Find the first intersection where the FRC curve is decreasing
    isect_inds = 1+floor(sz*isects);    % Indices of the intersections
    for ii = 1:length(isect_inds)
        isect_ind = isect_inds(ii);
        if frc_in(isect_ind+1) < frc_in(isect_ind)
            resolution = 1/isects(ii);
            break
        end
    end
end

if ~exist('resolution','var')
    resolution = nan;
    fprintf(' -- Could not find the resolution --\n')
end
end

