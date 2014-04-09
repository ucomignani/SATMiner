package dag.satmining.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public final class Timer {

    private static final Logger TLOG = LoggerFactory.getLogger("Timing");
    
    private Monitor _monitor;
    private String _name;
    
    private Timer(String name) {
        _name = name;
        _monitor = MonitorFactory.start(_name);
    }
    
    public static Timer start(String name) {
        return new Timer(name);
    }
    
    public void stopAndPrint() {
        _monitor.stop();
        TLOG.info("{} time: {} s",new Object[]{_name,_monitor.getLastValue()/1000.0});
    }
}
