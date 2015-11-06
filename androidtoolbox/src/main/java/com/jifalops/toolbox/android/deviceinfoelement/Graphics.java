package com.jifalops.toolbox.android.deviceinfoelement;

import android.app.ActivityManager;
import android.content.Context;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// TODO use multiple egl contexts (1 gives different results than 2,
// even if still using GLES20 methods.

// TODO more values & limits
public class Graphics extends ActiveElement implements GLSurfaceView.Renderer {
	public static final float OPENGLES_VERSION_10 = 1.0f;
	public static final float OPENGLES_VERSION_11 = 1.1f;
	public static final float OPENGLES_VERSION_20 = 2.0f;

    private static final int ACTIVE_ACTIONS = 3;
    public static final int ACTION_CREATED = 0;
    public static final int ACTION_CHANGED = 1;
    public static final int ACTION_DRAW = 2;

	public interface Callbacks extends ActiveElement.Callbacks {
		/** Corresponds to GLSurfaceView.Renderer.onSurfaceCreated(); */
		void onSurfaceCreated(GL10 gl, EGLConfig config);
		/** Corresponds to GLSurfaceView.Renderer.onSurfaceChanged(); */
		void onSurfaceChanged(GL10 gl, int width, int height);
		/** Corresponds to GLSurfaceView.Renderer.onDrawFrame(); */
		void onDrawFrame(GL10 gl);
	}
	
	private final float mOpenGlesVersion;
	private OpenGles mOpenGles;
	private GLSurfaceView mGlSurfaceView;
	
	public Graphics(Context context, Callbacks callbacks) {
		super(context, callbacks);

		String ver = openGlesVersion(context);
		mOpenGlesVersion = ver == null ? 0.0f : Float.valueOf(ver);

        setActiveActionCount(ACTIVE_ACTIONS);
	}
	
	/** 
	 * Gets the highest supported OpenGL version as a string 
	 * representation of a floating point number.
	 */
	public static String openGlesVersion(Context context) {
		return ((ActivityManager) context
			.getSystemService(Context.ACTIVITY_SERVICE))
			.getDeviceConfigurationInfo().getGlEsVersion();
	}
	
	public OpenGles getOpenGles() {
		return mOpenGles;
	}
	
	public GLSurfaceView getGlSurfaceView() {
		return mGlSurfaceView;
	}

    public void setGlSurfaceView(GLSurfaceView glSurfaceView) {
        mGlSurfaceView = glSurfaceView;

        if (Build.VERSION.SDK_INT >= 8) {
            glSurfaceView.setEGLContextClientVersion((int) mOpenGlesVersion);
        }

        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.requestRender();
    }
	
	/** 
	 * Get the highest supported OpenGL ES version.
	 * This will be available immediately after instantiation
	 * whereas getOpenGles() will only be ready after the GLSurfaceView
	 * has been created.	 
	 */
	public float getOpenGlesVersion() {
		return mOpenGlesVersion;
	}
	
	
	public void onPause() {
        if (mGlSurfaceView != null) {
		    mGlSurfaceView.onPause();
        }
	}
	
	public void onResume() {
        if (mGlSurfaceView != null) {
            mGlSurfaceView.onResume();
        }
	}
	
	
	private abstract class OpenGles {
		protected final float mOpenGlesVersion;
		
		protected String mRenderer;
		protected String mVersion;
		protected String mVendor;
		protected int mMaxTextureSize;
				
		protected String[] mExtensions;
		
		public OpenGles() {
			mOpenGlesVersion = Graphics.this.mOpenGlesVersion;
		}
		
		public float getOpenGlesVersion() {
			return mOpenGlesVersion;
		}
		
		public String getRenderer() {
			return mRenderer;
		}
		
		public String getVersion() {
			return mVersion;
		}
		
		public String getVendor() {
			return mVendor;
		}
		
		public int getMaxTextureSize() {
			return mMaxTextureSize;
		}	

