function tubulin_plotter(fname,savename)



data =importdata(fname);
fr = data(:,1);
x= data(:,2);
y= data(:,3);
z= data(:,4);
phot= data(:,5);


%whole image 
rangez=[-750,500]
rangex = [0,40000];
rangey = [0,40000]
pixSz=30;
satVal3d=0.005
satVal2d=0.005
blurSigma=3;
box = [rangex(1),rangey(1),rangex(2)-rangex(1),rangey(2)-rangey(1)];
figure('Name',fname(1:end-4));
[srIm_RGB] = renderStormData(x,y,'Z',z,'ZLim',rangez,'Saturate',satVal3d,'PixelSize',pixSz,'Sigma',blurSigma,'Box',box);
saveas(gcf,[savename,'_largeFOV_xy',num2str(pixSz),'nmpix.fig']);
imwrite(srIm_RGB,[savename,'_largeFOV_xy',num2str(pixSz),'nmpix.tif']);
figure('Name',fname(1:end-4));
hCbar= plot3DSTORMcolorbar([100, 5], 'v',rangez,2,'FlipCAxis')
saveas(gcf,[savename,'_largeFOV_colorbar',num2str(pixSz),'nmpix.fig']);
saveas(gcf,[savename,'_largeFOV_colorbar',num2str(pixSz),'nmpix.png']);


%2d gray plot of the xz projection
%have to manually filter in Y beforehand URGH
xCrop= x(y>=rangey(1)&y<=rangey(2));
zCrop= z(y>=rangey(1)&y<=rangey(2));

box = [rangex(1),rangez(1),rangex(2)-rangex(1),rangez(2)-rangez(1)];
figure('Name',fname(1:end-4));
[srIm_XZ] = renderStormData(xCrop,zCrop,'Saturate',satVal2d,'PixelSize',pixSz,'Sigma',blurSigma,'Box',box);
axis normal;
saveas(gcf,[savename,'_largeFOV_xz',num2str(pixSz),'nmpix.fig']);
imwrite(srIm_XZ,[savename,'_largeFOV_xz',num2str(pixSz),'nmpix.tif']);


%FOV1
rangez=[-700,400]
rangex = [20000,30000];
rangey = [17000,27000];
pixSz=5;
satVal3d=0.002
satVal2d=0.002
blurSigma=3;
box = [rangex(1),rangey(1),rangex(2)-rangex(1),rangey(2)-rangey(1)];
figure('Name',fname(1:end-4));
[srIm_RGB] = renderStormData(x,y,'Z',z,'ZLim',rangez,'Saturate',satVal3d,'PixelSize',pixSz,'Sigma',blurSigma,'Box',box);
saveas(gcf,[savename,'_FOV1_xy',num2str(pixSz),'nmpix.fig']);
imwrite(srIm_RGB,[savename,'_FOV1_xy',num2str(pixSz),'nmpix.tif']);

figure('Name',fname(1:end-4));
hCbar= plot3DSTORMcolorbar([100, 5], 'v',rangez,2,'FlipCAxis')
saveas(gcf,[savename,'_FOV1_colorbar',num2str(pixSz),'nmpix.fig']);
saveas(gcf,[savename,'_FOV1_colorbar',num2str(pixSz),'nmpix.png']);


%2d gray plot of the xz projection
%have to manually filter in Y beforehand 
xCrop= x(y>=rangey(1)&y<=rangey(2));
zCrop= z(y>=rangey(1)&y<=rangey(2));

box = [rangex(1),rangez(1),rangex(2)-rangex(1),rangez(2)-rangez(1)];
figure('Name',fname(1:end-4));
[srIm_XZ] = renderStormData(xCrop,zCrop,'Saturate',satVal2d,'PixelSize',pixSz,'Sigma',blurSigma,'Box',box);
saveas(gcf,[savename,'_FOV1_xz',num2str(pixSz),'nmpix.fig']);
imwrite(srIm_XZ,[savename,'_FOV1_xz',num2str(pixSz),'nmpix.tif']);

%FOV2
rangez=[-400,400]
rangex = [24000, 26500];
rangey = [20000, 22500 ];
pixSz=5;
satVal3d=0.002
satVal2d=0.002
blurSigma=3;
box = [rangex(1),rangey(1),rangex(2)-rangex(1),rangey(2)-rangey(1)];
figure('Name',fname(1:end-4));
[srIm_RGB] = renderStormData(x,y,'Z',z,'ZLim',rangez,'Saturate',satVal3d,'PixelSize',pixSz,'Sigma',blurSigma,'Box',box);
saveas(gcf,[savename,'_FOV2_xy',num2str(pixSz),'nmpix.fig']);
imwrite(srIm_RGB,[savename,'_FOV2_xy',num2str(pixSz),'nmpix.tif']);

