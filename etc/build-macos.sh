JAVA_HOME=$JAVA17_HOME

cd "$(dirname "$0")/.."

mvn clean package -DskipTests

$JAVA_HOME/bin/jpackage @target/jpackage-macos.args --type app-image

$JAVA_HOME/bin/jpackage @target/jpackage-macos.args --type dmg
