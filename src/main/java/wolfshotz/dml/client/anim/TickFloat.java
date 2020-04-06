package wolfshotz.dml.client.anim;

import wolfshotz.dml.util.MathX;

/**
 * Simple class to store and limitate a float value that is smoothed between its
 * current and previous tick value.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TickFloat
{

    private float min;
    private float max;
    private boolean clamp = false;
    private float current;
    private float previous;

    public TickFloat()
    {
        current = previous = 0;
    }

    public TickFloat(float value)
    {
        current = previous = value;
    }

    public TickFloat setLimit(float min, float max)
    {
        clamp = true;
        setMin(min);
        setMax(max);
        set(current);
        return this;
    }

    public float get(float x)
    {
        return MathX.terpLinear(previous, current, x);
    }

    public float get()
    {
        return current;
    }

    public void sync()
    {
        previous = current;
    }

    public void set(float value)
    {
        sync();
        current = clamp ? MathX.clamp(value, min, max) : value;
    }

    public void add(float value)
    {
        sync();
        current += value;
        if (clamp)
        {
            current = MathX.clamp(current, min, max);
        }
    }

    public float getPrevious()
    {
        return previous;
    }

    public float getMin()
    {
        return min;
    }

    public void setMin(float min)
    {
        this.min = min;
    }

    public float getMax()
    {
        return max;
    }

    public void setMax(float max)
    {
        this.max = max;
    }
}
