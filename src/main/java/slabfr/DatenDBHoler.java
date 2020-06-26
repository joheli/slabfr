package slabfr;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatenDBHoler extends DateiLeser {

	//private static final long serialVersionUID = 1L;
	private Connection con;
    private String call;
    private Object[] parameter;
    private String[] feldBezeichnungen = null;
    private String ip;
    private String[] ausgabeFelder = null;
    private final static Logger logger = Logger.getLogger(Start.class.getName());

    /**
     * Creates a new instance of DatenDBHoler
     */
    public DatenDBHoler(String ip, String call, Object[] parameter) throws SQLException, ClassNotFoundException {
        this.call = call;
        this.parameter = parameter;
        this.ip = ip;
        verbindungsEinsteller();
        tabellenHoler();
    }

    public DatenDBHoler(String ip, String call, Object[] parameter, String[] ausgabeFelder) throws SQLException, ClassNotFoundException {
        this.call = call;
        this.parameter = parameter;
        this.ip = ip;
        this.ausgabeFelder = ausgabeFelder;
        verbindungsEinsteller();
        tabellenHoler();
    }

    private void verbindungsEinsteller() throws SQLException, ClassNotFoundException {
        logger.log(Level.FINEST, "Versuche Verbindung zu {0} herzustellen...", ip);
        String sl_dburl = "jdbc:jtds:sybase://" + ip;
        Class.forName(Helfer.SL_DRIVER);
		    String sl_user = System.getenv("SL_USER"); // Umgebungsvariable muss gesetzt sein!
		    String sl_pass = System.getenv("SL_PASS"); // Umgebungsvariable muss gesetzt sein!
        this.con = DriverManager.getConnection(sl_dburl, sl_user, sl_pass);
        logger.finest("Verbindung steht.");
    }

    public String toString() {
        String returnString = "";
    	for (String[] z : this) {
            for (String f : z) {
                returnString = returnString + f + "|";
            }
            returnString = returnString + System.getProperty("line.separator");
        }
    	return returnString;
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

        rs = null;
        cs = null;
        first();
    }
}
