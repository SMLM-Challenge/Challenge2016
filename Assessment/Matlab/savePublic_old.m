function savePublic_old(results, results_mol, results_graph,res_folder)
%SAVEPUBLIC_OLD save results in public.csv

for k=1:length(results)
    strMod{k} = results{k}.modality;
end

strMod = unique(strMod);

fname = 'public.csv';
if isempty(results_graph)
    results_graph = fill_results_graph(results);
end

initLen = length(results_graph);

for m=1:length(strMod)
    fileID = fopen(strcat(res_folder,filesep,...
        results{1}.participant,filesep, strMod{m},filesep,fname),'w');
    
    formatSpec = strcat('%s,%s,%s,%s,%f,%f,%i,%i,%i,%i,%i,',...%FN
        '%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,',...%Dz
        '%f,%f,%f,%f,%f,%f,%f,%f,%f,',...%SNRxy
        '%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,',...%Detection ratio_mol
        '%f,%f,%f,',...%FWDM ( RMSE)
        '%f,%f,%f,%f,%f,%f,',...%Thres (RMSE)
        '%f,%f,%f,',...%Z max Jaccard
        '%f,%f,%f,',...%max jaccard
        '%f,%f,%f,',...%FWHM Jaccard
        '%f,%f,%f,%f,%f,%f,',...%max Z range FWHM Jaccard
        '%f,%f,%f,',...%FWHM Jaccard Thres
        '%f,%f,%f,%f,%f,%f,',...%max Z range thres Jaccard
        '%f,%f,%f,',...
        '%f,%f,%f,%f,%f','\n');%Thres Jaccard\n');
    
    fprintf(fileID,strcat('Dataset,Density,',...
        'Modality,','Wobble,','TolXY,','TolZ,',...
        '# Fluorophores Test,','Thres Photon,',...
        'TP,','FP,','FN,','Jaccard,','F-Score,','Recall,','Precision,',...
        'RMSExyz,','RMSExy,','RMSEz,',...
        'MADxyz,','MADxy,','MADz,',...
        'Dx,','Dy,','Dz,','Corr. photons,',...
        'FSC,','FRCyz,','FRCxz,','FRCxy,',...
        'SNRxyz,','SNRyz,','SNRxz,','SNRxy,',...
        'TP_mol,','FN_mol,','Recall_mol,','RMSExyz_mol,','RMSExy_mol,','RMSEz_mol,',...
        'MADxyz_mol,','MADxy_mol,','MADz_mol,',...
        'Detection ratio_mol,',...
        'Z min RMSE,','min RMSE,','FWDM (RMSE),',...
        'min Z range FWDM (RMSE),','max Z range FWDM (RMSE),','FWDM Thres (RMSE),',...
        'min Z range Thres (RMSE),','max Z range Thres (RMSE),','Thres (RMSE),',...
        'Z max recall,','Z max precision,','Z max Jaccard,',...
        'max recall,','max precision,','max Jaccard,',...
        'FWHM recall,','FWHM precision,','FWHM Jaccard,',...
        'min Z range FWHM recall,','max Z range FWHM recall,',...
        'min Z range FWHM precision,','max Z range FWHM precision,',...
        'min Z range FWHM Jaccard,','max Z range FWHM Jaccard,',...
        'Range recall Thres,','Range precision Thres,','Range Jaccard Thres,',...
        'min Z range thres recall,','max Z range thres recall,',...
        'min Z range thres precision,','max Z range thres precision,',...
        'min Z range thres Jaccard,','max Z range thres Jaccard,',...
        'Thres recall,','Thres precision,','Thres Jaccard,',...
        'Z min fitted RMSE,','min fitted RMSE,','FWDM (RMSE) fitted,',...
        'min Z range FWDM (RMSE) fitted,','max Z range FWDM (RMSE) fitted','\n'));
    
    for k=1:length(results)
        l=1;
        notFound = true;
        while l <= initLen && notFound
            if strcmp(results_graph{l}.modality, results{k}.modality)...
                    && strcmp(results_graph{l}.dataset, results{k}.dataset)...
                    && strcmp(results_graph{l}.participant, results{k}.participant)...
                    && strcmp(results_graph{l}.wobble, results{k}.wobble)...
                    && results_graph{l}.photonT==results{k}.photonT...
                    && ((strcmp(results_graph{l}.modality, '2D') && results{k}.dim3D==0)...
                    || (~strcmp(results_graph{l}.modality, '2D') && results{k}.dim3D==1))
                notFound = false;
            else
                l = l + 1;
                if l > initLen && length(results_graph)==initLen
                    for fn = fieldnames(results_graph{l-1})'
                        results_graph{l}.(fn{1}) = results_graph{l-1}.(fn{1});
                        for n = 1:numel(results_graph{l}.(fn{1}))
                            try
                                results_graph{l}.(fn{1})(n) = nan;
                            end
                        end
                    end
                end
            end
        end
        if strcmp(results{k}.modality,strMod{m})
            fprintf(fileID,formatSpec,...
                results{k}.dataset,results{k}.dataset(end-1:end),...
                results{k}.modality,results{k}.wobble,...
                results{k}.radTolXY,results{k}.radTolZ,...
                results{k}.nloc_test_initial,...
                results{k}.photonT,...
                results{k}.TP,results{k}.FP,results{k}.FN,...
                results{k}.Jaccard,results{k}.Fscore,results{k}.recall,results{k}.precision,...
                results{k}.RMSExyz,results{k}.RMSExy,results{k}.RMSEz,...
                results{k}.MADxyz,results{k}.MADxy,results{k}.MADz,results{k}.distX,...
                results{k}.distY,results{k}.distZ,results{k}.corrPhoton,...
                results{k}.FSC,results{k}.FRC{1},results{k}.FRC{2},results{k}.FRC{3},...
                results{k}.SNR{1},results{k}.SNR{2},...
                results{k}.SNR{3},results{k}.SNR{4},...
                results_mol{k}.TPmol,results_mol{k}.FNmol,...
                results_mol{k}.recall_mol,...
                results_mol{k}.RMSExyz_mol,results_mol{k}.RMSExy_mol,results_mol{k}.RMSEz_mol,...
                results_mol{k}.MADxyz_mol,results_mol{k}.MADxy_mol,results_mol{k}.MADz_mol,...
                results_mol{k}.ratio_det_per_mol_ave,...
                results_graph{l}.z_min_RMSE,results_graph{l}.min_RMSE,results_graph{l}.FWDM,...
                results_graph{l}.z_range_FWDM(1),results_graph{l}.z_range_FWDM(2),...
                results_graph{l}.FWDM_T, results_graph{l}.z_range_T_RMSE(1),...
                results_graph{l}.z_range_T_RMSE(2),results_graph{l}.RMSE_thres,...
                results_graph{l}.z_max_metric(1),results_graph{l}.z_max_metric(2),results_graph{l}.z_max_metric(3),...
                results_graph{l}.max_metric(1),results_graph{l}.max_metric(2),results_graph{l}.max_metric(3),...
                results_graph{l}.FWHM(1),results_graph{l}.FWHM(2),results_graph{l}.FWHM(3),...
                results_graph{l}.z_range_FWHM(1,1),results_graph{l}.z_range_FWHM(1,2),...
                results_graph{l}.z_range_FWHM(2,1),results_graph{l}.z_range_FWHM(2,2),...
                results_graph{l}.z_range_FWHM(3,1),results_graph{l}.z_range_FWHM(3,2),...
                results_graph{l}.FWHM_T(1),results_graph{l}.FWHM_T(2),results_graph{l}.FWHM_T(3),...
                results_graph{l}.z_range_T_metric(1,1),results_graph{l}.z_range_T_metric(1,2),...
                results_graph{l}.z_range_T_metric(2,1),results_graph{l}.z_range_T_metric(2,2),...
                results_graph{l}.z_range_T_metric(3,1),results_graph{l}.z_range_T_metric(3,2),...
                results_graph{l}.metric_thres(1),results_graph{l}.metric_thres(2),results_graph{l}.metric_thres(3),...
                results_graph{l}.z_min_fitted,results_graph{l}.min_fitted,...
                results_graph{l}.FWDM_fitted,results_graph{l}.z_range_FWDM_fitted(1),results_graph{l}.z_range_FWDM_fitted(2));
        end
    end
    fclose(fileID);
    
