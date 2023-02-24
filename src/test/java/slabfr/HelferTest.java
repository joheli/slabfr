/*
 * Test on methods in Helfer
 */
package slabfr;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
import slabfr.Helfer;
import slabfr.Helfer.SQLDatumException;
import java.util.Calendar;
import java.util.ArrayList;

public class HelferTest {
    @Test public void test01() {
        String q = Helfer.singleQuoteRemover("'r'");
        String t = "r";
        assertTrue(q.equals(t));
    }
    
    @Test public void test02() {
        String q = "juhuu";
        String t = "asdasdas$juhuuasdsa";
        assertTrue(Helfer.nadelImHeuHaufenGefunden(q, t));
    }
    
    @Test public void test03() throws SQLDatumException {
        Calendar start = Helfer.calDatum("01.01.2000");
        Calendar ende = Helfer.calDatum("01.02.2000");
        ArrayList<Calendar> list = Helfer.datumKette(start, ende, 5);
        assertTrue(list.size() == 8);
    }
}
