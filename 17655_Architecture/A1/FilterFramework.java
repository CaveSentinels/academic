package A1;
/******************************************************************************************************************
* File:A1.FilterFramework.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Initial rewrite of original assignment 1 (ajl).
*
* Description:
*
* This superclass defines a skeletal filter framework that defines a filter in terms of the input and output
* ports. All filters must be defined in terms of this framework - that is, filters must extend this class
* in order to be considered valid system filters. Filters as standalone threads until the inputport no longer
* has any data - at which point the filter finishes up any work it has to do and then terminates.
*
* Parameters:
*
* InputReadPorts:	This is a list of filter's input ports. Each filter can have multiple input ports.
 *                  Essentially this port is connected to another filter's piped
*					output steam. All filters connect to other filters by connecting their input ports to other
*					filter's output ports. This is handled by the Connect() method.
*
* OutputWritePort:	This is a list of filter's output port. Each filter can have multiple input ports.
 *                  Essentially the filter's job is to read data from the input port,
*					perform some operation on the data, then write the transformed data on the output port.
*
* A1.FilterFramework:  This is a reference to the filters that are connected to the instance filter's input ports. This
*					reference is to determine when the upstream filter has stopped sending data along the pipe.
*
* Internal Methods:
*
*	public void Connect( A1.FilterFramework Filter )
*	public byte ReadFilterInputPort()
*	public void WriteFilterOutputPort(byte datum)
*	public boolean EndOfInputStream()
*
******************************************************************************************************************/

import java.io.*;
import java.util.ArrayList;

public class FilterFramework extends Thread
{
	// Define list of filter input and output ports

    protected ArrayList<PipedInputStream> InputReadPorts = new ArrayList<PipedInputStream>();
    protected ArrayList<PipedOutputStream> OutputWritePorts = new ArrayList<PipedOutputStream>();

	// The following list of references to filters is used because java pipes are able to reliably
	// detect broken pipes on the input port of the filter. This variable will point to
	// the previous filter in the network and when it dies, we know that it has closed its
	// output pipe and will send no more data.

    private ArrayList<FilterFramework> InputFilters = new ArrayList<FilterFramework>();


    // This field is useful for filters that have multiple input ports so they don't mix bytes from different streams
    protected PipedInputStream currentInputPort;

    // This is used to give fair distribution while reading input from multiple ports
    private int inputPortLooper = 0;


	/***************************************************************************
	* InnerClass:: EndOfStreamExeception
	* Purpose: This
	*
	*
	*
	* Arguments: none
	*
	* Returns: none
	*
	* Exceptions: none
	*
	****************************************************************************/

	class EndOfStreamException extends Exception {
		
		static final long serialVersionUID = 0; // the version for serializing

		EndOfStreamException () { super(); }

		EndOfStreamException(String s) { super(s); }

	} // class


    /***************************************************************************
     * This is meant to be overridden by sub classes if they have limitations
     * or constraints on the allowed number of input and output ports
     ****************************************************************************/

    void validateNumberOfPorts(){
        // to be overridden
    }


	/***************************************************************************
	* CONCRETE METHOD:: Connect
	* Purpose: This method connects filters to each other.
     * Calling this method adds a new input port to the current object, and a new output port
     * to the passed filter.
	*
	* Arguments:
	* 	A1.FilterFramework - this is the filter that this filter will connect to.
	*
	* Returns: void
	*
	* Exceptions: IOException
	*
	****************************************************************************/