end
fprintf('The public assessment results are saved in the file %s in different modality folders\n',fname);
end

function results_graph = fill_results_graph(results)
res_len = length(results);
results_graph = cell(res_len,1);
for l = 1:res_len
    results_graph{l}.dim3D = results{l}.dim3D;
    results_graph{l}.photonT = results{l}.photonT;
    results_graph{l}.wobble = results{l}.wobble;
    results_graph{l}.participant = results{l}.participant;
    results_graph{l}.dataset = results{l}.dataset;
    results_graph{l}.modality = results{l}.modality;
    results_graph{l}.z_min_RMSE = nan;
    results_graph{l}.min_RMSE = nan;
    results_graph{l}.FWDM = nan;
    results_graph{l}.z_range_FWDM(1) = nan;
    results_graph{l}.z_range_FWDM(2) = nan;
    results_graph{l}.FWDM_T = nan;
    results_graph{l}.z_range_T_RMSE(1) = nan;
    results_graph{l}.z_range_T_RMSE(2) = nan;
    results_graph{l}.RMSE_thres = nan;
    results_graph{l}.z_max_metric(1) = nan;
    results_graph{l}.z_max_metric(2) = nan;
    results_graph{l}.z_max_metric(3) = nan;
    results_graph{l}.max_metric(1) = nan;
    results_graph{l}.max_metric(2) = nan;
    results_graph{l}.max_metric(3) = nan;
    results_graph{l}.FWHM(1) = nan;
    results_graph{l}.FWHM(2) = nan;
    results_graph{l}.FWHM(3) = nan;
    results_graph{l}.z_range_FWHM(1,1) = nan;
    results_graph{l}.z_range_FWHM(1,2) = nan;
    results_graph{l}.z_range_FWHM(2,1) = nan;
    results_graph{l}.z_range_FWHM(2,2) = nan;
    results_graph{l}.z_range_FWHM(3,1) = nan;
    results_graph{l}.z_range_FWHM(3,2) = nan;
    results_graph{l}.FWHM_T(1) = nan;
    results_graph{l}.FWHM_T(2) = nan;
    results_graph{l}.FWHM_T(3) = nan;
    results_graph{l}.z_range_T_metric(1,1) = nan;
    results_graph{l}.z_range_T_metric(1,2) = nan;
    results_graph{l}.z_range_T_metric(2,1) = nan;
    results_graph{l}.z_range_T_metric(2,2) = nan;
    results_graph{l}.z_range_T_metric(3,1) = nan;
    results_graph{l}.z_range_T_metric(3,2) = nan;
    results_graph{l}.metric_thres(1) = nan;
    results_graph{l}.metric_thres(2) = nan;
    results_graph{l}.metric_thres(3) = nan;
    results_graph{l}.z_min_fitted = nan;
    results_graph{l}.min_fitted = nan;
    results_graph{l}.FWDM_fitted = nan;
    results_graph{l}.z_range_FWDM_fitted(1) = nan;
    results_graph{l}.z_range_FWDM_fitted(2) = nan;
    results_graph{l}.z_max_fitted(1) = nan;
    results_graph{l}.z_max_fitted(2) = nan;
    results_graph{l}.z_max_fitted(3) = nan;
    results_graph{l}.max_fitted(1) = nan;
    results_graph{l}.max_fitted(2) = nan;
    results_graph{l}.max_fitted(3) = nan;
end
end