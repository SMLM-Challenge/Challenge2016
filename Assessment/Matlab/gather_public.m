%% Gather public.csv files to publics.csv
res_fold = 'assessment_results';
modality = {'AS','BP','DH','2D'};
folders = dir(res_fold);
folders = folders(4:end);

for k = 1:folders
    for l = 1:length(modality)
        fid = fopen([res_fold filesep folders(k) filesep modality{l}]);
        nCol = fgetl(fid);
        nCol = length(find(nCol==sep))+1;
        out = textscan(fid,repmat('%s ', 1, nCol),'delimiter',sep);
        fclose(fid);
        loc = zeros(length(out{1}),nCol);
        for m = 1:nCol
            loc(:,m) = str2double(out{m});
        end
        if indz==0
            if indint==0
                line2rm = sum(isnan(loc(:,[indframe indx indy])),2) > 0;
            else
                line2rm = sum(isnan(loc(:,[indframe indx indy indint])),2) > 0;
            end
        else
            line2rm = sum(isnan(loc(:,[indframe indx indy indz indint])),2) > 0;
        end
        loc(line2rm,:) = [];
        Nerrorline = sum(line2rm);
    end
end