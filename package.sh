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
    read TARGET;
  fi
fi

rm -rf "${TARGET}"
mkdir -p "${TARGET}"
TARGET=`cd "${TARGET}"; pwd`
echo "Target: ${TARGET}"

if [[ "${TARGET: -1}" != "/" ]]; then
  TARGET="${TARGET}/"
fi
mkdir -p ${TARGET}

echo "Building to ${TARGET}"

BIN=${TARGET}bin/
PLATFORM=${TARGET}platform/
USER=${TARGET}usr/

#mkdir -p $BIN
mkdir -p $PLATFORM
mkdir -p $USER

echo "Copying Scripts"
cd ${DIR}
cp -f scripts/start.sh ${TARGET}/
cp -f scripts/stop.sh ${TARGET}/

echo "Building Java Platform"
cd ${DIR}/java/
mvn clean install package assembly:directory -DskipTests
echo "Replacing ${PLATFORM}"
rm -rf ${PLATFORM}
mkdir -p ${PLATFORM}
cp -r target/rook-*-distribution/* ${PLATFORM}
