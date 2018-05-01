function [xnm, ynm, frame_out] = simBeadLocCorr(xnm, ynm, frame, gt)
%In case of exceeding number of localizations wrt expected one, pick the closest ones to the
%bead positions, not improving the results (even worse).
%DEPRECATED, not used
warning('warning : the function simBeadLocCorr should not be called');

nBeads = size(gt,1);
%frame_out = repmat(1:max(frame),[nBeads,1]); frame_out = frame_out(:);
frame_out = [];
for n=1:max(frame)
    ind = frame==n;
    if nnz(ind) > nBeads
        loc_frame = [xnm(ind), ynm(ind)];
        nLoc = size(loc_frame,1);
        pos2keep = knnsearch(loc_frame, gt, 'K',1);
        loc_frame(~ismember(1:nLoc, pos2keep),:) = nan;
        xnm(ind) = loc_frame(:,1);
        ynm(ind) = loc_frame(:,2);
        frame_out = [frame_out;ones(nBeads,1)*n];
    elseif nnz(ind) < nBeads
        frame_out = [frame_out;ones(nnz(ind),1)*n];
    end
end
xnm(isnan(xnm)) = [];
ynm(isnan(ynm)) = [];

end