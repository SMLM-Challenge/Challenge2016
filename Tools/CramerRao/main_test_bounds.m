%U: 75, Z: 50 for nu = 0.000213528237267334
maxsigU = 100;%25:25:200;
maxsigZ = 100;%[25:25:200];%[10,25,50,75,100,125,150,175];
minSigU = 100;
minSigZ = 100;
minNu = 1e-5;
maxNu = 250;
Ninterval = 16;
Npoints = 5e3;
parpool(min(feature('NumCores'),Ninterval));

camera_model = 'EMCCD';%'none';%
%nphoton = 5e8;
QE = 0.9;
bg = 0.0002;
sig_readout = 24;%74.4;
EmitPhot = 5000;
autoBg = 109;%Autofluorescence
eta = 0; %Mean of Gaussian random variable due to readout noise

if any(size(c).*delta/cam_pix~=1)
    padding = ceil(size(c)./delta).*delta -size(c);
    c = padarray(c,[padding(1:2),0],0,'post');
end

[Nx,Ny,~] = size(c);
detector_area = [Nx*delta(1),Ny*delta(2)];
Ncampix = ceil(detector_area/cam_pix);
Ntotpix = prod(Ncampix);
tic
Nphoton = @(N) N*QE;%For later change, if a function is rather output
%nu = Nphoton(EmitPhot)*computeEta(pos_mol,c,Ntotpix,step,Ncampix) + QE*autoBg + bg;
nu = [logspace(-5,2,8),150,200,250];
toc
gain = 1000;
%maximal (approx.) possible number of photons emitted by a fluorophore
%during one frame acquisition on ONE camera pixel

reltol = 1e-3;
abstol = 1e-7;
fact_tol = 1e0;

timer = zeros(length(nu),1); zminVec = timer; zmaxVec = timer;
camer = timer; Uvec = camer; Zvec = camer; noise_coeff = camer;

for k = 1:length(nu)
    res = struct;
    %for nnn = 1:length(setmaxU)
    %for mmm = 1:length(setmaxZ)
    curr_sig = sig_readout(min(k,end));
    curr_nu = nu(k);
    
    
    %possible outcomes from amplification of EMCCD, depends on photon input
    NsigU = max((maxsigU - minSigU)/log(maxNu/minNu)*log(curr_nu/minNu),0) + minSigU;
    NsigZ = max((maxsigZ - minSigZ)/log(maxNu/minNu)*log(curr_nu/minNu),0) + minSigZ;
    
    amp_pcount = nu(k)*gain;
    umin = 0.1; umax = max(1.4*(amp_pcount - 20*gain),0) + 50*gain + NsigU*sig_readout;
    
    %possible outcomes from amplification of EMCCD & Gaussian readout noise, depends on photon input
    zmin = min(amp_pcount - NsigU*sig_readout,0);
    zmax = umax;
    fprintf('Starting k : %i, U : %i, Z : %i, zmin/zmax : %1.2e/%1.2e, nu : %1.2e...',...
        k,NsigU, NsigZ, zmin,zmax,curr_nu);
    %z: camera electron count (realisation of amplification + Gaussian readout noise)
    curr_reltol = reltol/(max(fact_tol*(NsigU < 0.5*maxsigU),1));
    curr_abstol = abstol/(max(fact_tol*(NsigU < 0.5*maxsigU),1));
    tic
    
    intervals = linspace(zmin, zmax, 1 + max(Ninterval,1));
    curr_cam = cell(max(Ninterval,1),1);
    
    
    parfor p = 1:max(Ninterval,1)
        curr_zmin = intervals(p);
        curr_zmax = intervals(p + 1);
        finer_step = curr_nu <= 1e-2 & curr_zmin <= amp_pcount & curr_zmax >= amp_pcount;
        curr_N = Npoints*(50^(finer_step));
        if true
            z_vec = linspace(curr_zmin, curr_zmax,curr_N);
            curr_cam{p} = trapz(z_vec,noise_coef_fct_vec(z_vec, umin, umax, eta,...
                curr_sig, curr_nu, gain, camera_model, curr_reltol, curr_abstol,Npoints));
        else
            if amp_pcount >= curr_zmin && amp_pcount <= curr_zmax
                waypoints = amp_pcount;
            else
                waypoints = [];
            end
            
            curr_cam{p} = integral(@(z) noise_coef_fct_vec(z, umin, umax, eta,...
                curr_sig, curr_nu, gain, camera_model,curr_reltol,curr_abstol),...
                curr_zmin,curr_zmax,'ArrayValued',0,'RelTol',curr_reltol,'AbsTol',curr_abstol,...
                'Waypoints',waypoints);
        end
        fprintf('noise coeff %i/%i : %1.2e\n',p,max(Ninterval,1),...
            (curr_cam{p} - 1/max(Ninterval,1))*curr_nu);
    end
    curr_cam = sum(cell2mat(curr_cam));
    timer(k) = toc;
    camer(k) = curr_cam;
    zminVec(k) = zmin;
    zmaxVec(k) = zmax;
    Uvec(k) = maxsigU;
    Zvec(k) = maxsigZ;
    noise_coeff(k) = (curr_cam - 1)*curr_nu;
    fprintf('cam : %1.2e, noise coeff : %1.3f, time : %1.2f s\n',...
        curr_cam, noise_coeff(k), timer(k));
    %noise_coeff((nnn-1)*length(setZ) + mmm),timer((nnn-1)*length(setZ) + mmm));
    %iter = iter + 1;
    %end
end

fname = sprintf('fig8_%i_reltol_%i_abstol_%i.mat', k,log10(curr_reltol),log10(curr_abstol));
counter = 1;
while exist(fname,'file')
    fname = sprintf('fig8_%i_reltol_%i_abstol_%i_%i.mat', k, log10(curr_abstol),...
        log10(curr_abstol),counter);
    counter = counter + 1;
end

%save(fname,'timer','camer','Uvec','Zvec','setU','setZ','curr_nu','noise_coeff');
res.timer = timer; res.camer = camer; res.Uvec = Uvec; res.Zvec = Zvec;
res.setU = maxsigU; res.setZ = maxsigZ; res.curr_nu = curr_nu; res.noise_coeff = noise_coeff;
save_parfor(fname,res);
%%

V = cell2mat(noise_coeff)';
Vtime = cell2mat(timer)';
figure;
subplot(121);
imagesc(maxsigU, maxsigZ, V);colorbar;axis image;title('noise coeff');xlabel('U');ylabel('Z');
subplot(122);
imagesc(maxsigU, maxsigZ, Vtime);colorbar;axis image;title('Time [s]');xlabel('U');ylabel('Z');