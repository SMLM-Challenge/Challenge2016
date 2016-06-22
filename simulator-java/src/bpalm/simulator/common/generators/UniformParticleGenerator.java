package bpalm.simulator.common.generators;

import bpalm.simulator.Particle;

public class UniformParticleGenerator extends ParticleGenerator {
	
	public UniformParticleGenerator() {}
	
	@Override
	public Particle nextParticle() {
		Particle p = new Particle(template);
		
		p.x = randomGenerator.nextDouble();
		p.y = randomGenerator.nextDouble();
		p.z = randomGenerator.nextDouble();
		p.t = randomGenerator.nextDouble();
		
		return p;
	}

}
