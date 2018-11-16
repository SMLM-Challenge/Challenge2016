function [srIm,yIm,xIm,density] = renderStormData(X,Y,varargin)
%TODO saturate Xpercent before gamma correction
%TODO use a faster gaussian filter
% box = [xstart ystart xwidth yheight]
%Author: S Holden 

pixSize=20;
sigma=10;
gammaVal = 1;
satVal =0.0;
zlim = [-600 600];
nz =10;
satVal=0;
isBox = false;
box=[];
is3D = false;
useZCLim = false;
zclim=[];
ii = 1;
while ii <= numel(varargin)
  if strcmp(varargin{ii},'Z')
    is3D = true;
    Z= varargin{ii+1};
    ii = ii + 2;
  elseif strcmp(varargin{ii},'ZLim')
    zlim = varargin{ii+1};
    ii = ii + 2;
 elseif strcmp(varargin{ii},'Nz')
    nz = varargin{ii+1};
    ii = ii + 2;
  elseif strcmp(varargin{ii},'PixelSize')
    pixSize = varargin{ii+1};
    ii = ii + 2;
  elseif strcmp(varargin{ii},'Box')
    isBox = true;
    box = varargin{ii+1};
    ii = ii + 2;
  elseif strcmp(varargin{ii},'Sigma')
    sigma= varargin{ii+1};
    ii = ii + 2;
  elseif strcmp(varargin{ii},'Gamma')
    gammaVal= varargin{ii+1};
    ii = ii + 2;
  elseif strcmp(varargin{ii},'Saturate')
    satVal= varargin{ii+1};
    ii = ii + 2;
  elseif strcmp(varargin{ii},'ZCLim')
    useZCLim= true;
    zclim = varargin{ii+1};
    ii = ii + 2;
  else
      ii=ii+1;
  end
end

if is3D ==true
    [srIm ,m,n,density] = hist3D(X,Y,Z,pixSize,sigma,gammaVal, zlim,nz,satVal,box,zclim);
    imshow(srIm);
else
    [srIm ,m,n,density] = hist2D(X,Y,pixSize,sigma,gammaVal,satVal, box);
    imshow(srIm);
    colormap(gray);
end

xIm=n;
yIm=m;

%----------------------------------------------------------
function [srIm,m,n,density] =hist3D(XPosition,YPosition,ZPosition,pixSize,sigma,gammaVal,zlim,nz, satVal,box,zclim)
%HUEMAX = 240/360; %this is when you get range red --> blue (hsv circles around back to red
minC = 0;
maxC =1;

if isempty(box)
  minX = min(XPosition);
  maxX = max(XPosition);
  minY = min(YPosition);
  maxY = max(YPosition);
else
  minX = box(1);
  maxX = box(1)+box(3);
  minY = box(2);
  maxY = box(2)+box(4);
end

if ~exist('zlim','var')
  minZ = min(ZPosition);
  maxZ = max(ZPosition);
else
  minZ = zlim(1);
  maxZ = zlim(2);
end

if isempty(zclim)
    useZCLim=false;
else
    useZCLim=true;
end

%remove out of bounds data
isInBounds = XPosition > minX & XPosition < maxX ...
              & YPosition > minY & YPosition < maxY ...
              & ZPosition > minZ & ZPosition < maxZ;

XPosition = XPosition(isInBounds);
YPosition = YPosition(isInBounds);
ZPosition = ZPosition(isInBounds);

if useZCLim
    %clip extreme z values but do not discard the points
    ZPosition(ZPosition<zclim(1))=zclim(1);
    ZPosition(ZPosition>zclim(2))=zclim(2);
    %convert z to colour range, limits [0 1];
    z_cVal = (ZPosition-zclim(1))/(zclim(2)-zclim(1));
else
    %convert z to colour range, limits [0 1];
    z_cVal = (ZPosition-minZ)/(maxZ-minZ);
end
%use this to index into a lookuptable, here jet
z_hue=applyStormCmap(z_cVal);


n=minX:pixSize:maxX;
m=minY:pixSize:maxY;
nx = numel(n);
ny = numel(m);
RR = [...
      round((ny-1)*(YPosition-minY)/(maxY-minY))+1 ...
      round((nx-1)*(XPosition-minX)/(maxX-minX))+1];
density = accumarray(RR,1,[ny,nx]);
zsum = accumarray(RR,z_hue,[ny,nx]);
zavg=zsum./density;
zavgRaw=zavg;
zavg(isnan(zavg))=0;


%make the hsv image
hue = zavg;
sat = ones(size(density));
val = density/max(density(:));
srHSV = cat(3,hue,sat,val);
srRGB = hsv2rgb(srHSV);
%have to gaussian blur in rgb domain
srRGBblur = imgaussfilt(srRGB,sigma/pixSize);
srHSVblur = rgb2hsv(srRGBblur);

% ADJUST GAMMA HERE
val = srHSVblur(:,:,3);
val= saturateImage(val,satVal);
val= adjustGamma(val,gammaVal);
srHSVfinal = cat(3, srHSVblur(:,:,1:2),val);
%then do the final conversion to RGB
srRGBfinal= hsv2rgb(srHSVfinal);
srIm = srRGBfinal;


%keyboard
%-----------------------------------------------------------------------------------------------
function [srIm,m,n,density] =hist2D(XPosition,YPosition,pixSize,sigma,gammaVal,satVal,box)

if isempty(box)
  minX = min(XPosition);
  maxX = max(XPosition);
  minY = min(YPosition);
  maxY = max(YPosition);
else
  minX = box(1);
  maxX = box(1)+box(3);
  minY = box(2);
  maxY = box(2)+box(4);
end

%remove out of bounds data
isInBounds = XPosition > minX & XPosition < maxX ...
              & YPosition > minY & YPosition < maxY ;

XPosition = XPosition(isInBounds);
YPosition = YPosition(isInBounds);


n=minX:pixSize:maxX;
m=minY:pixSize:maxY;
nx = numel(n);
ny = numel(m);
RR = [...
      round((ny-1)*(YPosition-minY)/(maxY-minY))+1 ...
      round((nx-1)*(XPosition-minX)/(maxX-minX))+1];
density = accumarray(RR,1,[ny,nx]);

%TODO use a faster gauss filter here
sPix = sigma/pixSize;
gWindow = ceil(5*sPix);
gKern = fspecial('gaussian',gWindow, sPix);
dMax = max(density(:));
srIm = density/max(density(:));
srIm = imfilter(srIm,gKern,'replicate');

%srIm = gaussf(srIm/max(srIm(:)),[sigma/pxx sigma/pxy 0]);
% ADJUST GAMMA HERE
srIm(srIm<0)=0;
srIm = saturateImage(srIm,satVal);
srIm = adjustGamma(srIm,gammaVal);

%-----------------------------------------------------------------------------------------------

%-----------------------------------------------------------------------------------------------
function imG= adjustGamma(im,gammaVal)

imMax = max(im(:));
%normalise image
imG = ((im/imMax).^gammaVal)*imMax;

%-----------------------------------
function b= saturateImage(a, satVal)
% function saturateImage(fnameIn, fnameOut, satVal)
%this assumes 0<a<1

satLim = stretchlim(a(:), [0, 1-satVal]);
for ii = 1:size(a,3)
  a(:,:,ii)=imadjust(a(:,:,ii), satLim, [0 1]);
end
b=a;

%---------------------
function z_hue=applyStormCmap(z_cVal);
ncolor = 256;
[cmap stormHue linearHue]=stormCmap(ncolor);
z_hue = interp1(linearHue,stormHue,z_cVal);
