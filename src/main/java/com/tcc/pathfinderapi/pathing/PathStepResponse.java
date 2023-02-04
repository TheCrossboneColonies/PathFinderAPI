package com.tcc.pathfinderapi.pathing;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Result returned after a pathfinder makes a step
 */
public class PathStepResponse {

    private PathStepResult result;
    private Map<String, Object> metaData = new HashMap<>();

    public PathStepResponse(PathStepResult result){
        this.result = result;
    }

    public void addMetaData(String key, Object data){
        metaData.put(key, data);
    }

    public PathStepResult getResult() {
        return result;
    }

    @Nullable
    public Object getMetaData(String key){
        return metaData.get(key);
    }

}
