package A1;

/******************************************************************************************************************
 * File:A1.SystemA.java
 * Course: 17655
 * Project: Assignment 1
 * Copyright: Copyright (c) 2003 Carnegie Mellon University
 * Versions:
 *	1.0 November 2008 - Sample Pipe and Filter code (ajl).
 *
 * Description:
 *
 * This class serves as an example to illstrate how to use the PlumberTemplate to create a main thread that
 * instantiates and connects a set of filters. This example consists of three filters: a source, a middle filter
 * that acts as a pass-through filter (it does nothing to the data), and a sink filter which illustrates all kinds
 * of useful things that you can do with the input stream of data.
 *
 * Parameters: 		None
 *
 * Internal Methods:	None
 *
 ******************************************************************************************************************/
public class Plumber
{
    public static void main( String argv[])
    {
        /****************************************************************************
         * Here we instantiate the filters.
         ****************************************************************************/

        SourceFilter source = new SourceFilter("FlightData.dat");
        SinkFilter sink = new SinkFilter("TEST.txt");

        WildPointProblem pressure = new WildPointProblem();

        SinkFilter sink_invalid = new SinkFilter("WildPoints.dat");
        /****************************************************************************
         * Here we connect the filters
         ****************************************************************************/

        sink_invalid.Connect(pressure);
        sink.Connect(pressure);
        pressure.Connect(source);

        /****************************************************************************
         * Here we start the filters up. All-in-all,... its really kind of boring.
         ****************************************************************************/

        source.start();
        pressure.start();
        sink_invalid.start();
        sink.start();

    } // main

} // A1.Plumber