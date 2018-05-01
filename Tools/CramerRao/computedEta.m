function deta = computedEta(N, c, Ntotpix, delta, pos_mol,mu,cam_siz)
%Output : Ncampix x ndims(c)
%Ncampix: total number of camera pixel

deta = zeros(Ntotpix,ndims(c));

[Nx,Ny,Nz] = size(c);% V := prod(size(c))
%cam_siz = sqrt(Ntotpix)*ones(1,2);
NsplPix = ceil([Nx,Ny]./cam_siz);%# splines basis fct per camera pixel
Bz = computeSplineBasis3(pos_mol(3)/delta(3) - (1:Nz)');% Nz x 1
C = prod(delta(1:2))*squeeze(sum(sum(c,1),2))'...
    *computeSplineBasis3(pos_mol(3)/delta(3) - (1:Nz)');%normalization factor

% derivative wrt x
cpad = padarray(c,[1,0,0],0,'both');
diffC = (circshift(cpad,[-1,0,0]) - cpad)./C;%(Nx + 2) x Ny x Nz


%(Nx + 1) x Ny x Nz
coeff = permute(bsxfun(@(x,y) x.*y, permute(diffC(1:end-1,:,:)/delta(1),[3,1,2]), Bz),[2,3,1]);

% if mod(size(coeff,1),ceil(Nx/cam_siz(1)))~=0
%     size of coeff not exactly matching with camera pixel, pad with 0
%     coeff = padarray(coeff,[size(coeff,1) - mod(size(coeff,1),ceil(Nx/cam_siz(1))),0,0]/2,0,'both');
% end
% if mod(size(coeff,2),ceil(Ny/sqrt(Ntotpix)))~=0
%     coeff = padarray(coeff,[0, size(coeff,2) - mod(size(coeff,1),ceil(Ny/cam_siz(2))),0]/2,0,'both');
% end

deta(:,1) = -N*computeIntegralDer(sum(coeff,3),pos_mol(1:2),NsplPix, delta, cam_siz, 1); %Output : ceil(Nx/sqrt(Ncampix)) x ceil(Ny/sqrt(Ncampix))

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% derivative wrt y
cpad = padarray(c,[0,1,0],0,'both');
diffC = (circshift(cpad,[0,-1,0]) - cpad)./C;%Nx x (Ny + 2) x Nz


%Nx x (Ny + 1) x Nz
coeff = permute(bsxfun(@(x,y) x.*y, permute(diffC(:,1:end-1,:)/delta(2),[3,1,2]), Bz),[2,3,1]);

% if mod(size(coeff,1),ceil(Nx/cam_siz(1)))~=0
%     size of coeff not exactly matching with camera pixel, pad with 0
%     coeff = padarray(coeff,[size(coeff,1) - mod(size(coeff,1),ceil(Nx/cam_siz(1))),0,0]/2,0,'both');
% end
% if mod(size(coeff,2),ceil(Ny/sqrt(Ntotpix)))~=0
%     coeff = padarray(coeff,[0, size(coeff,2) - mod(size(coeff,1),ceil(Ny/cam_siz(2))),0]/2,0,'both');
% end

deta(:,2) = -N*computeIntegralDer(sum(coeff,3),pos_mol(1:2),NsplPix, delta, cam_siz, 2); %Output : ceil(Nx/sqrt(Ncampix)) x ceil(Ny/sqrt(Ncampix))

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% derivative wrt z
cpad = padarray(c,[0,0,1],0,'both');
diffC = (circshift(cpad,[0,0,-1]) - cpad)./C;%Nx x (Ny + 2) x Nz

Bz2 = computeSplineBasis2(pos_mol(3)/delta(3) - (1:(Nz+1))' + 0.5);% Nz x 1

%Nx x Ny x (Nz+1)
coeff = permute(bsxfun(@(x,y) x.*y, permute(diffC(:,:,1:end-1)/delta(3),[3,1,2]), Bz2),[2,3,1]);

% if mod(size(coeff,1),ceil(Nx/cam_siz(1)))~=0
%     size of coeff not exactly matching with camera pixel, pad with 0
%     coeff = padarray(coeff,[size(coeff,1) - mod(size(coeff,1),ceil(Nx/cam_siz(1))),0,0]/2,0,'both');
% end
% if mod(size(coeff,2),ceil(Ny/sqrt(Ntotpix)))~=0
%     coeff = padarray(coeff,[0, size(coeff,2) - mod(size(coeff,1),ceil(Ny/cam_siz(2))),0]/2,0,'both');
% end

deta(:,3) = N*computeIntegralDer(sum(coeff,3),pos_mol(1:2),NsplPix, delta, cam_siz, 3); %Output : ceil(Nx/sqrt(Ncampix)) x ceil(Ny/sqrt(Ncampix))

deta(:,3) = deta(:,3) - sum(coeff(:))*prod(delta(1:2))*mu(:);

end