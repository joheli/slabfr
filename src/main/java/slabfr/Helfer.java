package slabfr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helfer {
    
    public final static String SL_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
    // public final static String SL_DRIVER = "com.sybase.jdbc3.jdbc.SybDriver";
    public final static String CALL_START_1 = "{call dbo.";
    // zwischen CALL_START_1 und CALL_START_2 befindet sich der Name der Prozedur
    // also z.B. PR_MIKROSTAT für Resistenzstatistik oder PR_SUCHEWERTE für andere Recherchen
    public final static String CALL_START_2 = "(@DATUMVON=?, @DATUMBIS=?, ";
    public final static String CALL_END = ")}";
    private final static Logger logger = Logger.getLogger(Start.class.getName());
    
    /** Creates a new instance of Helfer */
    public Helfer() {
    }
    
    public static ArrayList<String[]> dateiLeserCSV(File ausleseDatei) throws FileNotFoundException, IOException {
        ArrayList<String[]> ergebnis = new ArrayList<String[]>();
        BufferedReader iS = new BufferedReader(new FileReader(ausleseDatei));
        String z;
        
        while ((z = iS.readLine()) != null) {
            String[] zeile = z.split(";");
            ergebnis.add(zeile);
            zeile = null;
        }
        
        // Reader explizit schliessen, damit das löschen danach möglich ist
        iS.close();
        
        // Entfernung der ersten Zeile
        if (ergebnis.size() > 0) ergebnis.remove(0);

        return ergebnis;
    }
    
    public static HashMap<String, String> parameterLeser(File parameterDatei) throws FileNotFoundException, IOException, Parameterdateifehler {
        HashMap<String, String> parameter = new HashMap<String, String>();
        
        if (!parameterDatei.exists()) {
            logger.log(Level.SEVERE, "Parameterdatei {0} nicht gefunden. Beende Programm.", parameterDatei.getCanonicalPath());
            System.exit(1);
        }
        
        BufferedReader iS = new BufferedReader(new FileReader(parameterDatei));
        String z;
        
        while ((z = iS.readLine()) != null) {
            if (!z.startsWith("|")) {
                StringTokenizer sT = new StringTokenizer(z, "=");
                if (sT.countTokens() > 1) parameter.put(sT.nextToken(), sT.nextToken());
            }
        }
        iS = null;
        
        String[] erforderlicheParameter = {"@DATUMVON", "@DATUMBIS", "ExportZiel","Abfrage","IP"};
        
        Boolean allesOk = true;
        
        for (String param:erforderlicheParameter) {
            if (!parameter.containsKey(param)) allesOk = false;
        }
        
        if (!allesOk) {
            throw new Parameterdateifehler("Parameterdatei " + parameterDatei.getCanonicalPath() + " nicht komplett auslesbar.");
        }
        
        // Standardwerte
        if (!parameter.containsKey("TagSchwelle")) parameter.put("TagSchwelle", "28");
        
        return parameter;
    }
   
    public static boolean nadelImHeuHaufenGefunden(String nadel, String heuHaufen) {
        Pattern p = Pattern.compile(nadel);
        Matcher m = p.matcher(heuHaufen);
        return m.find();
    }
    
    public static String nadelImHeuHaufen(String nadel, String heuHaufen) {
        if (nadelImHeuHaufenGefunden(nadel, heuHaufen)) {
            Pattern p = Pattern.compile(nadel);
            Matcher m = p.matcher(heuHaufen);
            m.find();
            return m.group();
        } else {
            return "";
        }
    }
    
    public static void zeilenAusgeber(String[] zeile) {
        System.out.println("");
        int i = 0;
        for (String s:zeile) {
            System.out.print(i + ": " + s + " | ");
            i++;
        }
    }
    
    public static java.util.Date text2date(String format, String datum) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return (java.util.Date) df.parse(datum);
    }
    
    public static java.util.Date text2date(String datum) throws ParseException {
        return text2date("dd.MM.yyyy", datum);
    }
    
    public static Calendar date2Calendar(java.util.Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
    }
    
    public static Calendar heuteCal() {
        Calendar cal = Calendar.getInstance();
        return cal;
    }
    
    public static Calendar vorXTagenCal(int x) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -x);
        return cal;
    }
    
    public static String heute() {
        return dateFormatMedium(heuteCal());
    }
    
    private static String dateFormatMedium(Calendar cal) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return df.format(cal.getTime());
    }
    
    public static String vorEinerWoche() {
        return dateFormatMedium(vorXTagenCal(7));
    }
    
    public static String vorXTagen(int x) {
        return dateFormatMedium(vorXTagenCal(x));
    }
    
    public static java.util.Date heuteDatum() {
        return heuteCal().getTime();
    }
    
    public static java.util.Date vorXTagenDatum(int x) {
        return vorXTagenCal(x).getTime();
    }
    
    public static java.sql.Date heuteDatumSql() {
        return cal2sqlDate(heuteCal());
    }
    
    public static java.sql.Date sqlDatum(String datum) throws SQLDatumException {
        return cal2sqlDate(calDatum(datum));
    }
    
    public static Calendar calDatum(String datum) throws SQLDatumException {
        StringTokenizer sT = new StringTokenizer(datum, ".");
        if (sT.countTokens() != 3) throw new SQLDatumException("Datum ungültig!");
        int tag = Integer.valueOf(sT.nextToken());
        int monat = Integer.valueOf(sT.nextToken()) - 1;
        int jahr = Integer.valueOf(sT.nextToken());
        Calendar cal = Calendar.getInstance();
        cal.set(jahr, monat, tag);
        return cal;
    }
    
    public static ArrayList<Calendar[]> datumKette2(Calendar von, Calendar bis, int tage) {
        ArrayList<Calendar> datumKette = datumKette(von, bis, tage);
        ArrayList<Calendar[]> ergebnis = new ArrayList<Calendar[]>();
        for (int i = 0; i < (datumKette.size()-1); i++) {
            if (i > 0) datumKette.get(i).add(Calendar.DAY_OF_YEAR, 1);
            Calendar t1 = Calendar.getInstance();
            Calendar t2 = Calendar.getInstance();
            t1.set(datumKette.get(i).get(Calendar.YEAR), datumKette.get(i).get(Calendar.MONTH), datumKette.get(i).get(Calendar.DAY_OF_MONTH));
            t2.set(datumKette.get(i+1).get(Calendar.YEAR), datumKette.get(i+1).get(Calendar.MONTH), datumKette.get(i+1).get(Calendar.DAY_OF_MONTH));
            Calendar[] paar = new Calendar[] {t1, t2};
            ergebnis.add(paar);
            t1 = null;
            t2 = null;
            paar = null;
        }
        return ergebnis;
    }
    
    public static ArrayList<Calendar> datumKette(Calendar von, Calendar bis, int tage) {
        ArrayList<Calendar> ergebnis = new ArrayList<Calendar>();
        Calendar schrittDatum = von;
        
        Calendar i = Calendar.getInstance();
        i.set(schrittDatum.get(Calendar.YEAR), schrittDatum.get(Calendar.MONTH), schrittDatum.get(Calendar.DAY_OF_MONTH));
        ergebnis.add(i);
        i = null;
        
        Calendar bisEnde = Calendar.getInstance();
        bisEnde.set(bis.get(Calendar.YEAR), bis.get(Calendar.MONTH), bis.get(Calendar.DAY_OF_MONTH));
        
        bis.add(Calendar.DAY_OF_YEAR, -tage);
        
        Calendar bisMinus = Calendar.getInstance();
        bisMinus.set(bis.get(Calendar.YEAR), bis.get(Calendar.MONTH), bis.get(Calendar.DAY_OF_MONTH));
        
        while (schrittDatum.before(bisMinus)) {
            schrittDatum.add(Calendar.DAY_OF_YEAR, tage);
            Calendar t = Calendar.getInstance();
            t.set(schrittDatum.get(Calendar.YEAR), schrittDatum.get(Calendar.MONTH), schrittDatum.get(Calendar.DAY_OF_MONTH));
            ergebnis.add(t);
            t = null;
        }
        
        ergebnis.add(bisEnde);
        return ergebnis;
    }
    
    public static String cal2String(Calendar cal, String trennZeichen, boolean alphabetisch) {
        String tag = cal.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) : String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        String mon = cal.get(Calendar.MONTH) < 9 ? "0" + String.valueOf(cal.get(Calendar.MONTH) + 1) : String.valueOf(cal.get(Calendar.MONTH) + 1);
        String jahr = String.valueOf(cal.get(Calendar.YEAR));
        String ergebnis = alphabetisch ? jahr + trennZeichen + mon + trennZeichen + tag : tag + trennZeichen + mon + trennZeichen + jahr;
        return ergebnis;
    }
    
    public static java.sql.Date vorXTagenSql(int x) {
        return cal2sqlDate(vorXTagenCal(x));
    }
    
    public static java.sql.Date cal2sqlDate(Calendar cal) {
        return new java.sql.Date(cal.getTime().getTime());
    }
    
    public static CallableStatement swissLabAbfrage_2Codes(Connection con, java.sql.Date von, java.sql.Date bis, String code1,
            String gleich1, String code2, String gleich2) throws SQLException {
        String call = "{call dbo.PR_SUCHEWERTE(@DATUMVON= ?, @DATUMBIS= ?, @CODE1= ?, @GLEICH1= ?, @CODE2= ?, @GLEICH2= ?, @ART='RT', @AKTIV=1, @LAUF=7, @MULTITYP=4)}";
        logger.log(Level.FINEST, "Abfrage: {0}", call);
        return swissLabAbfrage(con, call, new Object[] {von, bis, code1, gleich1, code2, gleich2});
    }
    
    public static CallableStatement swissLabAbfrage_Einsender_3Codes(Connection con, java.sql.Date von, java.sql.Date bis, 
            String einsendercode, String code1, String gleich1, String code2, String gleich2, String code3,
            String gleich3) throws SQLException {
        String call = "{call dbo.PR_SUCHEWERTE(@DATUMVON=?, @DATUMBIS=?, @EINSCODE=?, @CODE1=?, @GLEICH1=?, @CODE2=?, @GLEICH2=?, @CODE3=?, @GLEICH3=?, @ART='R', @AKTIV=1, @LAUF=7, @MULTITYP=4)}";
        logger.log(Level.FINEST, "Abfrage: {0}", call);
        return swissLabAbfrage(con, call, new Object[] {von, bis, einsendercode, code1, gleich1, code2, gleich2, code3, gleich3});
    }
    
    public static String singleQuoteRemover(String s) {
        if (s.startsWith("'")) s = s.substring(1);
        if (s.endsWith("'")) s = s.substring(0, (s.length()-1));
        return s;
    }
    
    public static CallableStatement swissLabAbfrage(Connection con, String call, Object[] parameter) throws SQLException {
        CallableStatement cs = con.prepareCall(call);
        // CallableStatement führt 'Stored Procedures' aus.
        // 'call' darf mehrere Fragezeichen '?' als Platzhalter enthalten
        // diese Platzhalter werden künnen in der folgenden Schleife nach und nach befüllt werden.
        for (int i=0; i < parameter.length; i++) {
            if (parameter[i] instanceof java.sql.Date) {
                cs.setDate((i+1), (java.sql.Date) parameter[i]);
            } else if (parameter[i] instanceof Integer) {
                cs.setInt((i+1), (Integer) parameter[i]);
            } else {
                cs.setString((i+1), (String) parameter[i]);
            }
        }
        return cs;
    }
    
    public static int[] intArrayPlusOne(int[] ia) {
        int[] result = new int[ia.length];
        for (int i=0; i<ia.length; i++) {
            result[i] = ia[i] + 1;
        }
        return result;
    }
    
    
    private static String zeile(String trennZeichen, String[] felder) {
        String zeile = "";
        for (int i=0; i < felder.length; i++) {
            String feld = felder[i] == null ? "" : felder[i].trim();
//            if (!isNumeric(feld)) feld = "\"" + feld + "\"";
            if (feld.contains(trennZeichen)) feld = feld.replaceAll(trennZeichen, " ");
            if (feld.startsWith("-")) feld = feld.replaceAll("-", " ");
            zeile = zeile + feld;
            if (i < (felder.length - 1)) zeile = zeile + trennZeichen;
            feld = null;
        }
        return zeile;
    }
    
    public static void abfrageZuCSVSchreiber(DatenDBHoler d, String tZ, File eZ, boolean an) throws IOException {
        abfrageZuCSVSchreiber(d, tZ, d.getFeldBezeichnungen(), eZ, an);
    }
    
    public static void abfrageZuCSVSchreiber(DateiLeser abfrage, String trennZeichen, 
            String[] feldBezeichnungen, File exportZiel, boolean anhaengen) throws IOException {
        String titelZeile = zeile(trennZeichen, feldBezeichnungen);
        
        BufferedWriter bW = new BufferedWriter(new FileWriter(exportZiel, anhaengen));         // true heisst: Daten werden angeh?ngt
        if (anhaengen == false) {
            bW.write(titelZeile);
            bW.newLine();
        }
        for (String[] z:abfrage) {
            bW.write(zeile(trennZeichen, z));
            bW.newLine();
        }
        bW.flush();
        bW.close();
        bW = null;
    }
    
    private static class SQLDatumException extends Exception {

  		private static final long serialVersionUID = 1L;
  
  		public SQLDatumException(String message) {
              super(message);
      }
    }
    
    private static class Parameterdateifehler extends Exception {

  		private static final long serialVersionUID = 1L;
  
  		public Parameterdateifehler(String message) {
              super(message);
        }
    }
}
