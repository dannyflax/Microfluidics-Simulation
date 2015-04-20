package flaxapps;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_F1;
import static java.awt.event.KeyEvent.VK_G;
import static java.awt.event.KeyEvent.VK_J;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_U;
import static java.awt.event.KeyEvent.VK_W;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_ONE;
import static javax.media.opengl.GL.GL_SRC_ALPHA;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.jogamp.opengl.util.FPSAnimator;

/**
 * @author Danny Flax
 */

public class MicroSimulation implements GLEventListener, KeyListener {

	private static String TITLE = "Team X-5 Microfluidics Experiment Simulation";
	private static final int CANVAS_WIDTH = 640; // width of the drawable
	private static final int CANVAS_HEIGHT = 700; // height of the drawable
	private static final int FPS = 100; // animator's target frames per second
	final static JFrame frame = new JFrame();
	
	Queue<YeastData> yeastQueue = new ArrayDeque<YeastData>();
	Queue<YeastData> shearingQueue = new ArrayDeque<YeastData>();
	Queue<WaterData> waterQueue = new ArrayDeque<WaterData>();
	
	ModelControl top;
	ModelControl tower;
	ModelControl yeast;
	ModelControl water;
	ModelControl syringe;
	
	public static JPanel mainPanel;
	public static GraphPanel gPanel;
	
	ChartPanel graph;
	
	int woodTexture;
	int chipTexture;
	int yeastTexture;
	int waterTexture;
	int saltWaterTexture;
	int plasticTexture;

	public boolean controlled = true;
	public boolean inSim = false;
	public boolean salt = false;
	
	boolean up = false;
	boolean left = false;
	boolean right = false;
	boolean down = false;
	
	boolean U = false;
	boolean J = false;
	

	private GLU glu; // for the GL Utility
	
	// The world
	Point c_mpos;
	Point p_mpos;

	public float lookUpMax = (float) -80.0;
	public float lookUpMin = (float) 80.0;

	float[] cOffset = { 0.0f, 0.0f, 0.0f, 1.0f };

	Shader_Manager sm = new Shader_Manager();

	int shader1;
	int particleShader;
	
	// x and z position of the player, y is 0
	public float posX = 3.2f;
	public float posZ = 22.6f;
	public float posY = 7;

	public float headingY = 0; // heading of player, about y-axis
	public float lookUpAngle = 0.0f;

	private float moveIncrement = .1f;
	// private float turnIncrement = 1.5f; // each turn in degree

	static GLCanvas canvas;

