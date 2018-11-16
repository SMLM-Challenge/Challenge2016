function [srIm_XZ X1] = plotZcrossSection(x,y,z,xyPos,lineWidth,rangez, pixSz,satVal,blurSigma,useAngleFormat)

if ~exist('useAngleFormat','var')
    useAngleFormat=false;
end

if ~useAngleFormat
    Dx = (xyPos(2,1)-xyPos(1,1));
    Dy = (xyPos(2,2)-xyPos(1,2));
    X0 = [xyPos(1,1),xyPos(1,2)]
    lineLength = sqrt(Dx^2+Dy^2)
    lineAngle = atan2(Dy,Dx) %in radians
else
    X0 = [xyPos(1),xyPos(2)];
    lineLength = xyPos(3);
    lineAngle = xyPos(4)*pi/180;%input is in degrees
    X1 = [X0(1)+lineLength*cos(lineAngle),X0(2)+lineLength*sin(lineAngle)];
end


%rotate the data parallel to x axis
R = [cos(-lineAngle),-sin(-lineAngle);...
      sin(-lineAngle),cos(-lineAngle)];

[XYRot]=R*[x';y'];
X0Rot = R*X0';
xRot = XYRot(1,:)' -X0Rot(1) ;
yRot = XYRot(2,:)' - X0Rot(2);
% then flip z & y ax s.t. y'=z & z'=-y. 
along_crossSec= xRot;
across_crossSec =-yRot;
z_crossSec  = -z ;

% apply the filter
alongMin = 0; alongMax = lineLength;
acrossMin = -lineWidth/2; acrossMax = +lineWidth/2;

isOk = (along_crossSec>=alongMin & along_crossSec<=alongMax...
        & across_crossSec>=acrossMin & across_crossSec<=acrossMax);
along_crossCrop= along_crossSec(isOk);
across_crossCrop= across_crossSec(isOk);
z_crossCrop= z_crossSec(isOk);


box = [alongMin,rangez(1),alongMax-alongMin,rangez(2)-rangez(1)];
[srIm_XZ] = renderStormData(along_crossCrop,z_crossCrop,'Saturate',satVal,'PixelSize',pixSz,'Sigma',blurSigma,'Box',box);
