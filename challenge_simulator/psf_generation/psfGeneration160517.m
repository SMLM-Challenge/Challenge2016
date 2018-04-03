%---------------
%astig PSF
fname = 'experimental-PSFs/AS-ZStack-6Beads-3um-10nmStep-AstigCylLens-5xAverage.tif';
saveName = 'interpolated-PSFs/astig-PSF-JBS-10nm-voxel.tif';
frz0 = 156;
pixSzIn = 160;
pixSzOut = [10];
nMol = 4;

generate_psf_lut4(fname,pixSzIn, pixSzOut, nMol,frz0,saveName);
%---------------
%DH PSF
fname = 'experimental-PSFs/DH-beads100nm_1.49NA100x_3um_20nm_2ImStep100ms_Pix16um_647.tif';
saveName = 'interpolated-PSFs/DH-PSF-10nm-voxel.tif';
frz0 = 130;
pixSzIn = 160;
pixSzOut = [10];
nMol = 3;

generate_psf_lut4(fname,pixSzIn, pixSzOut, nMol,frz0,saveName,'FramePerZPos',2,'InterpZFactor',2,'BoxSzNm',6000);


%---------------
%2D PSF
% Note the 2D PSF is also used to generate the semisynthetic biplane PSF
fname = 'experimental-PSFs/2D-160601 bead 0.1um nstorm 642nm 10nmZstep 42.7nmPix002.tif';
saveName = 'interpolated-PSFs/2D-PSF-newcastle-10nm-voxel.tif';
frz0 = 139;
pixSzIn = 42.7;
pixSzOut = [10];
nMol = 3;

generate_psf_lut4(fname,pixSzIn, pixSzOut, nMol,frz0,saveName,'FramePerZPos',1,'InterpZFactor',1,'BoxSzNm',6000);