	/** The entry main() method */
	public static void main(String[] args) {
		// Create the OpenGL rendering canvas
		canvas = new GLCanvas(); // heavy-weight GLCanvas

		canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		MicroSimulation renderer = new MicroSimulation();
		canvas.addGLEventListener(renderer);

		// For Handling KeyEvents
		canvas.addKeyListener(renderer);
		canvas.setFocusable(true);

		canvas.requestFocus();

		// Create a animator that drives canvas' display() at the specified FPS.
		final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

		// Create the top-level container frame
		// Swing's JFrame or AWT's Frame
//		frame.setUndecorated(true);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		mainPanel.add(canvas);
		
		gPanel = new GraphPanel();
		
		gPanel.addKeyListener(renderer);
		
		mainPanel.add(gPanel);
		
		frame.getContentPane().add(mainPanel);
		
		
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Use a dedicate thread to run the stop() to ensure that the
				// animator stops before program exits.
				new Thread() {
					@Override
					public void run() {
						animator.stop(); // stop the animator loop
						System.exit(0);
					}
				}.start();
			}
		});
		frame.setTitle(TITLE);
		frame.pack();

		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		frame.setVisible(true);
		animator.start(); // start the animation loop
	}

	// ------ Implement methods declared in GLEventListener ------

	@Override
	public void init(GLAutoDrawable drawable) {
		
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL graphics context
		
		glu = new GLU(); // get GL Utilities
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
		gl.glClearDepth(1.0f); // set clear depth value to farthest
		gl.glEnable(GL_DEPTH_TEST); // enables depth testing
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best
															// perspective
																// correction
		gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out
									// lighting
	
		// Read the world
		try {
			shader1 = sm.init("/spider", gl);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// Read the world
		try {
			particleShader = sm.init("/particle", gl);
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	
		// Blending control
		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f); // Brightness with alpha
		// Blending function For translucency based On source alpha value
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE);
		
		
		top = new ModelControl();
		tower = new ModelControl();
		syringe = new ModelControl();
		yeast = new ModelControl();
		water = new ModelControl();
		
		try {
			top.loadModelData("/CustomChipTextured.obj");
		} catch (IOException ex) {
			Logger.getLogger(MicroSimulation.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		
		try {
			syringe.loadModelData("/S+T_Textured.obj");
		} catch (IOException ex) {
			Logger.getLogger(MicroSimulation.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		
		try {
			tower.loadModelData("/tower_textured_final.obj");
		} catch (IOException ex) {
			Logger.getLogger(MicroSimulation.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		
		try {
			yeast.loadModelData("/YeastTextured2.obj");
		} catch (IOException ex) {
			Logger.getLogger(MicroSimulation.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		
		try {
			water.loadModelData("/WaterTexturedSheet.obj");
		} catch (IOException ex) {
			Logger.getLogger(MicroSimulation.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		
		woodTexture = setUp2DText(gl, "/Wood.jpg");
		chipTexture = setUp2DText(gl, "/chip_texture.png");
		yeastTexture = setUp2DText(gl, "/YeastTexture.png");
		waterTexture = setUp2DText(gl, "/WaterTexture.jpg");
		saltWaterTexture = setUp2DText(gl, "/SaltWaterTexture.png");
		plasticTexture = setUp2DText(gl, "/Plastic.jpeg");
		
		Robot r = null;
		try {
			r = new Robot();
		} catch (AWTException e2) {
			e2.printStackTrace();
		}
		if (r != null) {
			r.mouseMove(frame.getWidth() / 2, frame.getHeight() / 2);
		}
	
		BufferedImage cursorImg = new BufferedImage(16, 16,
				BufferedImage.TYPE_INT_ARGB);
	
		// Create a new blank cursor.
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				cursorImg, new Point(0, 0), "blank cursor");
		frame.setCursor(blankCursor);
		p_mpos = MouseInfo.getPointerInfo().getLocation();
		
		gPanel.originalWidth = gPanel.graph.getWidth();
		gPanel.originalHeight = CANVAS_HEIGHT;
		
		canvas.setSize(CANVAS_WIDTH+gPanel.getWidth(),CANVAS_HEIGHT);
		gPanel.hideGraph();
	}

	float simX = 10.4f, simY = 5.32f, simZ = -.07f;
	
	
	@Override
	public void display(GLAutoDrawable drawable) {
		
		if(!inSim){
			/**
			 * Initial control logic
			 */
			if (up) {
				posX -= (float) Math.sin(Math.toRadians(headingY)) * moveIncrement;
				posZ -= (float) Math.cos(Math.toRadians(headingY)) * moveIncrement;
			}
			if (down) {
				// Player move out, posX and posZ become bigger
				posX += (float) Math.sin(Math.toRadians(headingY)) * moveIncrement;
				posZ += (float) Math.cos(Math.toRadians(headingY)) * moveIncrement;
			}
			if (left) {
				// Player move out, posX and posZ become bigger
				posX -= (float) Math.sin(Math.toRadians(headingY + 90.0))
						* moveIncrement;
				posZ -= (float) Math.cos(Math.toRadians(headingY + 90.0))
						* moveIncrement;
			}
			if (right) {
				// Player move out, posX and posZ become bigger
				posX -= (float) Math.sin(Math.toRadians(headingY - 90.0))
						* moveIncrement;
				posZ -= (float) Math.cos(Math.toRadians(headingY - 90.0))
						* moveIncrement;
			}
			
			if (U){
				posY += moveIncrement;
			}
			
			if (J){
				posY -= moveIncrement;
			}
		
			if (controlled) {
				c_mpos = MouseInfo.getPointerInfo().getLocation();
				int xdif = c_mpos.x - p_mpos.x;
				int ydif = c_mpos.y - p_mpos.y;
				lookUpAngle += (ydif / 5.0);
				
				if ((lookUpAngle <= lookUpMax || lookUpAngle >= lookUpMin)) {
					lookUpAngle -= (ydif / 5.0);
				}
		
				headingY -= (xdif / 5.0);
		
				Robot r = null;
				try {
					r = new Robot();
				} catch (AWTException e2) {
					e2.printStackTrace();
				}
				if (r != null) {
					r.mouseMove(frame.getWidth() / 2, frame.getHeight() / 2);
				}
			}
		
		}
		else{
			salt = gPanel.salt;
			
			if(gPanel.resets){
				gPanel.resets = false;
				
				float yBounds = .0446f;
				float xBounds = .0115f;
				
				generateWater(3, .05f);
				generateYeast(500, simX - .002f + xBounds, simX - .002f - xBounds, simZ + yBounds, simZ - yBounds);
			}
			
			setYeastPercentage(gPanel.percentYeast);
			
//			float inc = 0.01f;
//			if (up) {
//				simY+=inc;
//			}
//			if (down) {
//				simY-=inc;
//			}
//			if (left) {
//				simX-=inc;
//			}
//			if (right) {
//				simX+=inc;
//			}
		}
		
		/**
		 * Drawing code
		 */
	
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color
																// and depth
																// buffers
		gl.glLoadIdentity(); // reset the model-view matrix
		gl.glEnable(GL.GL_TEXTURE_2D);
		
		/** Initial camera adjustment code **/
		
		
		
		if(!inSim){
			// Rotate up and down to look up and down
			gl.glRotatef(lookUpAngle, 1.0f, 0, 0);
			// Player at headingY. Rotate the scene by -headingY instead (add 360 to
			// get a
			// positive angle)
			gl.glRotatef(360.0f - headingY, 0, 1.0f, 0);
			
			gl.glTranslatef(-posX, -posY, -posZ);
		}
		else{
			// Rotate up and down to look up and down
			gl.glRotatef(90.0f, 1.0f, 0, 0);
			// Player at headingY. Rotate the scene by -headingY instead (add 360 to
			// get a
			// positive angle)
			gl.glRotatef(360.0f, 0, 1.0f, 0);
			
			gl.glTranslatef(-simX, -simY, -simZ);
		}
		
		int txt1 = gl.glGetUniformLocation(shader1, "mainTexture");
		gl.glUniform1f(txt1, woodTexture);
		
		
		gl.glUseProgram(shader1);
		
		gl.glBindTexture(GL.GL_TEXTURE_2D, chipTexture);
		top.drawModel(new flaxapps.Vertex(10.0f,5.0f,0.0f), gl, 0.0f);

		if(!inSim){
			gl.glBindTexture(GL.GL_TEXTURE_2D, woodTexture);
			tower.drawModel(new flaxapps.Vertex(-9.0f,5.0f, 8.5f), gl, 90.0f, 10.0f);
			
			
//			gl.glBindTexture(GL.GL_TEXTURE_2D, plasticTexture);
//			syringe.drawModel(new flaxapps.Vertex(1.8f,35.0f, -4.0f), gl, 45.0f + 180.0f, 35.0f);
		}
		else{
			gl.glUseProgram(particleShader);
			
			int lightU = gl.glGetUniformLocation(particleShader, "light");
			gl.glUniform3f(lightU, .25f, 1.0f, -.55f);
			
			gl.glBindTexture(GL.GL_TEXTURE_2D, yeastTexture);
			
			float s = .11f;
			float prevSim = 5.32f;
			for(YeastData d : yeastQueue){
				yeast.drawModel(new flaxapps.Vertex(d.x,prevSim - s,d.y), gl, 0.0f, .002f);
			}
			
			for(YeastData d : shearingQueue){
				yeast.drawModel(new flaxapps.Vertex(d.x,prevSim - s,d.y), gl, 0.0f, .002f);
				d.y+=0.001;
				
				if(d.y > .04){
//					shearingQueue.remove(d);
				}
			}
			
			gl.glUniform3f(lightU, 0.0f, 1.0f, 0.0f);
			
			if(!salt){
				gl.glBindTexture(GL.GL_TEXTURE_2D, waterTexture);
			}
			else{
				gl.glBindTexture(GL.GL_TEXTURE_2D, saltWaterTexture);
			}
			
			Queue<WaterData> tmp = new ArrayDeque<WaterData>();
			while(waterQueue.size() > 0){
				WaterData w = waterQueue.remove();
				
				float wi = .023f;
				float num = 16.0f;
				
				water.drawModel(new flaxapps.Vertex(simX - wi/2.0f + wi/num,prevSim - s - .028f,simZ + w.y), gl, 0.0f, .025f);
				
				w.y += .001;
				
				if(w.y < .075){
					tmp.add(w);
				}
				else{
					tmp.add(new WaterData(-.073f));
				}
			}
			
			waterQueue.addAll(tmp);
			
		}
		
		
		/** Cleanup code **/
		
//		gl.glDisable(GL.GL_BLEND);
		gl.glFlush();
		gl.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA,
				GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_COLOR);
		
	}

	/**
	 * Call-back handler for window re-size event. Also called when the drawable
	 * is first set to visible.
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context
		
		if (height == 0)
			height = 1; // prevent divide by zero
		float aspect = (float) width / height;
	
		// Set the view port (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);
	
		// Setup perspective projection, with aspect ratio matches viewport
		gl.glMatrixMode(GL_PROJECTION); // choose projection matrix
		gl.glLoadIdentity(); // reset projection matrix
		glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear,
														// zFar
	
		// Enable the model-view transform
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity(); // reset
		
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	class im {
		public ByteBuffer b;
		public int wi;
		public int he;
	}

	/**
	 * Called back before the OpenGL context is destroyed. Release resource such
	 * as buffers.
	 */
	
	public im makeImg(String txt) {
		BufferedImage bufferedImage = null;
		int w = 0;
		int h = 0;
		URL u = MicroSimulation.class.getResource(txt);
		try {
			bufferedImage = ImageIO.read(u.openStream());
			w = bufferedImage.getWidth();
			h = bufferedImage.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		WritableRaster raster = Raster.createInterleavedRaster(
				DataBuffer.TYPE_BYTE, w, h, 4, null);
		ComponentColorModel colorModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8,
						8, 8 }, true, false, ComponentColorModel.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);
		BufferedImage dukeImg = new BufferedImage(colorModel, raster, false,
				null);
	
		Graphics2D g = dukeImg.createGraphics();
		g.drawImage(bufferedImage, null, null);
		DataBufferByte dukeBuf = (DataBufferByte) raster.getDataBuffer();
		byte[] dukeRGBA = dukeBuf.getData();
		ByteBuffer bb = ByteBuffer.wrap(dukeRGBA);
		bb.position(0);
		bb.mark();
		im i = new im();
		i.b = bb;
		i.he = h;
		i.wi = w;
		return i;
	}

	
	
	int uniqueID = 4;
	
	/**
	 * 
	 * @param gl
	 * 		The GL context on which we setup the texture
	 * @param txt
	 * 		The name of the image file from which to build the texture
	 * @return
	 * 		The integer ID pointing to the texture
	 * @ensures
	 * 		Image at location @{code txt} becomes a texture loaded in context @{code gl}
	 * 		with a unique ID
	 */
	public int setUp2DText(GL2 gl, String txt) {
		im mud = this.makeImg(txt);
	
		gl.glBindTexture(GL.GL_TEXTURE_2D, uniqueID);
		
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL.GL_RGBA, mud.wi, mud.he, 0,
				GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, mud.b);
	
		// Use nearer filter if image is larger than the original texture
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_NEAREST);
		
		// Use nearer filter if image is smaller than the original texture
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
				GL.GL_NEAREST);
		
		// For texture coordinates more than 1, set to wrap mode to GL_REPEAT
		// for
		// both S and T axes (default setting is GL_CLAMP)
		
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_LINEAR);
		
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_LINEAR);
		
		uniqueID++;
		
		return uniqueID-1;
	}

	/**
	 * Called back immediately after the OpenGL context is initialized. Can be
	 * used to perform one-time initialization. Run only once.
	 */

	public static BufferedImage componentToImage(Component component,
			Rectangle region) throws IOException {
		BufferedImage img = new BufferedImage(component.getWidth(),
				component.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(component.getForeground());
		g.setFont(component.getFont());
		component.paintAll(g);
		if (region == null) {
			region = new Rectangle(0, 0, img.getWidth(), img.getHeight());
		}
		return img.getSubimage(region.x, region.y, region.width, region.height);
	}

	/**
	 * Called back by the animator to perform rendering.
	 */

	class AABB {
		flaxapps.Vertex c; // center point
		float[] r; // halfwidths

		public AABB(float[] ar, flaxapps.Vertex ac) {
			c = ac;
			r = ar;
		}

	};

	public boolean testAABBAABB(AABB a, AABB b) {
		if (Math.abs(a.c.x - b.c.x) > (a.r[0] + b.r[0]))
			return false;
		if (Math.abs(a.c.y - b.c.y) > (a.r[1] + b.r[1]))
			return false;
		if (Math.abs(a.c.z - b.c.z) > (a.r[2] + b.r[2]))
			return false;
		return true;
	}


	public void captureScreen(String fileName) throws Exception {
		/*
		 * Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		 * Rectangle screenRectangle = new Rectangle(screenSize); Robot robot =
		 * new Robot(); Component component = canvas;
		 * 
		 * BufferedImage img = new BufferedImage(component.getWidth(),
		 * component.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE); Graphics2D g
		 * = (Graphics2D) img.getGraphics();
		 * g.setColor(component.getForeground());
		 * g.setFont(component.getFont());
		 * 
		 * 
		 * ImageIO.write(img,"png",new File(fileName));
		 */
	}

	
	public void restoreControl(){
		frame.setCursor(Cursor.getDefaultCursor());

		Robot r = null;
		try {
			r = new Robot();
		} catch (AWTException e2) {
			
			e2.printStackTrace();
		}
		if (r != null) {

			r.mouseMove(frame.getWidth() / 2, frame.getHeight() / 2);

		}

		p_mpos = MouseInfo.getPointerInfo().getLocation();

		controlled = false;
	}
	
	public void stripControl(){
		BufferedImage cursorImg = new BufferedImage(16, 16,
				BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
		Cursor blankCursor = Toolkit.getDefaultToolkit()
				.createCustomCursor(cursorImg, new Point(0, 0),
						"blank cursor");
		frame.setCursor(blankCursor);
		controlled = true;
	}
	
	public void setYeastPercentage(float percentage){
		int num = (int) (500.0 * percentage/100.0);
		if(yeastQueue.size() > num){
			removeYeast(yeastQueue.size() - num);
			gPanel.setYeastLabel(percentage);
		}
	}
	
	public void removeYeast(int num){
		for(int i = 0; i < num; i++){
			if(yeastQueue.size() > 0){
				YeastData y = yeastQueue.remove();
				y.isDrifting = true;
				shearingQueue.add(y);
			}
		}
	}
	
	public void generateYeast(int num, float xMax, float xMin, float yMax, float yMin){
		shearingQueue.clear();
		yeastQueue.clear();
		Random r = new Random();
		for(int i = 0; i < num; i++){
			float x = (xMax - xMin)*r.nextFloat() + xMin;
			float y = (yMax - yMin)*r.nextFloat() + yMin;
			yeastQueue.add(new YeastData(x,y));
		}
	}
	
	public void generateWater(int num, float dif){
		for(int i = 0; i < num; i++){
			float y = 0 - dif*(float)i;
			waterQueue.add(new WaterData(y));
		}	
	}
	
	

	// ----- Implement methods declared in KeyListener -----

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		
		case VK_O:
			if(yeastQueue.size() > 0){
				YeastData y = yeastQueue.remove();
				y.isDrifting = true;
				shearingQueue.add(y);
			}
			break;
		case VK_SPACE:
			if(inSim){
				System.out.println("X: "+simX+", Y: "+simY+", Z: "+simZ);
			}
			else{
				System.out.println("X: "+posX+", Y: "+posY+", Z: "+posZ);
			}
			break;
		case VK_F1:
			if (controlled) {
				restoreControl();
			} else {
				stripControl();
			}

			break;
		case VK_ESCAPE:
			frame.dispose();
			System.exit(0);	
			break;

		case VK_G:
			if(!inSim){
				float yBounds = .0446f;
				float xBounds = .0115f;
				generateWater(3, .05f);
				generateYeast(500, simX - .002f + xBounds, simX - .002f - xBounds, simZ + yBounds, simZ - yBounds);
				gPanel.showGraph();
				canvas.setSize(CANVAS_WIDTH,CANVAS_HEIGHT);
				restoreControl();
			}
			else{
				gPanel.hideGraph();
				canvas.setSize(CANVAS_WIDTH + gPanel.originalWidth, CANVAS_HEIGHT);
				stripControl();
			}
			inSim = !inSim;
			break;
			
		case VK_W:
			up = true;
			break;
		case VK_S:
			down = true;
			break;
		case VK_D:
			right = true;
			break;
		case VK_A:
			left = true;
			break;
			
		case VK_U:
			U = true;
			break;
			
		case VK_J:
			J = true;
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case VK_W:
			up = false;
			break;
		case VK_S:
			down = false;
			break;
		case VK_D:
			right = false;
			break;
		case VK_A:
			left = false;
			break;
		case VK_U:
			U = false;
			break;
		case VK_J:
			J = false;
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()) {

		}
	}

	
	
	
	

	// A sector comprises many triangles (inner class)
	class Sector {
		Triangle[] triangles;

		// Constructor
		public Sector(int numTriangles) {
			triangles = new Triangle[numTriangles];
			for (int i = 0; i < numTriangles; i++) {
				triangles[i] = new Triangle();
			}
		}
	}

	// A triangle has 3 vertices (inner class)
	class Triangle {
		Vertex[] vertices = new Vertex[3];

		public Triangle() {
			vertices[0] = new Vertex();
			vertices[1] = new Vertex();
			vertices[2] = new Vertex();
		}
	}

	// A vertex has xyz (location) and uv (for texture) (inner class)
	class Vertex {
		float x, y, z; // 3D x,y,z location
		float u, v; // 2D texture coordinates

		public String toString() {
			return "(" + x + "," + y + "," + z + ")" + "(" + u + "," + v + ")";
		}
	}
}

class WaterData{
	public float y;
	public WaterData(float ty){
		y = ty;
	}
}

class YeastData{
	public float x;
	public float y;
	public boolean isDrifting = false;
	public YeastData(float tx, float ty){
		x = tx;
		y = ty;
	}
}

class GraphPanel extends JPanel implements ChangeListener, ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	ChartPanel graph;
	public int originalWidth = 0;
	public int originalHeight = 0;
	
	public float percentYeast = 100.0f;
	
	public int columnHeight = 15;
	public boolean salt = false;
	public boolean resets = false;
	
	JButton changeButton;
	JSlider slider;
	
	JLabel heightLabel;
	JLabel yeastLabel;
	
	public GraphPanel(){
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		addGraph();
	}
	
	public void hideGraph(){
		setSize(0,0);
	}
	
	public void showGraph(){
		setSize(originalWidth,originalHeight);
	}
	
	public void addGraph(){
		graph = new ChartPanel(createChart(createDataset()));
		add(graph);
		
		int FPS_MIN = 210;
		int FPS_MAX = 280;
		int FPS_INIT = 210;    //initial frames per second
		
		add(Box.createRigidArea(new Dimension(0, 40)));
		
		add(new JLabel("Water Column Height"));
		
		JPanel sliderPanel = new JPanel();
		
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));
		
		sliderPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		
		slider = new JSlider(JSlider.HORIZONTAL,
		                                      FPS_MIN, FPS_MAX, FPS_INIT);

		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		
		for(int i = 210; i <=280; i = i + 10){
			labelTable.put( new Integer( i ), new JLabel("" + (float)i/10.0) );
		}
		
		
		slider.setLabelTable( labelTable );
		
		//Turn on labels at major tick marks.
		slider.setMajorTickSpacing(1);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		
		Font font = new Font("Serif", Font.ITALIC, 15);
		slider.setFont(font);
		
		slider.addChangeListener(this);
		
		sliderPanel.add(slider, BorderLayout.CENTER);
		
		sliderPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		
		
		add(sliderPanel);
		
		changeButton = new JButton("Add Salt");
		JButton rBut = new JButton("Reset");
		
		JPanel checkPanel = new JPanel();
		checkPanel.add(changeButton, BorderLayout.CENTER);
		checkPanel.add(rBut, BorderLayout.CENTER);
		add(checkPanel);
		
		yeastLabel = new JLabel();
		setYeastLabel(100.0f);
		heightLabel = new JLabel("Column Height: 21.0 cm");
		
		add(yeastLabel);
		add(heightLabel);
		
		add(Box.createRigidArea(new Dimension(0, 50)));
		
		changeButton.addActionListener(this);
		rBut.addActionListener(this);
	}
	
	public void changeGraph(){
		graph.setChart(createChart(createDataset()));
	}
	
	/**
     * Creates a sample dataset.
     * 
     * @return a sample dataset.
     */
    private static XYDataset createDataset() {
        
        final XYSeries series1 = new XYSeries("Control");
        float factor = -.23f;
        float base = 6.4f;
        series1.add(21, 100.0);
        series1.add(22, 100.0);
        series1.add(23, 100.0);
        series1.add(23.478, 100.0);
        series1.add(24.0, 100.0*(24.0*factor + base));
        series1.add(25.0, 100.0*(25.0*factor + base));
        series1.add(26.0, 100.0*(26.0*factor + base));
        series1.add(27.0, 100.0*(27.0*factor + base));
        series1.add(27.9, 0.0*(28.0*factor + base));
        series1.add(28.0, 0.0*(28.0*factor + base));
        
        
        
        final XYSeries series2 = new XYSeries("Salt");
        factor = -.23f;
        base = 6.4f;
        series2.add(21, 100.0);
        series2.add(22, 100.0);
        series2.add(23, 100.0);
        series2.add(23.478, 100.0);
        series2.add(24.0, 100.0*(24.0*factor + base));
        series2.add(25.0, 100.0*(25.0*factor + base));
        series2.add(26.0, 100.0*(26.0*factor + base));
        series2.add(27.0, 100.0*(27.0*factor + base));
        series2.add(27.9, 0.0*(28.0*factor + base));
        series2.add(28.0, 0.0*(28.0*factor + base));
        
        
       
        final XYSeriesCollection dataset = new XYSeriesCollection();
        
        dataset.addSeries(series1);
        dataset.addSeries(series2);
       
                
        return dataset;
        
    }
	
	
	/**
     * Creates a chart.
     * 
     * @param dataset  the data for the chart.
     * 
     * @return a chart.
     */
    private JFreeChart createChart(final XYDataset dataset) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
            "% Yeast Remaining vs. Water Column Height",      // chart title
            "Water Column Height (cm)",                      // x axis label
            "% Yeast Remaining",               		        // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

