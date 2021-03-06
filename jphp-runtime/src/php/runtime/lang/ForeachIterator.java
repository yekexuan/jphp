package php.runtime.lang;


import php.runtime.Memory;
import php.runtime.env.TraceInfo;
import php.runtime.memory.LongMemory;
import php.runtime.memory.StringMemory;

import java.util.Iterator;

abstract public class ForeachIterator implements Iterable<Memory> {
    protected Object currentKey;
    protected Memory currentKeyMemory;
    protected Memory currentValue;
    protected boolean init = false;
    protected final boolean getReferences;
    protected final boolean getKeyReferences;
    protected final boolean withPrevious;
    protected boolean plainReferences = false;

    abstract protected boolean init();
    abstract protected boolean nextValue();
    abstract protected boolean prevValue();

    protected TraceInfo trace = TraceInfo.UNKNOWN;

    public ForeachIterator(boolean getReferences, boolean getKeyReferences, boolean withPrevious) {
        this.getReferences = getReferences;
        this.withPrevious = withPrevious;
        this.getKeyReferences = getKeyReferences;
    }

    public void setPlainReferences(boolean plainReferences) {
        this.plainReferences = plainReferences;
    }

    public boolean prev(){
        currentKeyMemory = null;
        if (!init || !withPrevious) {
            this.currentKey = null;
            this.currentValue = null;
            return false;
        } else
            return prevValue();
    }

    public boolean next(){
        currentKeyMemory = null;
        if (!init){
            init = true;
            if (!init())
                return false;
        }

        return nextValue();
    }

    public boolean end(){
        return false;
    }

    public Object getKey() {
        return currentKey;
    }

    abstract public void reset();

    public Memory getMemoryKey(){
        if (currentKeyMemory != null)
            return currentKeyMemory;

        if (currentKey instanceof String)
            return currentKeyMemory = new StringMemory((String)currentKey);
        if (currentKey instanceof Long)
            return currentKeyMemory = LongMemory.valueOf((Long)currentKey);
        if (currentKey instanceof Memory)
            return currentKeyMemory = (Memory) currentKey;

        return currentKeyMemory = Memory.NULL;
    }

    public Memory getValue() {
        return currentValue;
    }

    public TraceInfo getTrace() {
        return trace;
    }

    public void setTrace(TraceInfo trace) {
        this.trace = trace;
    }

    @Override
    public Iterator<Memory> iterator() {
        return new Iterator<Memory>() {
            protected Boolean hasNext;

            @Override
            public boolean hasNext() {
                if (hasNext == null) {
                    hasNext = ForeachIterator.this.next();
                }
                return hasNext;
            }

            @Override
            public Memory next() {
                if (hasNext != null) {
                    hasNext = null;
                    return ForeachIterator.this.getValue();
                } else {
                    ForeachIterator.this.next();
                    return ForeachIterator.this.getValue();
                }
            }

            @Override
            public void remove() {
                throw new IllegalStateException("Unsupported remove() method");
            }
        };
    }
}
