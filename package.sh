#/bin/bash
TARGET=$1
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [[ -z $TARGET ]]; then
  echo "Choose:"
  echo "  1. ./target/rook/"
  echo "  2. ${HOME}/rook/"
  echo "  3. Custom..."
  printf "Enter Target Choice: "
  read CHOICE
  if [[ $CHOICE == "1" ]]; then
    TARGET="./target/rook/"
  elif [[ $CHOICE == "2" ]]; then
    TARGET="${HOME}/rook/"
  else
    echo "ProTip: You can pass a custom target as an argument to bypass this prompt"
    printf "Enter Custom Target Path: "
    read LOCAL_TARGET;
  fi
fi

# Check if target is local or remote
if [[ $TARGET == *":"* ]]; then
  LOCAL_TARGET="./target/.tmp"
  REMOTE_TARGET=$TARGET
else
  echo $TARGET
  LOCAL_TARGET=$TARGET
fi

# setup LOCAL_TARGET and REMOTE_TARGET
rm -rf "${LOCAL_TARGET}/platform"
mkdir -p "${LOCAL_TARGET}"
LOCAL_TARGET=`cd "${LOCAL_TARGET}"; pwd`
if [[ -z $REMOTE_TARGET ]]; then
  echo "Target: ${LOCAL_TARGET}"
else
  echo "Local Target: $LOCAL_TARGET"
  echo "Remote Target: $REMOTE_TARGET"
fi

# Make sure directory ends with a /
if [[ "${LOCAL_TARGET: -1}" != "/" ]]; then
  LOCAL_TARGET="${LOCAL_TARGET}/"
fi

# Make the local directory
mkdir -p ${LOCAL_TARGET}

# Setup build directories
echo "Building to ${LOCAL_TARGET}"
BIN=${LOCAL_TARGET}bin/
PLATFORM=${LOCAL_TARGET}platform/
USER=${LOCAL_TARGET}usr/
mkdir -p $PLATFORM
mkdir -p $USER

# Clean PLATFORM directory
rm -rf ${PLATFORM}/*

# Copy scripts
echo "Copying Scripts"
cd ${DIR}
cp -f scripts/start.sh ${LOCAL_TARGET}/
cp -f scripts/stop.sh ${LOCAL_TARGET}/
cp -f scripts/cli.sh ${LOCAL_TARGET}/

# Build rook/java
echo "Building Java Platform"
cd ${DIR}/java/
mvn clean install package assembly:directory -DskipTests
echo "Replacing ${PLATFORM}"
rm -rf ${PLATFORM}
mkdir -p ${PLATFORM}
cp -r target/rook-*-distribution/* ${PLATFORM}

# Copy rook/html
echo "Copying Daemon HTML"
cd ${DIR}/html
cp -r daemon ${PLATFORM}/

# Build platform ui zip files
echo "Building UI zip files"
mkdir ${PLATFORM}/ui
cd ${DIR}/html/ui/packagemanager/ && zip -r ${PLATFORM}/ui/packagemanager.zip *
cd ${DIR}/html/ui/processmanager/ && zip -r ${PLATFORM}/ui/processmanager.zip *
cd ${DIR}/html/ui/sensorlogger/ && zip -r ${PLATFORM}/ui/sensorlogger.zip * 

# Copy to remote
if [[ -z $REMOTE_TARGET ]]; then
  # No remote. Done now
  echo "Done."
else
  scp -r $LOCAL_TARGET $REMOTE_TARGET
  echo "Copied. Done."
fi
