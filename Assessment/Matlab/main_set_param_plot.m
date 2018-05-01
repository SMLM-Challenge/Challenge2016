%% Load pairings file resulting from assessment program
clear pos
path = '~/Dropbox/smlm/figures/';
software = 'SMAP';%'STORMChaser';%
modality = 'BP';
dataset = 'MT1.N1.LD';
wobble = true;
photonT = true;
%File must be in the path
fname = dir(fullfile('assessment_results',software,...
    sprintf('pairings____%s____%s____%s____wobble_%s____border_450____photonT_%s*',...
    dataset,modality,software,strcat(wobble*'*',~wobble*'no'),...
    strcat(photonT*'*',~photonT*'0'))));
fname = fullfile('assessment_results',software,fname(1).name);
%%
[pos.frame,pos.x,pos.y,pos.z,pos.int] = importLocations(fname);

pos = struct2table(pos);

pos(isnan(pos.x),:) = [];
%% GT
clear gt

fname_gt = dir(fullfile('assessment_results','GT',...
    sprintf('pairings____%s____%s____GT____wobble_no____border_450____photonT_%s*',...
    dataset,modality,strcat(photonT*'*',~photonT*'0'))));
fname_gt = fullfile('assessment_results','GT',fname_gt(1).name);

[gt.frame,gt.x,gt.y,gt.z,gt.int] = importLocations(fname_gt);
gt = struct2table(gt);
gt(isnan(gt.x),:) = [];
%%
fov = [500, 300, 150];
pix_size = 1;
imsize = fov/pix_size;
doCorr = 0;
sigmin = 2*pix_size/(2*sqrt(2*log(2)));
sigmax = 4*pix_size/(2*sqrt(2*log(2)));
thresmax = quantile(pos.int,0.95);
thresmin = quantile(pos.int,0.05);
sig = max(min((pos.int - thresmin)/(thresmax - thresmin),1),0);
sig = sigmax + (sigmin - sigmax).*sqrt(sig);

shift = [3000,2200,0];%([6400,6400,1500] - fov)/2;

vecx = (1:pix_size:fov(1)) + shift(1);
vecy = (1:pix_size:fov(2)) + shift(2);
vecz = (1:pix_size:fov(3)) + shift(3);

sig_gt = max(min((gt.int - thresmin)/(thresmax - thresmin),1),0);
sig_gt = sigmax + (sigmin - sigmax).*sqrt(sig_gt);
%% Box and rotate
[newloc,ind_box] = boxrotate([pos.x,pos.y,pos.z],shift,fov);
[newloc_gt,ind_box_gt] = boxrotate([gt.x,gt.y,gt.z],shift,fov);
ngt = size(newloc_gt,1);
ntest = size(newloc,1);


%% GT : get gaussian rendered wrt intensity
tic
im_box_gt = gauss_render_intensity(newloc_gt,sig_gt(ind_box_gt), pix_size, imsize,false);
toc

%% Test : get gaussian rendered wrt intensity

tic
im_box = gauss_render_intensity(newloc,sig(ind_box), pix_size, imsize,false);
toc