figure('Name',fname(1:end-4));
hCbar= plot3DSTORMcolorbar([100, 5], 'v',rangez,2,'FlipCAxis')
saveas(gcf,[savename,'_FOV2_colorbar',num2str(pixSz),'nmpix.fig']);
saveas(gcf,[savename,'_FOV2_colorbar',num2str(pixSz),'nmpix.png']);


%2d gray plot of the xz projection
%have to manually filter in Y beforehand 
xCrop= x(y>=rangey(1)&y<=rangey(2));
zCrop= z(y>=rangey(1)&y<=rangey(2));

box = [rangex(1),rangez(1),rangex(2)-rangex(1),rangez(2)-rangez(1)];
figure('Name',fname(1:end-4));
[srIm_XZ] = renderStormData(xCrop,zCrop,'Saturate',satVal2d,'PixelSize',pixSz,'Sigma',blurSigma,'Box',box);
saveas(gcf,[savename,'_FOV2_xz',num2str(pixSz),'nmpix.fig']);
imwrite(srIm_XZ,[savename,'_FOV2_xz',num2str(pixSz),'nmpix.tif']);


%Line profiles

%FOV_paper
rangez=[-400,300]
rangex = [12000,26500];
rangey = [18500,27000];
pixSz=5;
satVal3d=0.002
satVal2d=0.002
blurSigma=3;
box = [rangex(1),rangey(1),rangex(2)-rangex(1),rangey(2)-rangey(1)];
figure('Name',fname(1:end-4));
[srIm_RGB,yIm,xIm] = renderStormData(x,y,'Z',z,'ZLim',rangez,'Saturate',satVal3d,'PixelSize',pixSz,'Sigma',blurSigma,'Box',box);
saveas(gcf,[savename,'_profiles_xy',num2str(pixSz),'nmpix.fig']);
imwrite(srIm_RGB,[savename,'_profiles_xy',num2str(pixSz),'nmpix.tif']);

figure('Name',fname(1:end-4));
hCbar= plot3DSTORMcolorbar([100, 5], 'v',rangez,2,'FlipCAxis')
saveas(gcf,[savename,'_profiles_colorbar',num2str(pixSz),'nmpix.fig']);
saveas(gcf,[savename,'_profiles_colorbar',num2str(pixSz),'nmpix.png']);



%Line profiles
xyPos{1} = 1.0e+04*[   1.9740    2.4322;...
    1.9932    2.4759]%SF6 profile6
xyPos{2} = 1.0e+04*[1.6509    2.0684;...
        1.7113    2.0738]%SF6 profile7
   

xyPos{3} = 1.0e+04*[1.5723    2.0078
    1.6326    2.0337 ]%SF6 profile8

xyPos{4} = 1.0e+04*[  2.1608    2.0958;...
    2.2192    2.1152]%SF6 profile9
   
%xyPos{5} = 1.0e+04*[ 2.1819    2.6637;...
%    2.2128    2.6413]%SF6 profile10
xyPos{5} = 1.0e+04*[   2.1282    2.3499;...
    2.1537    2.4014]%SF6 profile11
nProfile = numel(xyPos);
lineWidth = 250;
rangez = [-500 500];
pixSz=5;
satVal2d=0.001;
blurSigma=3;

for ii=1:nProfile
    figure('Name',fname(1:end-4));
    srIm_XZ=plotZcrossSection(x,y,z,xyPos{ii},lineWidth,rangez, pixSz,satVal2d,blurSigma);
    imwrite(srIm_XZ,[savename,'_profile',num2str(ii),'_',num2str(pixSz),'nmpix.tif']);
end

figure('Name',fname(1:end-4));
imagesc(xIm,yIm,srIm_RGB)
axis equal
hold all;
for ii = 1:nProfile
    plot(xyPos{ii}(:,1),xyPos{ii}(:,2),'w-','LineWidth',2)
    xT= xyPos{ii}(1,1)-50;
    yT= xyPos{ii}(1,2)-50;
    t=text(xT,yT,num2str(ii),'HorizontalAlignment','right');
    t.Color='w';
end
set(gca, 'xtick', 0);
set(gca, 'ytick', 0);
set(gca,'box','off')
%set(gca,'Visible','off');
saveas(gcf,[savename,'_profiles_xy_labelled',num2str(pixSz),'nmpix.fig'])
saveas(gcf,[savename,'_profiles_xy_labelled',num2str(pixSz),'nmpix.png'])
