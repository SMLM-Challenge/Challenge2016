nframes = 20000;
fov = 6400;

%Alexa647  = 1 > 4952
dye = 1; 
P = 400;              % Power laser in W / cm^2
framerate = 50;      % in Hz

% Alexa647  = 1 > 3052
% dye = 1; 
% P = 250;              % Power laser in W / cm^2
%framerate = 50;      % in Hz

%Dendra2 = 2 , > 543
% dye = 2; 
% P = 360;              % Power laser in W / cm^2
% framerate = 10;      % in Hz

activate_fluos(nframes, fov, 0, '/Users/sage/Desktop/activation-final/MT1.N1.LD/', dye, P, framerate);
