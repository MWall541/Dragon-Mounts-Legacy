package wolfshotz.dml.client.anim;

import wolfshotz.dml.util.MathX;

import java.util.Arrays;

/**
 * Very simple fixed size circular buffer implementation for animation purposes.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CircularBuffer
{
    private final float[] buffer;
    private int index = 0;

    public CircularBuffer(int size)
    {
        buffer = new float[size];
    }

    public void fill(float value)
    {
        Arrays.fill(buffer, value);
    }

    public void update(float value)
    {
        // move forward
        index++;

        // restart pointer at the end to form a virtual ring
        index %= buffer.length;

        buffer[index] = value;
    }

    public float get(float x, int offset)
    {
        int i = index - offset;
        int len = buffer.length - 1;
        return MathX.terpLinear(buffer[i - 1 & len], buffer[i & len], x);
    }

    public float get(float x, int offset1, int offset2)
    {
        return get(x, offset2) - get(x, offset1);
    }
}