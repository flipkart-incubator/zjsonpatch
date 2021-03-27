/*
 * Copyright 2021 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * ATTRIBUTION NOTICE:<br>
 * This class contains source code copied from <a href="https://github.com/apache/commons-collections/tree/9414e73a7b8c5434b7cfcc5a65fc9baa007a1861">
 * Apache commons-collection4
 * <a>
 * </p>
 */
class InternalUtils {

    static List<JsonNode> toList(ArrayNode input) {
        int size = input.size();
        List<JsonNode> toReturn = new ArrayList<JsonNode>(size);
        for (int i = 0; i < size; i++) {
            toReturn.add(input.get(i));
        }
        return toReturn;
    }


    //-----------------------------------------------------------------------
    /**
     * Returns the longest common subsequence (LCS) of two sequences (lists).
     *
     * @param <E>  the element type
     * @param a  the first list
     * @param b  the second list
     * @return the longest common subsequence
     * @throws NullPointerException if either list is {@code null}
     * @since 4.0
     */
    public static <E> List<E> longestCommonSubsequence(final List<E> a, final List<E> b) {
        return longestCommonSubsequence( a, b, DefaultEquator.defaultEquator() );
    }

    /**
     * Returns the longest common subsequence (LCS) of two sequences (lists).
     *
     * @param <E>  the element type
     * @param listA  the first list
     * @param listB  the second list
     * @param equator  the equator used to test object equality
     * @return the longest common subsequence
     * @throws NullPointerException if either list or the equator is {@code null}
     * @since 4.0
     */
    public static <E> List<E> longestCommonSubsequence(final List<E> listA, final List<E> listB,
                                                       final Equator<? super E> equator) {
        Objects.requireNonNull(listA, "listA");
        Objects.requireNonNull(listB, "listB");
        Objects.requireNonNull(equator, "equator");

        final SequencesComparator<E> comparator = new SequencesComparator<>(listA, listB, equator);
        final EditScript<E> script = comparator.getScript();
        final LcsVisitor<E> visitor = new LcsVisitor<>();
        script.visit(visitor);
        return visitor.getSubSequence();
    }
    
    /**
     * A helper class used to construct the longest common subsequence.
     */
    private static final class LcsVisitor<E> implements CommandVisitor<E> {
        private final ArrayList<E> sequence;

        LcsVisitor() {
            sequence = new ArrayList<>();
        }

        @Override
        public void visitInsertCommand(final E object) {
            // noop
        }

        @Override
        public void visitDeleteCommand(final E object) {
            // noop
        }

        @Override
        public void visitKeepCommand(final E object) {
            sequence.add(object);
        }

        public List<E> getSubSequence() {
            return sequence;
        }
    }
    
    /**
     * An equation function, which determines equality between objects of type T.
     * <p>
     * It is the functional sibling of {@link java.util.Comparator}; {@link Equator} is to
     * {@link Object} as {@link java.util.Comparator} is to {@link java.lang.Comparable}.
     * </p>
     *
     * @param <T> the types of object this {@link Equator} can evaluate.
     * @since 4.0
     */
    public static interface Equator<T> {
        /**
         * Evaluates the two arguments for their equality.
         *
         * @param o1 the first object to be equated.
         * @param o2 the second object to be equated.
         * @return whether the two objects are equal.
         */
        boolean equate(T o1, T o2);

        /**
         * Calculates the hash for the object, based on the method of equality used in the equate
         * method. This is used for classes that delegate their {@link Object#equals(Object) equals(Object)} method to an
         * Equator (and so must also delegate their {@link Object#hashCode() hashCode()} method), or for implementations
         * of {@link org.apache.commons.collections4.map.HashedMap} that use an Equator for the key objects.
         *
         * @param o the object to calculate the hash for.
         * @return the hash of the object.
         */
        int hash(T o);
    }
        
    /**
     * Default {@link Equator} implementation.
     * <p>
     * Copied from Apache commons-collections 
     *
     * @param <T>  the types of object this {@link Equator} can evaluate.
     * @since 4.0
     */
    public static class DefaultEquator<T> implements Equator<T>, Serializable {
    
        /** Serial version UID */
        private static final long serialVersionUID = 825802648423525485L;
    
        /** Static instance */
        @SuppressWarnings("rawtypes") // the static instance works for all types
        public static final DefaultEquator INSTANCE = new DefaultEquator<>();
    
        /**
         * Hashcode used for {@code null} objects.
         */
        public static final int HASHCODE_NULL = -1;
    
        /**
         * Factory returning the typed singleton instance.
         *
         * @param <T>  the object type
         * @return the singleton instance
         */
        @SuppressWarnings("unchecked")
        public static <T> DefaultEquator<T> defaultEquator() {
            return DefaultEquator.INSTANCE;
        }
    
        /**
         * Restricted constructor.
         */
        private DefaultEquator() {
        }
    
