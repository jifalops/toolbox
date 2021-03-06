package com.jifalops.toolbox.android.neuralnet;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class SampleList<T extends Sample> extends ArrayList<T> {
    private Scaler scaler;

    public SampleList() {}

    public SampleList(int capacity) {
        super(capacity);
    }

    public boolean isValid(Collection<? extends T> collection) {
        if (collection.size() == 0) return false;
        int outs = 0, len = 0;
        if (size() > 0) {
            outs = get(0).getNumOutputs();
            len = get(0).toArray().length;
            for (Sample s : collection)
                if (s.getNumOutputs() != outs || s.toArray().length != len) return false;
        } else {
            // internal consistency
            boolean first = true;
            for (Sample s : collection) {
                if (first) {
                    outs = s.getNumOutputs();
                    len = s.toArray().length;
                    first = false;
                }
                if (s.getNumOutputs() != outs || s.toArray().length != len) return false;
            }
        }
        return true;
    }

    public boolean isValid(T s) {
        if (size() == 0) return true;
        else return get(0).getNumOutputs() == s.getNumOutputs() &&
                get(0).toArray().length == s.toArray().length;
    }

    @Override
    public void add(int index, T object) {
        if (isValid(object)) {
            super.add(index, object);
            scaler = null;
        }
    }

    /** @return true if the sample was added, false if it has the wrong number of outputs. */
    @Override
    public boolean add(T s) {
        if (isValid(s)) {
            scaler = null;
            return super.add(s);
        }
        return false;
    }

    /** @return true if all items have the same number of outputs and were added, false if
     * any sample contains the wrong number of outputs (the whole collection is rejected).
     */
    @Override
    public boolean addAll(Collection<? extends T> collection) {
        if (isValid(collection)) {
            scaler = null;
            return super.addAll(collection);
        }
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> collection) {
        if (isValid(collection)) {
            scaler = null;
            return super.addAll(index, collection);
        }
        return false;
    }

    public List<double[]> toDoubleList() {
        List<double[]> list = new ArrayList<>(size());
        for (Sample s : this) {
            list.add(s.toArray());
        }
        return list;
    }

    public double[][] toDoubleArray() {
        return toDoubleList().toArray(new double[size()][]);
    }

    public int getNumInputs() { return get(0).toArray().length - get(0).getNumOutputs(); }
    public int getNumOutputs() { return get(0).getNumOutputs(); }
    public int getSampleSize() { return get(0).toArray().length; }

    public Scaler getScaler() {
        if (scaler == null && size() > 0) {
            scaler = new Scaler(toDoubleArray(), getNumOutputs());
        }
        return scaler;
    }
}
