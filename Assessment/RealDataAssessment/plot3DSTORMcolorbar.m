function h = plot3DSTORMcolorbar(dims, orientation,zLim,nTick,varargin)
% PLOT_COLORBAR plot a standalone colorbar for inclusion in a publication
%       H = PLOT_COLORBAR(DIMS, ORIENTATION TITLE_STRING) Plot a colorbar for
%       inclusion in a publication. DIMS sets the length and width of the
%       colorbar (in vertical mode). DIMS(1) will be the size of the colormap
%       used and DIMS(2) will be the number of times it is repeated (thickness
%       of image). ORIENTATION sets the orientation of the bar -- 'h', or 'v'.
%       TITLE_STRING sets the title of the axis used.
%
%       H = PLOT_COLORBAR(DIMS, ORIENTATION TITLE_STRING, CMAP) Works as above,
%       except that CMAP is a handle to a function to generate the colormap.
%
%       Examples:
%               h1 = plot_colorbar([100, 5], 'h', 'Test Colormap')
%               h2 = plot_colorbar([150, 10], 'v', 'Test Colormap', @hsv)
%
%       Bugs:
%               May not work well with wide images. 
%               Feel free to send in patches etc for any problems you find.
%
% Matt Foster <ee1mpf@bath.ac.uk>
%Extended  to allow to set the labels 181114 S Holden
HUEMAX = 240/360; %this is when you get range red --> blue (hsv circles around back to red

% Extract the width froms dims, if there is one.
if length(dims) < 2
  width = 5;
else
  width = dims(2);
end
doReverse=false;
ii = 1;
while ii <= numel(varargin)
  if strcmp(varargin{ii},'FlipCAxis')
    doReverse=true
    ii = ii + 1;
  else
    ii = ii + 1;
  end
end
map = stormCmap(dims(1));
%map = flipud(colormap);
switch lower(orientation)
case {'v', 'vert', 'vertical'}
  h = image(repmat(cat(3, map(:,1), map(:,2), map(:,3)), 1, width));
  % Remove ticks we dont want.
  set(gca, 'xtick', 0);
  ticks = get(gca, 'ytick');
  ticksMod = linspace(0.5, max(ticks),nTick);
  set(gca, 'ytick', ticksMod);
  cval = linspace(zLim(1),zLim(2),nTick);
  set(gca, 'yticklabel', cval);
  % Set up the axis
  title('Z (nm)')
  axis equal
  axis tight
  axis xy
    if doReverse
      set(gca, 'YDir', 'reverse');
    end


case {'h', 'horiz', 'horizontal'}
  h = image(repmat(cat(3, map(:,1)', map(:,2)', map(:,3)'), width, 1));
  
  % Remove ticks we dont want.
  set(gca, 'ytick', 0);
  ticks = get(gca, 'xtick');
  ticksMod = linspace(0.5, max(ticks),nTick);
  set(gca, 'xtick', ticksMod);
  cval = linspace(zLim(1),zLim(2),nTick);
  set(gca, 'xticklabel', cval);
  % Set up the axis
  title('Z (nm)')
  axis equal
  axis tight
  axis xy
    if doReverse
      set(gca, 'XDir', 'reverse');
    end

otherwise
  error('unknown colorbar orientation');
end

set(gca,'TickLength',[0 0]);

