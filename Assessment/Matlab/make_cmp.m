res_all = readtable('assessment_results/results_all.csv');

for n = 1:length(res_all.Properties.VariableNames)
    units.(res_all.Properties.VariableNames{n}) = [];
end

%Manual Setting
crit_oi = {'Wob_'};
Ncategories = 2;
%'DeltaX','DeltaY','FRC_xy','MAD_lat','SNR_xy','RMSE_lat','Jaccard','Correl_'
metric = {'Jaccard','RMSE_lat','FRC_xy','Correl_'};
color_code = 'Soft';
rad = 18; %for scatter
str_titles = {'Software','Dataset','Modality',...
    'Photons Threshold','Wobble'};%must correspond to "criterions"

%latex friendly
units.Jaccard = '\%';
units.RMSE_lat = 'nm';
units.RMSE_axial = 'nm';
units.DeltaX = 'nm'; units.DeltaY = 'nm'; units.DeltaZ = 'nm';
units.FRC_xy = 'nm'; units.FRC_yz = 'nm'; units.FRC_xz = 'nm'; units.FSC = 'nm';
units.SNR_xy = 'dB'; units.SNR_yz = 'dB'; units.SNR_xz = 'dB'; units.SNR_xyz = 'dB';
units.MAD_lat = 'nm'; units.MAD_ax = 'nm';

%end of manual setting

criterions = {'Soft','Dataset','Modality','Photons','Wob_'};

str_lab = str_titles(~cellfun(@isempty,strfind(criterions, crit_oi)));
str_lab = str_lab{1};

criterions = criterions(cellfun(@isempty,strfind(criterions, crit_oi)));

switch length(crit_oi)
    case 1
        crit_oi = crit_oi{1};
        clear str_cat
        switch crit_oi
            case 'Wob_'
                str_cat{1} = {'beads','file'};
                str_cat{2} = {'no'};
                Ncategories = 2;
            case 'Photons'
                str_cat{1} = unique(res_all.(crit_oi));
                str_cat{2} = 0;
                str_cat{1}(ismember(str_cat{1}, str_cat{2})) = [];
                Ncategories = 2;
            otherwise
                types = unique(res_all.(crit_oi));
                for k = 1:length(types)
                    str_cat{k} = types(k);
                end
        end
        categories = false(height(res_all), Ncategories);
        for k = 1:Ncategories
            categories(:,k) = ismember(res_all.(crit_oi), str_cat{k});
        end
        
        if Ncategories==2 %compare between 2 cases
            valid_part = unique(res_all.Soft);
            for k = 1:Ncategories
                valid_part = intersect(valid_part, unique(res_all.Soft(categories(:,k))));
            end
            for k = 1:Ncategories
                categories(:,k) = categories(:,k)...
                    & ismember(res_all.Soft, valid_part);
            end
            row_with = find(categories(:,1));
            coupling = zeros(length(row_with),2);
            coupling(:,1) = row_with;
            G = zeros(height(res_all), length(criterions));
            
            for c = 1:length(criterions)
                if strcmp(criterions{c},'Photons')
                    G(:, c) = findgroups(res_all.(criterions{c})>0);
                else
                    G(:, c) = findgroups(res_all.(criterions{c}));
                end
            end
            
            for g = 1:size(coupling, 1)
                ind = find(ismember(G, G(coupling(g,1),:),'rows'));
                coupling(g,2) = ind(ind~=coupling(g,1));
            end
            
            types_color = unique(res_all.(color_code)(coupling(:)));
            
            colors = squeeze(hsv2rgb(linspace(0,1-1/length(types_color),length(types_color)),...
                ones(1,length(types_color)),ones(1,length(types_color))));
            
            for m = 1:length(metric)
                figure;
                ax = axes;
                maxMet = max([res_all.(metric{m})(coupling(:,1));res_all.(metric{m})(coupling(:,2))]);
                minMet = min([res_all.(metric{m})(coupling(:,1));res_all.(metric{m})(coupling(:,2))]);
                x = res_all.(metric{m})(coupling(:,1));
                y = res_all.(metric{m})(coupling(:,2));
                x_col = res_all.(color_code)(coupling(:,1));
                y_col = res_all.(color_code)(coupling(:,2));
                for s = 1:length(types_color)
                    
                    x_tmp = x(cellfun(@(in) strcmp(in,types_color{s}),x_col));
                    y_tmp = y(cellfun(@(in) strcmp(in,types_color{s}),y_col));
                    
                    scatter(x_tmp,y_tmp,rad,colors(s,:),'filled');%coupling shares same color_code normally
                    hold on;
                end
                legend(types_color,'Location','NorthWest');
                
                plot(minMet:maxMet,minMet:maxMet,'black--');
                str_tit = metric{m};
                if any(strfind(str_tit,'_'))
                    str_tit = strcat('$',...
                        str_tit(1:strfind(str_tit,'_')),'{',...
                        str_tit(1+strfind(str_tit,'_'):end),'}$');
                end
                if ~isempty(units.(metric{m}))
                    str_tit = strcat(str_tit,' [',units.(metric{m}),']');
                end
                title(str_tit);
                grid on;
                xlabel(str_lab); ylabel(['No ', str_lab]);
                axis([min(ax.XLim(1),ax.YLim(1)),max(ax.XLim(2),ax.YLim(2)),...
                    min(ax.XLim(1),ax.YLim(1)),max(ax.XLim(2),ax.YLim(2))]);
                axis square tight
            end
        else %display for each category
            for k = 1:Ncategories
                for m = 1:length(metric)
                    figure;
                    ax = axes;
                    b = bar(res_all.(metric{m})(categories(:,k)));
                    emplac = 1;
                    ind_categ = find(categories(:,k));
                    for l = 2:nnz(categories(:,k))
                        emplac = [emplac; ~strcmp(cell2mat(res_all.Soft(ind_categ(l))),...
                            cell2mat(res_all.Soft(ind_categ(l-1))))];
                    end
                    emplac = [emplac; height(res_all)];
                    Ndatas = diff(find(emplac));
                    emplac = [0;cumsum(diff(find(emplac,nnz(emplac)-1)))] + Ndatas/2 + 0.5;
                    Ndatas = cumsum(Ndatas);
                    %b(1:Ndatas(1));
                    %for l = 2:length(Ndatas)
                    %    b;
                    %end
                    ax.XTick = emplac;
                    ax.LineWidth = 0.001;
                    ax.FontSize = 9;
                    ax.XTickLabel = unique(res_all.Soft);
                    ax.XTickLabelRotation = 45;
                    str_tit = metric{m};
                    if any(strfind(str_tit,'_'))
                        str_tit = strcat('$',...
                            str_tit(1:strfind(str_tit,'_')),'{',...
                            str_tit(1+strfind(str_tit,'_'):end),'}$');
                    end
                    str_tit = strcat(str_tit,' [',units.(metric{m}),']');
                    title(str_tit);
                    xlabel(str_lab); ylabel(['No ', str_lab]);
                end
            end
        end
    case 2
        
end