function coeff = noise_coef_fct(z, umin, umax, eta, sig, nu, gain,cam_model,reltol,abstol,Npoints)
%NOISE_COEF_FCT
%syms u;
integr_type = 2;

if true %max(-(umin:umax).*((umin:umax) + 2*eta - 2*z)/(2*sig^2) - (umin:umax)/gain) > 70
    % No simplification
    
    pkz = probCamera(z,nu,sig,umin,umax,gain,cam_model,eta,integr_type,reltol,abstol,Npoints);
    if integr_type==1
        syms u;
        coeff = vpaintegral(u_int(u, z, eta, sig, nu, gain, 1),...
            umin, umax,'ArrayValued',0,'RelTol',reltol,'AbsTol',abstol);
        coeff = vpa(exp(-vpa(nu))/(sqrt(2*pi)*sig*gain)*coeff);
    else
        u = linspace(umin,umax,Npoints);
        coeff = trapz(u, u_int(u, z, eta, sig, nu, gain, 1));
        coeff = vpa(exp(-vpa(nu))/(sqrt(2*pi)*sig*gain)*coeff);
    end
    if (logical(pkz==0) && logical(coeff==0)) || isnan(pkz) || isnan(coeff)
        pkz = probCamera(z,nu,sig,umin,umax,gain,cam_model,eta,0,reltol,abstol);
        coeff = integral(@(uprim) u_int(uprim, z, eta, sig, nu, gain,0),...
            umin, umax,'ArrayValued',0,'RelTol',reltol,'AbsTol',abstol);
        
        %Note: avoid ^2 (can be (1e+154)^2=inf whereas exp(-(z-eta).^2 [...]) ~=1e-154
        %with coeff.*coeff, it is still able to compute it.
        coeff = exp(vpa(-(z - eta).^2/(2*sig^2)))...
            .*exp(vpa(-nu))/(sqrt(2*pi)*sig*gain^2)... %gain^2 because not simplified in pkz expr.
            .*vpa(coeff).*vpa(coeff)/pkz;
    else
        coeff = coeff.*coeff/pkz;
    end
else %simplified expr.
    pkz = probCamera(z,nu,sig,umin,umax,gain,cam_model,eta,0,reltol,abstol);
    coeff = integral(@(u) u_int(u, z, eta, sig, nu, gain,0),...
        umin, umax,'ArrayValued',0,'RelTol',reltol,'AbsTol',abstol);
    
    %Note: avoid ^2 (can be (1e+154)^2=inf whereas exp(-(z-eta).^2 [...]) ~=1e-154
    %with coeff.*coeff, it is still able to compute it.
    coeff = exp(vpa(-(z - eta).^2/(2*sig^2)))...
        .*exp(vpa(-nu))/(sqrt(2*pi)*sig*gain^2)... %gain^2 because not simplified in pkz expr.
        .*vpa(coeff).*vpa(coeff)/pkz;
end
%coeff = double(coeff);

end

function coeff = u_int(u, z, eta, sig, nu, gain, mode)

switch mode
    case 0
        %simplified for EMCCD
        %coeff = exp(-u.*(u + 2*eta - 2*z)/(2*sig^2) - u/gain)...
        coeff = exp(-((u + eta - z).^2 - (eta - z)^2)/(2*sig^2) - u/gain)...
            .*besseli(0,2*sqrt(nu .* u/gain));
    case 1
        % Not simplified
        coeff = exp(-(z - u - eta).^2/(2*sig^2) - u/gain)...
            .*besseli(0,2*sqrt(nu .* u/gain));
end
%coeff(isnan(coeff)) = 0;
end