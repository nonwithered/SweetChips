mkdir build/lib build/classes
cd .. && ./gradlew publish && cd test
from_path="../build/org/sweetchips/"
from_file="$(ls ${from_path})"
cp="lib/asm-5.1.jar:lib/gradle-api-3.1.4.jar"
in=""
for i in ${from_file}; do
    cp="${cp}:${from_path}${i}/1.0.0/${i}-1.0.0.jar"
    in="${in} ${from_path}${i}/1.0.0/${i}-1.0.0.jar"
done
java -cp ${cp} org.sweetchips.plugin4gradle.demo.Launcher ${in} < in.txt