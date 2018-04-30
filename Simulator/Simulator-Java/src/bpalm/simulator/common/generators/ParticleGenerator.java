package bpalm.simulator.common.generators;

import java.util.Random;

import bpalm.simulator.Particle;

/**
 * A particle generator, as the name indicates, generates a particle according to a particular
 * pattern. The particle is generated each time a "nextParticle" is called.  
 * 
 * This default implementation generates a new particle with random coordinates between 0 and 1, but
 * other implementations are possible by calling the appropriate factory method.
 * 
 * @author Thomas Pengo, Laboratory of Experimental Biophysics, EPFL
 *
 */
abstract public class ParticleGenerator {
	
	double minX, maxX, minY, maxY, minZ, maxZ;
	Random randomGenerator = new Random();
	Particle template = new Particle();

	public Particle getTemplate() {
		return template;
	}

	public void setTemplate(Particle template) {
		this.template = template;
	}

	double scaleX=1, scaleY=1, scaleZ=1;
	double offX=0, offY=0, offZ=0;
	
	public static ParticleGenerator newRandomParticleGenerator() {
		return new UniformParticleGenerator();
	}

	public void setRandomGenerator(Random generator) {
		this.randomGenerator = generator;
	}

	public abstract Particle nextParticle();
	
	public Particle nextTraslatedParticle() {
		return nextParticle().scale(scaleX, scaleY, scaleZ).offset(offX, offY, offZ);
	}

	public void setScale(double d, double e, double f) {
		scaleX = d; scaleY = e; scaleZ = f;
	}
	
	public void setOffset(double d, double e, double f) {
		offX = d; offY = e; offZ = f;
	}
}
