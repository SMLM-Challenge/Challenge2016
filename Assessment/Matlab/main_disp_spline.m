%Spline n = 3
t = -3:0.01:3;
x = t;
x = abs(x);
ind0 = (x >= 0 & x < 1);
ind1 = (x >= 1 & x < 2);
ind2 = (x >= 2);
x(ind0) = 2/3 - x(ind0).^2 + x(ind0).^3/2;
x(ind1) = (2 - x(ind1)).^3/6;
x(ind2) = 0;
figure,plot(t,x);hold on;
fprintf('Integral of Spline basis %1.2e\n',sum(x)*0.01);

x = t;
x = abs(x);
%F^{-1} of sinc^(3 + 1) (Wolfralmalpha)
x = 1/12*(abs(x - 2).^3 - 4*abs(x - 1).^3 + 3*(x - 2).*x.^2 + 4);

plot(t,x,'--');
fprintf('Integral of Spline basis %1.2e\n',sum(x)*0.01);


%% degree n = 4
x = -2.5:0.01:2.5;
x = 1/48*((x - 5/2).^4.*(-sign(x - 5/2)) + 5*(x - 3/2).^4.*sign(x - 3/2)...
    - 10*(x - 1/2).^4.*sign(x - 1/2) + 10*(x + 1/2).^4.*sign(x + 1/2) ...
    - 5*(x + 3/2).^4.*sign(x + 3/2) + (x + 5/2).^4.*sign(x + 5/2));
figure;
plot(x,'--');
fprintf('Integral of Spline basis %1.2e\n',sum(x)*0.01);
