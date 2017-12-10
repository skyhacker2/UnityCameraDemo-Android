package io.github.skyhacker2.camera;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.io.IOException;

/**
 * Created by eleven on 2017/12/10.
 */

public class CameraPlugin implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = CameraPlugin.class.getSimpleName();

    private SurfaceTexture mSurfaceTexture;
    private Texture2DExt mTexture2DExt;
    private Texture2D mUnityTexture;
    private FBO mFBO;
    private float[] mMVPMatrix = new float[16];
    private boolean mFrameUpdated;          // 帧是否有更新

    private Camera mCamera;

    public void openCamera() {
        Log.d(TAG, "openCamera");
        mFrameUpdated = false;
        mMVPMatrix = new float[16];

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }

        mCamera = Camera.open();

        mTexture2DExt = new Texture2DExt(UnityPlayer.currentActivity, 0,0);
        mSurfaceTexture = new SurfaceTexture(mTexture2DExt.getTextureID());
        mSurfaceTexture.setOnFrameAvailableListener(this);
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();

    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public boolean isFrameUpdated() {
        return mFrameUpdated;
    }

    public int getWidth() {
        return mCamera.getParameters().getPreviewSize().width;
    }

    public int getHeight() {
        return mCamera.getParameters().getPreviewSize().height;
    }

    public int updateTexture() {
        synchronized (this) {
            if (mFrameUpdated) {
                mFrameUpdated = false;
            }
            mSurfaceTexture.updateTexImage();
//            mFrameUpdated = false;
            int width = mCamera.getParameters().getPreviewSize().width;
            int height = mCamera.getParameters().getPreviewSize().height;
            if (mUnityTexture == null) {
                Log.d(TAG, "width " + width + " height " + height);
                mUnityTexture = new Texture2D(UnityPlayer.currentActivity, width, height);
                mFBO = new FBO(mUnityTexture);
            }

            Matrix.setIdentityM(mMVPMatrix, 0);
            mFBO.FBOBegin();
            GLES20.glViewport(0, 0, width , height);
            mTexture2DExt.draw(mMVPMatrix);
            mFBO.FBOEnd();
            Point size = new Point();
            if (Build.VERSION.SDK_INT >= 17) {
                UnityPlayer.currentActivity.getWindowManager().getDefaultDisplay().getRealSize(size);
            } else {
                UnityPlayer.currentActivity.getWindowManager().getDefaultDisplay().getSize(size);
            }
//                Log.d(TAG, "size: " + size.x + " " + size.y);
            GLES20.glViewport(0,0,size.x,size.y);

            return mUnityTexture.getTextureID();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mFrameUpdated = true;
        Log.d(TAG, "onFrameAvailable");
    }
}
