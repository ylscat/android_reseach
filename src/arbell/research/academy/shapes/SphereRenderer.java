package arbell.research.academy.shapes;

import arbell.research.gl.Renderer;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static android.opengl.GLES20.*;

/**
 * Author: YinLanShan
 * Date: 13-6-19
 * Time: 16:37
 */
public class SphereRenderer extends Renderer
{
    int muRMatrixHandle;
    int muColorHandle;

    FloatBuffer mVertexBuffer;
    IntBuffer mLineIndices;
    IntBuffer mPrimaryLineIndices;

    public SphereRenderer(String vertexShader, String fragmentShader)
    {
        super(vertexShader, fragmentShader);
    }

    @Override
    protected void initVertex()
    {
        super.initVertex();
        final float SQRT5 = (float) Math.sqrt(5);
        final float E = 1 / (float) Math.sqrt((5 + SQRT5) / 8);
        final float Rp = (float) Math.sqrt((5 + SQRT5) / 10) * E;
        final float H = (float) Math.sqrt((5 + SQRT5) / 40) * E;
        final double rad72Degree = Math.PI * 2 / 5;

        float[] vertices = new float[]{
                0, 0, 1,
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
                0, 0, -1
        };
        int[] faces = new int[]{
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
        ArrayList<Float> dividedVertex = new ArrayList<Float>();
        ArrayList<Integer> dividedFaces = new ArrayList<Integer>();
        ArrayList<Integer> dividedLines = new ArrayList<Integer>();
        ArrayList<Integer> dividedPrimaryLines;
        for (float vertex : vertices)
            dividedVertex.add(vertex);
        for (int face : faces)
            dividedFaces.add(face);
        dividedPrimaryLines = getLines(dividedFaces);
        for (int k = 0; k < 0; k++)
        {
            semiDiv(dividedVertex, dividedFaces, dividedLines, dividedPrimaryLines);
        }

        for (int i = vertices.length; i < dividedVertex.size(); i += 3)
        {
            float x = dividedVertex.get(i);
            float y = dividedVertex.get(i + 1);
            float z = dividedVertex.get(i + 2);
            float r = (float) Math.sqrt(x * x + y * y + z * z);
            x /= r;
            y /= r;
            z /= r;
            dividedVertex.set(i, x);
            dividedVertex.set(i + 1, y);
            dividedVertex.set(i + 2, z);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(dividedVertex.size() * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        for (Float f : dividedVertex)
            mVertexBuffer.put(f);
        mVertexBuffer.position(0);


        bb = ByteBuffer.allocateDirect(dividedLines.size() * 4);
        bb.order(ByteOrder.nativeOrder());
        mLineIndices = bb.asIntBuffer();
        for (Integer i : dividedLines)
            mLineIndices.put(i);
        mLineIndices.position(0);

        bb = ByteBuffer.allocateDirect(dividedPrimaryLines.size() * 4);
        bb.order(ByteOrder.nativeOrder());
        mPrimaryLineIndices = bb.asIntBuffer();
        for (Integer i : dividedPrimaryLines)
            mPrimaryLineIndices.put(i);
        mPrimaryLineIndices.position(0);
    }

    @Override
    protected void getHandles()
    {
        super.getHandles();
        muRMatrixHandle = glGetUniformLocation(mProgram, "uRMatrix");
        muColorHandle = glGetUniformLocation(mProgram, "uColor");
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        super.onDrawFrame(gl);

        glUniformMatrix4fv(muRMatrixHandle, 1, false, mRotationMatrix, 0);

        glVertexAttribPointer(maPositionHandle, 3, GL_FLOAT, false,
                3 * 4, mVertexBuffer);
        glEnableVertexAttribArray(maPositionHandle);

        glUniform4f(muColorHandle, 0, 0, 0, 1);
        glDrawElements(GL_LINES, mLineIndices.capacity(),
                GL_UNSIGNED_INT, mLineIndices);
        glUniform4f(muColorHandle, 1, 0, 0, 1);
        glDrawElements(GL_LINES, mPrimaryLineIndices.capacity(),
                GL_UNSIGNED_INT, mPrimaryLineIndices);
    }

    private void semiDiv(ArrayList<Float> vertex, ArrayList<Integer> faces,
                         ArrayList<Integer> dividedLines, ArrayList<Integer> dividedPrimaryLines)
    {

        int vertexCount = vertex.size() / 3;
        int faceCount = faces.size() / 3;
        int[] newPointIndex = new int[3];
        float[] point1 = new float[3], point2 = new float[3];
        ArrayList<Integer> newFaces = new ArrayList<Integer>(faceCount * 3 * 4);
        HashMap<Integer, Integer> newVertexMap = new HashMap<Integer, Integer>();
        HashSet<Integer> primaryLineTraits = new HashSet<Integer>(dividedPrimaryLines.size() / 2);
        for (int i = 0, size = dividedPrimaryLines.size();
             i < size; i += 2)
        {
            int index1 = dividedPrimaryLines.get(i),
                    index2 = dividedPrimaryLines.get(i + 1);
            if (index2 < index1)
            {
                int temp = index1;
                index1 = index2;
                index2 = temp;
            }
            int trait = index1 * vertexCount + index2;
            primaryLineTraits.add(trait);
        }
        dividedPrimaryLines.clear();
        if (dividedLines != null)
            dividedLines.clear();
        for (int i = 0; i < faceCount; i++)
        {
            int offset = i * 3;
            int[] index = {faces.get(offset),
                    faces.get(offset + 1), faces.get(offset + 2)};

            for (int j = 0; j < 3; j++)
            {
                int k = j == 2 ? 0 : j + 1;
                int index1 = index[j], index2 = index[k];
                if (index2 < index1)
                {
                    int temp = index1;
                    index1 = index2;
                    index2 = temp;
                }
                int trait = index1 * vertexCount + index2;
                Integer mid = newVertexMap.get(trait);

                if (mid == null)
                {
                    mid = vertex.size() / 3;
                    newVertexMap.put(trait, mid);
                    for (int m = 0; m < 3; m++)
                    {
                        point1[m] = vertex.get(index1 * 3 + m);
                        point2[m] = vertex.get(index2 * 3 + m);
                        vertex.add((point1[m] + point2[m]) / 2);
                    }
                    if (primaryLineTraits.contains(trait))
                    {
                        dividedPrimaryLines.add(index1);
                        dividedPrimaryLines.add(mid);
                        dividedPrimaryLines.add(index2);
                        dividedPrimaryLines.add(mid);
                    } else
                    {
                        dividedLines.add(index1);
                        dividedLines.add(mid);
                        dividedLines.add(index2);
                        dividedLines.add(mid);
                    }
                }
                newPointIndex[j] = mid;
            }
            //new face1
            newFaces.add(index[0]);
            newFaces.add(newPointIndex[0]);
            newFaces.add(newPointIndex[2]);
            //new face2
            newFaces.add(newPointIndex[0]);
            newFaces.add(index[1]);
            newFaces.add(newPointIndex[1]);
            //new face3
            newFaces.add(newPointIndex[0]);
            newFaces.add(newPointIndex[1]);
            newFaces.add(newPointIndex[2]);
            //new face4
            newFaces.add(newPointIndex[2]);
            newFaces.add(newPointIndex[1]);
            newFaces.add(index[2]);

            //new lines
            if (dividedLines != null)
            {
                dividedLines.add(newPointIndex[0]);
                dividedLines.add(newPointIndex[1]);
                dividedLines.add(newPointIndex[1]);
                dividedLines.add(newPointIndex[2]);
                dividedLines.add(newPointIndex[2]);
                dividedLines.add(newPointIndex[0]);
            }
        }
        faces.clear();
        faces.addAll(newFaces);
    }
}
