package slabfr;

import java.util.ArrayList;
import java.util.Iterator;


public class DateiLeser extends ArrayList<String[]> implements ImportDaten {
    
	Iterator<String[]> i = null;
    String[] zeile = null;
    
    /** Creates a new instance of DateiLeser */
    public DateiLeser() {
    }

    public boolean empty() {
        return this.size() == 0;
    }
    
    public void first() {
        i = null; 
        i = this.iterator();
        if (i.hasNext()) {
            zeile = this.get(0);
        } else {
            zeile = null;
        }
    }

    public boolean next() {
        if (i.hasNext()) {
            zeile = i.next();
            return true;
        } else {
            return false;
        }
    }
}
