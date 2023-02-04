package com.tcc.pathfinderapi.pathing;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.errorHandling.PathException;
import com.tcc.pathfinderapi.pathing.PathFinderScheduler;
import com.tcc.pathfinderapi.pathing.PathFinderTask;
import com.tcc.pathfinderapi.pathing.PathStepResponse;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class PathFinder {

        private Location start;
        private Location end;

        public PathFinder(Location start, Location end){
                this.start = start;
                this.end = end;
        }

        public Location getStart(){
                return start;
        }

        public Location getEnd(){
                return end;
        }

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
