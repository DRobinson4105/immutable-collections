//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2024 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
//                                                                                                                       ~
//  Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in       ~
//  compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0   ~
//  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on  ~
//  an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the   ~
//  specific language governing permissions and limitations under the License.                                           ~
//                                                                                                                       ~
//  Maintainers:                                                                                                         ~
//      Wim Bast, Tom Brus                                                                                               ~
//                                                                                                                       ~
//  Contributors:                                                                                                        ~
//      Ronald Krijgsheld ✝, Arjan Kok, Carel Bast                                                                       ~
// --------------------------------------------------------------------------------------------------------------------- ~
//  In Memory of Ronald Krijgsheld, 1972 - 2023                                                                          ~
//      Ronald was suddenly and unexpectedly taken from us. He was not only our long-term colleague and team member      ~
//      but also our friend. "He will live on in many of the lines of code you see below."                               ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.collections;

import org.modelingvalue.collections.impl.GraphImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Triple;

import java.util.HashSet;
import java.util.function.Predicate;

public interface Graph<V, E> extends ContainingCollection<Triple<V, E, V>>, Mergeable<Graph<V, E>> {
    /**
     * Constructs an immutable graph from the specified edges and returns it.
     *
     * @param e array of edges, each represented by a {@link Triple} structured as (source vertex,
     *          edge weight, destination vertex)
     * @param <V> the type of vertices in the graph
     * @param <E> the type of edges in the graph
     * @return the constructed immutable graph, or an empty graph if no edges are provided
     * @throws NullPointerException if any of the edges are null
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    static <V, E> Graph<V, E> of(Triple<V, E, V>... e) {
        return e.length == 0 ? GraphImpl.EMPTY : new GraphImpl<>(e);
    }

    /**
     * Returns a set of all vertices in this graph.
     *
     * @return a set of vertices of type {@code V} in this graph
     */
    Set<V> getNodes();

    /**
     * Removes the specified vertex and all edges connected to it from this graph and returns the
     * updated graph.
     *
     * @param node vertex to be removed
     * @return a new graph without the specified vertex or any of the edges connected to it, or
     * this graph if {@code node} is null or does not exist in this graph
     */
    Graph<V, E> removeNode(V node);

    /**
     * Returns true if this graph contains the specified vertex.
     *
     * @param node vertex that is checked
     * @return {@code true} if this graph contains the specified vertex, {@code false} otherwise
     * or {@code node} is null
     */
    boolean containsNode(V node);

    /**
     * Adds a new edge with the specified weight between the source and destination vertices and
     * returns the updated graph. If the vertices do not exist in the graph, they will be added.
     *
     * @param src source vertex of the edge
     * @param dst destination vertex of the edge
     * @param val weight of the edge
     * @return a new graph with the added edge and vertices if they did not exist before, or this
     * graph if any of the parameters are null or the specified edge already exists in this graph
     */
    Graph<V, E> putEdge(V src, V dst, E val);

    /**
     * Returns true if the graph contains an edge with the specified weight between the given
     * source and destination vertices.
     *
     * @param src source vertex of the edge
     * @param dst destination vertex of the edge
     * @param val weight of the edge
     * @return true if the graph contains the specified edge, {@code false} otherwise or if any of
     * the parameters are null
     */
    boolean containsEdge(V src, V dst, E val);

    /**
     * Removes the specified edge with the specified weight between the source and destination
     * vertices and returns the updated graph. If either vertex has no other edges connected to it
     * after the removal, that vertex is also removed.
     *
     * @param src source vertex of the edge
     * @param dst destination vertex of the edge
     * @param val weight of the edge
     * @return a new graph without the specified edge and without either vertex if it no longer has
     * any connected edges, or this graph if any of the parameters are null or the specified edge
     * does not exist in this graph
     */
    Graph<V, E> removeEdge(V src, V dst, E val);

    /**
     * Removes all edges between the specified source and destination vertices and returns the
     * updated graph. If either vertex has no other edges connected to it after the removal, that
     * vertex is also removed.
     *
     * @param src source vertex
     * @param dst destination vertex
     * @return a new graph without any edges from the source to the destination and without either
     * vertex if it no loner has any connected edges, or this graph if {@code src} or {@code dst}
     * is null or does not exist in this graph
     */
    Graph<V, E> removeEdges(V src, V dst);

    /**
     * Returns a set of the edge weights between the specified source and destination vertices.
     *
     * @param src source vertex
     * @param dst destination vertex
     * @return a set of the edge weights from the source vertex to the destination vertex, or this
     * graph if {@code src} or {@code dst} is null or does not exist in this graph
     */
    Set<E> getEdges(V src, V dst);

