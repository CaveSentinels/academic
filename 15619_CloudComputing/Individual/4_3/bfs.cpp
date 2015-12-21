/**
 * This is the skeleton code for bfs.
 * Note the save and load functions in each struct and class are used to serialize
 * and deserialize the object. If you add members with non-primitive data types in
 * a struct or class, you need to implement the save and load functions. For more
 * information, please see the serialization section in the API documentation of
 * GraphLab.
 *
 *  @solution:
 *  Project 4.3 asks us to calculate the shortest path from every article to
 *  the most popular articles by using Breadth-First Search. To solve this
 *  problem, we can select one of the most-popular articles and then apply BFS
 *  to it as the starting vertex, and after we finish traversing the entire
 *  graph, we will find the shortest paths between the starting article to any
 *  other articles. This makes the algorithm look like a reversed BFS.
 *
 *  @GraphLab(PowerGraph):
 *  1). GraphLab performs the computation in multiple iterations.
 *  2). In very iteration, some vertices will be signaled to be computed
 *      in the next iteration. The entire computation process is completed
 *      when there are no vertices signaled.
 *  3). In every iteration, the code in the bfs class below will be executed.
 *  4). The bfs code for a specific vertex, say, A, in an iteration is executed
 *      in the following sequence:
 *      1). GraphLab collects all the messages that are sent to vertex A, then
 *          calls the operator += to merge all the messages into one message.
 *      2). init(msg) is called, and here the msg is the merged message.
 *      3). gather_edges() is called. In this particular problem, we don't need
 *          to gather anything.
 *      4). gater() is called for every edge that is returned from
 *          gather_edges().
 *      5). apply().
 *      6). scatter_edges() is called to return all the edges that the message
 *          to be scattered to.
 *      7). scatter() is called for every edge that is returned from
 *          scatter_edges().
 *  5). For more information about the procedure, see:
 *      https://github.com/dato-code/PowerGraph/blob/master/src/graphlab/vertex_program/ivertex_program.hpp
 */

#include <vector>
#include <string>
#include <fstream>

#include <graphlab.hpp>

const int MAX_LEN = 15;

/* The id of the current top vertex which we are processing */
int g_current_top_id = -1;  // Always initialize explicitly.

/**
 * The vertex data type and also the message type
 */
struct vertex_data {

    typedef std::vector<int> path_type;
    path_type shortest_path;    // The shortest path
    bool shortest_found;

    // Constructor
    vertex_data() : shortest_found(false) {
        // Empty
    }

    // GraphLab will use this function to merge messages
    vertex_data& operator+=(const vertex_data& other) {
        // Figure out which path is shorter.

        if (this == &other) {
            // Make sure it is not adding to itself.
            return *this;
        }

        size_t this_size = shortest_path.size();
        size_t other_size = other.shortest_path.size();

        if (this_size > other_size) {
            // this > other
            shortest_path = other.shortest_path;
        } else if (this_size == other_size) {
            // If this is of the same size with the other...
            for (size_t i = this_size; i > 0; --i) {
                if (shortest_path[i-1] > other.shortest_path[i-1]) {
                    // this > other
                    shortest_path = other.shortest_path;
                    break;
                } else if (shortest_path[i-1] < other.shortest_path[i-1]) {
                    // this < other
                    break;
                } else {
                    // this is still euqal to other.
                    // continue
                }
            }
        } else {
            // this_size < other_size
            // Do nothing
        }

        return *this;
    }

    void save(graphlab::oarchive& oarc) const {
        oarc << shortest_found << shortest_path;
    }

    void load(graphlab::iarchive& iarc) {
        iarc >> shortest_found >> shortest_path;
    }
};

/**
 * Definition of graph
 */
typedef graphlab::distributed_graph<vertex_data, graphlab::empty> graph_type;

/**
 * The bfs program.
 */
class bfs :
        public graphlab::ivertex_program<graph_type,    // Graph
                graphlab::empty,    // Gather type
                vertex_data>    // Message Type
{
private:
    vertex_data msg;

public:

    void init(icontext_type& context, const vertex_type& vertex,
              const message_type& msg) {
        // msg has the shortest path from g_current_top_id to this vertex.
        this->msg = msg;    // make a copy
    }

    // no gather required
    edge_dir_type gather_edges(icontext_type& context,
                               const vertex_type& vertex) const {
        return graphlab::NO_EDGES;
    }

    void apply(icontext_type& context, vertex_type& vertex,
               const gather_type& gather_result) {
        // When we are in the apply() method, that means the BFS has reached
        // to this vertex, and the shortest path from this vertex to the
        // current top vertex has been found.
        msg.shortest_found = true;

        // Append this vertex to the shortest path
        const int id = vertex.id();
        msg.shortest_path.push_back(id);

        // Update the vertex data.
        vertex_data & data = vertex.data();
        data.shortest_found = true;
        data.shortest_path = msg.shortest_path;
    }

    // do scatter on all the in-edges
    edge_dir_type scatter_edges(icontext_type& context,
                                const vertex_type& vertex) const {
        return graphlab::IN_EDGES;
    }

    void scatter(icontext_type& context, const vertex_type& vertex,
                 edge_type& edge) const {
        // [yw]
        // Get the source vertex on the in-edge.
        vertex_type source = edge.source();

        // We just signal the vertex if its shortest path is not found yet.
        if (!source.data().shortest_found) {
            context.signal(source, this->msg);
        }
    }

    void save(graphlab::oarchive& oarc) const {
        msg.save(oarc);
    }

    void load(graphlab::iarchive& iarc) {
        msg.load(iarc);
    }
};

