function [h,hsub] = dispOrthoView(str_title,loc,gt,rad, varargin)
%Display an orthoview
% Either loc and gt are list of particle (frame, X, Y, Z)
%        or cell containing the 3D image in the following way:
%        {XYZ},{YZ},{XZ},{XY}
%        When drawing rectangle, origin at bottom left
do2D = false;doRect = false;
k = 1;
while k < nargin - 4
    switch varargin{k}
        case 'cube'
            pos = varargin{k+1}(1:end);
            doRect = true;
        case '2D'
            do2D = varargin{k+1};
    end
    k = k + 2;
end


if iscell(loc)
    if iscell(gt)
        doScatter = false;
        if doRect
            if pos(3) < 0 %z component
                pos(3) = pos(3) + pos(6)/2;%set z=0 as min val
            end
            pos(1:3) = pos(1:3) + 1;
            pos(4:6) = pos(4:6) - 0.5;
        end
    else
        fprintf('Inputs are not consistent\n');
        return
    end
else
    doScatter = true;
end

h = figure('Name',str_title,'Color','black');
if ~do2D
    hsub{1} = subplot(3,3,[1,2,4,5]);
end
if doScatter
    scatter(loc(:,2), loc(:,3),rad,'r','filled');hold on;
    scatter(gt(:,3),gt(:,4),rad,'g','filled');
else
    im{1} = imfuse(loc{4},gt{4},'ColorChannels',[1,2,0],...
        'Scaling','independent');hold on;
    image(im{1});
end
if doRect
    rectangle('Position',[pos([2,1]), pos(4:5)],'EdgeColor','w');
end

axis off;title('XY','Color','w','FontSize',14);
if ~do2D
    hsub{2} = subplot(3,3,[7,8]);
    if doScatter
        scatter(loc(:,2), loc(:,4),rad,'r','filled');hold on;
        scatter(gt(:,3),gt(:,5),rad,'g','filled');
    else
        im{2}=imfuse(loc{3}',gt{3}','ColorChannels',[1,2,0],...
            'Scaling','independent');hold on;
        image(im{2});
    end
    if doRect
        rectangle('Position',[pos([1,3]), pos([4,6])],'EdgeColor','w');
    end
    
    axis off;title('XZ','Color','w','FontSize',14);
    hsub{3} = subplot(3,3,[3,6]);
    if doScatter
        scatter(loc(:,4), loc(:,3),rad,'r','filled');hold on;
        scatter(gt(:,5),gt(:,4),rad,'g','filled');
    else
        im{3}=imfuse(loc{2},gt{2},'ColorChannels',[1,2,0],...
            'Scaling','independent');hold on;
        image(im{3});
    end
    if doRect
        rectangle('Position',[pos([3,2]), pos([6,5])],'EdgeColor','w');
    end
    
    axis off;title('YZ','Color','w','FontSize',14);
end
drawnow;
end