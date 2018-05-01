function [PSF,summed,Nx,Ny] = load_PSF(dir_name, modality,Nz)

switch modality
    case {'2D','AS','DH'}
        fname = fullfile(dir_name, sprintf('%s-Exp.tif', modality));
    case 'BP'
        fname{1} = fullfile(dir_name, sprintf('%s+250.tif', modality));
        fname{2} = fullfile(dir_name, sprintf('%s-250.tif', modality));
    otherwise
        error('Unknown modality');
end

if iscell(fname)
    Ncam = length(fname);
    Ny = zeros(Ncam,1);
    for kk = 1:Ncam
        file{kk} = Tiff(fname{kk},'r');
        if kk==1
            Nx = file{kk}.getTag('ImageLength');
        elseif Nx~=file{kk}.getTag('ImageLength')
            error('Loaded Tiffs do not have the same dimension in X');
        end
        Ny(kk) = file{kk}.getTag('ImageWidth');
    end
    PSF = zeros(Nx,sum(Ny),Nz);
else
    file = Tiff(fname,'r');
    
    Nx = file.getTag('ImageLength');
    Ny = file.getTag('ImageWidth');
    PSF = zeros(Nx,Ny,Nz);
end
%figure(1);
summed = zeros(Nz,1);
for kk = 1:Nz
    if iscell(fname)
        for ll = 1:Ncam
            PSF(:,1 + sum(Ny(1:(ll-1))):sum(Ny(1:(ll-1)))+Ny(ll),kk) = file{ll}.read;
            if kk < Nz
                file{ll}.nextDirectory
            end
        end
        summed(kk) = sum(sum(PSF(:,:,kk),1),2);
    else
        PSF(:,:,kk) = file.read;
        %imagesc(PSF(:,:,kk));
        summed(kk) = sum(sum(PSF(:,:,kk),1),2);
        %axis image;title(sprintf('slice %i, slice sum %1.2f',kk,summed(kk)));
        %colorbar;pause(0.001);
        if kk < Nz
            file.nextDirectory;
        end
    end
end
Ny = sum(Ny);
PSF = PSF/summed(76);
end