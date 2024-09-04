package com.cometproject.api.utilities;

public record Pair<L, R>(L left, R right) {
    
    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair pairo = (Pair) o;
        return this.left.equals(pairo.left()) && this.right.equals(pairo.right());
    }
    
}
