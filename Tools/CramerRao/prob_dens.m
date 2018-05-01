function prob_distr = prob_dens(camera_model,gain,eta,u)
%Probability density of camera model for a single initial photonelectron

if min(u)<0
    error('u must be positive');
end

switch camera_model
    case 'EMCCD'
        %2004 Basden / 2016 Chao, exponential distribbution with param
        %1/gain, plot p(u) for u=[0,max]
        
        prob_distr = exp(-(u./gain + eta)).*sqrt(eta./(u*gain))...
            .*besseli(1,2*sqrt(eta.*u./gain));
        prob_distr(u==0) = exp(-eta);
    otherwise
        error('Not implemented yet');
end
fprintf('Empirical expectation : %1.2e; sum : %1.2e\n',mean(prob_distr.*u),sum(prob_distr));
%figure(10);plot(u,prob_distr);
end