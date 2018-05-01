clear fov
nframes = [151,19620,3020,20000,3125,20120,3020];
fov{1} = [12800,12800,1500];
fov{2} = [6400,6400,1500];
datasets = {'Beads','ER1.N3.LD','ER2.N3.HD','MT1.N1.LD',...
    'MT2.N1.HD','MT3.N2.LD','MT4.N2.HD'};

mod = {'AS','2D','DH','BP','DHNPC'};

if ~exist('RANDOM','dir')
    mkdir('RANDOM','upload');
end

for k = 1:length(datasets)
    for l = 1:length(mod)
        str = sprintf('%s____%s____RANDOM____RND.csv',datasets{k},mod{l});
        if ~exist(fullfile('RANDOM','upload',str))
            if strcmp(datasets{k},'Beads')
                aveDens = 6;
                curr_fov = fov{1};
            else
                aveDens = 20 - 18*isempty(strfind(datasets{k},'HD'));
                curr_fov = fov{2};
            end
            loc = rnd_loc(aveDens, nframes(k), curr_fov);
            csvwrite(sprintf('RANDOM/upload/%s',str),loc);
        end
    end
end