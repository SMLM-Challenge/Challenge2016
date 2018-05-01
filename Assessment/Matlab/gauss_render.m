function im = gauss_render(data, sig, pix_size, im_size,doCorr)
% data : list of particles Nparticles x 2-3 => (x,y)+z (opt) (nm)
% sig : sigma (nm) for each dimension
% pix_size
% im_size, size of obtained image
% varargin{1} : do the Z correction or not

D = size(data,2);

for k=1:D
    if k==3 && doCorr
        data(:,k) = data(:,k) + im_size(k)/2*pix_size;%start z_min @ 0
    end
    %locations below 0 set @ 0, above max_size set @ the limit, should they
    %be rather removed ?
    %set
    %data(:, k) = max(0,min(im_size(k)*pix_size,data(:,k)));
    %removed : useful for dispOrthoView of zoomed area
    data(data(:, k) < 0 | data(:, k) > im_size(k)*pix_size,:) = [];
end

if length(im_size)~=D
    fprintf('Image dimension not equal to the data dimension...\n');
    return;
end
if length(pix_size)==1
    pix_size = repmat(pix_size,D,1);
end
sig = sig./pix_size;
data = data./repmat(pix_size, 1, size(data,1))';
ind_data = ceil(data);
ind_data(ind_data==0) = 1;%border case
offset = data - ind_data + 0.5;


for k = 1:length(sig)
    marg(k) = ceil(3*sig(k));
    pos(k,:) = -marg(k):marg(k);
end

im = zeros(im_size + 2*marg);
gr = cell(D,1);
%tmp = tic;

for ii=1:size(data,1)
    for jj=1:D
        gr{jj} = gauss_kernel(offset(ii,jj), sig(jj), pos(jj,:));
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

function im = gauss_kernel(offset,sig,pos)
im = exp(-(offset - pos).^2/(2*sig^2));%/(sqrt(2*pi)*sig);
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