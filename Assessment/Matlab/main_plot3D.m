%% Load pairings file resulting from assessment program
clear pos
path = '~/Dropbox/smlm/figures/';
%Winners:
%2D LD 3D-DAOSTORM
%2D HD SMfit
%AS LD CSpline
%AS HD SMolPhot
%BP LD MIATool
%BP HD ThunderSTORM
%DH LD CSpline
%DH HD CSpline
software = 'CSpline';%'TVSTORM';%'CSpline';%'3D-WTM';%'MIATool-RMS';%'STORMChaser';
modality = 'AS';
dataset = 'MT3.N2.LD';
wobble = false;
photonT = true;
%File must be in the path
if wobble
    fname = dir(fullfile('assessment_results',software,...
        sprintf('pairings____%s____%s____%s____wobble_%s____border_450____photonT_*',...
        dataset,modality,software,'beads')));
    if isempty(fname)
        fname = dir(fullfile('assessment_results',software,...
            sprintf('pairings____%s____%s____%s____wobble_%s____border_450____photonT_*',...
            dataset,modality,software,'file')));
    end
    if isempty(fname)
        error('No file found');
    end
else
    fname = dir(fullfile('assessment_results',software,...
        sprintf('pairings____%s____%s____%s____wobble_%s____border_450____photonT_*',...
    dataset,modality,software,'no')));
end

iter = 0;
for kk = 1:length(fname)
    if ~isempty(strfind(fname(kk - iter).name,'photonT_0_'))
        fname(kk - iter) = [];
        iter = iter + 1;
    end
end
fname = fullfile('assessment_results',software,fname(1).name);

fprintf('Reading file %s\n',fname);

%%
[pos.frame,pos.x,pos.y,pos.z,pos.int] = importLocations(fname);

pos = struct2table(pos);

pos(isnan(pos.x),:) = [];
%% GT
clear gt

fname_gt = dir(fullfile('assessment_results','GT',...
    sprintf('pairings____%s____%s____GT____wobble_no____border_450____photonT_*',...
    dataset,modality)));

iter = 0;
for kk = 1:length(fname_gt)
    if ~isempty(strfind(fname_gt(kk - iter).name,'photonT_0_'))
        fname_gt(kk - iter) = [];
        iter = iter + 1;
    end
end

fname_gt = fullfile('assessment_results','GT',fname_gt(1).name);

fprintf('Reading file %s\n',fname_gt);

[gt.frame,gt.x,gt.y,gt.z,gt.int] = importLocations(fname_gt);
gt = struct2table(gt);
gt(isnan(gt.x),:) = [];
%%
center = false;
if center
    fov = [6400,6400,1500];
elseif strcmpi(dataset,'MT1.N1.LD') 
    fov = [3000, 1200, 1500];%[500,1200,1500];%
elseif strcmpi(dataset,'MT2.N1.HD')
    fov = [3000, 1200, 1500];
elseif strcmpi(dataset,'MT3.N2.LD')
    fov = [1500, 1800, 1500];
elseif strcmpi(dataset,'MT4.N2.HD')
    fov = [2400,2000,1500];%[1800,1500,1500];
end
pix_size = 2;%don't 1
imsize = fov/pix_size;
doCorr = 1;
sigmin = 10/(2*sqrt(2*log(2)));
sigmax = 10/(2*sqrt(2*log(2)));
thresmax = quantile(pos.int,0.95);
thresmin = quantile(pos.int,0.05);
sig = max(min((pos.int - thresmin)/(thresmax - thresmin),1),0);
sig = sigmax + (sigmin - sigmax).*sqrt(sig);

if center
    shift = ([6400,6400,1500] - fov)/2;
elseif strcmpi(dataset,'MT1.N1.LD')
    shift = [3000,2000,0];%[1650,4050,0];%
elseif strcmpi(dataset,'MT2.N1.HD')
    shift = [3000,2000,0];
elseif strcmpi(dataset,'MT3.N2.LD')
    shift = [3750,650,0];
elseif strcmpi(dataset,'MT4.N2.HD')
    shift = [2600,1200,0];%[2200,4500,0];%MT4
end
vecx = (1:pix_size:fov(1)) + shift(1);
vecy = (1:pix_size:fov(2)) + shift(2);
vecz = (1:pix_size:fov(3)) + shift(3);

thresmax = quantile(gt.int,0.95);
thresmin = quantile(gt.int,0.05);

sig_gt = max(min((gt.int - thresmin)/(thresmax - thresmin),1),0);
sig_gt = sigmax + (sigmin - sigmax).*sqrt(sig_gt);

%% Get "density map" invers. prop. to sqrt(estimated intensity), see sig exp.
tic
im = gauss_render_intensity([pos.x,pos.y,pos.z] - repmat(shift,height(pos),1),sig, pix_size, imsize,doCorr);
toc
%% Display 2D XZ view

