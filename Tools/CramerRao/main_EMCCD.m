%Implementation from
m = 300;
p = 3e2;
c = unique(max(-2e3:2e4 + m*p,0));
n = 1;
gpm = @(c) 1/(factorial(n-1).*m.^n).*c.^(n-1).*exp(-c/m);

%figure(2);
%plot(c,gpm(c));



Gpm = @(c) exp(-p).*(c==0) + sqrt(p./(c.*m)).*exp(-c./m - p).*besseli(1,2*sqrt(c.*p./m));

prob_distr = prob_dens('EMCCD',m, p, c);

figure(3);
plot(c,Gpm(c),'LineWidth',2);hold on;
plot(c,prob_distr,'--','LineWidth',2);
line([m*p,m*p],[0,1.1*max(prob_distr)],'LineWidth',2,'Color','g','LineStyle',':');
legend('version 1','version 2');
