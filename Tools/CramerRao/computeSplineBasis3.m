function x = computeSplineBasis3(x) %n = 3
x = abs(x);
%ind = (x >= 0 & x < 1);
%x(ind) = 2/3 - x(ind).^2 + x(ind).^3/2;
%ind = (x >= 1 & x < 2);
%x(ind) = (2 - x(ind)).^3/6;
%ind = (x >= 2);
%x(ind) = 0;

x = 1/12*(abs(x - 2).^3 - 4*abs(x - 1).^3 + 3*(x - 2).*x.^2 + 4);

end