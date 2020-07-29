package wolfshotz.dml.util;

/**
 * Math helper class.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MathX
{
    public static final double PI_D = Math.PI;
    public static final float PI_F = (float) Math.PI;
    private static final float[][] CR = {
            {-0.5f, 1.5f, -1.5f, 0.5f},
            {1.0f, -2.5f, 2.0f, -0.5f},
            {-0.5f, 0.0f, 0.5f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f}
    };

    /**
     * You no take constructor!
     */
    private MathX()
    {
    }

    // float sine function, may use LUT
    public static float sin(float a)
    {
        return (float) Math.sin(a);
    }

    // float cosine function, may use LUT
    public static float cos(float a)
    {
        return (float) Math.cos(a);
    }

    // float tangent function
    public static float tan(float a)
    {
        return (float) Math.tan(a);
    }

    // float atan2 function
    public static float atan2(float y, float x)
    {
        return (float) Math.atan2(y, x);
    }

    // float degrees to radians conversion
    public static float toRadians(float angdeg)
    {
        return (float) Math.toRadians(angdeg);
    }

    // float radians to degrees conversion
    public static float toDegrees(float angrad)
    {
        return (float) Math.toDegrees(angrad);
    }

    // normalizes a float degrees angle to between +180 and -180
    public static float normDeg(float a)
    {
        a %= 360;
        if (a >= 180)
        {
            a -= 360;
        }
        if (a < -180)
        {
            a += 360;
        }
        return a;
    }

    // normalizes a double degrees angle to between +180 and -180
    public static double normDeg(double a)
    {
        a %= 360;
        if (a >= 180)
        {
            a -= 360;
        }
        if (a < -180)
        {
            a += 360;
        }
        return a;
    }

    // normalizes a float radians angle to between +π and -π
    public static float normRad(float a)
    {
        a %= PI_F * 2;
        if (a >= PI_F)
        {
            a -= PI_F * 2;
        }
        if (a < -PI_F)
        {
            a += PI_F * 2;
        }
        return a;
    }

    // normalizes a double radians angle to between +π and -π
    public static double normRad(double a)
    {
        a %= PI_D * 2;
        if (a >= PI_D)
        {
            a -= PI_D * 2;
        }
        if (a < -PI_D)
        {
            a += PI_D * 2;
        }
        return a;
    }

    // float square root
    public static float sqrtf(float f)
    {
        return (float) Math.sqrt(f);
    }

    // numeric float clamp
    public static float clamp(float value, float min, float max)
    {
        return (value < min ? min : (Math.min(value, max)));
    }

    // numeric double clamp
    public static double clamp(double value, double min, double max)
    {
        return (value < min ? min : (Math.min(value, max)));
    }

    // numeric integer clamp
    public static int clamp(int value, int min, int max)
    {
        return (value < min ? min : (Math.min(value, max)));
    }

    public static float updateRotation(float r1, float r2, float step)
    {
        return r1 + clamp(normDeg(r2 - r1), -step, step);
    }

    public static float terpLinear(float a, float b, float x)
    {
        if (x <= 0)
        {
            return a;
        }
        if (x >= 1)
        {
            return b;
        }
        return a * (1 - x) + b * x;
    }

    public static float terpSmoothStep(float a, float b, float x)
    {
        if (x <= 0)
        {
            return a;
        }
        if (x >= 1)
        {
            return b;
        }
        x = x * x * (3 - 2 * x);
        return a * (1 - x) + b * x;
    }

    // http://www.java-gaming.org/index.php?topic=24122.0
    public static void terpCatmullRomSpline(float x, float[] result, float[]... knots)
    {
        int nknots = knots.length;
        int nspans = nknots - 3;
        int knot = 0;
        if (nspans < 1) throw new IllegalArgumentException("Spline has too few knots");
        x = MathX.clamp(x, 0, 0.9999f) * nspans;

        int span = (int) x;
        if (span >= nknots - 3)
        {
            span = nknots - 3;
        }

        x -= span;
        knot += span;

        int dimension = result.length;
        for (int i = 0; i < dimension; i++)
        {
            float knot0 = knots[knot][i];
            float knot1 = knots[knot + 1][i];
            float knot2 = knots[knot + 2][i];
            float knot3 = knots[knot + 3][i];

            float c3 = CR[0][0] * knot0 + CR[0][1] * knot1 + CR[0][2] * knot2 + CR[0][3] * knot3;
            float c2 = CR[1][0] * knot0 + CR[1][1] * knot1 + CR[1][2] * knot2 + CR[1][3] * knot3;
            float c1 = CR[2][0] * knot0 + CR[2][1] * knot1 + CR[2][2] * knot2 + CR[2][3] * knot3;
            float c0 = CR[3][0] * knot0 + CR[3][1] * knot1 + CR[3][2] * knot2 + CR[3][3] * knot3;

            result[i] = ((c3 * x + c2) * x + c1) * x + c0;
        }
    }
}
