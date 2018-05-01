function im = gauss_render_intensity(data, sig, pix_size, im_size,doCorr)
% data : list of particles Nparticles x 2-3 => (x,y)+z (opt) (nm)
% sig : sigma (nm) for each dimension
% pix_size
% im_size, size of obtained image
% varargin{1} : do the Z correction or not

doNorm = 1;
D = size(data,2);
if isscalar(sig)
    sig = sig*ones(size(data,1),1);
end

for d=1:D
    if d==3 && doCorr
        data(:,d) = data(:,d) + im_size(d)/2*pix_size(min(d,end));%start z_min @ 0
    end
    %locations below 0 set @ 0, above max_size set @ the limit, should they
    %be rather removed ?
    %set
    %data(:, k) = max(0,min(im_size(k)*pix_size,data(:,k)));
    %removed : useful for dispOrthoView of zoomed area
    ind_rm = data(:, d) < 0 | data(:, d) > im_size(d)*pix_size(min(d,end));
    data(ind_rm,:) = [];
    sig(ind_rm) = [];
end

if length(im_size)~=D
    fprintf('Image dimension not equal to the data dimension...\n');
    return;
end
%if length(pix_size)==1
%    pix_size = repmat(pix_size,D,1);
%end


sig = repmat(sig,[1,1 + D - size(sig,2)])./repmat(pix_size,[size(sig,1),1 + D - length(pix_size)]);
data = data./repmat(pix_size,[size(sig,1),1 + D - length(pix_size)]);
ind_data = ceil(data);
ind_data(ind_data==0) = 1;%border case
offset = data - ind_data + 0.5;

for d = 1:size(sig,2)
    marg(d) = ceil(3*max(sig(:,d)));
    pos(d,:) = -marg(d):marg(d);
end

im = zeros(im_size + 2*marg);
gr = cell(D,1);
%tmp = tic;

fprintf('%i molecules...\n',size(data,1));

for ii=1:size(data,1)
    for jj=1:D
        gr{jj} = gauss_kernel(offset(ii,jj), sig(ii,jj), pos(jj,:),doNorm);
    end
    GR = ktensor(gr);
    if D==2
        im(marg(1) + ind_data(ii,1) + pos(1,:),...
            marg(2) + ind_data(ii,2) + pos(2,:)) = ...
            im(marg(1) + ind_data(ii,1) + pos(1,:),...
            marg(2) + ind_data(ii,2) + pos(2,:)) + GR;
    else %D==3 normally
        try
        im(marg(1) + ind_data(ii,1) + pos(1,:),...
            marg(2) + ind_data(ii,2) + pos(2,:),...
            marg(3) + ind_data(ii,3) + pos(3,:)) =...
            im(marg(1) + ind_data(ii,1) + pos(1,:),...
            marg(2) + ind_data(ii,2) + pos(2,:),...
            marg(3) + ind_data(ii,3) + pos(3,:)) + GR;
        catch ME
            fprintf('%i\n',ii);
        end
    end
end
if D==2
    im = im(1 + marg(1):end-marg(1), 1 + marg(2):end-marg(2));
else
    im = im(1 + marg(1):end-marg(1), 1 + marg(2):end-marg(2),1 + marg(3):end-marg(3));
end
%fprintf('Gaussian rendering...%1.2f s\n',toc(tmp));
end

function im = gauss_kernel(offset,sig,pos,doNorm)
im = exp(-(offset - pos).^2/(2*sig^2));
if doNorm
    im = im/(sqrt(2*pi)*sig);
end
end
function GR = ktensor(gr)
if length(gr)==2
    GR = kron(gr{1}, gr{2}');
else
    GRXY = kron(gr{1}, gr{2}');
    GR = arrayfun(@(z) z*GRXY,gr{3},'UniformOutput',false);
    GR = cat(3,GR{:});
end

end