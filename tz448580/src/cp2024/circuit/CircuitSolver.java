package cp2024.circuit;

public interface CircuitSolver {
    /**  
     * Immediately returns a CircuitValue object that represents the result 
     * of solving the circuit. CircuitValue implementation will be a wrapper
     * for a Boolean value.
     */
    public CircuitValue solve(Circuit c);

    /**
     * Stops the solver.
     */
    public void stop();
}
