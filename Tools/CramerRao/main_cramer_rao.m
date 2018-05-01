%Cramer-Rao bounds
clear
dir_name = '~/Documents/SMLM/analysis/psf/';%'~/Downloads/PSF GL 200x200x151.tif';%
modality = 'AS';
Nz = 151;
[PSF,summedPSF,Nx,Ny] = load_PSF(dir_name,modality,Nz);
%% Fit cubic spline from 3D psf

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
%%

filter1D = [1,4,1]/6;%cubic
filter1D = circshift(filter1D,[0,0]);
filter3D = ktensor({filter1D,filter1D,filter1D});
filter3Dext = padarray(filter3D,[Nx,Ny,Nz]- length(filter1D),'post');
tic

ePSF = ifftn(fftn(c).*fftn(filter3Dext));
toc
err = (ePSF - PSF)./PSF;
fprintf('Average error : %1.4e\n',mean(err(:)));
%% Spatial Conv (Weird)
tic
ePSF = imfilter(c,filter3D,'symmetric','conv');%'replicate', 'symmetric', 'circular'
%ePSF = convn(c,filter3D,'same');
toc
err = (ePSF - PSF)./PSF;
fprintf('Average error : %1.4e\n',mean(err(:)));

%% separable filtering
tic
ePSF = c;
for kk = 1:3
    ePSF = filter(filter1D,1,ePSF,[],kk);
end
toc
err = (ePSF - PSF)./PSF;
fprintf('Average error : %1.4e\n',mean(err(:)));
%% reconstruction method
tic
[X, Y, Z] = ndgrid(1:Nx,1:Ny,1:Nz);
ePSF = evaluateCubicSpline(c,[X(:),Y(:),Z(:)]);
toc
err = (ePSF - PSF)./PSF;
fprintf('Average error : %1.4e\n',mean(err(:)));
%%
if ~exist('ePSF','var')
    ePSF = PSF;
end
figure(10);
for kk = 76 + 18
    subplot(131);imagesc(x,y,ePSF(:,:,kk));
    axis image;title(sprintf('Spline, axial position %1.2f nm',z(kk)));colorbar;
    subplot(132);imagesc(x,y,PSF(:,:,kk));
    axis image;title(sprintf('PSF, axial position %1.2f nm',z(kk)));colorbar;
    subplot(133);imagesc(x,y,abs(ePSF(:,:,kk) - PSF(:,:,kk))./PSF(:,:,kk));
    axis image;title(sprintf('PSF difference, axial position %1.2f nm',z(kk)));colorbar;
    pause(0.001);
end

%% Get CramerRao bounds
cam_pix = 100;%nm
delta = [dx,dy,dz];
%parpool(4);
mkdir('res');
Nphotons_set = [50,250,500,750,1000:500:1e4];
for Nphotons = 1:length(Nphotons_set)
    stack = repmat(struct('CRLB',[],'I',[],'cam_coef',[],...
        'nu',[],'deta',[],'par',[]),151,1);

    parfor k = 1:151
        pos_mol = [0,0,k].*delta;%bounds : [-215,215] x [-215,215] x [0,150] .* step        
        glob_timer = tic;
        [stack(k).I,stack(k).cam_coef,stack(k).nu,stack(k).deta,stack(k).par] ...
            = computeCramerRao(c, delta, cam_pix, pos_mol,...
            'EmitPhot',Nphotons,'camera_model','half');
        toc(glob_timer)
        CRLB = sqrt(diag(stack(k).I^(-1)));
        stack(k).CRLB.x = CRLB(1);
        stack(k).CRLB.y = CRLB(2);
        stack(k).CRLB.z = CRLB(3);
        stack(k).CRLB.photon = CRLB(4);
        stack(k).CRLB.bg = CRLB(5);
    end
    stack = struct2table(stack);
    save(sprintf('res/stack_photon_%i.mat',Nphotons_set(Nphotons)),stack);
end

%%
I = zeros(size(I));
for kk = 1:size(I,1)
    for ll = 1:size(I,2)
        for mm = 1:length(nu)
            I(kk,ll) = I(kk,ll) + cam_coef(mm)*deta(mm,kk)*deta(mm,ll);
        end
    end
end
CRLB = sqrt(diag(I^(-1)))
%%
tmp = I(1:3,1:3);
diag(tmp^(-1))
%%
ind = pairings.Zt <= 5 & pairings.Zt >= -5;
rmseX = sqrt(nanmean((pairings.Xt(ind) - pairings.Xs(ind)).^2))
rmseY = sqrt(nanmean((pairings.Yt(ind) - pairings.Ys(ind)).^2))
rmse = sqrt(nanmean((pairings.Xt(ind) - pairings.Xs(ind)).^2 + (pairings.Yt(ind) - pairings.Ys(ind)).^2))

%%

for k = 1:height(stack)
    CRLBx(k) = stack.CRLB{k}(1);
    CRLBy(k) = stack.CRLB{k}(2);
    CRLBz(k) = stack.CRLB{k}(3);
    photon(k) = stack.CRLB{k}(4);
    bg(k) = stack.CRLB{k}(5);
end
t = 1:10:151;
figure,plot(t,CRLBx);hold on;
plot(t,CRLBy);
plot(t,CRLBz);
legend('X','Y','Z');