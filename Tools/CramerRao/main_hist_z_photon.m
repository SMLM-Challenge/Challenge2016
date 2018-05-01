ind = diff(activations.xnano([1:end,end]))==0 & diff(activations.ynano([1:end,end]))==0 & diff(activations.znano([1:end,end]))==0;
Nmol = nnz(ind);
ind = ~ind;
tmp = cumsum(ind);
for kk = 1:Nmol
    Nphotons(kk) = sum(activations.intensity(kk==tmp));
    Nact(kk) = nnz(kk==tmp);
end