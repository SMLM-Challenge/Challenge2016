%Script for winners visualisations

%2D LD 3D-DAOSTORM
%2D HD SMfit
%AS LD CSpline
%AS HD SMolPhot
%BP LD MIATool
%BP HD ThunderSTORM
%DH LD CSpline
%DH HD CSpline
clear
winners = table(cell(10,1),cell(10,1),cell(10,1),'VariableNames',{'modality','density','software'});
winners.modality{1} = 'AS';winners.density{1} = 'LD';winners.software{1} = 'GT';
winners.modality{2} = 'AS';winners.density{2} = 'HD';winners.software{2} = 'GT';
winners.modality{3} = '2D';winners.density{3} = 'LD';winners.software{3} = 'GT';
winners.modality{4} = '2D';winners.density{4} = 'HD';winners.software{4} = 'GT';

winners.modality{5} = '2D';winners.density{5} = 'LD';winners.software{5} = '3D-DAOSTORM';
winners.modality{6} = '2D';winners.density{6} = 'HD';winners.software{6} = 'SMfit';
winners.modality{7} = 'AS';winners.density{7} = 'LD';winners.software{7} = 'CSpline';
winners.modality{8} = 'AS';winners.density{8} = 'HD';winners.software{8} = 'SMolPhot';
winners.modality{9} = 'BP';winners.density{9} = 'LD';winners.software{9} = 'MIATool';
winners.modality{10} = 'BP';winners.density{10} = 'HD';winners.software{10} = 'ThunderSTORM';
winners.modality{11} = 'DH';winners.density{11} = 'LD';winners.software{11} = 'CSpline';
winners.modality{12} = 'DH';winners.density{12} = 'HD';winners.software{12} = 'CSpline';

sigmin = 20/(2*sqrt(2*log(2)));
sigmax = 30/(2*sqrt(2*log(2)));
doInt = false;
Nneigh = 10;

save3Dvol = 1;%3D volumes or orthoview/color-coded
center = 0;
pix_size = 5;%don't 1


doCorr = 1;%leave it at 1, boolean for shift in z when gaussian rendering

folder_res = pwd;
if center
    folder_res = fullfile(folder_res,'res',strcat(save3Dvol*'3Dvol',~save3Dvol*'3Dcolored'),'full');
else
    folder_res = fullfile(folder_res,'res',strcat(save3Dvol*'3Dvol',~save3Dvol*'3Dcolored'),'zoom');
