package smlms.simulation;

public class DownsamplingModule {

	public float[][] run(float[][] image, int downsampling) {
		int mx = image.length;
		int my = image[0].length;
		int nx = mx / downsampling;
		int ny = my / downsampling;
		float[][] camera = new float[nx][ny];
		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++) {
			float sum = 0f;
			for(int i=0; i<downsampling; i++)
			for(int j=0; j<downsampling; j++) {
				sum += image[x*downsampling+i][ y*downsampling+j];
			}
			camera[x][y] = sum;
		}
		return camera;
	}
}
