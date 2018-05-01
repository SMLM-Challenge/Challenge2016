function save_in_parfor(fullname,struct2save)
%SAVEVAR To use save in parfor loop
%  INPUTS
%  fullname : full filename (no .mat at the end)
%  struct2save : save all the fields as separate variable
%  save violates transparency in parallel computing

save([fullname,'.mat'],'-struct','struct2save','-v7.3');

end

