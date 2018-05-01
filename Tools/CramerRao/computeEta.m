function eta = computeEta(pos_mol,c,delta,cam_siz)

%siz = sqrt(Ntotpix)*ones(1,2);
%siz = ceil([Nx*step(1)/sqrt(Ntotpix),Ny*step(2)/sqrt(Ntotpix)]);%
%campix_step = step(1:2).*NsplPix;

siz = size(c);

Bz = computeSplineBasis3(pos_mol(3)/delta(3) - (1:siz(3))');% Nz x 1


NsplPix = ceil(siz(1:2)./cam_siz);%# splines basis fct per camera pixel
C = prod(delta(1:2))*squeeze(sum(sum(c,1),2))'...
    *computeSplineBasis3(pos_mol(3)/delta(3) - (1:siz(3))');%normalization factor

coeff = permute(bsxfun(@(x,y) x.*y, permute(c/C,[3,1,2]), Bz),[2,3,1]);

eta = zeros(cam_siz);

%for cubic spline fitt.
%integr_bound = [-1.5:1:2,2*ones(1,6),1.5:-1:-1.5];

K = 100;


for ind_camx = 1:cam_siz(1)
    for ind_camy = 1:cam_siz(2)
        integrx = zeros(siz(1),1);
        integry = zeros(siz(2),1);
        for n = 1:siz(1)
            %Integrate in dim X
            %curr_s = inf;
            %shift to the camera pixel coordinate (0 at the first included knot)
            xprim = n - (ind_camx - 1)*NsplPix(1) - 1;
            
            if xprim <= NsplPix(1) + 2 - 1 && xprim >= - 2 %NsplPix +- spline basis fct support/2
                x = NsplPix(1) + (ind_camx - 1)*NsplPix(1) - pos_mol(1)/delta(1) - n;% "spline basis domain"
                lbx = (ind_camx - 1)*NsplPix(1) - pos_mol(1)/delta(1) - n;
                integrlbx = sum(computeSplineBasis4(lbx - 0.5 - (0:K)));
                integrx(n) = delta(1)*(sum(computeSplineBasis4(x - 0.5 - (0:K))) - integrlbx);
            end
        end
        for m = 1:siz(2)
            %Integrate in dim Y
            xprim = m - (ind_camy - 1)*NsplPix(2) - 1;
            if xprim <= NsplPix(2) + 2 - 1 && xprim >= - 2 %NsplPix +- spline basis fct support/2
                y = NsplPix(2) + (ind_camy - 1)*NsplPix(2) - pos_mol(2)/delta(2) - m;% "spline basis domain"
                lby = (ind_camy - 1)*NsplPix(2) - pos_mol(2)/delta(2) - m;
                integrlby = sum(computeSplineBasis4(lby - 0.5 - (0:K)));
                integry(m) = delta(2)*(sum(computeSplineBasis4(y - 0.5 - (0:K))) - integrlby);
            end
        end
        
        eta(ind_camx, ind_camy) = sum(sum(kron(integry',integrx).*sum(coeff,3)));
    end
end

eta = eta(:);