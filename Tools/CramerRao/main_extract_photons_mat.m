z_oi = [-15,15];

fold_name = 'res';
modality = 'DH';

tmp = struct2table(dir(fullfile(fold_name,modality,'stack_*')));
Np_set = height(tmp);
%%
z_vec = -750:10:750;
ind_oi = z_vec > z_oi(1) & z_vec < z_oi(2);

fprintf('Number of z-slices per bin : %i\n', nnz(ind_oi));

CRLB = table(ones(Np_set,1),ones(Np_set,1),ones(Np_set,1),ones(Np_set,1),...
    'VariableNames',{'Nphotons','x','y','z'});
for kk = 1:Np_set
    load(fullfile(fold_name,modality,tmp.name{kk}));
    CRLB.Nphotons(kk) = str2double(tmp.name{kk}(strfind(tmp.name{kk},'photon_')+7:strfind(tmp.name{kk},'_mod')-1));
    CRLB.x(kk) = mean(stack.CRLB.x(ind_oi));
    CRLB.y(kk) = mean(stack.CRLB.y(ind_oi));
    CRLB.z(kk) = mean(stack.CRLB.z(ind_oi));
end