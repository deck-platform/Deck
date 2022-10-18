javac -d ./build -cp ../../../../../android.jar *.java
cd build/ || exit
jar cvf deck-android.jar *
echo "Output: deck-android.jar"
# deck-android.jar should move to Deck-enabled Android procject
# cp deck-android.jar xxx