		public String[] getExtensions() {
			return mExtensions;
		}
		
		protected int getInt(int glConst) {
			int[] placeholder = {0};		
			if (mOpenGlesVersion == OPENGLES_VERSION_10) 
				GLES10.glGetIntegerv(glConst, placeholder, 0);
			else if (mOpenGlesVersion == OPENGLES_VERSION_11) 
				GLES11.glGetIntegerv(glConst, placeholder, 0);
			else if (mOpenGlesVersion == OPENGLES_VERSION_20) 
				GLES20.glGetIntegerv(glConst, placeholder, 0);
			return placeholder[0];
		}
	}
	
	private class OpenGles10 extends OpenGles {
		protected int mMaxTextureUnits;
		protected int mMaxTextureStackDepth;
		
		public OpenGles10() {
			super();
			mRenderer = GLES10.glGetString(GLES10.GL_RENDERER);
			mVersion = GLES10.glGetString(GLES10.GL_VERSION);
			mVendor = GLES10.glGetString(GLES10.GL_VENDOR);
			mMaxTextureSize = getInt(GLES10.GL_MAX_TEXTURE_SIZE);				
			mExtensions = GLES10.glGetString(GLES10.GL_EXTENSIONS).split(" ");
				
			mMaxTextureUnits = getInt(GLES10.GL_MAX_TEXTURE_UNITS);		
			mMaxTextureStackDepth = getInt(GLES10.GL_MAX_TEXTURE_STACK_DEPTH);
		}
		
		public int getMaxTextureUnits() {
			return mMaxTextureUnits;
		}
		
		public int getMaxTextureStackDepth() {
			return mMaxTextureStackDepth;
		}
	}
	
	private class OpenGles11 extends OpenGles10 {
		// same as parent
	}
	
	private class OpenGles20 extends OpenGles {
		protected int mMaxTextureImageUnits;
		protected int mMaxRenderBufferSize;

		public OpenGles20() {
			super();
			mRenderer = GLES20.glGetString(GLES20.GL_RENDERER);
			mVersion = GLES20.glGetString(GLES20.GL_VERSION);
			mVendor = GLES20.glGetString(GLES20.GL_VENDOR);
			mMaxTextureSize = getInt(GLES20.GL_MAX_TEXTURE_SIZE);							
			mExtensions = GLES20.glGetString(GLES20.GL_EXTENSIONS).split(" ");
			
			mMaxTextureImageUnits = getInt(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS);
			mMaxRenderBufferSize = getInt(GLES20.GL_MAX_RENDERBUFFER_SIZE);
		}
		
		public int getMaxTextureImageUnits() {
			return mMaxTextureImageUnits;
		}
		
		public int getMaxRenderBufferSize() {
			return mMaxRenderBufferSize;
		}
	}


	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (!isActionAllowed(ACTION_CREATED)) return;

		if (mOpenGles == null) {
			if (mOpenGlesVersion == OPENGLES_VERSION_10) mOpenGles = new OpenGles10();
			else if (mOpenGlesVersion == OPENGLES_VERSION_11) mOpenGles = new OpenGles11();
			else if (mOpenGlesVersion == OPENGLES_VERSION_20) mOpenGles = new OpenGles20();
		}

        setActionTime(ACTION_CREATED);
		((Callbacks) mCallbacks).onSurfaceCreated(gl, config);
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (!isActionAllowed(ACTION_CHANGED)) return;

        setActionTime(ACTION_CHANGED);
		((Callbacks) mCallbacks).onSurfaceChanged(gl, width, height);
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
        if (!isActionAllowed(ACTION_DRAW)) return;

        setActionTime(ACTION_DRAW);
		((Callbacks) mCallbacks).onDrawFrame(gl);
	}
	
	@Override
	public void start() {
		if (mIsActive) return;
		onResume();
		mIsActive = true;
	}
	
	@Override
	public void stop() {
		
		onPause();
		mIsActive = false;
	}
}
