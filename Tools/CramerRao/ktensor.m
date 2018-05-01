function GR = ktensor(gr)
if length(gr)==2
    GR = kron(gr{1}, gr{2}');
else
    GRXY = kron(gr{1}, gr{2}');
    GR = arrayfun(@(z) z*GRXY,gr{3},'UniformOutput',false);
    GR = cat(3,GR{:});
end