curr_im = squeeze(sum(im,2))';
figure;
imagesc(vecx,vecz, curr_im);colormap hot
title(sprintf('XZ view, %s %s %s',software,modality,dataset),'FontSize',16);
axis image;
%% XY
figure;
imagesc(vecx,vecy,sum(im,3));colormap hot;
title(sprintf('XY view, %s %s %s',software,modality,dataset),'FontSize',16);
axis image;

%% YZ
figure;
imagesc(vecy,vecz,squeeze(sum(im,1))');colormap hot;
title(sprintf('YZ view, %s %s %s',software,modality,dataset),'FontSize',16);
%caxis([quantile(curr_im(:),0.01),quantile(curr_im(:),0.999)])
axis image;

%% Get color coded depth (and display 3D as assessment
tic
[fig_h,circSize,color] = disp3D([pos.frame,pos.x,pos.y,pos.z,pos.int],...
    sprintf('%s %s %s',dataset, software,modality),im,pix_size);
toc
%% Display 2D depth color coded
figure;
scatter(pos.x,pos.y,circSize,color);
axis off;
%print(fullfile(path,sprintf('%s-%s-%s.pdf',dataset,software,modality)),'-dpdf','-r0');
set(gcf, 'PaperUnits', 'centimeters');
set(gcf,'PaperPosition', [0 0 10 15]);
saveas(gcf,fullfile(path,sprintf('%s-%s-%s.pdf',dataset,software,modality)));
title(sprintf('%s %s %s',dataset, software,modality),'FontSize',16);

%% Render GT
tic
im_gt = gauss_render_intensity([gt.x,gt.y,gt.z] - repmat(shift,height(gt),1),...
    sig_gt, pix_size, imsize,1);
toc
%%%
%[fig_h_gt,circSize_gt,color_gt] = disp3D([gt.frame,gt.x,gt.y,gt.z,gt.int]- repmat([0,shift,0],height(gt),1),'GT',im_gt,pix_size);
%% XZ GT

figure;
imagesc(squeeze(sum(im_gt,2))');colormap hot;
title(sprintf('XZ view, GT %s',dataset),'FontSize',16);
%axis image;
%% XY GT
figure;
imagesc(vecx,vecy,squeeze(sum(im_gt,3)));colormap hot;
title(sprintf('XY view, GT %s',dataset),'FontSize',16);
axis image;
%% YZ GT

figure;
imagesc(vecy,vecz,squeeze(sum(im_gt,1))');colormap hot;
title(sprintf('YZ view, GT %s',dataset),'FontSize',16);
axis image;
%% Disp GT
figure;
scatter(gt.x,gt.y,circSize_gt,color_gt);
title(sprintf('%s GT %s',dataset,modality),'FontSize',16);
axis off;
%% Slice of z
figure;
minz = -100;
maxz = 100;
ind_z = gt.z >= minz & gt.z <= maxz;
scatter3(gt.x(ind_z),gt.y(ind_z),gt.z(ind_z),5,'filled');
xlabel('X');ylabel('Y');zlabel('Z');

%%
figure;
scatter(newloc_gt(:,2),newloc_gt(:,3),20,...
    [0*ones(ngt,1),...
    (newloc_gt(:,1)-min(newloc_gt(:,1)))/(max(newloc_gt(:,1))-min(newloc_gt(:,1))),...
    0*ones(ngt,1)],...
    'filled');
hold on;
scatter(newloc(:,2),newloc(:,3),20,...
    [(newloc(:,1)-min(newloc(:,1)))/(max(newloc(:,1))-min(newloc(:,1)))...
    0*ones(ntest,1),0*ones(ntest,1)],...
    'filled');
axis image;
%%
figure;
scatter3(newloc_gt(:,1),newloc_gt(:,2),newloc_gt(:,3),20,'filled','g');hold;%...
%     [0*ones(ngt,1),...
%     (gt.int(ind_box_gt,1)-min(gt.int(ind_box_gt,1)))/(max(gt.int(ind_box_gt,1))...
%     -min(gt.int(ind_box_gt,1))),...
%     0*ones(ngt,1)],...
%     'filled');hold on;

scatter3(newloc(:,1),newloc(:,2),newloc(:,3),20,...
    [(pos.int(ind_box,1)-quantile(pos.int(ind_box,1),0.05))/(quantile(pos.int(ind_box,1),0.95)...
    -quantile(pos.int(ind_box,1),0.05)),...
    0*ones(ntest,1),...
    0*ones(ntest,1)],...
    'filled');
%%

figure,imagesc(squeeze(sum(im_box,1)));axis image;

%%
tmp = gcf;
tmp_gca = tmp.CurrentAxes;
loops = 100;
inc_view = [360/loops,0];
F(loops) = struct('cdata',[],'colormap',[]);
tmp_gca.View = [-37.5,30];
figure(tmp);

for kk = 1:loops
tmp_gca.View = tmp_gca.View + inc_view;
drawnow;
F(kk) = getframe;
end
%%
v = VideoWriter(sprintf('~/Dropbox/smlm/figures/%s_%s_video.mp4',software,modality),'MPEG-4');
open(v)
writeVideo(v,F);
close(v)