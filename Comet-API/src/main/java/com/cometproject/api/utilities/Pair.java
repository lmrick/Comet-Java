package com.cometproject.api.utilities;

public record Pair<L, R>(L left, R right) {
    
    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair<?, ?>(Object left1, Object right1))) return false;
        return this.left.equals(left1) && this.right.equals(right1);
    }
    
}