        /**
         * {@inheritDoc} Delegates to {@link Object#equals(Object)}.
         */
        @Override
        public boolean equate(final T o1, final T o2) {
            return o1 == o2 || o1 != null && o1.equals(o2);
        }
    
        /**
         * {@inheritDoc}
         *
         * @return {@code o.hashCode()} if {@code o} is non-
         *         {@code null}, else {@link #HASHCODE_NULL}.
         */
        @Override
        public int hash(final T o) {
            return o == null ? HASHCODE_NULL : o.hashCode();
        }
    
        private Object readResolve() {
            return INSTANCE;
        }
    }
    
    /**
     * Abstract base class for all commands used to transform an objects sequence
     * into another one.
     * <p>
     * When two objects sequences are compared through the
     * {@link SequencesComparator#getScript SequencesComparator.getScript} method,
     * the result is provided has a {@link EditScript script} containing the commands
     * that progressively transform the first sequence into the second one.
     * </p>
     * <p>
     * There are only three types of commands, all of which are subclasses of this
     * abstract class. Each command is associated with one object belonging to at
     * least one of the sequences. These commands are {@link InsertCommand
     * InsertCommand} which correspond to an object of the second sequence being
     * inserted into the first sequence, {@link DeleteCommand DeleteCommand} which
     * correspond to an object of the first sequence being removed and
     * {@link KeepCommand KeepCommand} which correspond to an object of the first
     * sequence which {@code equals} an object in the second sequence. It is
     * guaranteed that comparison is always performed this way (i.e. the
     * {@code equals} method of the object from the first sequence is used and
     * the object passed as an argument comes from the second sequence) ; this can
     * be important if subclassing is used for some elements in the first sequence
     * and the {@code equals} method is specialized.
     * </p>
     *
     * @see SequencesComparator
     * @see EditScript
     *
     * @since 4.0
     */
    public static abstract class EditCommand<T> {
    
        /** Object on which the command should be applied. */
        private final T object;
    
        /**
         * Simple constructor. Creates a new instance of EditCommand
         *
         * @param object  reference to the object associated with this command, this
         *   refers to an element of one of the sequences being compared
         */
        protected EditCommand(final T object) {
            this.object = object;
        }
    
        /**
         * Returns the object associated with this command.
         *
         * @return the object on which the command is applied
         */
        protected T getObject() {
            return object;
        }
    
        /**
         * Accept a visitor.
         * <p>
         * This method is invoked for each commands belonging to
         * an {@link EditScript EditScript}, in order to implement the visitor design pattern
         *
         * @param visitor  the visitor to be accepted
         */
        public abstract void accept(CommandVisitor<T> visitor);
    
    }
    
    /**
     * Command representing the keeping of one object present in both sequences.
     * <p>
     * When one object of the first sequence {@code equals} another objects in
     * the second sequence at the right place, the {@link EditScript edit script}
     * transforming the first sequence into the second sequence uses an instance of
     * this class to represent the keeping of this object. The objects embedded in
     * these type of commands always come from the first sequence.
     * </p>
     *
     * @see SequencesComparator
     * @see EditScript
     *
     * @since 4.0
     */
    public static class KeepCommand<T> extends EditCommand<T> {
    
        /**
         * Simple constructor. Creates a new instance of KeepCommand
         *
         * @param object  the object belonging to both sequences (the object is a
         *   reference to the instance in the first sequence which is known
         *   to be equal to an instance in the second sequence)
         */
        public KeepCommand(final T object) {
            super(object);
        }
    
        /**
         * Accept a visitor. When a {@code KeepCommand} accepts a visitor, it
         * calls its {@link CommandVisitor#visitKeepCommand visitKeepCommand} method.
         *
         * @param visitor  the visitor to be accepted
         */
        @Override
        public void accept(final CommandVisitor<T> visitor) {
            visitor.visitKeepCommand(getObject());
        }
    }
    
    /**
     * Command representing the insertion of one object of the second sequence.
     * <p>
     * When one object of the second sequence has no corresponding object in the
     * first sequence at the right place, the {@link EditScript edit script}
     * transforming the first sequence into the second sequence uses an instance of
     * this class to represent the insertion of this object. The objects embedded in
     * these type of commands always come from the second sequence.
     * </p>
     *
     * @see SequencesComparator
     * @see EditScript
     *
     * @since 4.0
     */
    public static class InsertCommand<T> extends EditCommand<T> {
    
        /**
         * Simple constructor. Creates a new instance of InsertCommand
         *
         * @param object  the object of the second sequence that should be inserted
         */
        public InsertCommand(final T object) {
            super(object);
        }
    
