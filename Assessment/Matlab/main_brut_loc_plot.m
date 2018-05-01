%% Load Brut loc

locup = dir(fullfile(software,'standard',sprintf('%s____%s____%s*',...
    dataset,modality,software)));
%header = textscan(fullfile(software,'upload',locup(1).name),'Delimiter',',',0,0);
locup = csvread(fullfile(software,'standard',locup(1).name),1,0);
locup = array2table(locup(:,1:end),'VariableNames',{'frame' 'x' 'y' 'z' 'int'});
%%
sigmin = 20/(2*sqrt(2*log(2)));
sigmax = 20/(2*sqrt(2*log(2)));
thresmax = quantile(locup.int,0.95);
thresmin = quantile(locup.int,0.05);
sig_locup = max(min((locup.int - thresmin)/(thresmax - thresmin),1),0);
sig_locup = sigmax + (sigmin - sigmax).*sqrt(sig_locup);
%sig_locup = sigmin + sig_locup./sqrt(locup.int);

%Get "density map" invers. prop. to sqrt(estimated intensity), see sig exp.
[im_locup] = gauss_render_intensity([locup.x,locup.y,locup.z] - repmat(shift,height(locup),1),...
    sig_locup, pix_size, imsize,doCorr);
%%
tic
im2Dcolored = depthcolor3D(im_gt,'jet',0.99,3);
toc
figure;
imagesc(im2Dcolored);
%% Display 2D XZ view

curr_im = squeeze(sum(im_locup,2))';
figure;
imagesc(vecx,vecz, curr_im);colormap hot
title(sprintf('XZ view brut, %s %s %s',software,modality,dataset),'FontSize',16);
axis image;
%% XY
figure;
imagesc(vecx,vecy,sum(im_locup,3));colormap hot;
title(sprintf('XY view brut, %s %s %s',software,modality,dataset),'FontSize',16);
axis image;

%% YZ
figure;
imagesc(vecy,vecz,squeeze(sum(im_locup,1))');colormap hot;
title(sprintf('YZ view, %s %s %s',software,modality,dataset),'FontSize',16);
%caxis([quantile(curr_im(:),0.01),quantile(curr_im(:),0.999)])
axis image;
%% for color coded scatter
[fig_h_locup,circSize_locup,color_locup,loc_rm] = disp3D([locup.frame,...
    locup.x,locup.y,locup.z,locup.int] - repmat([0,shift,0],height(locup),1),sprintf('%s %s %s brut',dataset, software,modality),im_locup,pix_size);

loc_rm = array2table(loc_rm,'VariableNames',locup.Properties.VariableNames);
%%
thresmax = quantile(loc_rm.int,0.95);
thresmin = quantile(loc_rm.int,0.05);
sig_locrm = max(min((loc_rm.int - thresmin)/(thresmax - thresmin),1),0);
sig_locrm = sigmax + (sigmin - sigmax).*sqrt(sig_locrm);
%% Disp brut loc
figure;
scatter3(loc_rm.y,loc_rm.x,loc_rm.z,10,color_locup,'filled');
view(0,90);
set(gcf,'Color','black');
xlim([0,fov(1)]);ylim([0,fov(2)]);
axis off;

%%
javaaddpath('/Applications/MATLAB_R2016a.app/java/mij.jar')
javaaddpath('/Applications/MATLAB_R2016a.app/java/ij.jar')