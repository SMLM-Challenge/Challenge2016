function out = assessment_graph_old(filenames,varargin)
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

n=1;
fov = [6400,6400,1500];%nm
fold_path = 'assessment_results';
Nelements = 150;
Ylimit = 2.5e3;
RMSE_thres = 50;%nm
metric_thres = [0.5,0.5,0.4];
while n < nargin-1
    switch varargin{n}
        case 'fov'
            fov = varargin{n+1};
        case 'fold_path'
            fold_path = varargin{n+1};
        case 'Nelements'
            Nelements = varargin{n+1};
        case 'RMSE'
            RMSE_thres = varargin{n+1};
        case 'metrics'
            metric_thres = varargin{n+1};
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
        for ii = 1:Ndatasets
            out{ii}.max_metric = nan(3,1);
            out{ii}.z_max_metric = nan(3,1);
            out{ii}.max_fitted = nan(3,1);
            out{ii}.z_max_fitted = nan(3,1);
            out{ii}.FWHM = nan(3,1);
            out{ii}.z_range_FWHM = nan(3,2);
            out{ii}.z_range_T_metric = nan(3,2);
            out{ii}.FWHM_T = nan(3,1);
            out{ii}.modality = modality;
            out{ii}.dataset = dataset;
            out{ii}.participant = participant;
            out{ii}.wobble = wobble_orig{ii};
            out{ii}.photonT = str2double(photonT{ii});
            out{ii}.RMSE_thres = RMSE_thres;
            out{ii}.metric_thres = metric_thres;
        end
        for fig_iter = 1:(3 + strcmp(str_interest,'Z')...
                *(1 + 2*(~strcmp(modality,'2D')))) %do TP, distance (~RMSE), recall, (precision and JAC)*(3D)
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
                    case 2
                        [~,~,binGT] = histcounts(pairings{ii}(:,ind_var), horiz_vec);
                        indXY = ~cellfun(@isempty,strfind(str_col, 'X')) & cellfun(@length,str_col)==1;
                        indXY = indXY | (~cellfun(@isempty,strfind(str_col, 'Y')) & cellfun(@length,str_col)==1);
                        indXYloc = ~cellfun(@isempty,strfind(str_col, 'X_loc')) & cellfun(@length,str_col)==5;
                        indXYloc = indXYloc | (~cellfun(@isempty,strfind(str_col, 'Y_loc')) & cellfun(@length,str_col)==5);
                        meanY = nan(Nelements4rmse,1); varY = meanY;
                        for m=1:Nelements4rmse
                            Y = sqrt(sum((pairings{ii}(binGT==m,indXY) - pairings{ii}(binGT==m,indXYloc)).^2,2));
                            meanY(m) = nanmean(Y);
                            varY(m) = nanstd(Y);
                        end
                        Ystr = 'RMSE$^{local}_{xy}$';
                    case 3
                        [~,~,binGT] = histcounts(pairings{ii}(:,ind_var), horiz_vec);
                        indZ = ~cellfun(@isempty,strfind(str_col, 'Z')) & cellfun(@length,str_col)==1;
                        indZloc = ~cellfun(@isempty,strfind(str_col, 'Z_loc')) & cellfun(@length,str_col)==5;
                        meanY = nan(Nelements4rmse,1); varY = meanY;
                        for m=1:Nelements4rmse
                            Y = sqrt(sum((pairings{ii}(binGT==m,indZ) - pairings{ii}(binGT==m,indZloc)).^2,2));
                            meanY(m) = nanmean(Y);
                            varY(m) = nanstd(Y);
                        end
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
                        Nfluor = csvread(Nfluor.name); Nfluor = Nfluor(:,2:4);
                        indXYZloc = ~cellfun(@isempty,strfind(str_col, 'X_loc')) & cellfun(@length,str_col)==5;
                        indXYZloc = indXYZloc | (~cellfun(@isempty,strfind(str_col, 'Y_loc')) & cellfun(@length,str_col)==5);
                        indXYZloc = indXYZloc | (~cellfun(@isempty,strfind(str_col, 'Z_loc')) & cellfun(@length,str_col)==5);
                        FPind = ~ismember(Nfluor,pairings{ii}(indPaired,indXYZloc),'rows');%rm the paired ones from FP estimation
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
                    fitH = [];
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
                    case 2
                        h{end+1} = errorbar(axes_main,horiz_vec,meanY,varY);
                        meanYplot = plot(axes_main,horiz_vec,meanY,'LineWidth',2);
                        if strcmp(str_interest,'Z')
                            p = polyfit(horiz_vec(~isnan(meanY))',...
                                meanY(~isnan(meanY)), 2);
                            
                            curve_fit = polyval(p, horiz_vec);
                            fitH{end+1} = plot(horiz_vec, curve_fit, 'black--','LineWidth',1);
                            
                            out{ii}.z_min_fitted = -p(2)/(2*p(1));
                            out{ii}.min_fitted = polyval(p, -p(2)/(2*p(1)));%min parabole
                            [out{ii}.min_RMSE, out{ii}.z_min_RMSE] = min(meanY);
                            out{ii}.z_min_RMSE = horiz_vec(out{ii}.z_min_RMSE);
                            out{ii}.z_range_FWDM = sort(roots([p(1:2),...
                                p(3) - 2*out{ii}.min_RMSE]));%Full Width Double Minimum
                            if any(imag(out{ii}.z_range_FWDM))%complex => min_RMSE never reached
                                out{ii}.z_range_FWDM = nan(2,1);
                            end
                            if  p(1) > 0 %smiling" parabola
                                out{ii}.z_range_FWDM_fitted = sort(roots([p(1:2),...
                                    p(3) - 2*out{ii}.min_fitted]));%Full Width Double Minimum fitted
                                out{ii}.z_range_T_RMSE = sort(roots([p(1:2),p(3) - RMSE_thres]));%threshold
                            else %meaningless FWDM
                                out{ii}.z_range_FWDM = nan(2,1);
                                out{ii}.z_range_T_RMSE = nan(2,1);
                                out{ii}.z_range_FWDM_fitted = nan(2,1);
                            end
                            out{ii}.FWDM_fitted = abs(diff(out{ii}.z_range_FWDM_fitted));
                            out{ii}.FWDM_T = abs(diff(out{ii}.z_range_T_RMSE));
                            out{ii}.FWDM = abs(diff(out{ii}.z_range_FWDM));
                        end
                    case 3
                        h{end+1} = errorbar(axes_main,horiz_vec,meanY,varY);
                        meanYplot = plot(axes_main,horiz_vec,meanY,'LineWidth',2);
                    otherwise %only if Z variable
                        h{end+1} = plot(axes_main, horiz_vec, metric);
                        fitted_gauss = fit(horiz_vec(~isnan(metric))', metric(~isnan(metric))','gauss1');
                        coeff = coeffvalues(fitted_gauss);
                        FWHM = 2*coeff(3)*sqrt(log(2));%see fitted gaussian formula in matlab
                        z_range_fwhm = sort([coeff(2) - FWHM/2, coeff(2) + FWHM/2]);
                        z_range_thres = sort([coeff(2) + coeff(3)*sqrt(log(coeff(1)/metric_thres(fig_iter-2))),...
                            coeff(2) - coeff(3)*sqrt(log(coeff(1)/metric_thres(fig_iter-2)))]);
                        fitH{end+1} = plot(horiz_vec,feval(fitted_gauss,horiz_vec), 'black--','LineWidth',1);
                        
                        out{ii}.max_fitted(fig_iter-2) = coeff(1);
                        out{ii}.z_max_fitted(fig_iter-2) = coeff(2);
                        [out{ii}.max_metric(fig_iter-2),out{ii}.z_max_metric(fig_iter-2)] = max(metric);
                        out{ii}.z_max_metric(fig_iter-2) = horiz_vec(out{ii}.z_max_metric(fig_iter-2));
                        out{ii}.FWHM(fig_iter-2) = FWHM;
                        out{ii}.z_range_FWHM(fig_iter-2,:) = z_range_fwhm;
                        out{ii}.z_range_T_metric(fig_iter-2,:) = z_range_thres;
                        out{ii}.FWHM_T(fig_iter-2) = abs(diff(z_range_thres));
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
                    case 2
                        set(meanYplot, 'Color', h{end}.Color,'LineStyle',h{end}.LineStyle);
                        strLegend{end+1} = sprintf('%s - %s %s', Ystr([1:4,6:end-1]), wobble{ii},dim3D{ii});
                    case 3
                        set(meanYplot, 'Color', h{end}.Color,'LineStyle',h{end}.LineStyle);
                        strLegend{end+1} = sprintf('%s - %s %s', Ystr([1:4,6:end-1]), wobble{ii},dim3D{ii});
                    otherwise
                        strLegend{end+1} = sprintf('%s - %s %s', Ystr, wobble{ii},dim3D{ii});
                end
                if ii==Ndatasets
                    legend(cat(1,h{:}), strLegend);
                end
                if fig_iter > 1 || max(var_countGT) < Ylimit
                    axis([horiz_vec([1,end]),0,inf]);
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
            
            print(fig,'-depsc',fullfile(save_folder,...
                'eps',[str_fname,'.eps']));
            print(fig,'-dpng',fullfile(save_folder,...
                'png',[str_fname,'.png']));
            if ~isempty(fitH)
                for iterH=1:length(fitH)
                    delete(fitH{iterH});
                end
                axis([horiz_vec([1,end]),0,inf]);
            end
            if ~any(strcmpi(str_interest,{'SNR','CV'})) || fig_iter~=2
                print(fig,'-dpng',fullfile(fold_path, participant,...
                    modality, 'png', [str_fname,'.png']));
            end
            close(fig)
            clear fig h strLegend axes_zoom meanYplot p z_range fitH
        end
    end
end