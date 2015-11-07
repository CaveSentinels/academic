%ECHO OFF
%ECHO Starting ECS System
PAUSE

REM ==================================================
REM Original System

%ECHO Starting ECS Monitoring Console
START "ECS CONSOLE" /MIN /NORMAL java ECSConsole %1

%ECHO Starting Temperature Controller Console
START "TEMPERATURE CONTROLLER" /MIN /NORMAL java TemperatureController %1

%ECHO Starting Humidity Controller
START "HUMIDITY CONTROLLER" /MIN /NORMAL java HumidityController %1

%ECHO Starting Temperature Sensor
START "TEMPERATURE SENSOR" /MIN /NORMAL java TemperatureSensor %1

%ECHO Starting Humidity Sensor Console
START "HUMIDITY SENSOR" /MIN /NORMAL java HumiditySensor %1

REM ==================================================
REM Components added in System A

REM Do not start System A Console because it has been included in System B Console.

REM %ECHO Starting System A Console
REM START "SYSTEM A CONSOLE" /MIN /NORMAL java SystemAConsole %1

%ECHO Starting IntrusionAlarmController Console
START "INTRUSION ALARM CONTROLLER" /MIN /NORMAL java IntrusionAlarmController %1

%ECHO Starting Window Break Sensor Console
START "WINDOW BREAK SENSOR" /MIN /NORMAL java WindowBreakSensor %1

%ECHO Starting Door Break Sensor
START "DOOR BREAK SENSOR" /MIN /NORMAL java DoorBreakSensor %1

%ECHO Starting Motion Detector
START "MOTION DETECTOR" /MIN /NORMAL java MotionDetector %1

REM ==================================================
REM Components added in System B

%ECHO Starting System B Console
START "SYSTEM B CONSOLE" /MIN /NORMAL java SystemBConsole %1

%ECHO Starting Fire Sensor Console
START "FIRE SENSOR CONSOLE" /MIN /NORMAL java FireSensor %1

%ECHO Starting Fire Controller Console
START "FIRE CONTROLLER CONSOLE" /MIN /NORMAL java FireAlarmController %1

%ECHO Starting Sprinkler Controller Console 
START "SPRINKLER CONTROLLER CONSOLE" /MIN /NORMAL java SprinklerController %1

REM ==================================================
REM System C

%ECHO Starting Maintenance Console
START "MAINTENANCE CONSOLE" /MIN /NORMAL java MaintenanceConsole %1