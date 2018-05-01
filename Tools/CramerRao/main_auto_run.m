%Cramer-Rao bounds
clear
modality_set = {'BP','AS','DH','2D'};
Nmod = length(modality_set);
dir_name = '~/Documents/SMLM/analysis/psf/';%'~/Downloads/PSF GL 200x200x151.tif';%
Nz = 151;
%%0
for l = 1%:Nmod
    modality = modality_set{l};
    
    [PSF,summedPSF,Nx,Ny] = load_PSF(dir_name,modality,Nz);
    %% Fit cubic spline from 3D psf
    fprintf('Starting %s\n',modality);
    %[Nx,Ny,Nz] = size(PSF);
    d = [3*ones(1,3),0,0,0];%degree of Bsplines
    dx = 10;%nm
    dy = 10;%nm
    dz = 10;%nm
    
    x = (1-Nx/2:Nx/2)*dx; y = (1-Ny/2:Ny/2)*dy; z = (1-Nz/2:Nz/2)*dz;
    %%
    tic
    %c = PSF;
    c = computeCubicSplineCoefs(PSF);
    %c = spm_bsplinc(PSF,d);
    toc
    
    %% Get CramerRao bounds
    cam_pix = 100;%nm
    delta = [dx,dy,dz];
    stack = repmat(struct('CRLB',[],'I',[],'cam_coef',[],...
        'nu',[],'deta',[]),151,1);
    %parpool(4);
    
    for k = 1:151
        pos_mol = [0,0,k].*delta;%bounds : [-215,215] x [-215,215] x [0,150] .* step
        
        glob_timer = tic;
        [stack(k).I,stack(k).cam_coef,stack(k).nu,stack(k).deta]...
            = computeCramerRao(c, delta, cam_pix, pos_mol);
        toc(glob_timer)
        CRLB = sqrt(diag(stack(k).I^(-1)));
        stack(k).CRLB.x = CRLB(1);
        stack(k).CRLB.y = CRLB(2);
        stack(k).CRLB.z = CRLB(3);
        stack(k).CRLB.photon = CRLB(4);
        stack(k).CRLB.bg = CRLB(5);
    end
    stack = struct2table(stack);
    stack.CRLB = struct2table(stack.CRLB);
    save(sprintf('res_%s.mat',modality),'stack','modality');
end
%%
if false
%%

set(0,'DefaultTextInterpreter','LaTex');
lw = 2;
fs = 14;
z = -750:10:750;
clear h;
%close all;

load('res_AS.mat');
figure(1);
plot(z, stack.CRLB.x,'LineWidth',lw);hold on;
plot(z, stack.CRLB.y,'LineWidth',lw);
plot(z, stack.CRLB.z,'LineWidth',lw);
h(1) = gca;
legend('X','Y','Z');
title(sprintf('Modality : %s',modality),'FontSize',fs);
ylabel('CRLB [nm]','FontSize',fs);
xlabel('Axial position [nm]','FontSize',fs);
axis([z(1),z(end), 0, 80]);

saveas(gcf,sprintf('CRLB_%s',modality),'fig');

load('res_DH.mat');
figure(2);
plot(z, stack.CRLB.x,'LineWidth',lw);hold on;
plot(z, stack.CRLB.y,'LineWidth',lw);
plot(z, stack.CRLB.z,'LineWidth',lw);
h(2) = gca;
legend('X','Y','Z');
title(sprintf('Modality : %s',modality),'FontSize',fs);
ylabel('CRLB [nm]','FontSize',fs);
xlabel('Axial position [nm]','FontSize',fs);
axis([z(1),z(end), 0, 80]);

saveas(gcf,sprintf('CRLB_%s',modality),'fig');

%load('res_BP_bg_50.mat');
load('res_BP_bg_100.mat');
figure(3);
plot(z, stack.CRLB.x,'LineWidth',lw);hold on;
plot(z, stack.CRLB.y,'LineWidth',lw);
plot(z, stack.CRLB.z,'LineWidth',lw);
h(3) = gca;
legend('X','Y','Z');
title(sprintf('Modality : %s',modality),'FontSize',fs);
ylabel('CRLB [nm]','FontSize',fs);
xlabel('Axial position [nm]','FontSize',fs);
axis([z(1),z(end), 0, 80]);

saveas(gcf,sprintf('CRLB_BG_109_%s',modality),'fig');
%saveas(gcf,sprintf('CRLB_bg_54_%s',modality),'fig');

load('res_2D.mat');
figure(4);
plot(z, stack.CRLB.x,'LineWidth',lw);hold on;
plot(z, stack.CRLB.y,'LineWidth',lw);
plot(z, stack.CRLB.z,'LineWidth',lw);
h(4) = gca;
legend('X','Y','Z');
title(sprintf('Modality : %s',modality),'FontSize',fs);
ylabel('CRLB [nm]','FontSize',fs);
xlabel('Axial position [nm]','FontSize',fs);
axis([z(1),z(end), 0, 80]);

saveas(gcf,sprintf('CRLB_%s',modality),'fig');

linkaxes(h,'xy');
%% Check min(nu(:)),max

stats_nu = table(cell(Nmod,1),ones(Nmod,1),ones(Nmod,1),ones(Nmod,1),ones(Nmod,1),...
    'VariableNames',{'modality','imin','min','imax','max'});

for kk = 1:length(modality_set)
    load(sprintf('res_%s.mat', modality_set{kk}));
    stats_nu.modality{kk} = strrep(modality_set{kk},'2D','twodim');
    [stats_nu.min(kk),stats_nu.imin(kk)] = min(cellfun(@(x) min(x), stack.nu));
    [stats_nu.max(kk), stats_nu.imax(kk)] = max(cellfun(@(x) max(x), stack.nu));
end

%%



%%
end