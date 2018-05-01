function [im2D,T1,T2,T3,L] = depthcolor3D(im,colormapType,t,dim,varargin)
%DEPTHCOLOR3D Color code for depth in 3D image im
%im : 3D intensity image (e.g. 3D Gaussian rendered)
%colormapType : string indicating colormap type (e.g. jet, parula)
%t : quantile for max intensity (0 to 1). Change the scaling of color (and
%    brightness)
%Optional input
% maxZ : shift the assigned color to depth
bg = 0;
siz = size(im);
Nz = siz(3);
siz = siz(~ismember(1:3,dim));

%[~,z_pos] = max(im,[],dim);
z_pos = ones(siz);
curr_im2D = zeros(siz);
L = 25;
T1 = 1;
T2 = 0.75;
T3 = 0.2;

if dim==3
    for kk = 1:siz(1)
        for ll = 1:siz(2)
            curr_ind = find(im(kk,ll,:) > T1,1,'last');
            
            if isempty(curr_ind)
                curr_ind = find(im(kk,ll,:) > T2,1,'last');
                if isempty(curr_ind)
                    curr_ind = find(im(kk,ll,:) > T3,1,'last');
                end
            end
            if ~isempty(curr_ind)
                z_pos(kk,ll) = curr_ind;
                
                curr_im2D(kk,ll) = sum(im(kk,ll,max(curr_ind-L,1):min(curr_ind+L,end)));
            end
        end
    end
elseif dim==1 
    z_pos = repmat(1:Nz,siz(1),1);
elseif dim==2
    z_pos = repmat(1:Nz,siz(1),1);
else
    warning('error in dim indication');
end

curr_im2D = squeeze(sum(im,dim));

max_int = quantile(curr_im2D(curr_im2D > 0),t);

if nargin > 4 %shift the color
    maxZ = varargin{1};
    z_pos = z_pos - Nz/2 + maxZ/2;
else
    maxZ = Nz;
end

try
    eval(sprintf('curr_color = %s(maxZ);',colormapType));
catch
    fprintf('Unknown colormap... Taking Jet colormap.\n');
end


im2D = reshape(curr_color(z_pos(:),:),[siz,3]);

im2D = im2D.*repmat(curr_im2D/max_int,[1,1,3]);

im2D(repmat(curr_im2D==0,[1,1,3])) = bg;

end

