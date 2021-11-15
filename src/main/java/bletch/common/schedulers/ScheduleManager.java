package bletch.common.schedulers;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.tangotek.tektopia.Village;
import net.tangotek.tektopia.tickjob.TickJob;
import net.tangotek.tektopia.tickjob.TickJobQueue;

import java.util.ArrayList;
import java.util.List;

public class ScheduleManager {

    protected final World world;
    protected List<IScheduler> schedulers;
    protected TickJobQueue jobs;

    public ScheduleManager(World worldIn) {
        this.world = worldIn;
        this.schedulers = new ArrayList<>();
        this.jobs = new TickJobQueue();

        setupServerJobs();
    }

    public void addScheduler(IScheduler scheduler) {
        if (scheduler == null)
            return;

        this.schedulers.add(scheduler);
    }

    public void addJob(TickJob job) {
        if (job == null)
            return;

        this.jobs.addJob(job);
    }

    public void onWorldTick(WorldTickEvent e) {
        this.jobs.tick();

        if (Village.isNightTime(world)) {
            this.schedulers.forEach((s) -> s.resetNight());
        } else {
            this.schedulers.forEach((s) -> s.resetDay());
        }
    }

    public void processSchedulers() {
        schedulers.forEach((s) -> s.update(this.world));
    }

    protected void setupServerJobs() {
        this.addJob(new TickJob(100, 100, true, () -> this.processSchedulers()));
    }

}
