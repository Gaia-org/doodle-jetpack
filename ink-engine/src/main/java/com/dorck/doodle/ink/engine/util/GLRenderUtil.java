package com.dorck.doodle.ink.engine.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLRenderUtil {
    private static final String TAG = GLRenderUtil.class.getSimpleName();

    public static FloatBuffer createFloatBuffer(int size) {
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(size * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        return allocateDirect.asFloatBuffer();
    }

    public static float[] toRGBA(int i) {
        return new float[]{((i >> 16) & 255) / 255.0f, ((i >> 8) & 255) / 255.0f, (i & 255) / 255.0f, (i >>> 24) / 255.0f};
    }

    public static RectF borderExpand(RectF rectF, float f) {
        RectF rectF2 = new RectF();
        rectF2.top = ((int) Math.floor(rectF.top)) - f;
        rectF2.bottom = ((int) Math.ceil(rectF.bottom)) + f;
        rectF2.left = ((int) Math.floor(rectF.left)) - f;
        rectF2.right = ((int) Math.ceil(rectF.right)) + f;
        return rectF2;
    }

    public static RectF toRectF(Rect rect) {
        return new RectF(rect.left, rect.top, rect.right, rect.bottom);
    }

    public static int createTexture2(Bitmap bitmap) {
        int[] iArr = new int[1];
        GLES30.glGenTextures(1, iArr, 0);
        int texId = iArr[0];
        if (texId == 0) {
            String errMsg = getGLErrorString();
//            EngLog.w(TAG, "error: " + errMsg);
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, iArr[0]);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        return texId;
    }

    public static int createTexture(Bitmap bitmap) {
        int[] iArr = new int[1];
        GLES30.glGenTextures(1, iArr, 0);
        int texId = iArr[0];
        if (texId == 0) {
            String errMsg = getGLErrorString();
//            EngLog.w(TAG, "error: " + errMsg);
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, iArr[0]);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        return texId;
    }

    private static String getGLErrorString() {
        int errorCode = GLES30.glGetError();
        switch (errorCode) {
            case GLES30.GL_NO_ERROR:
                return "No error";
            case GLES30.GL_INVALID_ENUM:
                return "Invalid enum";
            case GLES30.GL_INVALID_VALUE:
                return "Invalid value";
            case GLES30.GL_INVALID_OPERATION:
                return "Invalid operation";
            case GLES30.GL_OUT_OF_MEMORY:
                return "Out of memory";
            default:
                return "Unknown error";
        }
    }

    public static int createTextureAndBindFramebuffer(int w, int h) {
        int[] iArr = {0};
        GLES30.glGenTextures(1, iArr, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, iArr[0]);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, w, h, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, iArr[0]);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, iArr[0], 0);
        return iArr[0];
    }

    public static int createTextureAndBindFramebuffer(Bitmap bitmap) {
        int[] iArr = {0};
        GLES30.glGenTextures(1, iArr, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, iArr[0]);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, iArr[0]);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, iArr[0], 0);
        return iArr[0];
    }

    public static void deleteTextureAndFrameBuffer(int i) {
        if (GLES30.glIsTexture(i)) {
            GLES30.glDeleteTextures(1, new int[]{i}, 0);
        }
        if (GLES30.glIsFramebuffer(i)) {
            GLES30.glDeleteFramebuffers(1, new int[]{i}, 0);
        }
    }

    // 伪随机算法生成一个 [0,1] 的小数
    public static float randomDecimal(float x, float y) {
        // 定义常数
        float p = 43758.5453123f;
        float a = 12.9898f;
        float b = 78.233f;

        // 计算点积
        float dotProduct = x * a + y * b;

        // 计算 sin 和 fract
        float sine = (float) Math.sin(dotProduct);
        float result = sine * p;

        // 计算 fract
        result = (float) (result - Math.floor(result));
        return result;
    }

    public static Bitmap getBitmapFromDrawable(Context context, int drawableId) {
        Resources resources = context.getResources();
        return BitmapFactory.decodeResource(resources, drawableId);
    }
}
