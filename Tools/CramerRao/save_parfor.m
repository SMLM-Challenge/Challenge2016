function save_parfor(name_mat, struct2save)
% save_parfor 
%
%   Inputs: name_mat: where the .mat will be saved, absolute path is
%                     recommended.
%           varargin: the variables will be saved, don't pass on the names of
%                     the variables, i.e, strings.
%
%   Outputs: 
%
%
% EXAMPLE
%
%
% NOTES
% Wenbin, 11-Nov-2014
% History:
% Ver. 11-Nov-2014  1st ed.
 
 
save(name_mat,'-struct', 'struct2save','-v7.3');