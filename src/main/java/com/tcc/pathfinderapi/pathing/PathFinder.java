package com.tcc.pathfinderapi.pathing;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.errorHandling.PathException;
import com.tcc.pathfinderapi.pathing.PathFinderScheduler;
import com.tcc.pathfinderapi.pathing.PathFinderTask;
import com.tcc.pathfinderapi.pathing.PathStepResponse;
import com.tcc.pathfinderapi.pathing.pathfinders.Greedy;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class PathFinder {

        private Location start;
        private Location end;

        private int maxPathLength;

        public PathFinder(PathBuilder builder){
                this.start = builder.getStart();
                this.end = builder.getEnd();
                this.maxPathLength = builder.getMaxPathLength();
        }


        public abstract PathBuilder toBuilder();


        public Location getStart(){
                return start;
        }

        public Location getEnd(){
                return end;
        }
        public int getMaxPathLength() { return maxPathLength; }

        public final PathFinderTask run(){
                return PathFinderScheduler.run(this);
        }
        protected void onStart(){

        }
        protected void onComplete(){

        }
        protected void onSuccess(){

        }
        protected void onError(){

        }

        protected abstract PathStepResponse step();

}
