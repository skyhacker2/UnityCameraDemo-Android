package io.github.skyhacker2.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import junit.framework.Assert;

/**
 * Created by eleven on 16/9/7.
 */
public class Texture2DExt extends Texture2D {

    private static final String TAG = Texture2DExt.class.getSimpleName();

    public Texture2DExt(Context context, int width, int height) {
        super(context, width, height);

        mContext = context;
        initVertex();
        initShader();
        createProgram();

        int[] temps = new int[1];
        GLES20.glGenTextures(1, temps, 0);
        mTextureID = temps[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
        Utils.checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

//        GLES20.glTexImage2D(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0, GLES20.GL_RGBA, width, height, 0,
//                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        mWidth = width;
        mHeight = height;

    }

    public Texture2DExt(Context context, Bitmap bitmap) {
        super(context, bitmap);
    }


    @Override
    protected void initShader() {
        super.initShader();
        mFragmentCode = readShader("fragment_ext.glsl");
    }

    @Override
    public void draw(float[] mvpMatrix) {

//        Log.d(TAG, "draw");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Utils.checkGlError("glClearColor1");
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        Utils.checkGlError("glClearColor2");
        GLES20.glUseProgram(mProgram);

        // 一点要加这两行，不然会出现OUF OF MEMORY错误
        // http://forum.unity3d.com/threads/mixing-unity-with-native-opengl-drawing-on-android.134621/
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

//        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
//
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        Utils.checkGlError("glGetAttribLocation aPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        Assert.assertNotNull(vertexBuffer);
//
        int maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        GLES20.glVertexAttribPointer(
                maTextureHandle, 2,
                GLES20.GL_FLOAT, false,
                0, uvBuffer);

        Assert.assertNotNull(uvBuffer);

        GLES20.glEnableVertexAttribArray(maTextureHandle);
//
        int mSamplerLoc = GLES20.glGetUniformLocation (mProgram,  "sTexture" );
        GLES20.glUniform1i(mSamplerLoc, 0);
//
        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
//
//        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
//
////        int uSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
////        Utils.checkGlError("glGetUniformLocation uSTMatrixHandle");
////
////        float[] uSTMatrix = new float[16];
////        Matrix.setIdentityM(uSTMatrix, 0);
////        GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, uSTMatrix, 0);
////        Utils.checkGlError("glUniformMatrix4fv uSTMatrixHandle");
//
//
//        // Draw the square
////        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        Assert.assertNotNull(drawOrder);
        Assert.assertNotNull(drawListBuffer);
        Utils.checkGlError("glDrawElements");
//
//        // Disable vertex array
//        GLES20.glDisableVertexAttribArray(positionHandle);
//        GLES20.glDisableVertexAttribArray(maTextureHandle);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//        Log.d(TAG, "draw finished");
    }
}
