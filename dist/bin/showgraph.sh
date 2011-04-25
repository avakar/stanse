#!/bin/bash

TFILE=`mktemp --suffix .pdf`
dot -Tpdf -o $TFILE
evince $TFILE
rm $TFILE
