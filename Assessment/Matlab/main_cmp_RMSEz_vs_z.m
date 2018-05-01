%% LOAD RMSEz vs z from one software for all modalities and plot them
clear data
software = 'SMAP';%'SMAP';%'MIATool';

photonT = true;
dataset = 'MT3.N2.LD';

modalities = {'AS','BP','DHNPC'};
metrics = {'Jaccard','RMSEloc z','RMSEloc xy'};
path = fullfile('assessment_results',software);

fdname = metrics;

for kk = 1:length(modalities)
    for ll = 1:length(metrics)
        curr_path = fullfile(path,modalities{kk},'data');
        curr_data = dir(fullfile(curr_path,...
            sprintf('%s %s vs Z %s %s photons T %s.csv',...
            software,metrics{ll},dataset,modalities{kk},...
            strcat(photonT*'*',~photonT*'0'))));
        curr_data = csvread(fullfile(curr_path,curr_data(1).name));
        
        fdname{ll} = strrep(metrics{ll},' ','');
        
        data.(modalities{kk}).(fdname{ll}).z = curr_data(:,1);
        if size(curr_data,2) < 3
            data.(modalities{kk}).(fdname{ll}).metrics = curr_data(:,2);
        else
            data.(modalities{kk}).(fdname{ll}).metricsnowobble = curr_data(:,2);
            data.(modalities{kk}).(fdname{ll}).metrics = curr_data(:,3);
        end
    end
end

%% Plot

binAVG = 5;
color_set.AS = [0.8,0,0];
color_set.DHNPC = [0,0.8,0];
color_set.BP = [0,0,0.8];

fdname{4} = 'efficiency_RMSExyz';
%fdname{5} = 'efficiency_RMSExy';

LS_set.(fdname{1}) = '-'; LS_set.(fdname{2}) = '-'; LS_set.(fdname{3}) = '-';
LS_set.(fdname{4}) = '-';% LS_set.(fdname{5}) = '-';

Xlim = [-750,750];
Ylim.(fdname{1}) = [0,100];
Ylim.(fdname{2}) = [0,200];
Ylim.(fdname{3}) = [0,100];
Ylim.(fdname{4}) = [0,100];
%Ylim.(fdname{5}) = [0,100];

alp = [0.5,1];
%str_leg = {};
for ll = length(fdname)
    figure;%(9 + ll);
    clf;hold all;
    for kk = 1:length(modalities)
        if ll > length(metrics)
            efficiency.(modalities{kk}) = 100 ...
                - sqrt((100 - data.(modalities{kk}).Jaccard.metrics).^2 ...
                + (data.(modalities{kk}).RMSElocxy.metrics.^2 + (0.5*data.(modalities{kk}).RMSElocz.metrics).^2));
                %+ alp(ll - length(metrics))^2 ...
                %* data.(modalities{kk}).(fdname{1 + ll - length(metrics)}).metrics.^2);
%                 plot(data.(modalities{kk}).(fdname{1}).z,...
%                 efficiency.(modalities{kk}),...
%                 'Color',color_set.(modalities{kk}),...
%                 'LineStyle',LS_set.(fdname{1}),...
%                 'LineWidth',1);
            
            tmp = reshape(efficiency.(modalities{kk}),binAVG,...
                length(efficiency.(modalities{kk}))/binAVG);
            tmp = repelem(nanmean(tmp),6);
            tmp = tmp([2:end,end]);
            plot(data.(modalities{kk}).(fdname{1}).z(sort([1:end,5:5:end])),...
                tmp,...
                'Color',color_set.(modalities{kk}),...
                'LineStyle',LS_set.(fdname{1}),...
                'LineWidth',1.5);
        else
            plot(data.(modalities{kk}).(fdname{ll}).z,...
                data.(modalities{kk}).(fdname{ll}).metrics,...
                'Color',color_set.(modalities{kk}),...
                'LineStyle',LS_set.(fdname{ll}),...
                'LineWidth',1.5);
            %str_leg{end+1} = sprintf('%s %s',modalities{kk},fdname{ll});
            %plot(data.(modalities{kk}).(fdname{ll}).z,...
            %    data.(modalities{kk}).(fdname{ll}).metricsnowobble,'--');
            
            %str_leg{end+1} = sprintf('%s %s no wobble',modalities{kk},fdname{ll});
        end
    end
    grid on;box on;
    set(gca,'XLim',Xlim,'YLim',Ylim.(fdname{ll}));
    tmp = gca; tmp.XAxis.FontSize = 18;tmp.YAxis.FontSize = 18;
    title(sprintf('%s %s %s',software,dataset,fdname{ll}),'FontSize',18);
    %legend(str_leg);
    str_leg = {};
    
end
