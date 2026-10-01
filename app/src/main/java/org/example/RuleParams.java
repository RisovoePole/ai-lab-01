package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RuleParams {

    private ArrayList<Integer> params;

    public RuleParams(int size) {
        params = new ArrayList<Integer>(Collections.nCopies(size, 0));
    }

    public List<Integer> getParams() {
        return params;
    }

    public Integer getParam(int idx) {
        return params.get(idx);
    }

    public void addOne(int idx) {
        params.set(idx, params.get(idx) + 1);
    }
}