void initialize_vertex(graph_type::vertex_type& vertex) {
    // [yw] Initialize every vertex
    vertex.data().shortest_found = false;   // Initially, it's not found.
    vertex.data().shortest_path.clear();    // Initially, it should be empty.
}

struct shortest_path_writer {
    std::string save_vertex(const graph_type::vertex_type& vtx) {
        std::stringstream ss;

        const vertex_data::path_type &sp = vtx.data().shortest_path;
        size_t path_size = sp.size();

        if (path_size > 1) {
            // When path_size <= 1, two cases:
            // 1). The path is empty, meaning there is no path between source and destination;
            // 2). The source and destination are the same vertex.
            // We should not output the paths for the 2 cases above.
            ss << vtx.id() << '\t' << g_current_top_id << '\t';
            for (size_t i = path_size; i > 1; --i) {
                ss << sp[i - 1] << ' ';
            }
            ss << sp[0];
            ss << std::endl;
        }

        return ss.str();
    }

    std::string save_edge(graph_type::edge_type e) { return ""; }
};

int main(int argc, char** argv) {
    // Initialize control plain using mpi
    graphlab::mpi_tools::init(argc, argv);
    graphlab::distributed_control dc;
    global_logger().set_log_level(LOG_INFO);

    // ------------------------------------------------------------------------
    // Parse command line options
    graphlab::command_line_options
            clopts("bfs algorithm");
    std::string graph_dir;
    std::string format = "snap";
    std::string saveprefix;
    std::string top_ids;

    clopts.attach_option("graph", graph_dir,
                         "The graph file.");
    clopts.attach_option("format", format,
                         "graph format");
    clopts.attach_option("top", top_ids,
                         "The file which contains top 10 ids");
    clopts.attach_option("saveprefix", saveprefix,
                         "If set, will save the result to a "
                                 "sequence of files with prefix saveprefix");

    if(!clopts.parse(argc, argv)) {
        dc.cout() << "Error in parsing command line arguments." << std::endl;
        return EXIT_FAILURE;
    }

    // ------------------------------------------------------------------------
    // Build the graph
    graph_type graph(dc, clopts);

    dc.cout() << "Loading graph in format: "<< format << std::endl;
    graph.load_format(graph_dir, format);

    // must call finalize before querying the graph
    graph.finalize();
    dc.cout() << "#vertices:  " << graph.num_vertices() << std::endl
    << "#edges:     " << graph.num_edges() << std::endl;

    // ------------------------------------------------------------------------
    // Run the engine
    graphlab::synchronous_engine<bfs> engine(dc, graph, clopts);
    char id_str[MAX_LEN];
    std::ifstream fin(top_ids.c_str());
    // Iterate through every top vertex.
    while (fin >> g_current_top_id) {
        dc.cout() << "--------------------------------------------------" << std::endl
                  << "Current top vertex: " << g_current_top_id << std::endl
                  ;
        graph.transform_vertices(initialize_vertex);

        // [yw] Signal the current vertex. The computation will start from
        // this vertex.
        // vertex_data() is the initial message that is sent to the starting
        // vertex.
        engine.signal(g_current_top_id, vertex_data());

        // [yw] Start the engine.
        engine.start();

        // Save the entire graph into the file.
        // Now this saved file will have all the shortest paths from any other
        // vertex to the current top vertex.
        std::string tmp = saveprefix;
        tmp += '_';
        sprintf(id_str, "%d", g_current_top_id);
        tmp += id_str;
        graph.save(tmp,
                   shortest_path_writer(),
                   false,   // do not gzip
                   true,    // save vertices
                   false,   // do not save edges
                   1);      // one output file per machine
    }
    fin.close();

    // ------------------------------------------------------------------------
    // Tear-down communication layer and quit
    graphlab::mpi_tools::finalize();

    return EXIT_SUCCESS;
} // End of main