        /**
         * Accept a visitor. When an {@code InsertCommand} accepts a visitor,
         * it calls its {@link CommandVisitor#visitInsertCommand visitInsertCommand}
         * method.
         *
         * @param visitor  the visitor to be accepted
         */
        @Override
        public void accept(final CommandVisitor<T> visitor) {
            visitor.visitInsertCommand(getObject());
        }
    
    }
    /**
     * Command representing the deletion of one object of the first sequence.
     * <p>
     * When one object of the first sequence has no corresponding object in the
     * second sequence at the right place, the {@link EditScript edit script}
     * transforming the first sequence into the second sequence uses an instance of
     * this class to represent the deletion of this object. The objects embedded in
     * these type of commands always come from the first sequence.
     * </p>
     *
     * @see SequencesComparator
     * @see EditScript
     *
     * @since 4.0
     */
    public static class DeleteCommand<T> extends EditCommand<T> {
    
        /**
         * Simple constructor. Creates a new instance of {@link DeleteCommand}.
         *
         * @param object  the object of the first sequence that should be deleted
         */
        public DeleteCommand(final T object) {
            super(object);
        }
    
        /**
         * Accept a visitor. When a {@code DeleteCommand} accepts a visitor, it calls
         * its {@link CommandVisitor#visitDeleteCommand visitDeleteCommand} method.
         *
         * @param visitor  the visitor to be accepted
         */
        @Override
        public void accept(final CommandVisitor<T> visitor) {
            visitor.visitDeleteCommand(getObject());
        }
    }
    
    /**
     * This interface should be implemented by user object to walk
     * through {@link EditScript EditScript} objects.
     * <p>
     * Users should implement this interface in order to walk through
     * the {@link EditScript EditScript} object created by the comparison
     * of two sequences. This is a direct application of the visitor
     * design pattern. The {@link EditScript#visit EditScript.visit}
     * method takes an object implementing this interface as an argument,
     * it will perform the loop over all commands in the script and the
     * proper methods of the user class will be called as the commands are
     * encountered.
     * </p>
     * <p>
     * The implementation of the user visitor class will depend on the
     * need. Here are two examples.
     * </p>
     * <p>
     * The first example is a visitor that build the longest common
     * subsequence:
     * </p>
     * <pre>
     * import org.apache.commons.collections4.comparators.sequence.CommandVisitor;
     *
     * import java.util.ArrayList;
     *
     * public class LongestCommonSubSequence implements CommandVisitor {
     *
     *   public LongestCommonSubSequence() {
     *     a = new ArrayList();
     *   }
     *
     *   public void visitInsertCommand(Object object) {
     *   }
     *
     *   public void visitKeepCommand(Object object) {
     *     a.add(object);
     *   }
     *
     *   public void visitDeleteCommand(Object object) {
     *   }
     *
     *   public Object[] getSubSequence() {
     *     return a.toArray();
     *   }
     *
     *   private ArrayList a;
     *
     * }
     * </pre>
     * <p>
     * The second example is a visitor that shows the commands and the way
     * they transform the first sequence into the second one:
     * </p>
     * <pre>
     * import org.apache.commons.collections4.comparators.sequence.CommandVisitor;
     *
     * import java.util.Arrays;
     * import java.util.ArrayList;
     * import java.util.Iterator;
     *
     * public class ShowVisitor implements CommandVisitor {
     *
     *   public ShowVisitor(Object[] sequence1) {
     *     v = new ArrayList();
     *     v.addAll(Arrays.asList(sequence1));
     *     index = 0;
     *   }
     *
     *   public void visitInsertCommand(Object object) {
     *     v.insertElementAt(object, index++);
     *     display("insert", object);
     *   }
     *
     *   public void visitKeepCommand(Object object) {
     *     ++index;
     *     display("keep  ", object);
     *   }
     *
     *   public void visitDeleteCommand(Object object) {
     *     v.remove(index);
     *     display("delete", object);
     *   }
     *
     *   private void display(String commandName, Object object) {
     *     System.out.println(commandName + " " + object + " -&gt;" + this);
     *   }
     *
     *   public String toString() {
     *     StringBuffer buffer = new StringBuffer();
     *     for (Iterator iter = v.iterator(); iter.hasNext();) {
     *       buffer.append(' ').append(iter.next());
     *     }
     *     return buffer.toString();
     *   }
     *
     *   private ArrayList v;
     *   private int index;
     *
     * }
     * </pre>
     *
     * @since 4.0
     */
    public static interface CommandVisitor<T> {
    
        /**
         * Method called when an insert command is encountered.
         *
         * @param object object to insert (this object comes from the second sequence)
         */
        void visitInsertCommand(T object);
    
        /**
         * Method called when a keep command is encountered.
         *
         * @param object object to keep (this object comes from the first sequence)
         */
        void visitKeepCommand(T object);
    
        /**
         * Method called when a delete command is encountered.
         *
         * @param object object to delete (this object comes from the first sequence)
         */
        void visitDeleteCommand(T object);
    
    }
}
