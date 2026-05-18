package src.models;

public class User {
    final String userId;
    final String name;
    private final UserPortfolio userPortfolio;

    public User(String userId, String name){
        this.userId = userId;
        this.name = name;
        this.userPortfolio = new UserPortfolio();
    }

    public UserPortfolio getPortfolio(){
        return this.userPortfolio;
    }
}