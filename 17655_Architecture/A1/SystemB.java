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
public class SystemB
{
    public static void main( String argv[])
    {
        /****************************************************************************
         * Here we instantiate the filters.
         ****************************************************************************/

        SourceFilter source = new SourceFilter("FlightData.dat");
        SplitFilter split = new SplitFilter();
        TempConversion temperature = new TempConversion();
        AltitudeConversion altitude = new AltitudeConversion();
        WildPointProblem pressure = new WildPointProblem();

        MergeFrameFilter merge = new MergeFrameFilter();
        SinkFilter sink = new SinkFilter("OutputB.dat");
        SinkFilter sink_invalid = new SinkFilter("WildPoints.dat");

        /****************************************************************************
         * Here we connect the filters
         ****************************************************************************/
        /* Actual connections */
        sink.Connect(merge);
        sink_invalid.Connect(pressure);

        merge.Connect(altitude);
        merge.Connect(temperature);
        merge.Connect(pressure);

        altitude.Connect(split);
        temperature.Connect(split);
        pressure.Connect(split);

        split.Connect(source);

        
          
      

        /****************************************************************************
         * Here we start the filters up. All-in-all,... its really kind of boring.
         ****************************************************************************/
      	System.out.println("start");
     /*Actual start */
        source.start();
        split.start();
        temperature.start();
        altitude.start();
        pressure.start();
        merge.start();
        sink.start();
        sink_invalid.start();

    } // main

} // A1.Plumber