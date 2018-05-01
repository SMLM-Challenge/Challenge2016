function out = assessment_graph(filenames,varargin)
%ASSESSMENT_GRAPH : Create distribution of GT & TP wrt various variables
%and with different settings; wobble and pairing criterion (2D or 3D) curr.
%   Common features among compared results
%	-One participant
%   -One dataset
%   -One modality
%   -One intensity threshold (or not)
%    Note : only minor changes are needed for multiparticipants, datasets,
%    modalities, etc.
%   Additional metrics are calculated from the distribution.
%   varargin : 'RMSE' -> threshold to elicit an effective z-range for softw
%              'metrics' -> same for recall/precision/JAC rsp
%   Written by Thanh-an Pham, 2016

global max_z_range

n=1;
fov = [6400,6400,1500];%nm
fold_path = 'assessment_results';
Nelements = 150;
Ylimit = 3e3;
%fold_all = 'comparatif';
Ylimits = [inf,120,150,1,1,100];%nm
%RMSE_thres = 50;%nm
exclusion = 450;%nm
metric_thres = [0.25,0.5;0.25,0.5;25,50];%one metric per row (several thresholds)
while n < nargin-1
    switch varargin{n}
        case 'fov'
            fov = varargin{n+1};
        case 'fold_path'
            fold_path = varargin{n+1};
        case 'Nelements'
            Nelements = varargin{n+1};
        case 'metrics'
            metric_thres = varargin{n+1};
        case 'Nsamples_smooth'
            Nsamples_smooth = varargin{n+1};
        case 'Alpha_smooth'
            Alpha_smooth = varargin{n+1};
        case 'Ylimit4rmse'
            Ylimits = varargin{n+1};
        case 'exclusion'
            exclusion = varargin{n+1};
    end
    n = n+2;
end

Ndatasets = length(filenames);
dim3D = cell(Ndatasets,1);wobble = dim3D;wobble_orig = dim3D; photonT = dim3D; pairings = dim3D;
for kk=1:Ndatasets
    pairings{kk} = csvread(filenames{kk});
    
    wobble_orig{kk} = filenames{kk}(strfind(filenames{kk},'____wobble_')...
        +11:strfind(filenames{kk},'____border_')-1);
    if strcmp(wobble_orig{kk},'no')
        wobble{kk} = 'no wobble';
    else
        wobble{kk} = 'wobble';
    end
    dim3D{kk} = str2double(filenames{kk}(strfind(filenames{kk},'____dim3D_')...
        +10));
    if dim3D{kk}
        dim3D{kk} = '3D';
    else
        dim3D{kk} = '2D';
    end
    photonT{kk} = filenames{kk}(strfind(filenames{kk},'____photonT_')...
        +12:strfind(filenames{kk},'____date')-1);
end
sep = strfind(filenames{1},'____');
dataset = filenames{1}(sep(1)+4:sep(2)-1);
modality = filenames{1}(sep(2)+4:sep(3)-1);
participant = filenames{1}(sep(3)+4:sep(4)-1);

save_folder = fullfile(fold_path,participant,'figures','statistics');
if ~exist(save_folder,'dir')...
        || ~exist(fullfile(save_folder,'eps'),'dir')...
        || ~exist(fullfile(save_folder,'png'),'dir')
    mkdir(save_folder,'eps');
    mkdir(save_folder,'png');
    mkdir(save_folder,'data');
end

str_col = {'ID', 'X', 'Y', 'Z', 'Frame',...
    'Photons', 'Channel', 'Frame ON', 'Total','Background Mean',...
    'Background Stdev', 'Signal Mean', 'Signal Stdev','Signal Peak', 'Sigma X',...
    'Sigma Y', 'Sigma Z', 'Uncertainty', 'Closest ID','Closest Distance',...
    'Closest Count', 'CNR', 'SNR', 'PSNR','Unknown1',...
    'Unknown2','Frame_loc','X_loc','Y_loc','Z_loc','Photons_loc'};
init_length = length(str_col);