end
addpath(folder_res);
mkdir(folder_res);
set(0,'DefaultTextInterpreter','LaTex');
for kk = 1:height(winners)
    for ll = 1:2
        software = winners.software{kk};
        modality = winners.modality{kk};
        if strcmpi(winners.density{kk},'LD')
            if ll==1
                if strcmpi(winners.modality{kk},'2D')
                    dataset = 'ER1.N3.LD';
                else
                    dataset = 'MT1.N1.LD';
                end
            else
                dataset = 'MT3.N2.LD';
            end
        else
            if ll==1
                if strcmpi(winners.modality{kk},'2D')
                    dataset = 'ER2.N3.HD';
                else
                    dataset = 'MT2.N1.HD';
                end
                
            else
                dataset = 'MT4.N2.HD';
            end
        end
        locup = dir(fullfile(software,'standard',sprintf('%s____%s____%s*',...
            dataset,modality,software)));
        %header = textscan(fullfile(software,'upload',locup(1).name),'Delimiter',',',0,0);
        locup = csvread(fullfile(software,'standard',locup(1).name),1,0);
        locup = array2table(locup(:,1:end),'VariableNames',{'frame' 'x' 'y' 'z' 'int'});
        
        if center
            fov = [6400,6400,1500];
        elseif strcmpi(dataset,'MT1.N1.LD')
            fov = [1200,3000, 1500];%[500,1200,1500];%
        elseif strcmpi(dataset,'MT2.N1.HD')
            fov = [800,3000, 1500];
        elseif strcmpi(dataset,'MT3.N2.LD')
            fov = [1500,600,1500];%[1500, 1800, 1500];
        elseif strcmpi(dataset,'MT4.N2.HD')
            fov = [2000,1250,1500];%[1800,1500,1500];
        elseif strcmpi(dataset,'ER1.N3.LD')
            fov = [1400,3000, 1500];
        elseif strcmpi(dataset,'ER2.N3.HD')
            fov = [1500,3000, 1500];
        end
        
        if center
            shift = ([6400,6400,1500] - fov)/2;
        elseif strcmpi(dataset,'MT1.N1.LD')
            shift = [1950,4900,0] - [fov(1:2)/2,0];%[1650,4050,0];%
        elseif strcmpi(dataset,'MT2.N1.HD')
            shift = [1500,4750,0] - [fov(1:2)/2,0];
        elseif strcmpi(dataset,'MT3.N2.LD')
            shift = [4500,1600,0]-[fov(1:2)/2,0];%[3750,650,0];
        elseif strcmpi(dataset,'MT4.N2.HD')
            shift = [3650,1900,0]-[fov(1:2)/2,0];%[2200,4500,0];%MT4
        elseif strcmpi(dataset,'ER1.N3.LD')
            shift = [1400,4900,0] - [fov(1:2)/2,0];
        elseif strcmpi(dataset,'ER2.N3.HD')
            shift = [2700,1750,0] - [fov(1:2)/2,0];%croisement a droite %[1900,4900,0]-[fov(1:2)/2,0];%croisement a droite
        end
        
        imsize = fov/pix_size;
        if prod(imsize)*8 > 1.2e10
            fprintf('BIG volume ! > 12 Go...5 seconds for cancelling\n');pause(5);
        end
        if doInt
            thresmax = quantile(locup.int,0.95);
            thresmin = quantile(locup.int,0.05);
            sig_locup = max(min((locup.int - thresmin)/(thresmax - thresmin),1),0);
            sig_locup = sigmax + (sigmin - sigmax).*sqrt(sig_locup);
        else
            sig_locup = getSigma(sigmin,sigmax,[locup.x,locup.y,locup.z], Nneigh);
        end
        %sig_locup = sigmin + sig_locup./sqrt(locup.int);
        
        %Get "density map" invers. prop. to sqrt(estimated intensity), see sig exp.
        im_locup = gauss_render_intensity([locup.x,locup.y,locup.z] - repmat(shift,height(locup),1),...
            sig_locup, pix_size, imsize,doCorr);
        
        
        if save3Dvol
            %im_locup = im_locup*255/max(im_locup(:));
            fname = sprintf('dataset_%s_modality_%s_density_%s_software_%s_sig_%1.2f_%1.2f_pixsiz_%i_fov_%ix%ix%i_shift_%ix%ix%i_doInt_%i.tif',...
                dataset,modality,winners.density{kk},software,sigmin,sigmax,pix_size,fov,shift,doInt);
            try delete(fullfile(folder_res,fname));catch end
            for K = 1:imsize(3)
                imwrite(im_locup(:, :, K), fullfile(folder_res,fname), 'WriteMode', 'append','Compression','none');
            end
            
        else
            for dim = 1:3
                
                tic
                [im2Dcolored,T1,T2,T3,L] = depthcolor3D(im_locup,'jet',0.99,dim);
                toc
                
                fname = sprintf('dataset_%s_modality_%s_density_%s_software_%s_sig_%1.2f_%1.2f_pixsiz_%i_fov_%ix%ix%i_shift_%ix%ix%i_T1_%1.2f_T2_%1.2f_T3_%1.2f_L_%i_doInt_%i',...
                    dataset,modality,winners.density{kk},software,sigmin,sigmax,pix_size,fov,shift,T1,T2,T3,L,doInt);
                
                switch dim
                    case 1
                        fname = sprintf('%s_YZ.tiff',fname);
                    case 2
                        fname = sprintf('%s_XZ.tiff',fname);
                    case 3
                        fname = sprintf('%s_XY.tiff',fname);
                end
                
                
                if dim==1
                    im2Dcolored = permute(im2Dcolored,[2,1,3]);
                end
                figure(5);
                imagesc(im2Dcolored);axis image;
                title(fname);drawnow;%pause
                
                fprintf('Saving in %s, an RGB image %s\n',folder_res,fname);
                
                imwrite(im2Dcolored,fullfile(folder_res,fname));
            end
        end
    end
end