    /**
     * Returns a map of the incoming edge weights to the specified vertex. In the map, each edge
     * weight is mapped to a set of vertices that have edges with that weight to the specified
     * vertex.
     *
     * @param node vertex to be checked
     * @return a map where each key is an edge weight and each value is a set of vertices that have
     * edges with that weight to the specified vertex, or null if {@code node} is null or does not
     * exist in this graph
     */
    DefaultMap<E, Set<V>> getIncoming(V node);

    /**
     * Returns a set of vertices that have edges with the specified weight to the specified vertex.
     *
     * @param node vertex to be checked
     * @param val edge weight
     * @return a set of the vertices that have edges to the specified vertex with the specified
     * weight, or null if {@code node} is null or does not exist in this graph or {@code val} is
     * null
     */
    Set<V> getIncoming(V node, E val);

    /**
     * Returns a map of the outgoing edge weights from the specified vertex. In the map, each edge
     * weight is mapped to a set of vertices that have edges with that weight from the specified
     * vertex.
     *
     * @param node vertex to be checked
     * @return a map where each key is an edge weight and each value is a set of vertices that have
     * edges with that weight from the specified vertex, or null if {@code node} is null or does
     * not exist in this graph
     */
    DefaultMap<E, Set<V>> getOutgoing(V node);

    /**
     * Returns a set of vertices that have edges with the specified weight from the specified
     * vertex.
     *
     * @param node vertex to be checked
     * @param val edge weight
     * @return a set of the vertices that have edges from the specified vertex with the specified
     * weight, or null if {@code node} is null or does not exist in this graph or {@code val} is
     * null
     */
    Set<V> getOutgoing(V node, E val);

    /**
     * Returns a set of the weights of the edges directed to the specified vertex.
     *
     * @param node vertex to be checked
     * @return a set of the weights of the edges directed to the specified vertex, or null if
     * {@code node} is null or does not exist in this graph
     */
    Set<E> getIncomingEdges(V node);

    /**
     * Returns a set of the weights of the edges directed from the specified vertex.
     *
     * @param node vertex to be checked
     * @return a set of the weights of the edges directed from the specified vertex, or null if
     * {@code node} is null or does not exist in this graph
     */
    Set<E> getOutgoingEdges(V node);

    /**
     * Returns a set of vertices that have edges directed to the specified vertex.
     *
     * @param node vertex to be queried
     * @return a set of the vertices that have edges directed to the specified vertex, or null if
     * {@code node} is null or does not exist in this graph
     */
    Set<V> getIncomingNodes(V node);

    /**
     * Returns a set of vertices that have edges directed from the specified vertex.
     *
     * @param node vertex to be queried
     * @return a set of the vertices that have edges directed from the specified vertex, or null if
     * {@code node} is null or does not exist in this graph
     */
    Set<V> getOutgoingNodes(V node);

    /**
     * Returns true if the graph contains cycles when only considering the nodes and edges
     * specified by the given predicates.
     *
     * @param nodePredicate a predicate that returns {@code true} if a node should be considered
     *                      when detecting cycles
     * @param edgePredicate a predicate that returns {@code true} if an edge should be considered
     *                      when detecting cycles
     * @return {@code true} if there are cycles when only considering the nodes and edges specified
     * by the predicates
     * @throws NullPointerException if any of the predicates are null
     */
    default boolean hasCycles(Predicate<V> nodePredicate, Predicate<Triple<V, E, V>> edgePredicate) {
        HashSet<V> safe = new HashSet<>();
        HashSet<V> visited = new HashSet<>();

        for (V node : getNodes()) {
            if (cycleDetectionHelper(nodePredicate, edgePredicate, safe, visited, node)) {
                return true;
            }

            safe.addAll(visited);
            visited = new HashSet<>();
        }

        return false;
    }

    private boolean cycleDetectionHelper(Predicate<V> nodePredicate, Predicate<Triple<V, E, V>> edgePredicate, HashSet<V> safe, HashSet<V> visited, V curr) {
        if (safe.contains(curr)) return false;
        if (visited.contains(curr)) return true;
        visited.add(curr);

        for (V next : getOutgoingNodes(curr)) {
            if (nodePredicate.test(next) && getEdges(curr, next).anyMatch(e -> edgePredicate.test(Triple.of(curr, e, next))) &&
                    cycleDetectionHelper(nodePredicate, edgePredicate, safe, visited, next)) {
                return true;
            }
        }

        visited.remove(curr);
        return false;
    }

