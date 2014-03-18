#!/bin/sh
if [ -z "$minisat" ]
then
    if [ -x $(dirname $0)/minisat-all-models ]
    then
        minisat=$(dirname $0)/minisat-all-models
    elif [ -x $(dirname $0)/minisat ]
    then
        minisat=$(dirname $0)/minisat
    elif which minisat-all-models > /dev/null
    then
        minisat=minisat-all-models
    elif [ -x ../../../minisat_all_models/core/minisat ]
    then 
        minisat=../../../minisat_all_models/core/minisat
    fi
fi
if [ ! -z "$minisat" ]
then
    echo $minisat "$1 > $3"
    $minisat "$1" $4 $5 > "$3"
else
    echo "No minisat-all-models"
    exit 1
fi
