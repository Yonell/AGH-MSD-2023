public class IterationStatistics {
    private int iteration;
    private int dziks;
    private int dziksInCities;
    private int dziksInTheWild;
    private int food;

    public IterationStatistics(int iteration, int dziks, int dziksInCities, int dziksInTheWild, int food) {
        this.iteration = iteration;
        this.dziks = dziks;
        this.dziksInCities = dziksInCities;
        this.dziksInTheWild = dziksInTheWild;
        this.food = food;
    }

    public int getIteration() {
        return iteration;
    }

    public int getDziks() {
        return dziks;
    }

    public int getDziksInCities() {
        return dziksInCities;
    }

    public int getDziksInTheWild() {
        return dziksInTheWild;
    }

    public int getFood() {
        return food;
    }

    public String toString() {
    	return Integer.toString(iteration) + " " + Integer.toString(dziks) + " " + Integer.toString(dziksInCities) + " " + Integer.toString(dziksInTheWild) + " " + Integer.toString(food);
    }

}
