package arbell.research.academy.stereo;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Author: YinLanShan
 * Date: 14-8-26
 * Time: 14:07
 */
public class Face20 extends Polyhedron
{
    public float[] mTransform = new float[16];

    public Face20()
    {
        Matrix.setIdentityM(mTransform, 0);
        final float RADIUS = 0.25f;
        final float SQRT5 = (float) Math.sqrt(5);
        final float R = (float) Math.sqrt((5 + SQRT5) / 8)*RADIUS;
        final float Rp = (float) Math.sqrt((5 + SQRT5) / 10)*RADIUS;
        final float H = (float) Math.sqrt((5 + SQRT5) / 40)*RADIUS;
        final double rad72Degree = Math.PI * 2 / 5;

        float[] vertices = new float[]{
                0, 0, R,
                (float) (Rp * Math.cos(rad72Degree * 0)),
                (float) (Rp * Math.sin(rad72Degree * 0)), H,
                (float) (Rp * Math.cos(rad72Degree * 1)),
                (float) (Rp * Math.sin(rad72Degree * 1)), H,
                (float) (Rp * Math.cos(rad72Degree * 2)),
                (float) (Rp * Math.sin(rad72Degree * 2)), H,
                (float) (Rp * Math.cos(rad72Degree * 3)),
                (float) (Rp * Math.sin(rad72Degree * 3)), H,
                (float) (Rp * Math.cos(rad72Degree * 4)),
                (float) (Rp * Math.sin(rad72Degree * 4)), H,
                (float) (Rp * Math.cos(rad72Degree * 0.5)),
                (float) (Rp * Math.sin(rad72Degree * 0.5)), -H,
                (float) (Rp * Math.cos(rad72Degree * 1.5)),
                (float) (Rp * Math.sin(rad72Degree * 1.5)), -H,
                (float) (Rp * Math.cos(rad72Degree * 2.5)),
                (float) (Rp * Math.sin(rad72Degree * 2.5)), -H,
                (float) (Rp * Math.cos(rad72Degree * 3.5)),
                (float) (Rp * Math.sin(rad72Degree * 3.5)), -H,
                (float) (Rp * Math.cos(rad72Degree * 4.5)),
                (float) (Rp * Math.sin(rad72Degree * 4.5)), -H,
                0, 0, -R
        };

        byte[] faces = new byte[]{
                0, 1, 2,
                0, 2, 3,
                0, 3, 4,
                0, 4, 5,
                0, 5, 1,
                1, 6, 2,
                2, 6, 7,
                2, 7, 3,
                3, 7, 8,
                3, 8, 4,
                4, 8, 9,
                4, 9, 5,
                5, 9, 10,
                5, 10, 1,
                1, 10, 6,
                6, 11, 7,
                7, 11, 8,
                8, 11, 9,
                9, 11, 10,
                10, 11, 6
        };

        float[] colorsForEachFace = new float[]{
                1, 0, 0, 0.5f,
                0, 1, 0, 0.5f,
                0, 0, 1, 0.5f,
                1, 0, 0, 0.5f,
                0, 1, 0, 0.5f, //First 5
                0, 0, 1, 0.5f,
                1, 0, 0, 0.5f,
                0, 0, 1, 0.5f,
                0, 1, 0, 0.5f,
                1, 0, 0, 0.5f,
                0, 0, 1, 0.5f,
                0, 1, 0, 0.5f,
                1, 0, 0, 0.5f,
                0, 0, 1, 0.5f,
                0, 1, 0, 0.5f, // profile sides
                0, 0, 1, 0.5f,
                1, 0, 0, 0.5f,
                0, 1, 0, 0.5f,
                0, 0, 1, 0.5f,
                1, 0, 0, 0.5f,
        };

        int bufSize = faces.length * 3 * 4;
        ByteBuffer bb = ByteBuffer.allocateDirect(bufSize);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuf = bb.asFloatBuffer();
        bufSize = faces.length * 4 * 4;
        bb = ByteBuffer.allocateDirect(bufSize);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer colorBuf = bb.asFloatBuffer();
        bufSize = faces.length * 3 * 4;
        bb = ByteBuffer.allocateDirect(bufSize);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer normalBuf = bb.asFloatBuffer();

        float[] vector1 = new float[3];
        float[] vector2 = new float[3];
        float[] vector = new float[3];
        float[] point1 = new float[3];
        float[] point2 = new float[3];
        float[] point3 = new float[3];
        float[] color = new float[4];
        for (int i = 0; i < faces.length; i++)
        {
            int vertexIndex = 3 * faces[i];
            vertexBuf.put(vertices[vertexIndex]);
            vertexBuf.put(vertices[vertexIndex + 1]);
            vertexBuf.put(vertices[vertexIndex + 2]);
            if (i % 3 == 0)
            {
                int index = i / 3 * 4;
                System.arraycopy(colorsForEachFace, index, color, 0, 4);
                index = 3 * faces[i];
                System.arraycopy(vertices, index, point1, 0, 3);
                index = 3 * faces[i + 1];
                System.arraycopy(vertices, index, point2, 0, 3);
                index = 3 * faces[i + 2];
                System.arraycopy(vertices, index, point3, 0, 3);
                getVectorsFrom3Points(point1, point2, point3, vector1, vector2);
                cross(vector1, vector2, vector);
            }
            colorBuf.put(color);
            normalBuf.put(vector);
        }
        vertexBuf.position(0);
        colorBuf.position(0);
        normalBuf.position(0);
        mVertexBuffer = vertexBuf;
        mColorBuffer = colorBuf;
        mNormalBuffer = normalBuf;
    }

