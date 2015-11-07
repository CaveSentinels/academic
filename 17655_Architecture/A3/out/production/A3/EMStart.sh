#!/usr/bin/env bash
$echo OFF
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";rmiregistry\""
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java MessageManager\""
