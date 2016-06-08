% Test simulation - PALM like photokinetics
% tomas.lukes@epfl.ch

nfluo = 1000;

Ion = 5000;
frames = 200;
%FP-like param
Ton = 3;
Tdark = 2.5;
Tbl=1.5;
% giving mean on time per blink 1 frame
% Note "mean on time per blink" calculation below is an overestimate for small on-times due to quantization error

%% Generate fluorophore time trace

emitter_brightness = zeros(nfluo,frames);
onTime=[];
totOnTime = [];
for k=1:nfluo

    [photons]=brightness_palmSubframe(Ion,Ton,Tdark,Tbl,frames);
    disp(k);
    emitter_brightness(k,:)=photons;
    isOn = photons>0;
    tSwitch = find(diff(isOn));
    tState = diff(tSwitch);
    onTimeCur = tState(1:2:end);%the on times are every second "state" time from the switch vector
    onTime = [onTime, onTimeCur];
    totPhot(k) = sum(photons);
end


for f=1:frames
    noFl(f) = numel(find(emitter_brightness(:,f)>0.1*Ion));
end

% figure;
subplot(141);histogram(noFl);xlim([0 20]);
xlabel('Fluorophores per frame'); 
ylabel('Number of occurences');
disp(['Mean no of fluorophores per frame = ', num2str(mean(noFl))]);

for k=1:nfluo
    totOn(k) = numel(find(emitter_brightness(k,:)>0.1*Ion));
end

%nblinks(nblinks==0)=[];
%% figure;
%subplot(132);histogram(nblinks);
%xlabel('Number of blinks in one burst '); 
%ylabel('Number of occurences');
%% title(['Average blinks per fluorophore ',num2str(sum(nblinks)/nfluo)]);
%text(4,1050,['Average blinks per fluorophore ',num2str(sum(nblinks)/nfluo)],...
%    'HorizontalAlignment','left');
%disp(['Average time of a burst with 8 blinks = ',num2str(8*(Ton+Toff)/50)]);

phot_on = emitter_brightness(:);
phot_on = phot_on(phot_on>0);
subplot(142);histogram(phot_on(:));
xlabel('Number of photons per frame');
ylabel('Freq.');
p = mean(phot_on);
disp(['Mean number of photons ',num2str(p)]);

subplot(143); histogram(onTime);
xlabel('On time per blink');
ylabel('Freq.');
o=mean(onTime);
disp(['Mean on time per blink ', num2str(o)]);

subplot(144); histogram(totPhot);
xlabel('Number of photons per molecule');
ylabel('Freq.');
o=mean(totPhot);
disp(['Mean photons per molecule ', num2str(o)]);

%individual photon count plots
figure;
histogram(phot_on(:));
xlabel('Number of photons per frame');
ylabel('Freq.');
p = mean(phot_on);
disp(['Mean number of photons ',num2str(p)]);
title('Sub frame switching model, no noise');
saveas(gcf,['photon count subframe switch no poisson nphot', num2str(Ion),'.png']);
%add poisson noise
figure
photPois=poissrnd(phot_on);
histogram(photPois);
xlabel('Number of photons per frame');
ylabel('Freq.');
p = mean(photPois);
disp(['Mean number of photons ',num2str(p)]);
title('Sub frame switching model, with poisson noise');
saveas(gcf,['photon count sub frame swich with poisson nphot', num2str(Ion),'.png']);



%% Plot time traces

%figure('Position',[200, 200, 2000, 700]),
figure;
nMolPlot=5;
for ii = 1:nMolPlot
    subplot(nMolPlot,2,2*ii-1);plot(emitter_brightness(ii,:));
    if ii==1; title('Whole time trace (1-10)');end;
    set(gca,'XTickLabel','')
    ylim([0 Ion]);
    if ii==nMolPlot;
        set(gca,'Xtick',[0:2000:frames],'XTickLabel',[0:2000:frames]) 
        xlabel('Frames');
        ylabel('Photons');
    end

    idx = find(emitter_brightness(ii,:)>0);
    MINTRACE=nMolPlot;
    t= min(idx)-3:min(max([max(idx)+3, min(idx)-3+MINTRACE]),frames);
    ttrace = emitter_brightness(ii,t);
    subplot(nMolPlot,2,2*ii);plot(ttrace);
    set(gca,'YTickLabel','')
    ylim([0 Ion]);
    if ii==1; title('Zoom on the burst');end;

    %set(gca,'Xtick',[0:length(ttrace)/10:length(ttrace)],...
    %    'XTickLabel',[max(idx-15*(Ton+Toff),1):2*(Ton+Toff):min(idx+15*(Ton+Toff)-1,frames)]);
    if ii==nMolPlot;
    xlabel('Frames');
    end
end




        
