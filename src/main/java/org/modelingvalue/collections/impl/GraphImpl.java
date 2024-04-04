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

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Graph;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.util.Age;
import org.modelingvalue.collections.util.Pair;

import java.util.Objects;

public class GraphImpl<V, E> implements Graph<V, E> {
    private Map<V, Pair<Map<V, E>, Map<V, E>>> g;

    public GraphImpl(Map<V, Pair<Map<V, E>, Map<V, E>>> g) {
        this.g = g;
    }

    @Override
    public GraphImpl<V, E> addNode(V node) {
        Objects.requireNonNull(node);
        if (containsNode(node)) return this;
        return new GraphImpl<>(this.g.put(node, Pair.of(Map.of(), Map.of())));
    }

    @Override
    public Graph<V, E> removeNode(V node) {
        if (!containsNode(node)) return this;
        return new GraphImpl<>(g.removeKey(node));
    }

    @Override
    public boolean containsNode(V node) {
        return node != null && g.containsKey(node);
    }

    @Override
    public Collection<V> getAllNodes() {
        return g.toKeys();
    }

    @Override
    public GraphImpl<V, E> putEdge(V src, V dest, E val) {
        Objects.requireNonNull(src);
        Objects.requireNonNull(dest);
        if (Objects.equals(getEdge(src, dest),val)) return this;

        var res = addNode(src).addNode(dest);

        var srcP = res.g.get(src);
        var destP = res.g.get(dest);

        if (val == null) {
            srcP = Pair.of(srcP.a(), srcP.b().removeKey(dest));
            destP = Pair.of(destP.a().removeKey(src), destP.b());
        } else if (src.equals(dest)) {
            srcP = Pair.of(srcP.a().put(src, val), srcP.b().put(src, val));
        } else {
            srcP = Pair.of(srcP.a(), srcP.b().put(dest, val));
            destP = Pair.of(destP.a().put(src, val), destP.b());
        }

        res.g = res.g.put(dest, destP).put(src, srcP);
        return res;
    }

    @Override
    public E getEdge(V src, V dest) {
        return containsNode(src) ? g.get(src).b().get(dest) : null;
    }

    @Override
    public Graph<V, E> removeEdge(V src, V dest) {
        return putEdge(src, dest, null);
    }

    @Override
    public Map<V, E> getIncomingEdges(V node) {
        return g.containsKey(node) ? g.get(node).a() : null;
    }

    @Override
    public Map<V, E> getOutgoingEdges(V node) {
        return g.containsKey(node) ? g.get(node).b() : null;
    }

    @Override
    public int size() {
        return g.size();
    }

    @Override
    public int hashCode() {
        return this.g.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        @SuppressWarnings("unchecked")
        GraphImpl<V,E> other = (GraphImpl<V,E>) obj;

        if (!this.g.equals(other.g))
            return false;

        if (Age.age(this.g) > Age.age(other.g)) {
            other.g = this.g;
        } else {
            this.g = other.g;
        }

        return true;
    }
}