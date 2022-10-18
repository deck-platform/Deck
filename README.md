# Deck
Analyze data easily and flexibly on decentralized Android devices.

## Code Structure
* device/: Android side code 
* gateway/: Server side code 
* deck/: Deck package for developer 
* examples/: Example query tasks
* resources/: Part resources needed

## How to Run
1. Generate the `deck-1.0-jar-with-dependencies.jar` in deck folder.
    ```shell
    mvn assembly:assembly -f pom.xml
    ```
2. Run the gateway. 
   1. Run the redis.
   2. Install d8.jar to generate `.dex` file from `.java` files.
       ```
       mvn install:install-file -DgroupId=com.android.tools
       -DartifactId=r8
       -Dversion=1.0.0
       -Dpackaging=jar
       -Dfile=/tmp/deckData/resources/d8.jar
       ```
   3. Put the `deck-1.0-jar-with-dependencies.jar` and `android` in `/tmp/deckData/resources/soot/` to support soot.
   4. Modify the `application.properties` to configure the basic configuration such as redis configuration and basic directory.
   5. Run the gateway.
3. Install the app in Android.
   1. Create `deck/libs/` dir and put `deck-1.0-jar-with-dependencies.jar` into it.
   2. Create `app/assets/databases/` dir and put `sysinfo.db` into it.
   3. Create `app/assets/imgs/` dir and put multiple images into it.
   4. Modify the server url in `deck/gradle.properties`.
   5. Install app in phone.
4. Run the first Deck Task.
   ```shell
    cd examples/sqlite-query
    # modify the SQLQuery.java to change some basic configuration such as the server url.
    sh developer_run.sh
    ```
