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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Predicate;

public interface Graph<V, E> extends ContainingCollection<Triple<V, E, V>>, Mergeable<Graph<V, E>> {

    @SafeVarargs
    @SuppressWarnings("unchecked")
    static <V, E> Graph<V, E> of(Triple<V, E, V>... e) {
        return e.length == 0 ? GraphImpl.EMPTY : new GraphImpl<>(e);
    }

    Set<V> getNodes();

    Graph<V, E> removeNode(V node);

    boolean containsNode(V node);

    Graph<V, E> putEdge(V src, V dst, E val);

    boolean containsEdge(V src, V dst, E val);

    Graph<V, E> removeEdge(V src, V dst, E val);

    Graph<V, E> removeEdges(V src, V dst);

    Set<E> getEdges(V src, V dst);

    DefaultMap<E, Set<V>> getIncoming(V node);

    Set<V> getIncoming(V node, E val);

    DefaultMap<E, Set<V>> getOutgoing(V node);

    Set<V> getOutgoing(V node, E val);

    Set<E> getIncomingEdges(V node);

    Set<E> getOutgoingEdges(V node);

    Set<V> getIncomingNodes(V node);

    Set<V> getOutgoingNodes(V node);

    default boolean hasCycles(Predicate<V> nodePredicate, Predicate<Triple<V, E, V>> edgePredicate) {
        var inDegree = new HashMap<V, Integer>();
        var queue = new LinkedList<V>();
        var visited = new HashSet<>();
        var outgoing = new HashMap<V, HashSet<V>>();

        Set<V> nodes = getNodes();
        int size = nodes.size();

        for (V node : nodes) {
            if (!nodePredicate.test(node)) {
                visited.add(node);
                continue;
            }
            inDegree.put(node, getIncomingNodes(node).filter(inc -> {
                if (nodePredicate.test(inc) && getEdges(inc, node).anyMatch(e -> edgePredicate.test(Triple.of(inc, e, node)))) {
                    var set = outgoing.getOrDefault(inc, new HashSet<>());
                    set.add(node);
                    outgoing.put(inc, set);
                    return true;
                }
                return false;
            }).size());
            if (inDegree.get(node) == 0) queue.add(node);
        }

        while (!queue.isEmpty()) {
            V curr = queue.pollFirst();
            visited.add(curr);
            for (V next : getOutgoingNodes(curr)) {
                if (outgoing.containsKey(curr) && outgoing.get(curr).contains(next)) {
                    inDegree.put(next, inDegree.get(next) - 1);
                    if (inDegree.get(next) == 0) queue.add(next);
                }
            }
        }

        return visited.size() != size;
    }

    Graph<V, E> inverted();

    Graph<V, E> remove(Object e);

    Graph<V, E> removeAll(Collection<?> e);

    Graph<V, E> add(Triple<V, E, V> e);

    Graph<V, E> addAll(Collection<? extends Triple<V, E, V>> e);

    Graph<V, E> addUnique(Triple<V, E, V> e);

    Graph<V, E> addAllUnique(Collection<? extends Triple<V, E, V>> e);

    Graph<V, E> replace(Object pre, Triple<V, E, V> post);

    Graph<V, E> replaceFirst(Object pre, Triple<V, E, V> post);

    Graph<V, E> clear();
}
