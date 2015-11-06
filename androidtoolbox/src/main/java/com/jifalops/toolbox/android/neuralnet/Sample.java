package com.jifalops.toolbox.android.neuralnet;

/**
 *
 */
public interface Sample {
    /** The returned array should have inputs before outputs and should not be scaled */
    double[] toArray();
    int getNumOutputs();
}
