package com.github.kay9.dragonmounts.accessors;

public interface ModelPartAccess
{
    float getXScale();

    float getYScale();

    float getZScale();

    void setXScale(float x);

    void setYScale(float y);

    void setZScale(float z);

    default void setRenderScale(float x, float y, float z)
    {
        setXScale(x);
        setYScale(y);
        setZScale(z);
    }
}