    @Override
    public void draw(Renderer renderer)
    {
        super.draw(renderer);
        GLES20.glUniformMatrix4fv(renderer.muRMatrixHandle, 1, false, mTransform, 0);

        GLES20.glVertexAttribPointer(renderer.maPositionHandle, 3, GLES20.GL_FLOAT, false,
                3 * 4, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(renderer.maPositionHandle);
        GLES20.glVertexAttribPointer(renderer.maColorHandle, 4, GLES20.GL_FLOAT, false,
                4 * 4, mColorBuffer);
        GLES20.glEnableVertexAttribArray(renderer.maColorHandle);
        GLES20.glVertexAttribPointer(renderer.maNormalHandle, 3, GLES20.GL_FLOAT, false,
                3 * 4, mNormalBuffer);
        GLES20.glEnableVertexAttribArray(renderer.maNormalHandle);

        Matrix.multiplyMM(renderer.mMVPMatrix, 0, renderer.mProjMatrixLeft, 0, renderer.mVMatrixLeft, 0);
        GLES20.glUniformMatrix4fv(renderer.muMVPMatrixHandle, 1, false, renderer.mMVPMatrix, 0);
        GLES20.glViewport(0, 0, renderer.mWidth/2, renderer.mHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexBuffer.capacity() / 3);

        Matrix.multiplyMM(renderer.mMVPMatrix, 0, renderer.mProjMatrixRight, 0, renderer.mVMatrixRight, 0);
        GLES20.glUniformMatrix4fv(renderer.muMVPMatrixHandle, 1, false, renderer.mMVPMatrix, 0);
        GLES20.glViewport(renderer.mWidth/2, 0, renderer.mWidth/2, renderer.mHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexBuffer.capacity() / 3);
    }

    private void getVectorsFrom3Points(float[] p1, float[] p2, float[] p3, float[] v1, float[] v2)
    {
        for (int i = 0; i < 3; i++)
        {
            v1[i] = p2[i] - p1[i];
            v2[i] = p3[i] - p1[i];
        }
    }

    private void cross(float[] p1, float[] p2, float[] result)
    {
        result[0] = p1[1] * p2[2] - p2[1] * p1[2];
        result[1] = p1[2] * p2[0] - p2[2] * p1[0];
        result[2] = p1[0] * p2[1] - p2[0] * p1[1];
    }

    public void rotate(float degree)
    {
        double rad = degree/180f*Math.PI;
        float sin = (float)Math.sin(rad);
        float cos = (float)Math.cos(rad);
        mTransform[8] = sin;
        mTransform[10] = cos;
        mTransform[0] = cos;
        mTransform[2] = -sin;
        mTransform[12] = 0.5f*sin;
        mTransform[14] = 0.5f*cos;
    }
}