//        final StandardLegend legend = (StandardLegend) chart.getLegend();
  //      legend.setDisplaySeriesShapes(true);
        
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
    
        
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        renderer.setSeriesVisible(0, !salt);
        renderer.setSeriesVisible(1, salt);
        
        plot.setRenderer(renderer);

        
        XYItemRenderer renrer = plot.getRenderer();
        renrer.setSeriesPaint(0, Color.blue);
        
        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
          
        
        return chart;
        
    }

    public void setHeightLabel(float height){
    	heightLabel.setText("Column Height: " + height + " cm");
    }
    
    public void setYeastLabel(float percent){
    	if(percent < 0){
    		percent = 0;
    	}
    	yeastLabel.setText("Yeast Remaining: " + (int)percent + " %, " + (int)(5*percent) + " cells");
    }
    
	@Override
	public void stateChanged(ChangeEvent e) {
		float newValue = slider.getValue()/10.0f;
		
		setHeightLabel(newValue);
		
		if(newValue < 23.5){
			newValue = 100.0f;
		}
		else{
			newValue = 100.0f*(-.23f*newValue + 6.4f);
		}
		this.percentYeast = newValue;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String a = e.getActionCommand();
		if(a.equals("Add Salt")){
			addSalt();
			resets = true;
			slider.setValue(210);
			setYeastLabel(100);
			salt = true;
			changeGraph();
		}
		else if(a.equals("Return To Control")){
			returnToControl();
			resets = true;
			slider.setValue(210);
			setYeastLabel(100);
			salt = false;
			changeGraph();
		}
		else if(a.equals("Reset")){
			resets = true;
			slider.setValue(210);
			setYeastLabel(100);
		}
	}
	
	public void addSalt(){
		changeButton.setText("Return To Control");
		salt = true;
	}
	
	public void returnToControl(){
		changeButton.setText("Add Salt");
		salt = false;
	}
}


