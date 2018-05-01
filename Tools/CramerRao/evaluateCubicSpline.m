function y=evaluateCubicSpline(coefs,x)
%-------------------------------------------------
% function y=evaluateCubicSpline(coefs,x)
%
% See the functions below for a description of the parameters
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
% Note: This code is quite generic since it works for any set of points x
%       As a drawback the computation (in particular for 3D x) can tale
%       few minutes. There is probably possibilities of improvement.
%
% See also computeCubicSplineCoefs
%
% Emmanuel Soubies, emmanuel.soubies@eplf.ch, 2017
%-------------------------------------------------

%% Some parameters
ndms=ndims(x);                       % Number of dimensions
if isvector(x), ndms=1; end

%% Loop over the dimensions
switch ndms
    case 1
        y=evaluateCubicSpline1D(coefs,x);
    case 3
        y=evaluateCubicSpline2D(coefs,x);
    case 4
        y=evaluateCubicSpline3D(coefs,x);
end
end

function y=evaluateCubicSpline3D(coefs,x)
%-------------------------------
% function y=evaluateCubicSpline3D(coefs,x)
%
% Evaluate the spline at positions in x 
%  - coef contains spline coefficients (2D matrix N x M x P)
%  - x contains the position where the spline will be 
%    evaluated (N x M x P x 3 matrix where x(:,:,1) has constant rows with
%    values in [1,size(coefs,1)], x(:,:,2) has constant columns with
%    values in [1,size(coefs,2)] and x(:,:,3) has constant values in the 
%    third direction within [1,size(coefs,3)] (extention of the 2D case).
%-------------------------------
fx=floor(x);
sz=size(coefs);
p1{1}=[2,1:sz(1)-1];               p2{1}=[2,1:sz(2)-1];              p3{1}=[2,1:sz(3)-1]; 
p1{2}=1:sz(1);                     p2{2}=1:sz(2);                    p3{2}=1:sz(3);
p1{3}=[2:sz(1),sz(1)-1];           p2{3}=[2:sz(2),sz(2)-1];          p3{3}=[2:sz(3),sz(3)-1];
p1{4}=[3:sz(1),sz(1)-1,sz(1)-2];   p2{4}=[3:sz(2),sz(2)-1,sz(2)-2];  p3{4}=[3:sz(3),sz(3)-1,sz(3)-2]; 
c=@(p,q,k) arrayfun(@(v,u,w) coefs(p1{p}(v),p2{q}(u),p3{k}(w)),fx(:,:,:,1),fx(:,:,:,2),fx(:,:,:,3));
[w1{1},w1{2},w1{3},w1{4}]=getCubicSpline(x(:,:,:,1)-fx(:,:,:,1));
[w2{1},w2{2},w2{3},w2{4}]=getCubicSpline(x(:,:,:,2)-fx(:,:,:,2));
[w3{1},w3{2},w3{3},w3{4}]=getCubicSpline(x(:,:,:,3)-fx(:,:,:,3));
y=zeros(size(x,1),size(x,2),size(x,3));
id=1;
for ii=1:4
    for jj=1:4
        for kk=1:4
            disp(['Progression : ',num2str(round(id/64*100)),'%']);
            y=y+c(ii,jj,kk).*w1{ii}.*w2{jj}.*w3{kk};
            id=id+1;
        end
    end
end
end


function y=evaluateCubicSpline2D(coefs,x)
%-------------------------------
% function y=evaluateCubicSpline2D(coefs,x)
%
% Evaluate the spline at positions in x 
%  - coef contains spline coefficients (2D matrix N x M)
%  - x contains the position where the spline will be 
%    evaluated (N x M x 2 matrix where x(:,:,1) has constant rows with
%    values in [1,size(coefs,1)] and x(:,:,2) has constant columns with
%    values in [1,size(coefs,2)]. E.g.
%
%    x(:,:,1)=[  1   1   1       x(:,:,2)=[ 1  1.5  2
%              1.5 1.5 1.5                  1  1.5  2
%                2   2   2]                 1  1.5  2]
%-------------------------------
fx=floor(x);
sz=size(coefs);
p1{1}=[2,1:sz(1)-1];               p2{1}=[2,1:sz(2)-1]; 
p1{2}=1:sz(1);                     p2{2}=1:sz(2);
p1{3}=[2:sz(1),sz(1)-1];           p2{3}=[2:sz(2),sz(2)-1];
p1{4}=[3:sz(1),sz(1)-1,sz(1)-2];   p2{4}=[3:sz(2),sz(2)-1,sz(2)-2]; 
c=@(p,q) arrayfun(@(v,u) coefs(p1{p}(v),p2{q}(u)),fx(:,:,1),fx(:,:,2));
[w1{1},w1{2},w1{3},w1{4}]=getCubicSpline(x(:,:,1)-fx(:,:,1));
[w2{1},w2{2},w2{3},w2{4}]=getCubicSpline(x(:,:,2)-fx(:,:,2));
y=zeros(size(x,1),size(x,2));
id=1;
for ii=1:4
    for jj=1:4
        disp(['Progression : ',num2str(round(id/16*100)),'%']);
        y=y+c(ii,jj).*w1{ii}.*w2{jj};
        id=id+1;
    end
end
end

function y=evaluateCubicSpline1D(coefs,x)
%-------------------------------
% function y=evaluateCubicSpline1D(coefs,x)
%
% Evaluate the spline at positions in x 
%  - coef contains spline coefficients (1D vector)
%  - x contains the position where the spline will be 
%    evaluated (vector with values in [1 length(coefs)])
%-------------------------------
n=length(coefs);
fx=floor(x);
p1=[2,1:n-1];     c1=arrayfun(@(v) coefs(p1(v)),fx);
p2=1:n;           c2=arrayfun(@(v) coefs(p2(v)),fx);
p3=[2:n,n-1];     c3=arrayfun(@(v) coefs(p3(v)),fx);
p4=[3:n,n-1,n-2]; c4=arrayfun(@(v) coefs(p4(v)),fx);
[w1,w2,w3,w4]=getCubicSpline(x-fx);
y=c1.*w1+c2.*w2+c3.*w3+c4.*w4;
end

function [v0,v1,v2,v3]=getCubicSpline(t)
%-------------------------------
% function v=getCubicSpline(t)
% 
% For a value t in [0 1] return the weights
% to assign to each spline coef
%-------------------------------
assert(all(t(:)>=0) && all(t(:)<=1),'Wrong value for t !');
t1=1-t;
t2=t.^2;
v0=t1.^3/6;
v1=2/3+0.5*t2.*(t-2);
v3=(t2.*t)/6;
v2=1-v3-v1-v0;
end