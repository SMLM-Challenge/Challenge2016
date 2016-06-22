package bpalm.simulator.common.generators;

import bpalm.simulator.Particle;

public class FunctionParticleGenerator extends ParticleGenerator {

	Function theFunction;

	public FunctionParticleGenerator() {
		this(new SpiralFunction());
	}
	
	public FunctionParticleGenerator(Function theFunction) {
		this.theFunction = theFunction;
	}
	
	@Override
	public Particle nextParticle() {
		double rand = randomGenerator.nextDouble();

		Particle p = new Particle(template);
		
		p.x = theFunction.getX(rand);
		p.y = theFunction.getY(rand);
		p.z = theFunction.getZ(rand);
		p.t = randomGenerator.nextDouble();
		
		return p;
	}
}
