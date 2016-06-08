%---------------
%2D PSF
fname = 'final competition beads/ZStack-6Beads-3um-10nmStep-2D-5xAverage.tif';
saveName = 'final competition beads/2D-PSF-JBS-10nm-voxel.tif';
frz0 = 141;
pixSzIn = 160;
pixSzOut = [10];
nMol = 3;

%generate_psf_lut4(fname,pixSzIn, pixSzOut, nMol,frz0,saveName);

%---------------
%astig PSF
fname = 'final competition beads/ZStack-6Beads-3um-10nmStep-AstigCylLens-5xAverage.tif';
saveName = 'final competition beads/astig-PSF-JBS-10nm-voxel.tif';
frz0 = 156;
pixSzIn = 160;
pixSzOut = [10];
nMol = 4;

%generate_psf_lut4(fname,pixSzIn, pixSzOut, nMol,frz0,saveName);
%---------------
%DH PSF
fname = 'final competition beads/DH1_beads100nm_1.49NA100x_Zrange3um_ZStep20nm_2Imagesperstep_Exp100ms_Pixel16um_laser647.tif';
saveName = 'final competition beads/DH-PSF-10nm-voxel.tif';
frz0 = 130;
pixSzIn = 160;
pixSzOut = [10];
nMol = 3;

%generate_psf_lut4(fname,pixSzIn, pixSzOut, nMol,frz0,saveName,'FramePerZPos',2,'InterpZFactor',2,'BoxSzNm',6000);


%---------------
%Alternate 2D PSF - use this one
fname = '160602 updated 2d psf/160601 bead 0.1um nstorm 642nm 10nmZstep 42.7nmPix002.tif';
saveName = '160602 updated 2d psf/2D-PSF-newcastle-10nm-voxel.tif';
frz0 = 139;
pixSzIn = 42.7;
pixSzOut = [10];
nMol = 3;

generate_psf_lut4(fname,pixSzIn, pixSzOut, nMol,frz0,saveName,'FramePerZPos',1,'InterpZFactor',1,'BoxSzNm',6000);

