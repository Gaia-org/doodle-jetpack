package com.dorck.doodle.ink.engine.util;

import android.opengl.GLES30;
import android.util.Log;

public class OpenGLShaderUtil {
    private static final String TAG = OpenGLShaderUtil.class.getSimpleName();

    public static int createShader(int shaderType, String shaderCode) {
        int glCreateShader = GLES30.glCreateShader(shaderType);
        if (glCreateShader != 0) {
            GLES30.glShaderSource(glCreateShader, shaderCode);
            GLES30.glCompileShader(glCreateShader);
            // 检查编译状态
            int[] compileStatus = new int[1];
            GLES30.glGetShaderiv(glCreateShader, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                // 编译失败，打印错误日志并删除着色器对象
                String errorMsg = GLES30.glGetShaderInfoLog(glCreateShader);
                Log.e(TAG, "Shader compilation failed: " + errorMsg);
                GLES30.glDeleteShader(glCreateShader);
            } else {
                Log.d(TAG, "Shader compilation succeed: " + compileStatus[0]);
            }
        }
        return glCreateShader;
    }

    public static int createProgram(int vertexShader, int fragShader) {
        if (vertexShader == 0 || fragShader == 0) {
            return -1;
        }
        int glCreateProgram = GLES30.glCreateProgram();
        GLES30.glAttachShader(glCreateProgram, vertexShader);
        GLES30.glAttachShader(glCreateProgram, fragShader);
        GLES30.glLinkProgram(glCreateProgram);
        // 检查链接状态
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(glCreateProgram, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            // 链接失败，打印错误日志并删除程序对象
            String errorMsg = GLES30.glGetProgramInfoLog(glCreateProgram);
            GLES30.glDeleteProgram(glCreateProgram);
            Log.e(TAG, "Program link failed: " + errorMsg);
        } else {
            Log.d(TAG, "Program link succeed: " + linkStatus[0]);
        }

        return glCreateProgram;
    }
}
