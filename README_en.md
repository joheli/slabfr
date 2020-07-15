# slabfr

![](https://github.com/joheli/slabfr/workflows/Java%20CI/badge.svg)

[deutsch](README.md)

`slabfr` is a simple query tool for the laboratory information system [Swisslab](https://www.nexus-swisslab.de). The following features distinguish it from proprietary solutions by Swisslab:

  * possible scheduled and automated use
  * possible prospective use with specification of temporal frames relative to query date
  * possible issuing of queries spanning many years
  * use on different operating systems, provided a Java runtime environment is available

## Prerequisites

The program needs a Java runtime environment of 1.8 or higher. Java can be obtained from [Oracle](https://www.java.com/de/download/) or other sources.
  
## Use

### Parameter file

The program just uses one positional argument specifying the parameter file. The [template](Parameterdatei/Parameter.Muster) is currently in German, as Swisslab currently has not spread beyond German speaking countries. The parameter file specifies

  * the internet address of the Swisslab server
  * the name of the query procedure, e.g. `PR_SUCHEWERTE`
  * query parameters like `@DATUMVON`, `@DATUMBIS`, etc.
  * the path of the export file.

### Environment variables

Access to the Swisslab database requires credentials, which currently have to be passed via environmental variables. Here, `SL_USER` specifies the user, while `SL_PASS` must contain a valid password.

### Call

Please type `./slabfr {Parameterdatei}` or `slabfr.bat {Parameterdatei}` for operating systems Windows or Linux, respectively.

### Scheduled use

You can schedule calls with tools on your operating system. For Linux use `cron`, while for Windows use the 'Task Scheduler'.

## Binary versions

Please see 'Releases'.

## Tip

`slabfr` can be combined with [DaBaDEx](https://github.com/joheli/DaBaDEx) to retrieve data and upload it to a separate database.


