package org.example.script;

import org.example.Utils.Hash;

import java.util.Stack;

public class Op {

    /**
     * Duplicates the top element of the stack
     * @param stack a {@link Stack} object
     * @return a {@code boolean}
     */
    public boolean opDup(Stack<byte[]> stack) {
        if (stack.isEmpty()) {
            return false;
        }
        stack.push(stack.push(stack.pop()));
        return true;
    }

    /**
     * Consumes the top element of the stack, performs a hash256 operation on it and pushes the result onto the stack
     * @param stack a {@link Stack} object
     * @return a {@code boolean}
     */
    public boolean opHash256(Stack<byte[]> stack) {
        if (stack.isEmpty()) return false;
        stack.push(Hash.hash256(stack.pop()));
        return true;
    }

    /**
     * Consumes the top element of the stack, performs a hash160 operation on it and pushes the result onto the stack
     * @param stack a {@link Stack} object
     * @return a {@code boolean}
     */
    public boolean opHash160(Stack<byte[]> stack) {
        if (stack.isEmpty()) return false;
        stack.push(Hash.hash160(stack.pop()));
        return true;
    }

}
