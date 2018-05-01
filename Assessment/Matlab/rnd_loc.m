function loc = rnd_loc(ave_dens,nframes,fov)
%RND_LOC Random Localization
%   ave_dens : Number of fluorophores per frame on average
%   nframes : number of frame
%   fov : Field Of View

Nmol_tot = ave_dens*nframes;

if iscolumn(fov)
    fov = fov';
end
maxInt = 1e4;

loc = repmat([fov, maxInt], Nmol_tot, 1).*rand(Nmol_tot,4);
loc(:,3) = loc(:,3) - fov(3)/2;

loc = [reshape(repmat(1:nframes,ave_dens,1),Nmol_tot,1),loc];

end

