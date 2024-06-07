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

package org.modelingvalue.collections.impl;

import org.modelingvalue.collections.*;
import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.mutable.MutableList;
import org.modelingvalue.collections.mutable.MutableMap;
import org.modelingvalue.collections.util.*;

import java.io.Serial;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public class GraphImpl<V, E> extends CollectionImpl<Triple<V, E, V>> implements Graph<V, E> {

    @Serial
    private static final long serialVersionUID = -9154911675408612745L;

    // incoming (edge -> set of vertices)
    // outgoing (edge -> set of vertices)
    // incoming (vertex -> set of edges)
    // outgoing (vertex -> set of edges)
//    protected Map<V, Quadruple<Map<E, Set<V>>, Map<E, Set<V>>, Map<V, Set<E>>, Map<V, Set<E>>>> g;
    protected Map<V, Pair<Map<E, Set<V>>, Map<E, Set<V>>>> evGraph;
    protected Map<V, Pair<Map<V, Set<E>>, Map<V, Set<E>>>> veGraph;

    public GraphImpl(Triple<V, E, V>[] edges) {
        this.evGraph = Map.of();
        this.veGraph = Map.of();

        for (Triple<V, E, V> edge : edges) {
            GraphImpl<V, E> next = this.putEdge(edge.a(), edge.c(), edge.b());
            this.evGraph = next.evGraph;
            this.veGraph = next.veGraph;
        }
    }

    protected GraphImpl(Map<V, Pair<Map<E, Set<V>>, Map<E, Set<V>>>> evGraph, Map<V, Pair<Map<V, Set<E>>, Map<V, Set<E>>>> veGraph) {
        this.evGraph = evGraph;
        this.veGraph = veGraph;
    }

    @Override
    public Graph<V, E> removeNode(V node) {
        if (!containsNode(node)) return this;

        var evGraph = new MutableMap<>(this.evGraph);
        var veGraph = new MutableMap<>(this.veGraph);

        removeNodeHelper(evGraph, veGraph, node, false);
        removeNodeHelper(evGraph, veGraph, node, true);

        return new GraphImpl<>(evGraph.toImmutable().removeKey(node), veGraph.toImmutable().removeKey(node));
    }

    private void removeNodeHelper(
            MutableMap<V, Pair<Map<E, Set<V>>, Map<E, Set<V>>>> evGraph,
            MutableMap<V, Pair<Map<V, Set<E>>, Map<V, Set<E>>>> veGraph,
            V node,
            boolean isReversed
    ) {
        var nodePair = veGraph.get(node);
        for (Entry<V, Set<E>> other : getOVE(nodePair, isReversed)) {
            var otherPairEV = evGraph.get(other.getKey());
            var otherPairVE = veGraph.get(other.getKey());

            var edgeMap = getIEV(otherPairEV, isReversed);
            for (E edge : getOVE(nodePair, isReversed).get(other.getKey())) {
                edgeMap = edgeMap.put(edge, edgeMap.get(edge).remove(node));
            }

            evGraph.put(other.getKey(), makePair(
                    edgeMap, getOEV(otherPairEV, isReversed), isReversed
            ));

            veGraph.put(other.getKey(), makePair(
                    getIVE(otherPairVE, isReversed).removeKey(node), getOVE(otherPairVE, isReversed), isReversed
            ));
        }
    }

    private static <V, E> Pair<Map<E, Set<V>>, Map<E, Set<V>>> makePair(Map<E, Set<V>> a, Map<E, Set<V>> b, boolean isReversed) {
        return isReversed ? Pair.of(b, a) : Pair.of(a, b);
    }

    private static <V, E> Map<E, Set<V>> getIEV(
            Pair<Map<E, Set<V>>, Map<E, Set<V>>> pair,
            boolean isReversed
    ) {
        return isReversed ? pair.b() : pair.a();
    }

    private static <V, E> Map<E, Set<V>> getOEV(
            Pair<Map<E, Set<V>>, Map<E, Set<V>>> pair,
            boolean isReversed
    ) {
        return isReversed ? pair.a() : pair.b();
    }

    private static <V, E> Map<V, Set<E>> getIVE(
            Pair<Map<V, Set<E>>, Map<V, Set<E>>> pair,
            boolean isReversed
    ) {
        return isReversed ? pair.b() : pair.a();
    }

    private static <V, E> Map<V, Set<E>> getOVE(
            Pair<Map<V, Set<E>>, Map<V, Set<E>>> pair,
            boolean isReversed
    ) {
        return isReversed ? pair.a() : pair.b();
    }

    @Override
    public boolean containsNode(V node) {
        return node != null && evGraph.containsKey(node);
    }

    @Override
    public Set<V> getNodes() {
        return evGraph.toKeys().asSet();
    }

    @Override
    public GraphImpl<V, E> putEdge(V src, V dst, E val) {
        if (src == null || dst == null || val == null) return this;

        var EVGraph = evGraph;
        var VEGraph = veGraph;

        if (!EVGraph.containsKey(src)) {
            EVGraph = EVGraph.put(src, Pair.of(Map.of(), Map.of()));
            VEGraph = VEGraph.put(src, Pair.of(Map.of(), Map.of()));
        }

        if (!EVGraph.containsKey(dst)) {
            EVGraph = EVGraph.put(dst, Pair.of(Map.of(), Map.of()));
            VEGraph = VEGraph.put(dst, Pair.of(Map.of(), Map.of()));
        }

        if (VEGraph.get(src).b().get(dst) != null && VEGraph.get(src).b().get(dst).contains(val)) return this;
        var srcPairEV = EVGraph.get(src);
        var srcPairVE = VEGraph.get(src);

        EVGraph = EVGraph.put(src, Pair.of(srcPairEV.a(), putEdgeHelperEV(srcPairEV.b(), dst, val)));
        VEGraph = VEGraph.put(src, Pair.of(srcPairVE.a(), putEdgeHelperVE(srcPairVE.b(), dst, val)));

        var dstPairEV = EVGraph.get(dst);
        var dstPairVE = VEGraph.get(dst);

        EVGraph = EVGraph.put(dst, Pair.of(putEdgeHelperEV(dstPairEV.a(), src, val), dstPairEV.b()));
        VEGraph = VEGraph.put(dst, Pair.of(putEdgeHelperVE(dstPairVE.a(), src, val), dstPairVE.b()));

        return new GraphImpl<>(EVGraph, VEGraph);
    }

    private static <V, E> Map<V, Set<E>> putEdgeHelperVE(Map<V, Set<E>> map, V node, E val) {
        return map.put(node, map.getOrDefault(node, Set.of()).add(val));
    }

    private static <V, E> Map<E, Set<V>> putEdgeHelperEV(Map<E, Set<V>> map, V node, E val) {
        return map.put(val, map.getOrDefault(val, Set.of()).add(node));
    }

    @Override
    public Set<E> getEdges(V src, V dst) {
        if (!containsNode(src) || !containsNode(dst)) return null;
        return veGraph.get(src).b().containsKey(dst) ? veGraph.get(src).b().get(dst) : Set.of();
    }

    @Override
    public boolean containsEdge(V src, V dst, E val) {
        if (src == null || dst == null || val == null) return false;

        return evGraph.containsKey(src) && evGraph.get(src).b().containsKey(val) &&
                evGraph.get(src).b().get(val).contains(dst);
    }

    @Override
    public Graph<V, E> removeEdge(V src, V dst, E val) {
        if (!containsEdge(src, dst, val)) return this;

        var EVGraph = evGraph;
        var VEGraph = veGraph;
        var srcPairEV = EVGraph.get(src);
        var srcPairVE = VEGraph.get(src);

        EVGraph = EVGraph.put(src, Pair.of(srcPairEV.a(), removeEdgeHelperEV(srcPairEV.b(), dst, val)));
        VEGraph = VEGraph.put(src, Pair.of(srcPairVE.a(), removeEdgeHelperVE(srcPairVE.b(), dst, val)));

        var dstPairEV = EVGraph.get(dst);
        var dstPairVE = VEGraph.get(dst);

        EVGraph = EVGraph.put(dst, Pair.of(removeEdgeHelperEV(dstPairEV.a(), src, val), dstPairEV.b()));
        VEGraph = VEGraph.put(dst, Pair.of(removeEdgeHelperVE(dstPairVE.a(), src, val), dstPairVE.b()));

        if (quadEmpty(EVGraph.get(src), VEGraph.get(src))) {
            EVGraph = EVGraph.removeKey(src);
            VEGraph = VEGraph.removeKey(src);
        }

        if (EVGraph.containsKey(dst) && quadEmpty(EVGraph.get(dst), VEGraph.get(dst))) {
            EVGraph = EVGraph.removeKey(dst);
            VEGraph = VEGraph.removeKey(dst);
        }

        return new GraphImpl<>(EVGraph, VEGraph);
    }

    private static <V, E> boolean quadEmpty(Pair<Map<E, Set<V>>, Map<E, Set<V>>> evPair, Pair<Map<V, Set<E>>, Map<V, Set<E>>> vePair) {
        return evPair.a().isEmpty() && evPair.b().isEmpty() && vePair.a().isEmpty() && vePair.b().isEmpty();
    }

    private static <V, E> Map<V, Set<E>> removeEdgeHelperVE(Map<V, Set<E>> map, V node, E val) {
        if (map.get(node).size() == 1) {
            return map.removeKey(node);
        } else {
            return map.put(node, map.get(node).remove(val));
        }
    }

    private static <V, E> Map<E, Set<V>> removeEdgeHelperEV(Map<E, Set<V>> map, V node, E val) {
        if (map.get(val).size() == 1) {
            return map.removeKey(val);
        } else {
            return map.put(val, map.get(val).remove(node));
        }
    }

    @Override
    public Graph<V, E> removeEdges(V src, V dst) {
        if (src == null || dst == null || !evGraph.containsKey(src) || !veGraph.get(src).b().contains(dst)) return this;

        var EVGraph = evGraph;
        var VEGraph = veGraph;
        var srcPairEV = EVGraph.get(src);
        var srcPairVE = VEGraph.get(src);
        var dstPairEV = EVGraph.get(dst);
        var dstPairVE = VEGraph.get(dst);
        var srcEdgeMap = srcPairEV.b();
        var dstEdgeMap = dstPairEV.a();

        for (E val : srcPairVE.b().get(dst)) {
            srcEdgeMap = removeEdgeHelperEV(srcEdgeMap, dst, val);
            dstEdgeMap = removeEdgeHelperEV(dstEdgeMap, src, val);
        }

        EVGraph = EVGraph.put(src, Pair.of(srcPairEV.a(), srcEdgeMap));
        VEGraph = VEGraph.put(src, Pair.of(srcPairVE.a(), srcPairVE.b().removeKey(dst)));

        dstPairEV = EVGraph.get(dst);
        dstPairVE = VEGraph.get(dst);

        EVGraph = EVGraph.put(dst, Pair.of(dstEdgeMap, dstPairEV.b()));
        VEGraph = VEGraph.put(dst, Pair.of(dstPairVE.a().removeKey(src), dstPairVE.b()));

        if (quadEmpty(EVGraph.get(src), VEGraph.get(src))) {
            EVGraph = EVGraph.removeKey(src);
            VEGraph = VEGraph.removeKey(src);
        }

        if (EVGraph.containsKey(dst) && quadEmpty(EVGraph.get(dst), VEGraph.get(dst))) {
            EVGraph = EVGraph.removeKey(dst);
            VEGraph = VEGraph.removeKey(dst);
        }

        return new GraphImpl<>(EVGraph, VEGraph);
    }

    @Override
    public Map<E, Set<V>> getIncoming(V node) {
        return node != null && evGraph.containsKey(node) ? evGraph.get(node).a() : null;
    }

    @Override
    public Set<V> getIncoming(V node, E val) {
        return node != null && val != null && evGraph.containsKey(node) ? evGraph.get(node).a().get(val) : null;
    }

    @Override
    public Map<E, Set<V>> getOutgoing(V node) {
        return node != null && evGraph.containsKey(node) ? evGraph.get(node).b() : null;
    }

    @Override
    public Set<V> getOutgoing(V node, E val) {
        return node != null && val != null && evGraph.containsKey(node) ? evGraph.get(node).b().get(val) : null;
    }

    @Override
    public Set<E> getIncomingEdges(V node) {
        return node != null && evGraph.containsKey(node) ? evGraph.get(node).a().toKeys().asSet() : null;
    }

    @Override
    public Set<E> getOutgoingEdges(V node) {
        return node != null && evGraph.containsKey(node) ? evGraph.get(node).b().toKeys().asSet() : null;
    }

    @Override
    public Set<V> getIncomingNodes(V node) {
        return node != null && veGraph.containsKey(node) ? veGraph.get(node).a().toKeys().asSet() : null;
    }

    @Override
    public Set<V> getOutgoingNodes(V node) {
        return node != null && veGraph.containsKey(node) ? veGraph.get(node).b().toKeys().asSet() : null;
    }

    @Override
    public Graph<V, E> inverted() {
        var EVGraph = evGraph;
        var VEGraph = veGraph;

        for (V node : getNodes()) {
            var pairEV = EVGraph.get(node);
            var pairVE = VEGraph.get(node);
            EVGraph = EVGraph.put(node, Pair.of(pairEV.b(), pairEV.a()));
            VEGraph = VEGraph.put(node, Pair.of(pairVE.b(), pairVE.a()));
        }

        return new GraphImpl<>(EVGraph, VEGraph);
    }

    @Override
    public int size() {
        return evGraph.size();
    }

    @Override
    public int hashCode() {
        return this.evGraph.hashCode() + this.veGraph.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        @SuppressWarnings("unchecked")
        GraphImpl<V, E> other = (GraphImpl<V, E>) obj;

        if (!this.evGraph.equals(other.evGraph))
            return false;

        if (Age.age(this.evGraph) > Age.age(other.evGraph)) {
            other.evGraph = this.evGraph;
            other.veGraph = this.veGraph;
        } else {
            this.evGraph = other.evGraph;
            this.veGraph = other.veGraph;
        }

        return true;
    }

    @Override
    public int numEdges() {
        int count = 0;

        for (var outer : evGraph) {
            var map = outer.getValue().b();
            System.out.println(outer.getKey());
            System.out.println("v");

            for (var inner : map) {
                System.out.println(inner.getKey());
                if (inner.getValue() != null) {
                    count += inner.getValue().size();
                }
            }
            System.out.println("^");
        }

        return count;
    }

    @Override
    protected Stream<Triple<V, E, V>> baseStream() {
        MutableList<Triple<V, E, V>> list = new MutableList<>(List.of());
        evGraph.forEach(s -> s.getValue().b().forEach(e -> e.getValue().forEach(t -> list.add(Triple.of(s.getKey(), e.getKey(), t)))));
        return list.stream();
    }

    @Override
    public Spliterator<Triple<V, E, V>> spliterator() {
        return evGraph.flatMap(s -> s.getValue().b().flatMap(e -> e.getValue().map(t -> Triple.of(s.getKey(), e.getKey(), t)))).spliterator();
    }

    @Override
    public Iterator<Triple<V, E, V>> iterator() {
        return evGraph.flatMap(s -> s.getValue().b().flatMap(e -> e.getValue().map(t -> Triple.of(s.getKey(), e.getKey(), t)))).iterator();
    }

    @Override
    public boolean isEmpty() {
        return evGraph.isEmpty();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean contains(Object e) {
        if (!(e instanceof Triple triple)) return false;

        return containsEdge((V) triple.a(), (V) triple.c(), (E) triple.b());
    }

    @Override
    public <R> Collection<R> linked(TriFunction<Triple<V, E, V>, Triple<V, E, V>, Triple<V, E, V>, R> function) {
        List<R> list = List.of();
        Triple<V, E, V> last1 = null, last2 = null;

        for (var s : evGraph) {
            for (var e : s.getValue().b()) {
                for (var t : e.getValue()) {
                    var curr = Triple.of(s.getKey(), e.getKey(), t);

                    if (last1 != null) {
                        list = list.add(function.apply(last1, last2, curr));
                    }

                    last1 = last2;
                    last2 = curr;
                }
            }
        }

        return list;
    }

    @Override
    public void linked(TriConsumer<Triple<V, E, V>, Triple<V, E, V>, Triple<V, E, V>> consumer) {
        TriFunction<Triple<V, E, V>, Triple<V, E, V>, Triple<V, E, V>, Integer> tri = (a, b, c) -> {
            consumer.accept(a, b, c);
            return 0;
        };

        linked(tri);
    }

    @Override
    public <R> Collection<R> indexed(BiFunction<Triple<V, E, V>, Integer, R> function) {
        if (function == null) return null;

        List<R> list = List.of();
        int index = 0;

        for (var s : evGraph) {
            for (var e : s.getValue().b()) {
                for (var t : e.getValue()) {
                    var curr = Triple.of(s.getKey(), e.getKey(), t);

                    list = list.add(function.apply(curr, index++));
                }
            }
        }

        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends ContainingCollection<Triple<V, E, V>>> StreamCollection<R[]> compare(R other) {
        R a = (R) List.of(this.toList());
        R b = (R) List.of(other.toList());
        return a.compare(b);
    }

    @Override
    public Triple<V, E, V> get(int index) {
        if (index < 0 || index >= size())
            throw new IndexOutOfBoundsException();

        for (var s : evGraph) {
            for (var e : s.getValue().b()) {
                for (var t : e.getValue()) {
                    var curr = Triple.of(s.getKey(), e.getKey(), t);

                    if (index-- == 0)
                        return curr;
                }
            }
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ContainingCollection<Triple<V, E, V>> remove(Object e) {
        Triple<V, E, V> triple = (Triple) e;
        return removeEdge(triple.a(), triple.c(), triple.b());
    }

    @Override
    public ContainingCollection<Triple<V, E, V>> removeAll(Collection<?> e) {
        if (e == null) return this;

        Graph<V, E> result = this;

        for (Object edge : e) {
            result = (Graph<V, E>) result.remove(edge);
        }

        return result;
    }

    @Override
    public ContainingCollection<Triple<V, E, V>> add(Triple<V, E, V> e) {
        return putEdge(e.a(), e.c(), e.b());
    }

    @Override
    public ContainingCollection<Triple<V, E, V>> addAll(Collection<? extends Triple<V, E, V>> e) {
        if (e == null) return this;

        Graph<V, E> result = this;

        for (Triple<V, E, V> edge : e) {
            result = result.putEdge(edge.a(), edge.c(), edge.b());
        }

        return result;
    }

    @Override
    public ContainingCollection<Triple<V, E, V>> addUnique(Triple<V, E, V> e) {
        return add(e);
    }

    @Override
    public ContainingCollection<Triple<V, E, V>> addAllUnique(Collection<? extends Triple<V, E, V>> e) {
        return addAll(e);
    }

    @Override
    public ContainingCollection<Triple<V, E, V>> replace(Object pre, Triple<V, E, V> post) {
        return remove(pre).add(post);
    }

    @Override
    public ContainingCollection<Triple<V, E, V>> replaceFirst(Object pre, Triple<V, E, V> post) {
        return replace(pre, post);
    }

    @Override
    public ContainingCollection<Triple<V, E, V>> clear() {
        return Graph.of();
    }

    @Override
    public Collection<Triple<V, E, V>> reverse() {
        return this;
    }

    @Override
    public Spliterator<Triple<V, E, V>> reverseSpliterator() {
        return spliterator();
    }

    @Override
    public ListIterator<Triple<V, E, V>> listIterator() {
        return new GraphIterator<>(List.of(this.toList()), 0, numEdges());
    }

    @Override
    public ListIterator<Triple<V, E, V>> listIterator(int index) {
        return new GraphIterator<>(List.of(this.toList()), index, numEdges());
    }

    @Override
    public ListIterator<Triple<V, E, V>> listIteratorAtEnd() {
        int size = numEdges();
        return new GraphIterator<>(List.of(this.toList()), size, size);
    }

    @Override
    public void javaSerialize(Serializer s) {
        s.writeInt(size());
        for (Object e : this) {
            s.writeObject(e);
        }
    }

    @Override
    public void javaDeserialize(Deserializer s) {
        int size = s.readInt();

        for (int i = 0; i < size; i++) {
            Triple<V, E, V> curr = s.readObject();
            putEdge(curr.a(), curr.c(), curr.b());
        }
    }

    @Override
    public Graph<V, E> merge(Graph<V, E>[] branches, int length) {
        int biggest = -1;
        int size = -1;

        for (int i = 0; i < length; i++) {
            if (branches[i].size() > size) {
                size = branches[i].size();
                biggest = i;
            }
        }

        Graph<V, E> result = biggest >= 0 ? branches[biggest] : this;

        for (int i = 0; i < length; i++) {
            if (i != biggest) {
                for (Triple<V, E, V> edge : this) {
                    if (!branches[i].contains(edge)) {
                        result = (Graph<V, E>) result.remove(edge);
                    }
                }

                for (int eb = 0; eb < branches[i].size(); eb++) {
                    Triple<V, E, V> edge = branches[i].get(eb);

                    if (!contains(edge) && !result.contains(edge)) {
                        result = (Graph<V, E>) result.add(edge);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Graph<V, E> getMerger() {
        return Graph.of();
    }

    @Override
    public Class<?> getMeetClass() {
        return Graph.class;
    }

    public String toString() {
        return baseStream().toList().toString();
    }

    private static final class GraphIterator<V, E> implements ListIterator<Triple<V, E, V>> {
        List<Triple<V, E, V>> edges;
        int idx, size;

        private GraphIterator(List<Triple<V, E, V>> edges, int idx, int size) {
            if (idx < 0 || idx > size) {
                throw new IndexOutOfBoundsException();
            }

            this.edges = edges;
            this.idx = idx;
            this.size = size;
        }

        @Override
        public boolean hasNext() {
            return idx < size;
        }

        @Override
        public Triple<V, E, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return edges.get(idx++);
        }

        @Override
        public boolean hasPrevious() {
            return idx > 0;
        }

        @Override
        public Triple<V, E, V> previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            return edges.get(--idx);
        }

        @Override
        public int nextIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int previousIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Triple<V, E, V> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Triple<V, E, V> e) {
            throw new UnsupportedOperationException();
        }
    }
}