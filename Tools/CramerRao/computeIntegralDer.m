function integr = computeIntegralDer(coeffxy,pos_mol,NsplPix,delta,cam_siz,der)
%Output: siz
%Integrate spline basis function of degree 2/3 for der/other dimension rsp.
%Use integral relation with (sum of shifted) basis func of degree 3/4 rsp.
%For one camera pixel (others are shifted version of this integral)
siz = size(coeffxy);
%campix_step = step(1:2).*NsplPix;

integr = zeros(cam_siz);

K = 100;
switch der
    case 1
        for ind_camx = 1:cam_siz(1)
            for ind_camy = 1:cam_siz(2)
                int_der = zeros(siz(1),1);
                int_other = zeros(siz(2),1);
                for n = 1:siz(1)
                    %Integrate in dim X (horizontal)
                    %curr_s = inf;
                    %shift to the camera pixel coordinate (0 at the first included knot)
                    xprim = n - (ind_camx - 1)*NsplPix(1) - 1;
                    if xprim <= NsplPix(1) + 2 - 1 && xprim >= - 2 %NsplPix +- spline basis fct support/2 
                        x = NsplPix(1) + (ind_camx - 1)*NsplPix(1) - pos_mol(1)/delta(1) - n + 0.5;% "spline basis domain"
                        lbx = (ind_camx - 1)*NsplPix(1) - pos_mol(1)/delta(1) - n + 0.5;
                        integrlbx = sum(computeSplineBasis3(lbx - 0.5 - (0:K)));
                        int_der(n) = delta(1)*(sum(computeSplineBasis3(x - 0.5 - (0:K))) - integrlbx);
                    end
                end
                for m = 1:siz(2)
                    %Integrate in dim Y
                    xprim = m - (ind_camy - 1)*NsplPix(2) - 1;
                    if xprim <= NsplPix(2) + 2 - 1 && xprim >= - 2 %NsplPix +- spline basis fct support/2
                        y = NsplPix(2) + (ind_camy - 1)*NsplPix(2) - pos_mol(2)/delta(2) - m;% "spline basis domain"
                        lby = (ind_camy - 1)*NsplPix(2) - pos_mol(2)/delta(2) - m;
                        integrlby = sum(computeSplineBasis4(lby - 0.5 - (0:K)));
                        int_other(m) = delta(2)*(sum(computeSplineBasis4(y - 0.5 - (0:K))) - integrlby);
                    end 
                end
                
                integr(ind_camx,ind_camy) = sum(sum(kron(int_other',int_der).*coeffxy));
            end
        end
    case 2
        for ind_camx = 1:cam_siz(1)
            for ind_camy = 1:cam_siz(2)
                int_other = zeros(siz(1),1);
                int_der = zeros(siz(2),1);
                for n = 1:siz(1)
                    %Integrate in dim X
                    %curr_s = inf;
                    %shift to the camera pixel coordinate (0 at the first included knot)
                    xprim = n - (ind_camx - 1)*NsplPix(1) - 1;
                    if xprim <= NsplPix(1) + 2 - 1 && xprim >= - 2 %NsplPix +- spline basis fct support/2
                        x = NsplPix(1) + (ind_camx - 1)*NsplPix(1) - pos_mol(1)/delta(1) - n;% "spline basis domain"
                        lbx = (ind_camx - 1)*NsplPix(1) - pos_mol(1)/delta(1) - n;
                        integrlbx = sum(computeSplineBasis4(lbx - 0.5 - (0:K)));
                        int_other(n) = delta(1)*(sum(computeSplineBasis4(x - 0.5 - (0:K))) - integrlbx);
                    end
                end
                for m = 1:siz(2)
                    %Integrate in dim Y (vertical)
                    xprim = m - (ind_camy - 1)*NsplPix(2) - 1;
                    if xprim <= NsplPix(2) + 2 - 1 && xprim >= - 2 %NsplPix +- spline basis fct support/2
                        y = NsplPix(2) + (ind_camy - 1)*NsplPix(2) - pos_mol(2)/delta(2) - m + 0.5;% "spline basis domain"
                        lby = (ind_camy - 1)*NsplPix(2) - pos_mol(2)/delta(2) - m + 0.5;
                        integrlby = sum(computeSplineBasis3(lby - 0.5 - (0:K)));
                        int_der(m) = delta(2)*(sum(computeSplineBasis3(y - 0.5 - (0:K))) - integrlby);
                    end
                end
                
                integr(ind_camx,ind_camy) = sum(sum(kron(int_der',int_other).*coeffxy));
            end
        end
    case 3
        for ind_camx = 1:cam_siz(1)
            for ind_camy = 1:cam_siz(2)
                int_x = zeros(siz(1),1);
                int_y = zeros(siz(2),1);
                for n = 1:siz(1)
                    %Integrate in dim X
                    %curr_s = inf;
                    %shift to the camera pixel coordinate (0 at the first included knot)
                    xprim = n - (ind_camx - 1)*NsplPix(1) - 1;
                    if xprim <= NsplPix(1) + 2 - 1 && xprim >= - 2 %NsplPix +- spline basis fct support/2
                        x = NsplPix(1) + (ind_camx - 1)*NsplPix(1) - pos_mol(1)/delta(1) - n;% "spline basis domain"
                        lbx = (ind_camx - 1)*NsplPix(1) - pos_mol(1)/delta(1) - n;
                        integrlbx = sum(computeSplineBasis4(lbx - 0.5 - (0:K)));
                        int_x(n) = delta(1)*(sum(computeSplineBasis4(x - 0.5 - (0:K))) - integrlbx);
                    end
                end
                for m = 1:siz(2)
                    %Integrate in dim Y (vertical)
                    xprim = m - (ind_camy - 1)*NsplPix(2) - 1;
                    if xprim <= NsplPix(2) + 2 - 1 && xprim >= - 2 %NsplPix +- spline basis fct support/2
                        y = NsplPix(2) + (ind_camy - 1)*NsplPix(2) - pos_mol(2)/delta(2) - m;% "spline basis domain"
                        lby = (ind_camy - 1)*NsplPix(2) - pos_mol(2)/delta(2) - m;
                        integrlby = sum(computeSplineBasis4(lby - 0.5 - (0:K)));
                        int_y(m) = delta(2)*(sum(computeSplineBasis4(y - 0.5 - (0:K))) - integrlby);
                    end
                end
                
                integr(ind_camx,ind_camy) = sum(sum(kron(int_y',int_x).*coeffxy));
            end
        end
    otherwise
        error('not ready yet');
end
integr = integr(:);
end