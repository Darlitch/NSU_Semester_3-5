import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Iterator;

public class Observer {
    private Map<String, Long> liveCopies;
    private int timeout = 5000;

    public Observer() {
        liveCopies = new Hashtable<String, Long>();
    }

    // ДОДЕЛАТЬ ФЛАГИ синхронизации
    public void updateLiveCopies(Datagram datagram) throws UnsupportedEncodingException {
        int oldSize = liveCopies.size();
        String key = datagram.addr.toString().substring(1) + ":" + Integer.toString(datagram.port) + " - " + new String(datagram.msg, "UTF-8");
        liveCopies.put(key, System.currentTimeMillis());
        if (liveCopies.size() > oldSize) {
            printLiveCopies();
        }
    }

    public void removeDeadCopies() {
        long currTime = System.currentTimeMillis();
        int flag = 0;
        Iterator<Map.Entry<String, Long>> iterator = liveCopies.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (currTime - entry.getValue() > timeout) {
                iterator.remove();
                flag = 1;
            }
        }
        if (flag == 1) {
            printLiveCopies();
        }
    }

    public synchronized void printLiveCopies() {
        if (liveCopies.isEmpty()) {
            System.out.println("No live copies");
            return;
        }
        for (String key : liveCopies.keySet()) {
            System.out.println(key);
        }
        System.out.println(" ");
    }
}
