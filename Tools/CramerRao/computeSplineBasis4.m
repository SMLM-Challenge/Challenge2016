function x = computeSplineBasis4(x)

x = 1/48*((x - 5/2).^4.*(-sign(x - 5/2)) + 5*(x - 3/2).^4.*sign(x - 3/2)...
    - 10*(x - 1/2).^4.*sign(x - 1/2) + 10*(x + 1/2).^4.*sign(x + 1/2) ...
    - 5*(x + 3/2).^4.*sign(x + 3/2) + (x + 5/2).^4.*sign(x + 5/2));

end