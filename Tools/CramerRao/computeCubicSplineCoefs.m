function coefs=computeCubicSplineCoefs(im)
%-------------------------------------------------
% function coefs=computeCubicSplineCoefs(im)
%
% Decompose the image im on a B-spline basis
%
% Exemple (in 2D):
%     im=double(imread('cell.tif'));
%     coefs=computeCubicSplineCoefs(im);
%     sz=size(im);
%     [C,R]=meshgrid(1:0.4:sz(2),1:sz(1)); % refine in y direction
%     y=evaluateCubicSpline(coefs,cat(3,R,C));
%     figure; subplot(1,2,1); imagesc(im); axis image; axis off;
%     subplot(1,2,2); imagesc(y); axis image; axis off;
%     figure; plot(1:sz(2),im(10,:),'o'); hold all;grid;
%     plot(1:0.4:sz(2),y(10,:),'x'); legend('Initial','Interpolated');
%     title('Extraction of 10th line');set(gca,'FontSize',14);
%     axis([1 sz(2) 40 220]);
%
% See also evaluateCubicSpline
%
% Emmanuel Soubies, emmanuel.soubies@eplf.ch, 2017
%-------------------------------------------------

%% Some parameters
c0=6;
a=sqrt(3)-2;
ndms=ndims(im);                       % Number of dimnsions
allElements = repmat({':'},1,ndms);   % to access elements in a vectorial mode
k0=ceil(log(eps)/log(abs(a)));       % number of recursion for initializing causal filter

%% Loop over the dimensions
coefs=im;
for n=1:ndms
    if size(im,n)~=1
        elem=allElements;
        % -- Recursive causal filter
        % Initialisation
        elem{n}=1;
        polek=a;
        elemtmp=elem;
        for k=2:min(k0,size(im,n))
            elemtmp{n}=k;
            coefs(elem{:})= coefs(elem{:}) + polek*coefs(elemtmp{:});
            polek=polek*a;
        end
        % Loop
        for k=2:size(im,n)
            elemprev=elem;
            elem{n}=k;
            coefs(elem{:})=coefs(elem{:}) + a*coefs(elemprev{:});            
        end
        % -- Recursive anti-causal filter
        % Initialisation
        elem{n}=size(im,n);
        elemprev=elem;elemprev{n}=size(im,n)-1;
        coefs(elem{:})=(a/(a^2-1))* (coefs(elem{:}) + a*coefs(elemprev{:}));
        % Loop
        for k=size(im,n)-1:-1:1
            elemprev=elem;
            elem{n}=k;
            coefs(elem{:})= a*(coefs(elemprev{:})-coefs(elem{:}));            
        end
        % -- Gain 
        coefs=coefs*c0;
    end
end
end
