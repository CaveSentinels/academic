import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.sql.Timestamp;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

public class Coordinator extends Verticle {

    // --------------------------------------------------
    // The operator(PUT/GET)
    private class Operation extends Object {

        private static final int OP_UNKNOWN = -1;
        private static final int OP_PUT = 0;
        private static final int OP_GET = 1;

        private int opType = OP_UNKNOWN;
        private long millis;
        private String timestamp;
        private String key;
        private String value;
        private String loc;

        public Operation(int type, long millis, String tm, String key, String value, String loc) {
            this.opType = type;
            this.millis = millis;
            this.timestamp = tm;
            this.key = key;
            this.value = value;
            this.loc = loc;
        }

        @Override
        public String toString() {
            String str;

            if (opType == OP_PUT) {
                str = "{ PUT: key = " + key + " value = " + value + " millis = " + String.valueOf(millis) + " timestamp = " + timestamp + " }";
            } else {
                str = "{ GET: key = " + key + " loc = " + loc + " millis = " + String.valueOf(millis) + " timestamp = " + timestamp + " }";
            }

            return str;
        }

        public int GetOpType() { return opType; }
        public long GetMillis() { return millis; }
        public String GetTimestamp() { return timestamp; }
        public String GetKey() { return key; }
        public String GetValue() { return value; }
        public String GetLoc() { return loc; }
    }

    // --------------------------------------------------
    // The operation(PUT/GET) executor.
    private class OpExecutor extends Thread {

        private String key = null;
        private final ArrayBlockingQueue<Operation> queue = new ArrayBlockingQueue<Operation>(5);    // TODO: Magic number!

        public OpExecutor(String key) {
            this.key = key;
        }

        public void Put(long millis, String timestamp, String value) {
            try {
                queue.put(new Operation(Operation.OP_PUT, millis, timestamp, key, value, null/*loc*/));
//                synchronized (queue) {
//                    queue.notify();  // FIXME: notify or notifyAll?
//                }
            } catch(Exception e) {
                System.out.println("Exception: Put: " + e.getMessage());
            }
        }

        public void Get(long millis, String timestamp, String loc) {
            try {
                queue.put(new Operation(Operation.OP_GET, millis, timestamp, key, null/*value*/, loc));
//                synchronized (queue) {
//                    queue.notify();  // FIXME: notify or notifyAll?
//                }
            } catch (Exception e) {
                System.out.println("Exception: Get: " + e.getMessage());
            }
        }

        public void Start() {
            this.start();
        }

        public void Stop() {
            this.interrupt();
        }

        private void invokeProtectedPut(String dbDNS, String key, String value) {
            try {
                KeyValueLib.PUT(dbDNS, key, value);
            } catch(Exception e) {
                System.err.println("ERROR: Exception at KeyValueLib.PUT: db = " + dbDNS +
                        " key = " + key +
                        " value = " + value);
            }
        }

        private void invokeProtectedGet(String dbDNS, String key) {
            try {
                KeyValueLib.GET(dbDNS, key);
            } catch(Exception e) {
                System.err.println("ERROR: Exception at KeyValueLib.GET: db = " + dbDNS +
                        " key = " + key);
            }
        }

        @Override
        public void run() {
            System.out.println("" + String.valueOf(this.getId()));
            while (!this.isInterrupted()) {
                try {
                    // Do not always wait.
//                    queue.wait(10);
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println("[DEBUG] queue is waken up.");
                }

                if (queue.isEmpty()) {
                    continue;
                }

                // Get all the operations
                ArrayList<Operation> operations = new ArrayList<Operation>();
                for (int i = 0; i < queue.size(); ++i) {
                    try {
                        operations.add(queue.take());
                    } catch(Exception e) {
                        System.err.println("[DEBUG] queue.take()");
                    }
                }

                // Sort all the operations according to the timestamp.
                ArrayList<Operation> sortedOps = new ArrayList<Operation>(operations.size());

                for (int k = 0; k < operations.size(); ++k) {
                    Operation op = operations.get(k);
                    int i = 0;
                    while (i < sortedOps.size()) {
                        if (op.GetMillis() < sortedOps.get(i).GetMillis()) {
                            break;
                        }
                        ++i;
                    }
                    sortedOps.add(i, op);
                }

                // We must start from the first one because we want to guarantee the
                // order of timestamp.
                for (int i = 0; i < operations.size(); ++i) {
                    Operation op = operations.get(i);
                    if (op.GetOpType() == Operation.OP_PUT) {
                        System.out.println("[DEBUG] Executing PUT: " + op.toString());
                        invokeProtectedPut(dataCenter1, op.GetKey(), op.GetValue());
                        invokeProtectedPut(dataCenter2, op.GetKey(), op.GetValue());
                        invokeProtectedPut(dataCenter3, op.GetKey(), op.GetValue());
                    } else if (op.GetOpType() == Operation.OP_GET) {
                        System.out.println("[DEBUG] Executing GET: " + op.toString());
                        if (op.GetLoc().equals("1")) {
                            invokeProtectedGet(dataCenter1, op.GetKey());
                        } else if (op.GetLoc().equals("2")) {
                            invokeProtectedGet(dataCenter2, op.GetKey());
                        } else if (op.GetLoc().equals("3")) {
                            invokeProtectedGet(dataCenter3, op.GetKey());
                        } else {
                            System.err.println("ERROR: Unrecognized database: " + op.GetLoc());
                        }
                    } else {
                        System.err.println("ERROR: Unknown operation type: " +
                                String.valueOf(op.GetOpType()));
                    }
                }
            }
        }
    }

