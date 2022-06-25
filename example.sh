# build typescript frontend
(
  cd client || exit
  npm run build
)

# create fat jar with all dependencies
./gradlew shadowJar

# run example application
java -jar ./build/libs/siweb-framework-*-all.jar