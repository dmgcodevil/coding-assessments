public interface GamemodeService {

    /**
     * Reports the game mode for the given region. Creates a new game mode if it doesn't exist.
     *
     * @param gamemode game mode
     * @param region   country code in (ISO 3166)
     */
    void report(String gamemode, String region);

    /**
     * Gets the most popular game mode for the given region.
     *
     * @param region country code in (ISO 3166)
     * @return the most popular game mode
     */
    String getMostPopular(String region);
}
