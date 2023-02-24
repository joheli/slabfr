package slabfr;

/* import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException; */
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

public class DatenDBHoler extends DateiLeser {

	  private Connection con;
    private String call;
    private Object[] parameter;
    private String[] feldBezeichnungen = null;
    private String ip;
    private String[] ausgabeFelder = null;
    private Properties connectionInfo;
    private final static Logger logger = Logger.getLogger(Start.class.getName());

    /**
     * Creates a new instance of DatenDBHoler
     */
     
    public DatenDBHoler(String ip, String call, Object[] parameter, Properties connectionInfo) {
        this(ip, call, parameter, null, connectionInfo);
    }
    
    public DatenDBHoler(String ip, String call, Object[] parameter, String[] ausgabeFelder, Properties connectionInfo) {
        this.call = call;
        this.parameter = parameter;
        this.ip = ip;
        this.ausgabeFelder = ausgabeFelder;
        this.connectionInfo = connectionInfo;
        try {
          verbindungsEinsteller();
          tabellenHoler();
          kappeVerbindung();
        } catch (Exception e) {
          logger.log(Level.SEVERE, "DatenDBHoler war nnicht erfolgreich. Folgender Fehler wurde gemeldet: {0}", e.getMessage());
        }
    }

    private void verbindungsEinsteller() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        logger.log(Level.FINE, "Versuche Verbindung zu {0} herzustellen...", ip);
        String sl_dburl = "jdbc:jtds:sybase://" + ip;
        // String sl_dburl = "jdbc:sybase:Tds:" + ip;
        // Class.forName(Helfer.SL_DRIVER);
        DriverManager.registerDriver((Driver) Class.forName(Helfer.SL_DRIVER).newInstance());
        // copyConnectionInfo ist eine Kopie von connectionInfo, welches in die LogDatei geschrieben werden kann
        Properties copyConnectionInfo = (Properties) connectionInfo.clone();
        copyConnectionInfo.setProperty("user", "XXX"); // überschreibe "user"
        copyConnectionInfo.setProperty("password", "XXX"); // überschreibe "password"
        logger.log(Level.FINE, "Inhalt von connectionInfo: {0}", copyConnectionInfo.toString());
        // Verbindung mit url und connectionInfo herstellen
        this.con = DriverManager.getConnection(sl_dburl, connectionInfo);
        logger.log(Level.FINEST, "Verbindung {0} steht.", con.toString());
    }

    public String[] getFeldBezeichnungen() {
        return feldBezeichnungen;
    }

    private boolean feldKommtInErgebnis(String feldName, String[] feldListe) {
        for (String s : feldListe) {
            if (s.startsWith(feldName)) {
                return true;
            }
        }
        return false;
    }

    private void tabellenHoler() throws SQLException {
        logger.log(Level.FINEST, "Sende Abfrage zu {0}", ip);
        CallableStatement cs = Helfer.swissLabAbfrage(con, call, parameter);
        ResultSet rs = cs.executeQuery();

        int spaltenAnz = rs.getMetaData().getColumnCount();
        int zeilenAnz = 0;

        ArrayList<String> feldBezeichnungenTemp = new ArrayList<String>();
        ArrayList<Integer> spaltenNr = new ArrayList<Integer>();

        int i = 1;

        if (ausgabeFelder != null) {
            logger.finest("Nur ausgewählte Spalten werden eingebunden.");
            for (i = 1; i <= spaltenAnz; i++) {
                if (feldKommtInErgebnis(rs.getMetaData().getColumnName(i), ausgabeFelder)) {
                    spaltenNr.add(Integer.valueOf(i));
                    feldBezeichnungenTemp.add(rs.getMetaData().getColumnName(i));
                }
            }
        } else {
            logger.finest("Alle Spalten werden eingebunden.");
            for (i = 1; i <= spaltenAnz; i++) {
                spaltenNr.add(Integer.valueOf(i));
                feldBezeichnungenTemp.add(rs.getMetaData().getColumnName(i));
            }
        }

        feldBezeichnungen = feldBezeichnungenTemp.toArray(new String[feldBezeichnungenTemp.size()]);

        while (rs.next()) {
            ArrayList<String> z = new ArrayList<String>();
            for (Integer j : spaltenNr) {
                z.add(rs.getString(Integer.valueOf(j)));
            }
            this.add(z.toArray(new String[z.size()]));
//            System.out.print(".");
            z = null;
            zeilenAnz++;
        }

        logger.log(Level.FINEST, "Abfrage erfolgreich. Es wurden {0} Datensätze zurückgeliefert.", zeilenAnz);

        //rs = null;
        //cs = null;
        rs.close();
        logger.log(Level.FINEST, "ResultSet {0} geschlossen.", rs.toString());
        cs.close(); 
        logger.log(Level.FINEST, "Statement {0} geschlossen.", cs.toString());
        first();
    }
    
    private void kappeVerbindung() throws SQLException {
      if (con != null) {
        con.close();
        logger.log(Level.FINEST, "Verbindung {0} geschlossen.", con.toString());
      }
    }
}
