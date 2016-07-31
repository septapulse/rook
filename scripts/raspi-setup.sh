#!/bin/bash

# check sudo
if [ "$EUID" -ne 0 ]; then 
  echo "This script requires root permissions. Try running like this instead:"
  echo "  sudo ${0}"
  exit
fi

# check internet connection
ping -qc 1 www.github.com &> /dev/null || { echo "Could not connect to www.github.com. Exiting..." ; exit 1 }

# starting. will take a while.
printf "\n\nThis may take a while... Go grab a coffee or something.\n\n"
sleep 5

# update system
apt-get update
apt-get -y upgrade

# install dependencies
apt-get -y install git
apt-get -y install maven
apt-get -y install oracle-java8-jdk
apt-get -y install vim
 
# update-alternative
sudo update-alternatives --set java /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/bin/javac

# JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt
echo "export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt" >> ${HOME}/.bashrc

# TODO install latest version of rook to home directory

# TODO start rook on system startup

# TODO be able to build from source or use latest release

