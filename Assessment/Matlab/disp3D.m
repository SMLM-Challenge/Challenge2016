function [fig,circSize,color,loc] = disp3D(loc,str_title,im3D,pix_siz)
%im3D (3D gaussian rendered localizations) and pix_siz will define circle
doProj = [true,false];
maxSize = 6;
minSize = 2;
fig = cell(2,1);

for imType = 1:2
    sizIm = size(im3D);
    fov = sizIm*pix_siz;
    
    corrZ = fov(3)/2;
    ind_rm = loc(:,2) < 0 | loc(:,2) > fov(1) |...
        loc(:,3) < 0 | loc(:,3) > fov(2) |...
        loc(:,4) < -corrZ | loc(:,4) > fov(3) - corrZ;
    loc(ind_rm,:) = [];
    
    Nel = size(loc,1);
    circSize = zeros(Nel,1);
    
    for k=1:Nel
        circSize(k) = im3D(1 + max(min(floor(loc(k,2)/pix_siz), sizIm(1)-1),0),...
            1 + max(min(floor(loc(k,3)/pix_siz), sizIm(2)-1),0),...
            1 + max(min((corrZ + floor(loc(k,4))/pix_siz), sizIm(3)-1),0));
    end
    maxVal = max(circSize) + (max(circSize)==0)*1;
    circSize = (maxSize-minSize)*log10(1 + 99*circSize/maxVal)/log10(100) + minSize;%(1 - exp(-circSize/max(circSize)));
    
    color = squeeze(hsv2rgb((loc(:,4)-min(loc(:,4)))/(max(loc(:,4)-min(loc(:,4)))),...
        ones(Nel,1),ones(Nel,1)));
    
    %colorPlane = squeeze(hsv2rgb((gt(:,4)-min(gt(:,4)))/(max(gt(:,4)-min(gt(:,4)))),...
    %ones(res.nloc_gt_initial,1),0.2*ones(res.nloc_gt_initial,1)));
    colorPlane = 'w';
    fig{imType} = figure;
    
    whitebg(fig{imType});
    if doProj(imType)
        %xz plane,-100 + min(loc(:,2))*
        scatter3(zeros(Nel,1),loc(:,3),loc(:,4),circSize,colorPlane,'filled');hold on;
        %yz plane-100 + min(loc(:,1))*
        scatter3(loc(:,2),...
            zeros(Nel,1),loc(:,4),circSize,colorPlane,'filled');
        %z plane
        scatter3(loc(:,2),loc(:,3),-fov(3)/2*ones(Nel,1),circSize,colorPlane,'filled');
    end
    %3D
    scatter3(loc(:,2),loc(:,3),loc(:,4), circSize, color, 'filled');hold on;
    if doProj(imType)
        zAx = -fov(3)/2:max(fov(3)/2,max(loc(:,4)));
        plot3(zeros(length(zAx),1),zeros(length(zAx),1),zAx,'w','LineWidth',1.5);
        set(gca,'xtick',linspace(0,max(loc(:,2)),10));
        set(gca,'ytick',linspace(0,max(loc(:,3)),10));
        set(gca,'ztick',unique([linspace(zAx(1),0,5), linspace(0,zAx(end),5)]))
    else
        zAx = -fov(3)/2:max(fov(3)/2,max(loc(:,4)));
        plot3(zeros(length(zAx),1),zeros(length(zAx),1),zAx,'w','LineWidth',1.5);
    end
    curr_ax = gca;
    set(curr_ax,'Ydir','reverse','Xdir','reverse');
    set(curr_ax,'LineWidth',1.5,'YAxisLocation','origin','XAxisLocation','origin',...
        'xticklabel',{[]},'yticklabel',{[]},'zticklabel',{[]});
    title(str_title);hold off;
    grid on
    if doProj(imType) %improve by getting positions of xlabel before xticklabel set off
        %text(3953.8699714910617,6191.461925112759,-333.8702107707759,'X');
        %text(6305.590234897347,3771.309889470256,-361.34195007938615,'Y');
        %text(6798.014514492112,-319.45371226913994,512.4110141202436,'Z');
        text(3903.998118570962,6354.910704097885,-1034.204448664219,'X');
        text(6474.1412913783715,3758.0686139824684,-1073.1666362493488,'Y');
        text(6801.183338021612,-378.47701227403013,224.90072171690554,'Z');
    else
        text(3903.998118570962,6354.910704097885,-1034.204448664219,'X');
        text(6474.1412913783715,3758.0686139824684,-1073.1666362493488,'Y');
        text(6801.183338021612,-378.47701227403013,224.90072171690554,'Z');
    end
    axis tight;
    curr_ax.XAxis.TickLength = [0,0];
    curr_ax.YAxis.TickLength = [0,0];
    curr_ax.ZAxis.Visible= 'off';
    
    fig{imType}.InvertHardcopy = 'off';drawnow
end
end