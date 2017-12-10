package io.github.skyhacker2.camera;

import android.opengl.GLES20;

/**
 * Created by eleven on 16/9/7.
 */
public class FBO {
    private Texture2D mTexture2D;
    private int mFBOID;
    private int mRBOID;

    public FBO(Texture2D texture2D) {
        mTexture2D = texture2D;
        int depthID;
        int[] temps = new int[1];
        // Render buffer
        GLES20.glGenTextures(1, temps, 0);
        depthID = temps[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, temps[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, texture2D.getWidth(), texture2D.getHeight(), 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_SHORT, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glGenFramebuffers(1, temps, 0);
        mFBOID = temps[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOID);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTexture2D.getTextureID(), 0);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, depthID, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);


    }

    public void FBOBegin() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOID);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        Utils.checkGlError("glBindBuffer GL_ARRAY_BUFFER 0");

    }

    public void FBOEnd() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }
}
