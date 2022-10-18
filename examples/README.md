# How to run

1. Put `deck-1.0-jar-with-dependencies.jar` in this folder

2. Run command: such as `java -classpath deck-1.0-jar-with-dependencies.jar picture/HandlePicture.java`

# How to generate a single dex file

1. Enter a folder with example.

2. Run command: `java -cp ../deck-1.0-jar-with-dependencies.jar dextest.java` will generate `dextest.class`.

3. Run command: `d8 dextest.class` will generate `classes.dex`.