	void Connect( FilterFramework Filter )
	{
		try
		{
			// Create new input port in the current filter
            PipedInputStream newInputPort = new PipedInputStream();
            this.InputReadPorts.add(newInputPort);

            // Create new output port to the passed filter
            PipedOutputStream newOutputPort = new PipedOutputStream();
            Filter.OutputWritePorts.add(newOutputPort);

            // Connecting the ports together
            newInputPort.connect(newOutputPort);
            InputFilters.add(Filter);

		} // try

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " A1.FilterFramework error connecting::"+ Error );

		} // catch


        validateNumberOfPorts();

	} // Connect

	/***************************************************************************
	* CONCRETE METHOD:: ReadFilterInputPort
	* Purpose: This method reads data from the input ports one port at a time and one byte at a time.
	*
	* Arguments: void
	*
	* Returns: byte of data read from the input port of the filter.
	*
	* Exceptions: IOExecption, EndOfStreamException (rethrown)
	*
	****************************************************************************/

	byte ReadFilterInputPort() throws EndOfStreamException
	{
		byte datum = 0;

		/***********************************************************************
		* Since delays are possible on upstream filters, we first wait until
		* there is data available on the input port. We check,... if no data is
		* available on the input port we wait for a quarter of a second and check
		* again. Note there is no timeout enforced here at all and if upstream
		* filters are deadlocked, then this can result in infinite waits in this
		* loop. It is necessary to check to see if we are at the end of stream
		* in the wait loop because it is possible that the upstream filter completes
		* while we are waiting. If this happens and we do not check for the end of
		* stream, then we could wait forever on an upstream pipe that is long gone.
		* Unfortunately Java pipes do not throw exceptions when the input pipe is
		* broken.
		***********************************************************************/

        // This will be true if data is available at any input port
        boolean availableInput = false;

		try
		{
            do {

                for (int i=0; i< InputReadPorts.size(); i++){

                    // Give fair distribution between input ports so that the last read port get the lowest priority
                    inputPortLooper = (inputPortLooper + 1) % InputReadPorts.size();

                    if (InputReadPorts.get(inputPortLooper).available() != 0) {
                        availableInput = true;
                        currentInputPort = InputReadPorts.get(inputPortLooper);
                        break;
                    }
                }

                if (!availableInput) {

                    if (EndOfInputStream()) {
                        throw new EndOfStreamException("End of input stream reached");

                    } //if

                    sleep(250);
                }

            } while (!availableInput); // while

		} // try

		catch( EndOfStreamException Error )
		{
			throw Error;

		} // catch

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " Error in read port wait loop::" + Error );

		} // catch

		/***********************************************************************
		* If at least one byte of data is available on the input
		* pipe we can read it. We read and write one byte to and from ports.
		***********************************************************************/

		try
		{
			datum = (byte) currentInputPort.read();
			return datum;

		} // try

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " Pipe read error::" + Error );
			return datum;

		} // catch

	} // ReadFilterPort

	/***************************************************************************
	* CONCRETE METHOD:: WriteFilterOutputPort
	* Purpose: This method writes data to all output ports one byte at a time.
	*
	* Arguments:
	* 	byte datum - This is the byte that will be written on the output port.of
	*	the filter.
	*
	* Returns: void
	*
	* Exceptions: IOException
	*
	****************************************************************************/

    void WriteFilterOutputPort(byte datum){
        this.WriteFilterOutputPort(datum, null);
    }

	void WriteFilterOutputPort(byte datum, PipedOutputStream outputPort)
	{
		try
		{
            // if output port is defined, then we will only write to this port
            if (outputPort != null){
                outputPort.write((int) datum);
                outputPort.flush();

            } else {
                for (PipedOutputStream out: OutputWritePorts){
                    out.write((int) datum);
                    out.flush();
                }
            }


		} // try

		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch

		return;

	} // WriteFilterPort

	/***************************************************************************
	* CONCRETE METHOD:: EndOfInputStream
	* Purpose: This method is used within this framework which is why it is private
	* It returns a true when there is no more data to read on all input ports of
	* the instance filter. What it really does is to check if the upstream filter
	* is still alive. This is done because Java does not reliably handle broken
	* input pipes and will often continue to read (junk) from a broken input pipe.
	*
	* Arguments: void
	*
	* Returns: A value of true if the previous filter has stopped sending data,
	*		   false if it is still alive and sending data.
	*
	* Exceptions: none
	*
	****************************************************************************/

	private boolean EndOfInputStream()
	{
        for (FilterFramework inputFilter: InputFilters) {
            if (inputFilter.isAlive()) {
                return false;
            }
        }

        return true;

	} // EndOfInputStream

	/***************************************************************************
	* CONCRETE METHOD:: ClosePorts
	* Purpose: This method is used to close all input and output ports of the
	* filter. It is important that filters close their ports before the filter
	* thread exits.
	*
	* Arguments: void
	*
	* Returns: void
	*
	* Exceptions: IOExecption
	*
	****************************************************************************/

	void ClosePorts()
	{
		try
		{
            for (PipedInputStream inputPort: InputReadPorts){
                inputPort.close();
            }

            for (PipedOutputStream outputPort: OutputWritePorts){
                outputPort.close();
            }

		}
		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );

		} // catch

	} // ClosePorts

	/***************************************************************************
	* CONCRETE METHOD:: run
	* Purpose: This is actually an abstract method defined by Thread. It is called
	* when the thread is started by calling the Thread.start() method. In this
	* case, the run() method should be overridden by the filter programmer using
	* this framework superclass
	*
	* Arguments: void
	*
	* Returns: void
	*
	* Exceptions: IOExecption
	*
	****************************************************************************/

	public void run()
    {
		// The run method should be overridden by the subordinate class. Please
		// see the example applications provided for more details.

	} // run

} // A1.FilterFramework class
