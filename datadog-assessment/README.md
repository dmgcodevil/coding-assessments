## Requirements

1) Download and install scala sdk from here:  https://www.scala-lang.org/download/ , see: `Other ways to install Scala`

## How to run

from command line execute `scala app.jar -f [absolute_path_to_log_file]` or 
`scala app.jar` to read log entries from the console

## Example:

Folder structure:

```
./
  | - app.jar
  | - sample_csv.txt
```

From the command line: `scala app.jar -f ./sample_csv.txt`

For help use `scala app.jar --help` : 

```
'--metrics-interval-millis' - time duration in millis to flush collected metrics (optional). default = 10000
'-f' - a path to http log file (optional). If not specified the program reads from the console
'--log-poll-interval-millis' - time duration in millis to check the log file for changes (optional). default =  1000
'--threshold-per-sec' - max number of hits per second allowed within 'alert-interval-millis' (optional). default = 10
'--alert-interval-millis' - time duration in millis to monitor alerts (optional). default =  120000
```

## How to run tests

1) Install `sbt`
2) From the project folder: `sbt test`


## How to build

1) Install `sbt`
2) From the project folder: `sbt assembly`
3) `cd ./target/scala-2.13` you should see  `app.jar`

## Overall architecture

The application consists of the following basic components:
1) `LogReader` reads log entries from a buffered stream. 
`LogReader` provides a smart constructors to instantiate a specific implementation, 
e.g.: `LogReader.console` uses `LogReader` implementation to read from the console.

2) `LogParser` parses a raw string into a specific `LogEntry` type. For instance, `HttpLogParser` parses a raw string into `HttpLogEntry`

3) `Monitor` receives `LogEntry`s and accumulates metrics. `HttpMonitor` requires two dependencies:  `MetricsCollector` and `Alerter`

4) `HttpMetricsCollector` accumulates metrics for each unique resource section and submits them to `EventBus` every `metrics-interval-millis`

5) `Alerter` watches for alerts for each unique resource section and submits them to `EventBus` every `alert-interval-millis`

6) `Reporter` accepts an `Event` and displays an information. For instance: `ConsoleReporter` prints an event to the console

7) `Event` interface marker used to mark some objects that can be sent to `EventBus`

8) `EventBus` is a  simple `PUB-SUB` based on non-blocking queue

## Time and Space complexity

Time - O(N) where N - number of log entries
Space - O(N) where N - number of unique resource sections, i.e. for the following three log lines:

```
"10.0.0.1","-","apache",1549574322,"GET /report HTTP/1.0",500,1234
"10.0.0.2","-","apache",1549574329,"POST /api/user HTTP/1.0",200,1261
"10.0.0.1","-","apache",1549573862,"GET /api/help HTTP/1.0",200,1234
```

we need to keep metrics only for two sections: `/report` and `/api`


## Improvements

1) Change `MainApp` to read logs from multiple files. Likely it's easy to do by creating a function:

`def process(f: File): IO[Unit] = ???` then call it for each provided file and run in **parallel**:

```
  def process(files: List[File]): IO[Unit] = {
    files.map(process).parSequence_
  }
```

2) Split load between multiple workers by using consistent hashing on the first resource section:

`hash(resource.firstSection)  % workersN`

where each `Worker` will have an exclusive access to a particular `HttpMonitor`.

`EventBus`  will be shared by all workers and subscribers.

3) Use bounded queue to enable `back-pressure`