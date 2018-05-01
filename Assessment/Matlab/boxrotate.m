function [ newloc,ind ] = boxrotate(loc,orig,fov)
%BOXROTATE Summary of this function goes here
%   Detailed explanation goes here
shift = orig + fov/2;
thetaxy = -deg2rad(6.35);
thetaxz = -deg2rad(5.08);
dn = 0;

Rxy = [cos(thetaxy),-sin(thetaxy),0;sin(thetaxy),cos(thetaxy),0;0,0,1];
Rxz = [cos(thetaxz),0,sin(thetaxz);0,1,0;-sin(thetaxz),0,cos(thetaxz)];
ind = all(loc >= repmat(orig,size(loc,1),1),2)...
    & all(loc <= repmat(orig + fov,size(loc,1),1),2);
newloc = loc(ind,:);

newloc = newloc - repmat(shift,size(newloc,1),1);

newloc = Rxy*newloc';

newloc = (Rxz*newloc)';

newloc = newloc + repmat(fov/2,size(newloc,1),1) + dn;
end

