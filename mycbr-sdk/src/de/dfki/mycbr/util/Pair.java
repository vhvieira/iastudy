/*
 * myCBR License 3.0
 * 
 * Copyright (c) 2006-2015, by German Research Center for Artificial Intelligence (DFKI GmbH), Germany
 * 
 * Project Website: http://www.mycbr-project.net/
 * 
 * This library is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.
 * 
 * endOfLic */

package de.dfki.mycbr.util;

/**
 * Utility class to provide pairs of objects.
 *
 * @author myCBR Team
 * @param <Type1> the type of the first element of the pair
 * @param <Type2> the type of the second element of the pair
 */
public final class Pair<Type1, Type2> {

    /**
     *
     */
    private Type1 first;

    /**
     *
     */
    private Type2 second;

    /**
     *
     */
    private int hash = 0;

    /**
     *
     */
    private static final int HASH_CODE = 31;

    /**
     *
     * @param f the first element
     * @param s the second element
     */
    public Pair(final Type1 f, final Type2 s) {
        this.first = f;
        this.second = s;
        hash();
    }

    /**
     *
     * @return the first element of this pair
     */
    public Type1 getFirst() {
        return first;
    }

    /**
     *
     * @return the second element of this pair
     */
    public Type2 getSecond() {
        return second;
    }

    /**
     *
     * @param f the new first element
     */
    public void setFirst(final Type1 f) {
        this.first = f;
        hash();
    }

    /**
     *
     * @param s the new second element
     */
    public void setSecond(final Type2 s) {
        this.second = s;
        hash();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth == null || !(getClass().isInstance(oth))) {
            return false;
        }
        Pair<Type1, Type2> other = (Pair<Type1, Type2>) oth;
        return (((first == null && other.first == null)
                           || first.equals(other.first))
                    &&
                 ((second == null && other.second == null)
                          || second.equals(other.second)));
    }

    /**
     * @return string representation of this pair
     */
    public String toString() {
        return "(" + first.toString() + "," + second.toString() + ")";
    }

    /**
     *
     */
    private void hash() {
        if (first != null) {
            hash = first.hashCode() * HASH_CODE;
        }
        if (second != null) {
            hash += second.hashCode();
        }
    }
}