    // --------------------------------------------------
	//Default mode: Strongly consistent. Possible values are "strong" and "causal"
	private static String consistencyType = "strong";

    // FIXME: The executorHashMap needs write/read protection!!!
    private static HashMap<String/*key*/, OpExecutor> executorHashMap = new HashMap<String, OpExecutor>();

	/**
	 * three dataCenter instances
	 */
	private static final String dataCenter1 = "ec2-52-1-181-92.compute-1.amazonaws.com";
	private static final String dataCenter2 = "ec2-52-1-13-105.compute-1.amazonaws.com";
	private static final String dataCenter3 = "ec2-52-4-74-18.compute-1.amazonaws.com";

    private OpExecutor findOrCreateOpExecutor(String key) {
        OpExecutor ex;

        if (!executorHashMap.containsKey(key)) {
            // If we can't find the executor, we need to create one first.
            ex = new OpExecutor(key);
            executorHashMap.put(key, ex);
            ex.Start();
        }

        ex = executorHashMap.get(key);

        return ex;
    }

    private void handlePutConsistencyStrong(String key, String value, long millis, String timestamp) {

        // Find or create an operation executor.
        OpExecutor ex = findOrCreateOpExecutor(key);

        // Then we tried to put the value in.
        ex.Put(millis, timestamp, value);

        // That's all! We're done!
    }

    private void handleGetConsistencyStrong(String key, String loc, long millis, String timestamp) {

        // Find or create an operation executor.
        OpExecutor ex = findOrCreateOpExecutor(key);

        // Then we tried to put the value in.
        ex.Get(millis, timestamp, loc);

        // That's all! We're done!
    }

    private void handlePutConsistencyCasual(String key, String value, long millis, String timestamp) {
        // TODO: Implement me!
    }

    private void handleGetConsistencyCasual(String key, String loc, long millis, String timestamp) {
        // TODO: Implement me!
    }

	@Override
	public void start() {
		//DO NOT MODIFY THIS
		KeyValueLib.dataCenters.put(dataCenter1, 1);
		KeyValueLib.dataCenters.put(dataCenter2, 2);
		KeyValueLib.dataCenters.put(dataCenter3, 3);
		final RouteMatcher routeMatcher = new RouteMatcher();
		final HttpServer server = vertx.createHttpServer();
		server.setAcceptBacklog(32767);
		server.setUsePooledBuffers(true);
		server.setReceiveBufferSize(4 * 1024);

		routeMatcher.get("/put", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				MultiMap map = req.params();
				final String key = map.get("key");
				final String value = map.get("value");
				//You may use the following timestamp for ordering requests
                final long currTimeMillis = System.currentTimeMillis();
                final String timestamp = new Timestamp(currTimeMillis
                                                + TimeZone.getTimeZone("EST").getRawOffset()).toString();
                System.out.println("[DEBUG] Incoming PUT: key = " + key + " value = " + value + " time = " + timestamp);
				Thread t = new Thread(new Runnable() {
					public void run() {
                        if (Coordinator.consistencyType.equals("strong")) {
                            handlePutConsistencyStrong(key, value, currTimeMillis, timestamp);
                        } else if (Coordinator.consistencyType.equals("causal")) {
                            handlePutConsistencyCasual(key, value, currTimeMillis, timestamp);
                        } else {
                            System.err.println("ERROR: Unrecognized consistency type: " + Coordinator.consistencyType);
                        }
					}
				});
				t.start();
				req.response().end(); //Do not remove this
			}
		});

		routeMatcher.get("/get", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				MultiMap map = req.params();
				final String key = map.get("key");
				final String loc = map.get("loc");
				//You may use the following timestamp for ordering requests
                final long currTimeMillis = System.currentTimeMillis();
				final String timestamp = new Timestamp(currTimeMillis
								+ TimeZone.getTimeZone("EST").getRawOffset()).toString();
                System.out.println("[DEBUG] Incoming GET: key = " + key + " loc = " + loc + " time = " + timestamp);
				Thread t = new Thread(new Runnable() {
					public void run() {
						//TODO: Write code for GET operation here.
                                                //Each GET operation is handled in a different thread.
                                                //Highly recommended that you make use of helper functions.
                        if (Coordinator.consistencyType.equals("strong")) {
                            handleGetConsistencyStrong(key, loc, currTimeMillis, timestamp);
                        } else if (Coordinator.consistencyType.equals("causal")) {
                            handleGetConsistencyCasual(key, loc, currTimeMillis, timestamp);
                        } else {
                            System.err.println("ERROR: Unrecognized consistency type: " + Coordinator.consistencyType);
                        }
						req.response().end("0"); //Default response = 0
					}
				});
				t.start();
			}
		});

		routeMatcher.get("/consistency", new Handler<HttpServerRequest>() {
                        @Override
                        public void handle(final HttpServerRequest req) {
                                MultiMap map = req.params();
                                consistencyType = map.get("consistency");
                                //This endpoint will be used by the auto-grader to set the 
				//consistency type that your key-value store has to support.
                                //You can initialize/re-initialize the required data structures here
                                req.response().end();
                        }
                });

		routeMatcher.noMatch(new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				req.response().putHeader("Content-Type", "text/html");
				String response = "Not found.";
				req.response().putHeader("Content-Length",
						String.valueOf(response.length()));
				req.response().end(response);
				req.response().close();
			}
		});
		server.requestHandler(routeMatcher);
		server.listen(8080);
	}
}
