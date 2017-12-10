package io.github.skyhacker2.camera;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by eleven on 16/9/7.
 */
public class Texture2D {

    protected int mTextureID;
    protected String mVertexCode;
    protected String mFragmentCode;
    protected int mProgram;

    protected FloatBuffer vertexBuffer;
    protected ShortBuffer drawListBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -1f, 1f, 0.0f,
            -1f, -1f, 0.0f,
            1f, -1f, 0.0f,
            1f, 1f, 0.0f
    };

    protected short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    protected final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float[] uvs = new float[] {
            0.0f, 0.0f, // top left (V2)
            0.0f, 1.0f, // bottom left (V1)
            1.0f, 1.0f, // top right (V4)
            1.0f, 0.0f // bottom right (V3)
    };

    protected FloatBuffer uvBuffer;

    protected Context mContext;

    protected int mWidth;
    protected int mHeight;

    public Texture2D(int textureID) {
        mTextureID = textureID;
    }

    public Texture2D(Context context, int width, int height) {
        mContext = context;
        initVertex();
        initShader();
        createProgram();


        int[] temps = new int[1];
        GLES20.glGenTextures(1, temps, 0);
        mTextureID = temps[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        mWidth = width;
        mHeight = height;

    }

    public Texture2D(Context context, Bitmap bitmap) {
        mContext = context;
        initVertex();
        initShader();
        createProgram();
        loadTexture(bitmap);

        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
    }

    protected void loadTexture(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
//        checkGlError("glGenTextures");
        mTextureID = textures[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
//        checkGlError("glBindTexture");
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        checkGlError("glActiveTexture");

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        bitmap.recycle();
    }


    protected void initVertex() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer uvbb = ByteBuffer.allocateDirect(uvs.length * 4);
        uvbb.order(ByteOrder.nativeOrder());
        uvBuffer = uvbb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);
    }

    protected void createProgram() {
        mProgram = GLES20.glCreateProgram();
        int vertexShader = Utils.loadShader(GLES20.GL_VERTEX_SHADER, mVertexCode);
        int fragmentShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentCode);

        GLES20.glAttachShader(mProgram, vertexShader);
        Utils.checkGlError("glAttachShader vertexShader");
        GLES20.glAttachShader(mProgram, fragmentShader);
        Utils.checkGlError("glAttachShader fragmentShader");
        GLES20.glLinkProgram(mProgram);
    }

    protected void initShader() {
        mVertexCode = readShader("vertex.glsl");
        mFragmentCode = readShader("fragment_default.glsl");
    }

    protected String readShader(String name) {
        AssetManager am = mContext.getAssets();
        try {
            InputStream input = am.open(name);

            byte[] bytes = new byte[input.available()];
            input.read(bytes, 0, input.available());
            return new String(bytes, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Utils.checkGlError("glClearColor1");
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        Utils.checkGlError("glClearColor2");

        GLES20.glUseProgram(mProgram);


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        Utils.checkGlError("glGetAttribLocation aPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(
                positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        int maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        uvBuffer.position(0);
        GLES20.glVertexAttribPointer(
                maTextureHandle, 2,
                GLES20.GL_FLOAT, false,
                0, uvBuffer);
        Utils.checkGlError("glGetAttribLocation maTextureHandle");


        int mSamplerLoc = GLES20.glGetUniformLocation (mProgram,  "aTexture" );
        GLES20.glUniform1i(mSamplerLoc, 0);
        Utils.checkGlError("glUniform1i mSamplerLoc");

        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        Utils.checkGlError("glUniformMatrix4fv mvpMatrixHandle");

//        int uSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
//        Utils.checkGlError("glGetUniformLocation uSTMatrixHandle");
//
//        float[] uSTMatrix = new float[16];
//        Matrix.setIdentityM(uSTMatrix, 0);
//        GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, uSTMatrix, 0);
//        Utils.checkGlError("glUniformMatrix4fv uSTMatrixHandle");


        // Draw the square

//        GLES20.glDrawElements(
//                GLES20.GL_TRIANGLES, drawOrder.length,
//                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

//        Utils.checkGlError("glDrawElements");

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
        Utils.checkGlError("glDisableVertexAttribArray positionHandle");
        GLES20.glDisableVertexAttribArray(maTextureHandle);
        Utils.checkGlError("glDisableVertexAttribArray maTextureHandle");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public int getTextureID() {
        return mTextureID;
    }

    public void setTextureID(int textureID) {
        mTextureID = textureID;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void destory() {
        if (mTextureID != 0){
            int[] tmps = new int[1];
            tmps[0] = mTextureID;
            GLES20.glDeleteTextures(1, tmps, 0);
            mTextureID = 0;
        }
    }
}
