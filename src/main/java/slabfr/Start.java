package slabfr;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Start implements Runnable {

	private static String version = "0.4.7";
	Logger logger;
	FileHandler fH;
	private boolean veraendereExportZiel = false;
	private HashMap<String, String> p;

	public Start() throws SecurityException, IOException {
		this.logger = Logger.getLogger(this.getClass().getName());
		this.fH = new FileHandler(this.getClass().getName() + "_log.txt", true);
		fH.setFormatter(new SimpleFormatter());
		logger.addHandler(fH);
	}

	private void abfrageProzedur() {
		String abfrage;
		Object[] parameter;
		String ip;
		String[] ausgabeFelder = null;
		DatenDBHoler dbh;
		File exportZiel;
		ArrayList<Calendar[]> dKette;
		int tagSchwelle;
		String call;
		boolean anhaengen = false;
		// pOD = Parameter Ohne Datum
		ArrayList<Object> pOD = new ArrayList<Object>();
		// pODn = Parameter Ohne Datum Name
		ArrayList<String> pODn = new ArrayList<String>();

		logger.log(Level.INFO, "SwissLab Abfrageprogramm Version {0}", version);

		try {
			ip = String.valueOf(p.get("IP"));
			if (p.containsKey("Felder")) {
				ausgabeFelder = String.valueOf(p.get("Felder")).split(",");
			}
			tagSchwelle = Integer.valueOf(p.get("TagSchwelle"));

			if (p.containsKey("Fortsetzung")) {
				anhaengen = p.get("Fortsetzung").equals("ja");
			}

			Calendar vonC = Calendar.getInstance();
			Calendar bisC = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

			if (p.get("@DATUMBIS").length() < 6) {
				veraendereExportZiel = true;
				vonC.add(Calendar.DAY_OF_MONTH,
						Integer.parseInt(p.get("@DATUMVON")));
				bisC.add(Calendar.DAY_OF_MONTH,
						Integer.parseInt(p.get("@DATUMBIS")));
			} else {
				vonC = Helfer.calDatum(p.get("@DATUMVON"));
				bisC = Helfer.calDatum(p.get("@DATUMBIS"));
			}

			String vonDatum = sdf.format(vonC.getTime());
			String bisDatum = sdf.format(bisC.getTime());
			logger.log(Level.FINE, "Startdatum: {0}", vonDatum);
			logger.log(Level.FINE, "Enddatum: {0}", bisDatum);

			if (veraendereExportZiel) {
				exportZiel = new File(exportZielAnpasser(p.get("ExportZiel"),
						"_" + vonDatum + "_" + bisDatum));
			} else {
				exportZiel = new File(p.get("ExportZiel"));
			}

			abfrage = p.get("Abfrage");
			logger.info("Formuliere Abfrage ...");

			// Object[] parameter einlesen
			Iterator<String> itKys = p.keySet().iterator();
			while (itKys.hasNext()) {
				String ky = itKys.next();
				// im Folgenden werden die Abfrageparameter in die Variable pOD
				// eingelesen
				if (ky.startsWith("@") && !ky.startsWith("@DATUM")) {
					// System.out.println(ky);
					String parWert = p.get(ky);
					pODn.add(ky);
					if (parWert.startsWith("'")) {
						pOD.add(Helfer.singleQuoteRemover(parWert));
					} else {
						pOD.add(Integer.valueOf(parWert));
					}
				}
			}

			// String call erstellen
			call = Helfer.CALL_START_1 + abfrage + Helfer.CALL_START_2;
			for (int i = 0; i < pODn.size(); i++) {
				call += pODn.get(i) + "=?";
				if (i < (pODn.size() - 1)) {
					call += ", ";
				}
			}
			call += Helfer.CALL_END;

			logger.info("Schicke Abfrage zu Swisslab Datenbank ...");

			int anzAbfragen = 1;

			// Das Zeitfenster wird je nach Tagschwelle zerlegt
			if ((bisC.getTimeInMillis() - vonC.getTimeInMillis()) > 2
					* tagSchwelle * 24 * 60 * 60 * 1000L) {
				logger.log(
						Level.FINE,
						"Die Abfrage wird zerlegt, da der Zeitabstand über dem Doppelten der Tagschwelle (={0} Tage) liegt",
						tagSchwelle);
				dKette = Helfer.datumKette2(vonC, bisC, tagSchwelle);
				for (int x = 0; x < dKette.size(); x++) {
					parameter = new Object[2 + pOD.size()];

					// die ersten zwei Parameter sind 'Datum von' und 'Datum
					// bis'
					parameter[0] = Helfer.cal2sqlDate(dKette.get(x)[0]);
					parameter[1] = Helfer.cal2sqlDate(dKette.get(x)[1]);

					for (int y = 0; y < pOD.size(); y++) {
						parameter[2 + y] = pOD.get(y);
					}

					logger.log(Level.FINE,
							"übermittle Teilabfrage {0} ...", anzAbfragen);
					anzAbfragen++;
					dbh = new DatenDBHoler(ip, call, parameter, ausgabeFelder);
					
					Helfer.abfrageZuCSVSchreiber(dbh, ";", exportZiel, anhaengen | (x > 0));

					parameter = null;
					dbh = null;
				}
			} else {
				logger.log(
						Level.FINE,
						"Die Abfrage wird nicht zerlegt, da der Zeitabstand unter dem Doppelten der Tagschwelle (={0} Tage) liegt",
						tagSchwelle);
				parameter = new Object[2 + pOD.size()];

				parameter[0] = Helfer.cal2sqlDate(vonC);
				parameter[1] = Helfer.cal2sqlDate(bisC);

				for (int y = 0; y < pOD.size(); y++) {
					parameter[2 + y] = pOD.get(y);
				}

				dbh = new DatenDBHoler(ip, call, parameter, ausgabeFelder);
				
				Helfer.abfrageZuCSVSchreiber(dbh, ";", exportZiel, anhaengen);

				parameter = null;
				dbh = null;
			}
			logger.log(
					Level.INFO,
					"Die Abfrage wurde erfolgreich in die Datei {0} exportiert.",
					exportZiel.getCanonicalPath());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Start s = new Start();
			if (args.length > 0) {
				try {
					s.p = Helfer.parameterLeser(new File(args[0]));
					s.logger.log(Level.INFO, "Parameterdatei ok.");
					s.run();
				} catch (Exception e) {
					s.logger.log(Level.SEVERE, e.getMessage());
				} finally {
					s.fH.close();
				}
			} else {
				s.logger.log(Level.SEVERE, "Bitte Parameterdatei angeben!");
				s.hilfe();
			}
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}
	}

	private void hilfe() {
		System.out.println();
		System.out.println("Gebrauch des Programmes slabfr, Version " + version
				+ ":");
		System.out.println();
		if (System.getProperty("os.name").equalsIgnoreCase("Linux")) {
		  System.out.println("    ./slabfr Parameterdatei");
		} else {
		  System.out.println("    slabfr.bat Parameterdatei");
		}
		System.out.println();
	}

	private String exportZielAnpasser(String exportZiel, String zusatz) {
		if (!exportZiel.contains(".")) {
			logger.log(
					Level.WARNING,
					"Exportziel hat keine vernünftige Dateiendung. Es wird Standarddatei 'export.csv' gewählt.");
			exportZiel = "export.csv";
		}
		// wo ist der letzte Punkt?
		int i = exportZiel.lastIndexOf(".");
		String prefix = exportZiel.substring(0, i);
		String suffix = exportZiel.substring(i, exportZiel.length());

		// gib es zurück
		return prefix + zusatz + suffix;
	}

	public void run() {
		abfrageProzedur();
	}
}
