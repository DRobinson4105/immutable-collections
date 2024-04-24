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

public interface Graph<V, E> {
    static <V, E> Graph<V, E> of() {
        return new GraphImpl<>(Map.of());
    }

    Collection<V> getAllNodes();

    Graph<V, E> addNode(V node);

    Graph<V, E> removeNode(V node);

    boolean containsNode(V node);

    Graph<V, E> putEdge(V src, V dest, E val);

    E getEdge(V src, V dest);

    Map<V, E> getIncomingEdges(V node);

    Map<V, E> getOutgoingEdges(V node);

    Graph<V, E> removeEdge(V src, V dest);

    default boolean containsEdge(V src, V dest) {
        return getEdge(src, dest) != null;
    }

    default boolean hasCycles() {
        var safe = new java.util.HashSet<V>();

        nextNode: for (V node : getAllNodes()) {
            var active = new java.util.HashSet<V>();
            var queue = new java.util.LinkedList<V>();
            queue.add(node);

            while (!queue.isEmpty()) {
                V curr = queue.pollFirst();
                active.add(curr);
                if (safe.contains(curr)) continue nextNode;

                for (V next : getOutgoingEdges(curr).toKeys()) {
                    if (active.contains(next)) {
                        return true;
                    } else {
                        queue.add(next);
                    }
                }
            }

            safe.addAll(active);
        }

        return false;
    }

    int size();
}
