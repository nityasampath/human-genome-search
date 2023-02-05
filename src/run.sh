#!/bin/sh

javac -d ../bin GenomeSearch.java
java ../bin/GenomeSearch "$@" 