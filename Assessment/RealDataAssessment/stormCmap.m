%---------------------
function [cmap stormHue linearHue]=stormCmap(ncolor);
linearHue = linspace(0,1,ncolor);
cmap=colormap(jet(ncolor));
cmaphsv=rgb2hsv(cmap);
stormHue= cmaphsv(:,1);
stormHue=unique(stormHue,'stable');
nVal = numel(stormHue);
stormHue = [interp1(1:nVal,stormHue,linspace(1,nVal,ncolor))]';

sat = ones(size(stormHue));
val = ones(size(stormHue));
stormHSV = [stormHue,sat,val];
cmap = hsv2rgb(stormHSV);
