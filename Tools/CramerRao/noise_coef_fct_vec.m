function coeff = noise_coef_fct_vec(z, umin, umax, eta, sig, nu, gain, cam_model, reltol, abstol,Npoints)



%fprintf('doing vectorial way...');
%arrayfun is slower than a loop (+0.05 sec/element)
%tic
%coeff = arrayfun(@(x) noise_coef_fct(x, umin, umax, eta, sig, nu, gain, cam_model, reltol, abstol),z);
%toc
%tic
coeff = zeros(size(z));
for k = 1:length(z)
    coeff(k) = noise_coef_fct(z(k), umin, umax, eta, sig, nu, gain, cam_model, reltol, abstol,Npoints);
end
%toc
% if isscalar(z)
%     diffZ = 1;
% else
%     diffZ = diff(z([1,1:end]));
% end


coeff = double(coeff);
T = 1;%min(nu*gain*diffZ,1e0);
coeff(coeff > T) = 0;%sometimes vpa still wrongly behaves

end