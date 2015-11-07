#!/usr/bin/env bash
$echo OFF
$echo Starting ECS System
sleep
# ****************************************
# Original System components
$echo Starting ECS Monitoring Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java ECSConsole $1\""
$echo Starting Temperature Controller Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java TemperatureController $1\""
$echo Starting Humidity Sensor Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java HumidityController $1\""
$echo Starting Temperature Sensor
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java TemperatureSensor $1\""
$echo Starting Humidity Sensor Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java HumiditySensor $1\""
# ****************************************
# Compnents from System A
# Do not start System A Console because it has been included in System B Console.
#$echo Starting System A Console
#osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java SystemAConsole $1\""
$echo Starting IntrusionAlarmController Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java IntrusionAlarmController $1\""
$echo Starting Window Break Sensor Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java WindowBreakSensor $1\""
$echo Starting Door Break Sensor Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java DoorBreakSensor $1\""
$echo Starting Motion Detector Sensor Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java MotionDetector $1\""
# ****************************************
# Components from System B
$echo Starting System B Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java SystemBConsole $1\""
$echo Starting Fire Sensor Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java FireSensor $1\""
$echo Starting Fire Controller Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java FireAlarmController $1\""
$echo Starting Sprinkler Controller Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java SprinklerController $1\""
# ****************************************
# System C: Only the maintenance console.
$echo Starting Maintenance Console
osascript -e "tell application \"Terminal\" to do script \"cd \\\"$(PWD)\\\";java MaintenanceConsole $1\""