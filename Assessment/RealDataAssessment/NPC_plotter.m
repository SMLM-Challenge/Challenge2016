function NPC_plotter(fname,savename);

%savename = fname(1:end-4);




data =importdata(fname);
fr = data(:,1);
x= data(:,2);
y= data(:,3);
z= data(:,4);
phot= data(:,5);


%whole image 
rangez=[-500,500]
rangex = [0,20000];
rangey = [0,20000]
pixSz=20;
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
saveas(gcf,[savename,'_largeFOV_xz',num2str(pixSz),'nmpix.fig']);
imwrite(srIm_XZ,[savename,'_largeFOV_xz',num2str(pixSz),'nmpix.tif']);


%FOV1
rangez=[-200,200]
rangex = [6000,8000];
rangey = [6000,8000];
pixSz=5;
satVal3d=0.001
satVal2d=0.001
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


%sub image 
rangez=[-500,300]
rangex = [7200,16500];
rangey = [3800,5800]
pixSz=3;
satVal3d=0.005
satVal2d=0.005
blurSigma=3;
box = [rangex(1),rangey(1),rangex(2)-rangex(1),rangey(2)-rangey(1)];
figure('Name',fname(1:end-4));
[srIm_RGB,yIm,xIm] = renderStormData(x,y,'Z',z,'ZLim',rangez,'Saturate',satVal3d,'PixelSize',pixSz,'Sigma',blurSigma,'Box',box);
saveas(gcf,[savename,'_profile_xy',num2str(pixSz),'nmpix.fig']);
imwrite(srIm_RGB,[savename,'_profile_xy',num2str(pixSz),'nmpix.tif']);
figure('Name',fname(1:end-4));
hCbar= plot3DSTORMcolorbar([100, 5], 'v',rangez,2,'FlipCAxis')
saveas(gcf,[savename,'_profile_colorbar',num2str(pixSz),'nmpix.fig']);
saveas(gcf,[savename,'_profile_colorbar',num2str(pixSz),'nmpix.png']);

%Line profiles
xyPos =[ 7732, 5223, 8300, -5];
lineWidth =600;
rangez = [-800 800];
pixSz=10;
satVal2d=0.001;
blurSigma=3;

useAngleFormat =true;
figure('Name',fname(1:end-4))
[srIm_XZ X1]=plotZcrossSection(x,y,z,xyPos,lineWidth,rangez, pixSz,satVal2d,blurSigma,useAngleFormat);
imwrite(srIm_XZ,[savename,'_profile_xz',num2str(pixSz),'nmpix.tif']);

figure('Name',fname(1:end-4));
imagesc(xIm,yIm,srIm_RGB)
axis equal 
hold all;
ii=1;
%calculate the offsets
offset = lineWidth/2;
X0 = [xyPos(1:2)];
theta = xyPos(4)*pi/180;
thetaUp = theta+pi/2;
thetaDown = theta-pi/2;
X0up=[X0(1)-offset*cos(thetaUp), X0(2)-offset*sin(thetaUp)];
X1up=[X1(1)-offset*cos(thetaUp), X1(2)-offset*sin(thetaUp)];
X0down=[X0(1)-offset*cos(thetaDown), X0(2)-offset*sin(thetaDown)];
X1down=[X1(1)-offset*cos(thetaDown), X1(2)-offset*sin(thetaDown)];

plot([X0up(1) X1up(1)],[X0up(2) X1up(2)],'w--','LineWidth',2)
plot([X0down(1) X1down(1)],[X0down(2) X1down(2)],'w--','LineWidth',2)
%plot([X0(1) X1(1)],[X0(2) X1(2)],'w-','LineWidth',2)
set(gca, 'xtick', 0);
set(gca, 'ytick', 0);
set(gca,'box','off')
saveas(gcf,[savename,'_profile_xy_labelled',num2str(pixSz),'nmpix.fig'])
saveas(gcf,[savename,'_profile_xy_labelled',num2str(pixSz),'nmpix.png'])
