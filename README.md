# slabfr

![](https://github.com/joheli/slabfr/workflows/Java%20CI/badge.svg)

`slabfr` ist ein einfaches Abfrageprogramm für das Laborinformationssystem [Swisslab](https://www.nexus-swisslab.de). Folgende Eigenschaften unterscheiden es von den Swisslab-eigenen Abfragewerkzeugen:

  * möglicher zeitgesteuerter und automatisierter Einsatz
  * möglicher prospektiver Einsatz mit Anfrage von relativen Zeitfenstern ab Abfragedatum
  * mögliche Abfrage von langen Zeitabschnitten auch über viele Jahre
  * Einsatz auf verschiedenen Betriebssystemen, welche eine Java-Laufzeitumgebung vorhalten

## Voraussetzungen

Das Programm erfordert eine Java Laufzeitumgebung von Version 1.8 oder höher. Java kann z.B. von [Oracle](https://www.java.com/de/download/) heruntergeladen werden.
  
## Nutzung

### Parameterdatei

Das Programm benötigt als einziges Argument eine Parameterdatei. Die beiliegende [Musterdatei](Parameterdatei/Parameter.Muster) ist durch die Kommentierung mehr oder weniger selbsterklärend. In ihr ist die Angabe von

  * Adresse des Swisslab-Servers
  * Auswahl der Abfrageprozedur, z.B. `PR_SUCHEWERTE`
  * Angabe von Abfrageparametern, wie z.B. `@DATUMVON`, `@DATUMBIS`, etc.
  * Angabe der Exportdatei im csv-Format
  
erforderlich.

### Umgebungsvariablen

Der Zugriff auf die Swisslab Datenbank erfordert Kontodaten, die in Form von Umgebungsvariablen bereitgestellt werden müssen. `SL_USER` muss hierbe den zugreifenden Nutzer, `SL_PASS` das zugehörige Passwort beinhalten.

### Aufruf

Abhängig vom Betriebssystem muss die Datei `slabfr.bat` (für Windows) oder `slabfr` (für Linux) eingesetzt werden. Für Linux erfolgt der Aufruf z.B. durch die Eingabe `./slabfr {Parameterdatei}`, bei Windows hingegen durch `slabfr.bat {Parameterdatei}`.

### Zeitsteuerung

Die Zeitsteuerung kann über Bordmittel des Betriebssystems eingerichtet werden. Für Linux eignet sich das Programm `cron`, wohingegen bei Windows die "Aufgabenplanung" eingesetzt werden kann. 

## Ausführbare Version

Siehe unter 'Releases'.

## Tipp

Das Programm `slabfr` kann mit dem Programm [DaBaDEx](https://github.com/joheli/DaBaDEx) kombiniert werden, um Daten z.B. in eine Überwachgungsdatenbank zu transferieren.


