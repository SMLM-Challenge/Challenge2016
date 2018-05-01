function [I,cam_coef,nu,deta,par] = computeCramerRao(c,delta,cam_pix,pos_mol,varargin)
%c: spline coefficients of PSF fitting
%step: step between knot
%cam_pix: camera pixel size in the object plane
%pos: molecule position (0,0) at the top left

par = inputParser;
addParameter(par,'QE',0.9,@isnumeric);
addParameter(par,'spurious_charge',0.0002,@isnumeric);
addParameter(par,'camera_model','half');%'EMCCD','none'
addParameter(par,'sig_readout',74.4,@isnumeric);
addParameter(par,'EmitPhot',5000,@isnumeric);
addParameter(par,'autoBg',109,@isnumeric);
addParameter(par,'eta',0,@isnumeric);
addParameter(par,'gain',300,@isnumeric);
%numerical integration for camera noise coefficient
%related to maximal (approx.) possible number of photons emitted by a fluorophore
%during one frame acquisition on ONE camera pixel
addParameter(par,'NsigU',100,@isnumeric);
addParameter(par,'NsigZ',100,@isnumeric);
addParameter(par,'umin',0.1,@isnumeric);
addParameter(par,'Npoints',1e4,@isnumeric);
addParameter(par,'Npool',4,@isnumeric);

parse(par,varargin{:});
par = par.Results;
reltol = 1e-8; abstol = 1e-11;%deprecated since trapz

par.Npool = max(par.Npool,1);
if any(size(c).*delta/cam_pix~=1)
    padding = ceil(size(c)./delta).*delta -size(c);
    c = padarray(c,[padding(1:2),0],0,'post');
end

[Nx,Ny,~] = size(c);
detector_area = [Nx*delta(1),Ny*delta(2)];
cam_siz = ceil(detector_area/cam_pix);
Ntotpix = prod(cam_siz);
tic
par.bg = par.spurious_charge + par.autoBg*par.QE;
%Nphoton = @(N) N*QE;%For later change, if a function is rather output
mu = par.EmitPhot*par.QE*computeEta(pos_mol,c,delta,cam_siz);
nu = mu + par.bg;
[nu_unique, ~, ind_nu] = unique(round(nu*10)/10);

toc

%syms z;
tic
switch par.camera_model
    case 'EMCCD'
        cam_coef = zeros(length(nu_unique),1);
        for k = 1:length(nu_unique)
            curr_nu = nu_unique(min(k,end));
            
            amp_pcount = curr_nu*par.gain;
            umax = max(1.4*(amp_pcount - 20*par.gain),0) + 50*par.gain + par.NsigU*par.sig_readout;
            
            %possible outcomes from amplification of EMCCD & Gaussian readout noise, depends on photon input
            zmin =  min(amp_pcount - par.NsigU*par.sig_readout,0);
            zmax = umax;
            fprintf('Starting k : %i, U : %i, Z : %i, zmin/zmax : %1.2e/%1.2e, nu : %1.2e...',...
                k, par.NsigU, par.NsigZ, zmin, zmax,curr_nu);
            intervals = linspace(zmin, zmax, 1 + par.Npool);
            curr_cam = zeros(par.Npool,1);
            parfor p = 1:par.Npool
                curr_zmin = intervals(p);
                curr_zmax = intervals(p + 1);
                
                z_vec = linspace(curr_zmin, curr_zmax, par.Npoints);
                curr_cam(p) = trapz(z_vec,noise_coef_fct_vec(z_vec, par.umin, umax, par.eta,...
                    par.sig_readout, curr_nu, par.gain, par.camera_model, reltol, abstol, par.Npoints));
                
                fprintf('noise coeff %i/%i : %1.2e\n',p,par.Npool,...
                    (curr_cam(p) - 1/par.Npool)*curr_nu);
            end
            
            cam_coef(k) = sum(curr_cam);
            
            if mod(k, 1)==0
                fprintf('%i/%i : %1.2f, noise coeff : %1.2e\n',k, length(cam_coef),toc,(cam_coef(k) - 1)*curr_nu);
            end
        end
        cam_coef = cam_coef - 1;
    case {'none','Poisson'}
        cam_coef = 1./nu_unique;
    case 'half'
        cam_coef = 5e-1./nu_unique;
    otherwise
        error('Not implemented yet');
end
cam_coef = cam_coef(ind_nu);
toc

%Compute eta derivative = mu derivative
deta = computedEta(par.EmitPhot,c,Ntotpix,delta,pos_mol,mu,cam_siz); %Output: Ncampix x 3
deta = [deta, mu/par.EmitPhot, ones(Ntotpix,1)];%x,y,z,N,background
I = zeros(size(deta,2));
for kk = 1:size(I,1)
    for ll = 1:size(I,2)
        for mm = 1:Ntotpix
            I(kk,ll) = I(kk,ll) + cam_coef(mm)*deta(mm,kk)*deta(mm,ll);
        end
    end
end
end



