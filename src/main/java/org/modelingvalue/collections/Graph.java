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
import org.modelingvalue.collections.impl.ListImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Triple;

import java.util.function.Predicate;

public interface Graph<V, E> extends ContainingCollection<Triple<V, E, V>>, Mergeable<Graph<V, E>> {

    @SafeVarargs
    @SuppressWarnings({"rawtypes", "unchecked"})
    static <V, E> Graph<V, E> of(Triple<V, E, V>... e) {
        return e.length == 0 ? new GraphImpl(new Triple[]{}) : new GraphImpl<>(e);
    }

    Set<V> getNodes();

    Graph<V, E> removeNode(V node);

    boolean containsNode(V node);

    Graph<V, E> putEdge(V src, V dst, E val);

    boolean containsEdge(V src, V dst, E val);

    Graph<V, E> removeEdge(V src, V dst, E val);

    Graph<V, E> removeEdges(V src, V dst);

    Set<E> getEdges(V src, V dst);

    Map<E, Set<V>> getIncoming(V node);

    Set<V> getIncoming(V node, E val);

    Map<E, Set<V>> getOutgoing(V node);

    Set<V> getOutgoing(V node, E val);

    Set<E> getIncomingEdges(V node);

    Set<E> getOutgoingEdges(V node);

    Set<V> getIncomingNodes(V node);

    Set<V> getOutgoingNodes(V node);

    default boolean hasCycles(Predicate<V> skipNode, Predicate<Triple<V, E, V>> skipEdge) {
        var safe = new java.util.HashSet<V>();

        nextNode: for (V node : getNodes()) {
            if (skipNode.test(node)) continue;
            var active = new java.util.HashSet<V>();
            var queue = new java.util.LinkedList<V>();
            queue.add(node);

            while (!queue.isEmpty()) {
                V curr = queue.pollFirst();
                active.add(curr);
                if (safe.contains(curr)) continue nextNode;

                for (Entry<E, Set<V>> entry : getOutgoing(curr)) {
                    for (V next : entry.getValue()) {
                        if (skipNode.test(next) || skipEdge.test(Triple.of(curr, entry.getKey(), next))) {
                            continue;
                        }

                        if (active.contains(next)) {
                            return true;
                        } else {
                            queue.add(next);
                        }
                    }
                }
            }

            safe.addAll(active);
        }

        return false;
    }

    Graph<V, E> inverted();

    int numEdges();
}
