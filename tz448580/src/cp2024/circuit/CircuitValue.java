package cp2024.circuit;

/** 
 * Interface for a deferred (future) value returned by a CircuitSolver.
 * Important: tests will intentionally interrupt only threads blocked
 * on CircuitValue.getValue() method.
 */
public interface CircuitValue {
    public boolean getValue() throws InterruptedException;
}