    /**
     * Returns a new graph with all the directed edges reversed. Each edge in the original graph is
     * reversed so that it points from its destination to its source in the new graph.
     * @return a new graph with all directed edges reversed
     */
    Graph<V, E> inverted();

    /**
     * Removes the specified edge and returns the updated graph. If either vertex from the edge has
     * no other edges connected to it after the removal, that vertex is also removed. The edge is
     * assumed to be a {@link Triple} in the form of (source vertex, edge weight, destination
     * vertex).
     *
     * @param e edge to be removed, represented as a {@link Triple} in the form of (source vertex,
     *          edge weight, destination vertex)
     * @return a new graph without the specified edge and without either vertex if it no longer has
     * any connected edges, or this graph if {@code e} is null or the specified edge does not exist
     * in this graph
     */
    Graph<V, E> remove(Object e);

    /**
     * Removes the specified edges and returns the updated graph. If any vertex from the edges has
     * no other edges connected to it after the removal, that vertex is also removed. Each edge is
     * assumed to be a {@link Triple} in the form of (source vertex, edge weight, destination
     * vertex).
     *
     * @param e collection of edges to be removed, each represented as a {@link Triple} in the
     *          form of (source vertex, edge weight, destination vertex)
     * @return a new graph without the specified edges and without any vertices that no longer have
     * any connected edges, or this graph if {@code e} is null or none of the specified edges exist
     * in this graph
     */
    Graph<V, E> removeAll(Collection<?> e);

    /**
     * Adds the specified edge and returns the updated graph. If the vertices do not exist in the
     * graph, they will be added.
     *
     * @param e edge to be added, represented as a {@link Triple} in the form of (source vertex,
     *          edge weight, destination vertex)
     * @return a new graph with the added edge and vertices if they did not exist before, or this
     * graph if {@code e} is null or already exists in this graph
     */
    Graph<V, E> add(Triple<V, E, V> e);

    /**
     * Adds the specified edges and returns the updated graph. If the vertices do not exist in the
     * graph, they will be added.
     *
     * @param e collection of edges to be added, each represented as a {@link Triple} in the form
     *          of (source vertex, edge weight, destination vertex)
     * @return a new graph with the added edges and vertices if they did not exist before, or this
     * graph if {@code e} is null or all of the edges already exist in this graph
     */
    Graph<V, E> addAll(Collection<? extends Triple<V, E, V>> e);

    /**
     * Adds the specified edge and returns the updated graph. If the vertices do not exist in the
     * graph, they will be added.
     *
     * @param e edge to be added, represented as a {@link Triple} in the form of (source vertex,
     *          edge weight, destination vertex)
     * @return a new graph with the added edge and vertices if they did not exist before, or this
     * graph if {@code e} is null or already exists in this graph
     */
    Graph<V, E> addUnique(Triple<V, E, V> e);

    /**
     * Adds the specified edges and returns the updated graph. If the vertices do not exist in the
     * graph, they will be added.
     *
     * @param e collection of edges to be added, each represented as a {@link Triple} in the form
     *          of (source vertex, edge weight, destination vertex)
     * @return a new graph with the added edges and vertices if they did not exist before, or this
     * graph if {@code e} is null or all of the edges already exist in this graph
     */
    Graph<V, E> addAllUnique(Collection<? extends Triple<V, E, V>> e);

    /**
     * If {@code pre} is a {@link Triple} in the form of (source vertex, edge weight, destination
     * vertex) and that edge exists in this graph, {@code pre} is removed and {@code post} is added
     * and the updated graph is returned. If {@code pre} does not exist in this graph, then this
     * graph is returned.
     *
     * @param pre edge to be removed
     * @param post edge to be added
     * @return a new graph with {@code pre} removed and {@code post} added if and only if
     * {@code pre} exists in this graph, otherwise this graph
     */
    Graph<V, E> replace(Object pre, Triple<V, E, V> post);

    /**
     * If {@code pre} is a {@link Triple} in the form of (source vertex, edge weight, destination
     * vertex) and that edge exists in this graph, {@code pre} is removed and {@code post} is added
     * and the updated graph is returned. If {@code pre} does not exist in this graph, then this
     * graph is returned.
     *
     * @param pre edge to be removed
     * @param post edge to be added
     * @return a new graph with {@code pre} removed and {@code post} added if and only if
     * {@code pre} exists in this graph, otherwise this graph
     */
    Graph<V, E> replaceFirst(Object pre, Triple<V, E, V> post);

    /**
     * Returns an empty graph with no vertices or edges.
     *
     * @return an empty graph
     */
    Graph<V, E> clear();
}
