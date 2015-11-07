package A1;

/******************************************************************************************************************
 * File:A1.SystemC.java
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
public class SystemC
{
    public static void main( String argv[])
    {
        /****************************************************************************
         * Here we instantiate the filters.
         ****************************************************************************/

        SourceFilter sourceA = new SourceFilter("SubSetA.dat");
        SourceFilter sourceB = new SourceFilter("FlightData.dat");
        MergeStreamFilter merge = new MergeStreamFilter();
        Filter_LessThan10KAltitude altitude = new Filter_LessThan10KAltitude();
        SinkFilter sinkLessThan10K = new SinkFilter("LessThan10K.dat");
        WildPointProblem pressure = new WildPointProblem();
        SinkFilter sink_invalid = new SinkFilter("PressureWildPoints.dat");

        /****************************************************************************
         * Here we connect the filters
         ****************************************************************************/

        sinkLessThan10K.Connect(altitude);
        sink_invalid.Connect(pressure);
        pressure.Connect(altitude);
        altitude.Connect(merge);
        merge.Connect(sourceB);
        merge.Connect(sourceA);

        /****************************************************************************
         * Here we start the filters up. All-in-all,... its really kind of boring.
         ****************************************************************************/

        sourceA.start();
        sourceB.start();
        merge.start();
        altitude.start();
        pressure.start();
        sinkLessThan10K.start();
        sink_invalid.start();

    } // main

} // A1.SystemC