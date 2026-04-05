CarRentalSystem (modular split)

Files:
- CarRentalSystem.java (entry point)
- Common.java (shared config, DB connection handle, console helpers)
- DatabaseManager.java
- MainMenuModule.java
- CarModule.java
- BookingModule.java
- CustomerModule.java
- ReportModule.java

Compile:
javac *.java

Run (with MySQL Connector/J in classpath):
java -cp .;mysql-connector-j-8.x.x.jar CarRentalSystem
