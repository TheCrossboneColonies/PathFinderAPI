package com.tcc.pathfinderapi.pathing;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Result returned after a pathfinder makes a step.
 */
public class PathStepResponse {


    private PathStepResult pathStepResult;
    private Map<String, Object> metaData = new HashMap<>();

    public PathStepResponse (PathStepResult pathStepResult) { this.pathStepResult = pathStepResult; }

    public void addMetaData (String key, Object data) { this.metaData.put(key, data); }

    public PathStepResult getResult () { return this.pathStepResult; }

    @Nullable
    public Object getMetaData (String key) { return this.metaData.get(key); }
}
