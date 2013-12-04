package edu.northwestern.cbits.intellicare.conductor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class AvatarActivity extends ConsentedActivity 
{
	// Used to handle pause and resume...
	private static AvatarActivity master = null;

	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer fb = null;
	private World world = null;
	private RGBColor back = new RGBColor(50, 50, 100);

	private float touchTurn = 0;
	private float touchTurnUp = 0;

	private float xpos = -1;
	private float ypos = -1;

	private Object3D cube = null;

	private Light sun = null;

	protected void onCreate(Bundle savedInstanceState) 
	{
		if (master != null) 
		{
			copy(master);
		}

		super.onCreate(savedInstanceState);
		mGLView = new GLSurfaceView(getApplication());

		mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() 
		{
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) 
			{
				int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] result = new int[1];
				egl.eglChooseConfig(display, attributes, configs, 1, result);
				return configs[0];
			}
		});

		renderer = new MyRenderer();
		mGLView.setRenderer(renderer);
		setContentView(mGLView);
	}

	protected void onPause() 
	{
		super.onPause();
	
		mGLView.onPause();
	}

	protected void onResume() 
	{
		super.onResume();

		mGLView.onResume();
	}

	protected void onStop() 
	{
		super.onStop();
	}

	private void copy(Object src) 
	{
		try 
		{
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			
			for (Field f : fs) 
			{
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
	}

	public boolean onTouchEvent(MotionEvent me) 
	{
		switch(me.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				xpos = me.getX();
				ypos = me.getY();
				
				return true;
			case MotionEvent.ACTION_UP:
				xpos = -1;
				ypos = -1;
				touchTurn = 0;
				touchTurnUp = 0;
				
				return true;
			case MotionEvent.ACTION_MOVE:
				float xd = me.getX() - xpos;
				float yd = me.getY() - ypos;

				xpos = me.getX();
				ypos = me.getY();

				touchTurn = xd / -100f;
				touchTurnUp = yd * 2; // / -100f;

				return true;
		}

		try
		{
			Thread.sleep(15);
		}
		catch (Exception e) 
		{
			// No need for this...
		}

		return super.onTouchEvent(me);
	}

	protected boolean isFullscreenOpaque() 
	{
		return true;
	}

	class MyRenderer implements GLSurfaceView.Renderer 
	{
		public MyRenderer() 
		{

		}

		public void onSurfaceChanged(GL10 gl, int w, int h) 
		{
			if (fb != null)
				fb.dispose();

			fb = new FrameBuffer(gl, w, h);

			if (master == null) 
			{
				world = new World();
				world.setAmbientLight(200, 200, 200);

				sun = new Light(world);
				sun.setIntensity(250, 250, 250);

				try 
				{
					cube = this.loadModel((float) 0.55);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}

				cube.build();

				world.addObject(cube);

				Camera cam = world.getCamera();
				cam.moveCamera(Camera.CAMERA_MOVEOUT, 500);
				cam.moveCamera(Camera.CAMERA_MOVEUP, 250);
				cam.lookAt(cube.getTransformedCenter());

				SimpleVector sv = new SimpleVector();
				sv.set(cube.getTransformedCenter());
				sv.y -= 500;
				sv.z -= 500;
				sun.setPosition(sv); 
				
				MemoryHelper.compact();

				if (master == null) 
				{
					Logger.log("Saving master Activity!");
					master = AvatarActivity.this;
				}
			}
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) 
		{

		}

		public void onDrawFrame(GL10 gl) 
		{
			if (touchTurn != 0) 
			{
				cube.rotateY(touchTurn);
				touchTurn = 0;
			}
			
			cube.translate(0, touchTurnUp, 0);

			Camera cam = world.getCamera();
			cam.lookAt(cube.getTransformedCenter());

			fb.clear(back);
			world.renderScene(fb);
			world.draw(fb);
			fb.display();
		}
		
		private Object3D loadModel(float scale) throws IOException 
	    {
	 	   InputStream objStream = AvatarActivity.this.getAssets().open("Android.obj");
	 	   InputStream mtlStream = AvatarActivity.this.getAssets().open("Android.mtl");
	 	   

	 	   Object3D[] model = Loader.loadOBJ(objStream, mtlStream, scale);
	 	   Object3D o3d = new Object3D(0);
	         

	 	   Object3D temp = null;
	       
	 	   for (int i = 0; i < model.length; i++) 
	 	   {
	 		   temp = model[i];
	 		   temp.setCenter(SimpleVector.ORIGIN);
	 		   temp.rotateX((float)( Math.PI));
	 		   temp.rotateMesh();
	 		   temp.setRotationMatrix(new Matrix());
	 		   o3d = Object3D.mergeObjects(o3d, temp);
	 		   o3d.build();
	 	   }

	 	   return o3d;
	    }
	}
}
