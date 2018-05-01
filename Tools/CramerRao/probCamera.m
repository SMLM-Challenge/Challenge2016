function pkz = probCamera(z,nu,sig,umin,umax,gain,cam_model,eta,mode,reltol,abstol,Npoints) %,step_int

%reltol = 1e-8;
%abstol = 1e-11;
%z = vpa(z);
switch cam_model
    case 'EMCCD'
        switch mode
            case 0 %simplified expr.
                %integrate over u from umin to umax
                pkz = integral(@(u) EMCCD(z, nu, sig, u, gain, eta,0),...
                    umin,umax,'ArrayValued',0,'RelTol',reltol,'AbsTol',abstol);
                
                %pkz = exp(-nu)/(sqrt(2*pi)*sig)*(1 + pkz);%simplified in expr.
                pkz = 1 + pkz;
            case 1 %No simplification
                %integrate over u from umin to umax
                syms u;
                pkz = vpaintegral(EMCCD(z, nu, sig, u, gain, eta,1),...
                    umin,umax,'ArrayValued',0,'RelTol',reltol,'AbsTol',abstol);
                
                pkz = exp(-vpa(nu))/(sqrt(2*pi)*sig)*(exp(-(vpa(z) - eta)^2/(2*sig^2)) + pkz);
            case 2 %No simplification, trapezoidal rule
                u = linspace(umin,umax,Npoints);
                pkz = trapz(u, EMCCD(z, nu, sig, u, gain, eta,1));
                
                pkz = exp(-vpa(nu))/(sqrt(2*pi)*sig)*(exp(-(z - eta)^2/(2*sig^2)) + pkz);
                pkz(isinf(pkz) || isnan(pkz)) = 0;
        end
        %if isnan(out)
        %    out = 0;
        %end
end
end
function out = EMCCD(z,nu,sig,u,gain,eta,mode)
switch mode
    case 0
        %out = exp(-u.*(u + 2*eta - 2*z)/(2*sig^2) - u/gain);
        out = exp((-((u + eta - z).^2 - (eta - z)^2)/(2*sig^2) - u/gain));
        out = out.*sqrt(nu*u/gain).*besseli(1, (2*sqrt(nu*u/gain)))./u;
    case 1
        out = exp(-(z - u - eta).^2/(2*sig^2) - u/gain);
        out = out.*sqrt(nu*u/gain).*besseli(1, 2*sqrt(nu*u/gain))./u;
end
%out = double(out);
out(u==0) = 0;
end