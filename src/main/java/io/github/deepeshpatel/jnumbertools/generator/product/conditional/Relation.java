/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.BitSet;

/**
 * Immutable allowed-value relation between two dimensions, stored in both directions.
 *
 * <p>For dimensions {@code u < v}, {@code uToV[a]} contains the values of {@code v}
 * allowed with value {@code a} of {@code u}; {@code vToU[b]} contains the reverse
 * relation. The rows are never exposed outside this package-private implementation.</p>
 */
final class Relation {
    final BitSet[] uToV;
    final BitSet[] vToU;

    private Relation(BitSet[] uToV, BitSet[] vToU) {
        this.uToV = uToV;
        this.vToU = vToU;
    }

    static Relation full(int du, int dv) {
        BitSet[] uv = new BitSet[du];
        for (int a = 0; a < du; a++) {
            uv[a] = new BitSet(dv);
            uv[a].set(0, dv);
        }
        BitSet[] vu = new BitSet[dv];
        for (int b = 0; b < dv; b++) {
            vu[b] = new BitSet(du);
            vu[b].set(0, du);
        }
        return new Relation(uv, vu);
    }

    /**
     * Returns a restricted copy in which each trigger value allows only the supplied target values.
     */
    Relation restrict(BitSet triggers, BitSet allowedTargets, boolean triggerIsU) {
        BitSet[] uv = deepCopy(uToV);
        BitSet[] vu = deepCopy(vToU);
        int targetSize = triggerIsU ? vu.length : uv.length;
        for (int a = triggers.nextSetBit(0); a >= 0; a = triggers.nextSetBit(a + 1)) {
            for (int b = allowedTargets.nextClearBit(0); b < targetSize; b = allowedTargets.nextClearBit(b + 1)) {
                if (triggerIsU) {
                    uv[a].clear(b);
                    vu[b].clear(a);
                } else {
                    vu[a].clear(b);
                    uv[b].clear(a);
                }
            }
        }
        return new Relation(uv, vu);
    }

    private static BitSet[] deepCopy(BitSet[] rows) {
        BitSet[] copy = new BitSet[rows.length];
        for (int i = 0; i < rows.length; i++) {
            copy[i] = (BitSet) rows[i].clone();
        }
        return copy;
    }
}