out = print_fig('Z','Xlimits',[-fov(3)/2,fov(3)/2]);
print_fig('Photons','Xlimits',[0,inf]);
print_fig('SNR');
%print_fig('CNR');
%print_fig('PSNR');
%print_fig('Closest Distance','X-axis','log','Xlimits',[0,500]);%not useful
print_fig('CV','division','Signal Stdev','Signal Mean');

    function out = print_fig(str_interest,varargin)
        out = cell(Ndatasets,1);
        meanRMSExy = out;meanRMSEz = out;horiz_vec4xy = out; horiz_vec4z = out;
        for ii = 1:Ndatasets
            out{ii}.modality = modality;
            out{ii}.dataset = dataset;
            out{ii}.participant = participant;
            out{ii}.wobble = wobble_orig{ii};
            out{ii}.photonT = str2double(photonT{ii});
            out{ii}.metric_thres = metric_thres;
            out{ii}.min_z_range_metric = nan(size(metric_thres));
            out{ii}.max_z_range_metric = nan(size(metric_thres));
            out{ii}.meanRMSExy_onRangeZ = nan(size(metric_thres));
            out{ii}.stdRMSExy_onRangeZ = nan(size(metric_thres));
            out{ii}.CVRMSExy_onRangeZ = nan(size(metric_thres));
            out{ii}.meanRMSEz_onRangeZ = nan(size(metric_thres));
            out{ii}.stdRMSEz_onRangeZ = nan(size(metric_thres));
            out{ii}.CVRMSEz_onRangeZ = nan(size(metric_thres));
            out{ii}.range_metric = nan(size(metric_thres));
            out{ii}.max_metric = nan(3,1);
            out{ii}.min_z_FWHM_metric = nan(3,1);
            out{ii}.max_z_FWHM_metric = nan(3,1);
            out{ii}.FWHM = nan(3,1);
        end
        
        for fig_iter = 1:(3 + strcmp(str_interest,'Z')...
                *(1 + 2*(~strcmp(modality,'2D')))) %do TP, distance (~RMSE), (recall, (precision and JAC)*(3D))*Z
            switch fig_iter
                case 1
                    strLegend{1} = 'Ground Truth';
                    y_var = 'TP';
                case 2
                    strLegend = [];
                    y_var = 'RMSEloc xy';
                case 3
                    strLegend = [];
                    y_var = 'RMSEloc z';
                case 4
                    strLegend = [];
                    y_var = 'Recall';
                case 5
                    strLegend = [];
                    y_var = 'Precision';
                case 6
                    strLegend = [];
                    y_var = 'Jaccard';
            end
            str_fname = sprintf('%s %s vs %s %s %s photons T %s',...
                participant,y_var, str_interest,...
                dataset,modality,photonT{1});
            str_tit = str_fname;
            str_tit(strfind(str_tit,'_')) = '-';
            doLogx = false;
            for ii=1:Ndatasets
                k=1;
                while k <= nargin-1
                    switch varargin{k}
                        case 'Xlimits'
                            Xlimits = varargin{k+1};
                        case 'step'
                            step = varargin{k+1};
                        case 'Ylimit'
                            Ylimit = varargin{k+1};
                        case 'X-axis'
                            if strcmp(varargin{k+1},'log')
                                doLogx = true;
                            end
                        case 'division' %create new column resulting from division between 2 columns
                            ind_var_up = ~cellfun(@isempty,strfind(str_col, varargin{k+1}))...
                                & cellfun(@length,str_col)==length(varargin{k+1});
                            ind_var_bottom = ~cellfun(@isempty,strfind(str_col, varargin{k+2}))...
                                & cellfun(@length,str_col)==length(varargin{k+2});
                            pairings{ii}(:,end+1) = pairings{ii}(:,ind_var_up)...
                                ./pairings{ii}(:,ind_var_bottom);
                            ind_var = ~cellfun(@isempty,strfind(str_col, str_interest))...
                                & cellfun(@length,str_col)==length(str_interest);
                            if ~any(ind_var)
                                str_col{end+1} = str_interest;
                            end
                            k=k+1;
                    end
                    k=k+2;
                end
                
                ind_var = ~cellfun(@isempty,strfind(str_col, str_interest))...
                    & cellfun(@length,str_col)==length(str_interest);
                if exist('Xlimits','var')
                    if isinf(Xlimits(1))
                        Xlimits(1) = min(pairings{ii}(:,ind_var));
                    elseif isinf(Xlimits(2))
                        Xlimits(2) = max(pairings{ii}(:,ind_var));
                    end
                else
                    Xlimits = [min(pairings{ii}(:,ind_var)),...
                        max(pairings{ii}(:,ind_var))];
                end
                switch fig_iter
                    case 1
                        if ~exist('step','var')
                            step = diff(Xlimits)/Nelements;
                        end
                    case 2
                        Nelements4rmse = round(Nelements/3);
                        step = diff(Xlimits)/Nelements4rmse;
                    case 3
                        Nelements4rmse = round(Nelements/3);
                        step = diff(Xlimits)/Nelements4rmse;
                    otherwise
                        Nelements4others = round(Nelements/3);
                        step = diff(Xlimits)/Nelements4others;
                end
                horiz_vec = Xlimits(1):step:Xlimits(2);
                switch fig_iter
                    case 1
                        var_countGT = histcounts(pairings{ii}(:,ind_var), horiz_vec);
                        var_countTested = histcounts(pairings{ii}(~isnan(pairings{ii}(:,init_length)),...
                            ind_var), horiz_vec);
                        Ystr = 'Fluorophore counts';
                        
                        max_z_range(1) = horiz_vec(find(var_countGT,1,'first')) + step/2;
                        max_z_range(2) = horiz_vec(find(var_countGT,1,'last')) + step/2;
                    case 2
                        indXY = ~cellfun(@isempty,strfind(str_col, 'X')) & cellfun(@length,str_col)==1;
                        indXY = indXY | (~cellfun(@isempty,strfind(str_col, 'Y')) & cellfun(@length,str_col)==1);
                        indXYloc = ~cellfun(@isempty,strfind(str_col, 'X_loc')) & cellfun(@length,str_col)==5;
                        indXYloc = indXYloc | (~cellfun(@isempty,strfind(str_col, 'Y_loc')) & cellfun(@length,str_col)==5);
                        
                        [~,~,binGT] = histcounts(pairings{ii}(:,ind_var), horiz_vec);
                        meanY = nan(Nelements4rmse,1); varY = meanY;
                        for m=1:Nelements4rmse
                            Y = sum((pairings{ii}(binGT==m,indXY) - pairings{ii}(binGT==m,indXYloc)).^2,2);
                            meanY(m) = sqrt(nanmean(Y));%RMSE = sqrt(sum(dist.^2)/TP)
                            varY(m) = nanstd(sqrt(Y));% a bit meaningless
                        end

                        finerStep = diff(Xlimits)/Nelements;
                        horiz_vec4xy{ii} = Xlimits(1):finerStep:Xlimits(2);%2do better formulation
                        [~,~,binGT] = histcounts(pairings{ii}(:,ind_var), horiz_vec4xy{ii});
                        meanRMSExy{ii} = nan(Nelements,1);
                        for m=1:Nelements
                            Y = sum((pairings{ii}(binGT==m,indXY) - pairings{ii}(binGT==m,indXYloc)).^2,2);
                            meanRMSExy{ii}(m) = sqrt(nanmean(Y));
                        end
                        horiz_vec4xy{ii} = horiz_vec4xy{ii}(1:end-1) + finerStep/2;
                        Ystr = 'RMSE$^{local}_{xy}$';
                    case 3
                        indZ = ~cellfun(@isempty,strfind(str_col, 'Z')) & cellfun(@length,str_col)==1;
                        indZloc = ~cellfun(@isempty,strfind(str_col, 'Z_loc')) & cellfun(@length,str_col)==5;
                        
                        [~,~,binGT] = histcounts(pairings{ii}(:,ind_var), horiz_vec);
                        meanY = nan(Nelements4rmse,1); varY = meanY;
                        for m=1:Nelements4rmse
                            Y = (pairings{ii}(binGT==m,indZ) - pairings{ii}(binGT==m,indZloc)).^2;
                            meanY(m) = sqrt(nanmean(Y));
                            varY(m) = nanstd(sqrt(Y));%a bit meaningless
                        end
                        finerStep =diff(Xlimits)/Nelements;
                        horiz_vec4z{ii} = Xlimits(1):finerStep:Xlimits(2);%2do better formulation
                        [~,~,binGT] = histcounts(pairings{ii}(:,ind_var), horiz_vec4z{ii});
                        meanRMSEz{ii} = nan(Nelements,1);
                        for m=1:Nelements
                            Y = (pairings{ii}(binGT==m,indZ) - pairings{ii}(binGT==m,indZloc)).^2;
                            meanRMSEz{ii}(m) = sqrt(nanmean(Y));
                        end
                        horiz_vec4z{ii} = horiz_vec4z{ii}(1:end-1) + finerStep/2;
                        Ystr = 'RMSE$^{local}_{z}$';
                    otherwise
                        indPaired = ~isnan(pairings{ii}(:,init_length));
                        GT = histcounts(pairings{ii}(:,ind_var), horiz_vec);
                        TP = histcounts(pairings{ii}(indPaired,...
                            ind_var), horiz_vec);
                        
                        FN = GT - TP;
                        %FP is only an approximate estimation per bin
                        Nfluor = dir(fullfile(participant,'standard',...
                            [filenames{ii}(sep(1)+4:sep(4)-1),'*']));
                        Nfluor = csvread(Nfluor.name);
                        Nfluor = Nfluor(Nfluor(:,2) > exclusion & Nfluor(:,3) > exclusion...
                            & Nfluor(:,2) < fov(1) - exclusion & Nfluor(:,3) < fov(2) - exclusion,:);
                        Nfluor = Nfluor(:,2:4);
                        indXYZloc = ~cellfun(@isempty,strfind(str_col, 'X_loc')) & cellfun(@length,str_col)==5;
                        indXYZloc = indXYZloc | (~cellfun(@isempty,strfind(str_col, 'Y_loc')) & cellfun(@length,str_col)==5);
                        indXYZloc = indXYZloc | (~cellfun(@isempty,strfind(str_col, 'Z_loc')) & cellfun(@length,str_col)==5);
                        if str2double(photonT{ii}) > 0
                            pairings_nonT = dir(fullfile(fold_path,participant,...
                                strcat(filenames{ii}(1:strfind(filenames{ii},'____photonT_')+11),'0*')));
                            pairings_nonT = csvread(fullfile(fold_path,participant,...
                                pairings_nonT.name));
                            FPind = ~ismember(Nfluor,pairings_nonT(:,indXYZloc),'rows');%rm the paired ones without photon thresholding (not part of FP)
                        else
                            FPind = ~ismember(Nfluor,pairings{ii}(indPaired,indXYZloc),'rows');%rm the paired ones (not part of FP)
                        end
                        FP = histcounts(Nfluor(FPind, end), horiz_vec);
                        switch fig_iter
                            case 4
                                metric = TP./GT;
                                Ystr = 'Recall';
                            case 5
                                metric = TP./(FP + TP);
                                Ystr = 'Precision';
                            case 6
                                metric = TP./(FN + FP + TP)*100;
                                Ystr = 'Jaccard';
                        end
                        metric(isinf(metric)) = nan;
                        if strcmp(str_interest,'Z') %4,5,6
                            metric_tmp = metric; metric_tmp(isnan(metric_tmp)) = 0;
                            smoothed_metric = conv(metric_tmp, gausswin(Nsamples_smooth,Alpha_smooth)'...
                                /sum(gausswin(Nsamples_smooth,Alpha_smooth)),'same');
                            %Range based on Threshold
                            for iter_T = 1:length(metric_thres(fig_iter-3,:))
                                inters = isect(horiz_vec(1:end-1), smoothed_metric,...
                                    metric_thres(fig_iter-3,iter_T)*ones(size(smoothed_metric)))+ step/2;
                                if isempty(inters)
                                    out{ii}.min_z_range_metric(fig_iter-3, iter_T) = 0;
                                    out{ii}.max_z_range_metric(fig_iter-3, iter_T) = 0;
                                else
                                    minZ = max(max_z_range(1),inters(1));
                                    maxZ = min(max_z_range(2),inters(end));
                                    
                                    out{ii}.min_z_range_metric(fig_iter-3, iter_T) = minZ;
                                    out{ii}.max_z_range_metric(fig_iter-3, iter_T) = maxZ;
                                    
                                    %calculate mean,std of RMSExy on this range
                                    out{ii}.meanRMSExy_onRangeZ(fig_iter-3, iter_T) =...
                                        nanmean(meanRMSExy{ii}(horiz_vec4xy{ii}>=minZ & horiz_vec4xy{ii}<=maxZ));
                                    out{ii}.stdRMSExy_onRangeZ(fig_iter-3, iter_T) =...
                                        nanstd(meanRMSExy{ii}(horiz_vec4xy{ii}>=minZ & horiz_vec4xy{ii}<=maxZ));
                                    out{ii}.CVRMSExy_onRangeZ(fig_iter-3, iter_T) = ...
                                        out{ii}.stdRMSExy_onRangeZ(fig_iter-3, iter_T)/out{ii}.meanRMSExy_onRangeZ(fig_iter-3, iter_T);
                                    %calculate mean,std of RMSEz on this range
                                    out{ii}.meanRMSEz_onRangeZ(fig_iter-3, iter_T) =...
                                        nanmean(meanRMSEz{ii}(horiz_vec4z{ii}>=minZ & horiz_vec4z{ii}<=maxZ));
                                    out{ii}.stdRMSEz_onRangeZ(fig_iter-3, iter_T) =...
                                        nanstd(meanRMSEz{ii}(horiz_vec4z{ii}>=minZ & horiz_vec4z{ii}<=maxZ));
                                    out{ii}.CVRMSEz_onRangeZ(fig_iter-3, iter_T) = ...
                                        out{ii}.stdRMSEz_onRangeZ(fig_iter-3, iter_T)/out{ii}.meanRMSEz_onRangeZ(fig_iter-3, iter_T);
                                end
                                out{ii}.range_metric(fig_iter-3, iter_T)=...
                                    out{ii}.max_z_range_metric(fig_iter-3, iter_T)...
                                    -out{ii}.min_z_range_metric(fig_iter-3, iter_T);
                            end
                            %FWHM on smoothed
                            out{ii}.max_metric(fig_iter-3) = max(smoothed_metric);
                            inters = isect(horiz_vec(1:end-1), smoothed_metric,...
                                max(metric)/2*ones(size(smoothed_metric))) + step/2;
                            if isempty(inters) %should never happen, in case, assume too perfect
                                fprintf('Should never happen except GT : %s\n',participant);
                                out{ii}.min_z_FWHM_metric(fig_iter-3) = max_z_range(1);
                                out{ii}.max_z_FWHM_metric(fig_iter-3) = max_z_range(2);
                            else
                                out{ii}.min_z_FWHM_metric(fig_iter-3) = max(max_z_range(1), inters(1));
                                out{ii}.max_z_FWHM_metric(fig_iter-3) = min(max_z_range(2), inters(end));
                            end
                            out{ii}.FWHM(fig_iter-3) =...
                                out{ii}.max_z_FWHM_metric(fig_iter-3)...
                                -out{ii}.min_z_FWHM_metric(fig_iter-3);
                        end
                end
                horiz_vec = horiz_vec(1:end-1) + step/2;
                if ~exist('fig','var')
                    fig = figure;
                    switch fig_iter
                        case 1
                            if doLogx
                                h{1} = semilogx(horiz_vec, var_countGT,...
                                    'Color',[0.4660,0.6740,0.1880]);
                            else
                                h{1} = plot(horiz_vec, var_countGT,...
                                    'Color',[0.4660,0.6740,0.1880]);
                            end
                        otherwise
                            h = [];
                    end
                    smoothed_plot = [];
                    hold on;
                    xlabel(str_interest);ylabel(Ystr);
                    
                    title(str_tit);
                    axes_main = gca;
                end
                switch fig_iter
                    case 1
                        if doLogx
                            h{end+1} = semilogx(axes_main,horiz_vec, var_countTested);
                        else
                            h{end+1} = plot(axes_main,horiz_vec, var_countTested);
                        end
                    case {2,3}
                        %h{end+1} = errorbar(axes_main,horiz_vec,meanY,varY);
                        h{end+1} = plot(axes_main,horiz_vec, meanY);%meanYplot 
                    otherwise %only if Z variable
                        h{end+1} = plot(axes_main, horiz_vec, metric);
                        if exist('smoothed_metric','var')
                            smoothed_plot{end+1} = plot(axes_main, horiz_vec,...
                                smoothed_metric,'LineWidth',2);
                        end
                end
                switch [wobble{ii}, dim3D{ii}]
                    case 'no wobble3D'
                        set(h{end},'Color',[0,0.4470,0.7410],...
                            'LineStyle','-');%pretty dark blue
                    case 'no wobble2D'
                        set(h{end},'Color',[0.3010,0.7450,0.9330]);%pretty light blue
                    case 'wobble3D'
                        set(h{end},'Color',[0.6350,0.0780,0.1840],...
                            'LineStyle','-');%pretty dark red
                    case 'wobble2D'
                        set(h{end},'Color',[0.8500,0.3250,0.0980]);%pretty light red/orange
                end
                switch fig_iter
                    case 1
                        strLegend{end+1} = sprintf('%s - %s %s', y_var, wobble{ii},dim3D{ii});
                    case {2,3}
                        %set(meanYplot, 'Color', h{end}.Color,'LineStyle',h{end}.LineStyle);
                        strLegend{end+1} = sprintf('%s - %s %s', Ystr([1:4,6:end-1]), wobble{ii},dim3D{ii});
                    otherwise
                        if exist('smoothed_plot','var')
                            set(smoothed_plot{end}, 'Color', h{end}.Color,'LineStyle','--');
                        end
                        strLegend{end+1} = sprintf('%s - %s %s', Ystr, wobble{ii},dim3D{ii});
                end
                if ii==Ndatasets
                    legend(cat(1,h{:}), strLegend);
                end
                if fig_iter > 1 || max(var_countGT) < Ylimit
                    axis([horiz_vec([1,end]),0,Ylimits(fig_iter)]);
                    %axis([horiz_vec([1,end]),0,inf]);
                    %axis 'auto y'
                else
                    axis(axes_main,[horiz_vec([1,end]),0,Ylimit]);
                    plot(axes_main,horiz_vec(var_countGT>Ylimit),Ylimit,'bp');
                    windowSize = round(length(horiz_vec)/5);
                    b = (1/windowSize)*ones(1, windowSize);
                    [~, pos] = min(filter(b,1,var_countGT));
                    if ~exist('axes_zoom','var')
                        axes_zoom = axes('position',...
                            [max(min(pos/length(horiz_vec),0.6),0.2) .5 .25 .25]);
                        xlabel(str_interest);ylabel('Fluorophores count');
                        box on;hold on;
                        xvalGT = [];yvalGT = [];xvalTe = [];yvalTe = [];
                    end
                    indOI = var_countGT > Ylimit;
                    xvalGT = [xvalGT, horiz_vec(indOI)];
                    yvalGT = [yvalGT, var_countGT(indOI)];
                    xvalTe = [xvalTe, horiz_vec(indOI)];
                    yvalTe = [yvalTe, var_countTested(indOI)];
                    if ii==Ndatasets
                        semilogy(axes_zoom, xvalGT,yvalGT,'bp',...
                            xvalTe,yvalTe,'rp');
                        legend('GT','TP');
                    end
                end
            end
            if exist('axes_zoom','var')
                axis(axes_zoom, 'tight');
            end
            
            Yhandle = get(fig.Children,'Children'); Yhandle = Yhandle{2};
            YData = zeros(length(horiz_vec),length(Yhandle));
            for Yiter =  1:length(Yhandle)
                YData(:,Yiter) = Yhandle(Yiter).YData;
            end
            dlmwrite(fullfile(save_folder,'data',[str_fname,'.csv']),[horiz_vec',YData],'precision',8);
            
            print(fig,'-depsc',fullfile(save_folder,...
                'eps',[str_fname,'.eps']));
            print(fig,'-dpng',fullfile(save_folder,...
                'png',[str_fname,'.png']));
            %public, do not save {RMSExy,z} vs {SNR, CV}
            %NEITHER RMSEz vs (any) when 2D
            %AND rm the smoothed version for public
            if ~isempty(smoothed_plot)
                for iterS=1:length(smoothed_plot)
                    delete(smoothed_plot{iterS});
                end
                %to be sure
                axis([horiz_vec([1,end]),0,Ylimits(fig_iter)]);
            end
            if (~any(strcmpi(str_interest,{'SNR','CV'})) || (fig_iter~=2 && fig_iter~=3))...
                    && ~(strcmp(modality,'2D') && fig_iter==3)
                print(fig,'-dpng',fullfile(fold_path, participant,...
                    modality, 'png', [str_fname,'.png']));
                dlmwrite(fullfile(fold_path, participant, modality,...
                    'data',[str_fname,'.csv']),[horiz_vec',YData],'precision',8);
            end
            close(fig)
            clear fig h strLegend axes_zoom meanYplot p z_range smoothed_metric smoothed_plot YData
        end
    end
end