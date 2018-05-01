function sig = getSigma(sigmin,sigmax,loc,k)
%GETSIGMA

%[V,C] = voronoin(unique(loc,'rows'));
%sig = zeros(length(C),1);
%for kk = 1:length(C)
    %if isfinite(V(C{kk}))
        %try
        %[~,sig(kk)] = convhulln(V(C{kk},:));
        %catch
        %    [~,sig(kk)] = convhulln(V(C{kk},:),{'Qt','Pp'});
        %    %fprintf('%i\n',kk);
        %end
    %else
    %    sig(kk) = inf;
    %end
%end

Nmol = size(loc,1);

sig = zeros(Nmol,1);
tic
neigh = knnsearch([loc,(1:Nmol)'],[loc,(1:Nmol)'],'K',k,'Distance',@mycustomdist);
toc
for kk = 1:Nmol 
    X = loc(neigh(kk,:),:) - repmat(loc(kk,:),k,1);
    sig(kk) = real(sqrt(det((X*X')/k)));
end

maxsig = quantile(sig,0.95);
minsig = quantile(sig,0.05);
sig = max(min((sig - minsig)/(maxsig - minsig),1),0);
sig = sigmin + (sigmax - sigmin).*sig;

end

function d2 = mycustomdist(zi,zj)
d2 = sum((repmat(zi(1:end-1),[size(zj,1),1]) - zj(:,1:end-1)).^2,2);
d2(zi(end)==zj(:,end)) = inf;
end