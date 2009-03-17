nexmlLuceneIndex is a standardalone program which reads a list of *.nexml files, parses
and indexes them based on a list of user specified fields.  The indexer is done using
Lucene, and parsing is based on the current NeXML schema which is evolving.

The program is for proof-of-concept, and therefore, very effort is spent in error checking
and making it configurable.

To compile and run the program, make sure you have Maven installed:
+ mvn -e exec:java -Dexec.mainClass=org.nescent.dbhack.phylr.lucene.Main
